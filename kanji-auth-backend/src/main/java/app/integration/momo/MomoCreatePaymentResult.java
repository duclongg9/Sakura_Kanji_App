package app.integration.momo;

/**
 * Kết quả trả về khi tạo đơn hàng thanh toán với MoMo.
 */
public class MomoCreatePaymentResult {
    private final String payUrl;
    private final String deeplink;
    private final String requestId;

    public MomoCreatePaymentResult(String payUrl, String deeplink, String requestId) {
        this.payUrl = payUrl;
        this.deeplink = deeplink;
        this.requestId = requestId;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public String getRequestId() {
        return requestId;
    }
}
