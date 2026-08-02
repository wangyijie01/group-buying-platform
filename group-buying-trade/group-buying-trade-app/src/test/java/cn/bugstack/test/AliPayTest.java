package cn.bugstack.test;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

@Slf4j
public class AliPayTest {

    // 沙箱凭据仅从环境变量读取，避免测试密钥进入版本库。
    public static String app_id = System.getenv("ALIPAY_APP_ID");
    public static String merchant_private_key = System.getenv("ALIPAY_MERCHANT_PRIVATE_KEY");
    public static String alipay_public_key = System.getenv("ALIPAY_PUBLIC_KEY");
    public static String notify_url = System.getenv().getOrDefault("ALIPAY_NOTIFY_URL", "http://127.0.0.1:8070/api/v1/alipay/alipay_notify_url");
    // 回调地址需替换为支付平台可访问的公网地址。
    public static String return_url = System.getenv().getOrDefault("ALIPAY_RETURN_URL", "http://127.0.0.1:8070");
    // 「沙箱环境」
    public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    // 签名方式
    public static String sign_type = "RSA2";
    // 字符编码格式
    public static String charset = "utf-8";

    private AlipayClient alipayClient;

    @Before
    public void init() {
        Assume.assumeTrue("未配置支付宝沙箱环境变量，跳过外部支付测试",
                hasText(app_id) && hasText(merchant_private_key) && hasText(alipay_public_key));
        this.alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id,
                merchant_private_key,
                "json",
                charset,
                alipay_public_key,
                sign_type);
    }

    private boolean hasText(String value) {
        return null != value && !value.trim().isEmpty();
    }

    @Test
    public void test_aliPay_pageExecute() throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 构造网页支付请求。
        request.setNotifyUrl(notify_url);
        request.setReturnUrl(return_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "demo000091001902");  // 业务侧生成的幂等订单号。
        bizContent.put("total_amount", "0.01"); // 沙箱测试金额。
        bizContent.put("subject", "测试商品");   // 收银台展示名称。
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");  // 支付宝网页支付产品码。
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();
        log.info("测试结果：{}", form);

        // pageExecute 返回可直接交给浏览器的自动提交表单。
    }

    /**
     * 查询订单
     */
    @Test
    public void test_alipay_certificateExecute() throws AlipayApiException {

        AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
        bizModel.setOutTradeNo("571486993823");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(bizModel);

        String body = alipayClient.execute(request).getBody();
        log.info("测试结果：{}", body);
    }

    /**
     * 退款接口
     */
    @Test
    public void test_alipay_refund() throws AlipayApiException {
        AlipayTradeRefundRequest request =new AlipayTradeRefundRequest();
        AlipayTradeRefundModel refundModel =new AlipayTradeRefundModel();
        refundModel.setOutTradeNo("daniel82AAAA000032333361X03");
        refundModel.setRefundAmount("1.00");
        refundModel.setRefundReason("退款说明");
        request.setBizModel(refundModel);

        AlipayTradeRefundResponse execute = alipayClient.execute(request);
        log.info("测试结果：{}", execute.isSuccess());
    }

    public static void main(String[] args) {
        System.out.println(new BigDecimal("9.99").doubleValue());
    }

}
