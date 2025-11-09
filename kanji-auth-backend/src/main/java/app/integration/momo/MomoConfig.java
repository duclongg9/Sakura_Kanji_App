package app.integration.momo;

/**
 * Cấu hình kết nối MoMo đọc từ biến môi trường hoặc giá trị mặc định.
 */
public class MomoConfig {
    private final String partnerCode;
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final String redirectUrl;
    private final String ipnUrl;
    private final boolean stubMode;

    private MomoConfig(String partnerCode, String accessKey, String secretKey, String endpoint,
                       String redirectUrl, String ipnUrl, boolean stubMode) {
        this.partnerCode = partnerCode;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.redirectUrl = redirectUrl;
        this.ipnUrl = ipnUrl;
        this.stubMode = stubMode;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public boolean isStubMode() {
        return stubMode;
    }

    /**
     * Đọc cấu hình từ biến môi trường, khi thiếu khóa bí mật sẽ bật chế độ stub.
     */
    public static MomoConfig load() {
        String partner = env("MOMO_PARTNER_CODE", "MOMOXXXX");
        String access = env("MOMO_ACCESS_KEY", "access_demo");
        String secret = env("MOMO_SECRET_KEY", "secret_demo");
        String endpoint = env("MOMO_ENDPOINT", "https://test-payment.momo.vn/v2/gateway/api/create");
        String redirect = env("MOMO_REDIRECT_URL", "https://example.com/momo/return");
        String ipn = env("MOMO_IPN_URL", "https://example.com/api/payments/momo/callback");
        boolean stub = Boolean.parseBoolean(env("MOMO_STUB_MODE", "false"));
        if (secret == null || secret.isBlank() || "secret_demo".equals(secret)) {
            stub = true;
        }
        return new MomoConfig(partner, access, secret, endpoint, redirect, ipn, stub);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
