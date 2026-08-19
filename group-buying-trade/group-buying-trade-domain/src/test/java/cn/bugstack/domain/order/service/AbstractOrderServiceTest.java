package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.*;
import cn.bugstack.domain.order.model.valobj.MarketTypeVO;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import com.alipay.api.AlipayApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractOrderServiceTest {

    @Mock
    private IOrderRepository repository;
    @Mock
    private IProductPort port;

    private TestOrderService service;

    @BeforeEach
    void setUp() {
        service = new TestOrderService(repository, port);
    }

    @Test
    void shouldReuseExistingUnpaidOrder() throws Exception {
        ShopCartEntity cart = cart(MarketTypeVO.NO_MARKET);
        when(repository.queryUnPayOrder(cart)).thenReturn(OrderEntity.builder()
                .orderId("order-existing")
                .payUrl("https://pay.example/existing")
                .orderStatusVO(OrderStatusVO.PAY_WAIT)
                .build());

        PayOrderEntity result = service.createOrder(cart);

        assertEquals("order-existing", result.getOrderId());
        assertEquals("https://pay.example/existing", result.getPayUrl());
        verify(port, never()).queryProductByProductId(anyString());
        assertNull(service.savedAggregate);
    }

    @Test
    void shouldCreateGroupOrderWithMarketingLockBeforePrepay() throws Exception {
        ShopCartEntity cart = cart(MarketTypeVO.GROUP_BUY_MARKET);
        ProductEntity product = ProductEntity.builder()
                .productId("sku-01")
                .productName("社区生鲜套餐")
                .price(new BigDecimal("29.90"))
                .build();
        MarketPayDiscountEntity discount = MarketPayDiscountEntity.builder()
                .originalPrice(new BigDecimal("29.90"))
                .deductionPrice(new BigDecimal("5.00"))
                .payPrice(new BigDecimal("24.90"))
                .build();
        when(repository.queryUnPayOrder(cart)).thenReturn(null);
        when(port.queryProductByProductId("sku-01")).thenReturn(product);
        service.discountToReturn = discount;

        PayOrderEntity result = service.createOrder(cart);

        assertNotNull(service.savedAggregate);
        assertEquals("user-01", service.savedAggregate.getUserId());
        assertSame(discount, service.prepayDiscount);
        assertEquals(new BigDecimal("29.90"), service.prepayOriginalAmount);
        assertEquals(service.generatedOrderId, result.getOrderId());
        assertEquals("https://pay.example/new", result.getPayUrl());
    }

    private ShopCartEntity cart(MarketTypeVO marketType) {
        return ShopCartEntity.builder()
                .userId("user-01")
                .productId("sku-01")
                .activityId(100123L)
                .marketTypeVO(marketType)
                .build();
    }

    private static final class TestOrderService extends AbstractOrderService {

        private CreateOrderAggregate savedAggregate;
        private MarketPayDiscountEntity discountToReturn;
        private MarketPayDiscountEntity prepayDiscount;
        private BigDecimal prepayOriginalAmount;
        private String generatedOrderId;

        private TestOrderService(IOrderRepository repository, IProductPort port) {
            super(repository, port);
        }

        @Override
        protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
            this.savedAggregate = orderAggregate;
            this.generatedOrderId = orderAggregate.getOrderEntity().getOrderId();
        }

        @Override
        protected MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId,
                                                               String productId, String orderId) {
            return discountToReturn;
        }

        @Override
        protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName,
                                                String orderId, BigDecimal totalAmount) throws AlipayApiException {
            return doPrepayOrder(userId, productId, productName, orderId, totalAmount, null);
        }

        @Override
        protected PayOrderEntity doPrepayOrder(String userId, String productId, String productName,
                                                String orderId, BigDecimal totalAmount,
                                                MarketPayDiscountEntity marketPayDiscountEntity) {
            this.prepayDiscount = marketPayDiscountEntity;
            this.prepayOriginalAmount = totalAmount;
            return PayOrderEntity.builder()
                    .orderId(orderId)
                    .payUrl("https://pay.example/new")
                    .build();
        }

        @Override
        public void changeOrderPaySuccess(String orderId, Date orderTime) {
            throw new UnsupportedOperationException("not required by this unit test");
        }

        @Override
        public List<String> queryNoPayNotifyOrder() {
            return Collections.emptyList();
        }

        @Override
        public List<String> queryTimeoutCloseOrderList() {
            return Collections.emptyList();
        }

        @Override
        public boolean changeOrderClose(String orderId) {
            return false;
        }

        @Override
        public void changeOrderMarketSettlement(List<String> outTradeNoList) {
            throw new UnsupportedOperationException("not required by this unit test");
        }

        @Override
        public boolean refundMarketOrder(String userId, String orderId) {
            return false;
        }

        @Override
        public boolean refundPayOrder(String userId, String orderId) {
            return false;
        }
    }
}
