package app.integration.momo;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hỗ trợ ký request theo chuẩn HmacSHA256 của MoMo.
 */
public final class MomoSignature {
    private MomoSignature() {
    }

    /**
     * Tạo chữ ký HMAC-SHA256 với khóa bí mật.
     *
     * @param rawData   chuỗi rawSignature theo tài liệu MoMo.
     * @param secretKey khóa bí mật.
     * @return chữ ký dạng hex.
     */
    public static String sign(String rawData, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign MoMo payload", ex);
        }
    }
}
