package app.integration.momo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.json.JSONObject;

/**
 * Client gọi API tạo đơn hàng MoMo hoặc sinh URL giả lập khi chạy stub.
 */
public class MomoClient {
    private final MomoConfig config;
    private final HttpClient httpClient;

    public MomoClient(MomoConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Tạo yêu cầu thanh toán mới.
     *
     * @param plan             gói VIP cần mua.
     * @param orderId          mã đơn hàng nội bộ.
     * @param description      mô tả hiển thị cho người dùng.
     * @param extraDataPayload dữ liệu thêm gửi MoMo (base64 hoặc chuỗi JSON).
     * @return {@link MomoCreatePaymentResult} chứa URL thanh toán.
     * @throws IOException          khi gọi HTTP thất bại.
     * @throws InterruptedException nếu request bị hủy.
     */
    public MomoCreatePaymentResult createPayment(MomoUpgradePlan plan, String orderId, String description,
                                                 String extraDataPayload) throws IOException, InterruptedException {
        String requestId = UUID.randomUUID().toString();
        if (config.isStubMode()) {
            String payUrl = "https://test-payment.momo.vn/pay?orderId=" + orderId;
            String deeplink = "momo://app?action=payWithApp&orderId=" + orderId;
            return new MomoCreatePaymentResult(payUrl, deeplink, requestId);
        }

        JSONObject body = new JSONObject()
                .put("partnerCode", config.getPartnerCode())
                .put("partnerName", "Sakura Kanji")
                .put("storeId", "SakuraKanjiApp")
                .put("requestType", "captureWallet")
                .put("ipnUrl", config.getIpnUrl())
                .put("redirectUrl", config.getRedirectUrl())
                .put("orderId", orderId)
                .put("amount", plan.getAmount())
                .put("lang", "vi")
                .put("orderInfo", description)
                .put("requestId", requestId)
                .put("extraData", extraDataPayload != null ? extraDataPayload : "")
                .put("autoCapture", true);

        String rawSignature = "accessKey=" + config.getAccessKey()
                + "&amount=" + plan.getAmount()
                + "&extraData=" + (extraDataPayload != null ? extraDataPayload : "")
                + "&ipnUrl=" + config.getIpnUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + description
                + "&partnerCode=" + config.getPartnerCode()
                + "&redirectUrl=" + config.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=captureWallet";
        body.put("signature", MomoSignature.sign(rawSignature, config.getSecretKey()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getEndpoint()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("MoMo returned HTTP " + response.statusCode());
        }
        JSONObject json = new JSONObject(response.body());
        if (json.optInt("resultCode", -1) != 0) {
            throw new IOException("MoMo error: " + json.optString("message", "unknown"));
        }
        return new MomoCreatePaymentResult(
                json.optString("payUrl"),
                json.optString("deeplink"),
                requestId
        );
    }
}
