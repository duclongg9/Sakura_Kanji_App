package app.storage;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tiện ích lưu trữ ảnh chứng từ chuyển khoản cho yêu cầu nâng cấp tài khoản.
 */
public final class UpgradeReceiptStorage {

    private static final Path ROOT_DIRECTORY = Paths.get(System.getProperty("java.io.tmpdir"),
            "sakura-kanji", "upgrade-receipts");

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private static final Set<String> EXTENSION_WHITELIST = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static volatile boolean initialized = false;

    private UpgradeReceiptStorage() {
    }

    /**
     * Lưu trữ tệp ảnh từ multipart request và trả về tên file đã lưu.
     *
     * @param part phần multipart chứa ảnh.
     * @return tên file đã lưu.
     * @throws IOException              nếu quá trình ghi tệp thất bại.
     * @throws IllegalArgumentException nếu tệp không phải là hình ảnh hợp lệ.
     */
    public static String store(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String extension = determineExtension(part);
        Path directory = ensureDirectory();
        String fileName = UUID.randomUUID().toString() + extension;
        Path destination = directory.resolve(fileName);
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    /**
     * Lấy đường dẫn tuyệt đối đến tệp ảnh đã lưu.
     *
     * @param fileName tên file cần tìm.
     * @return {@link Path} tương ứng.
     * @throws IOException nếu không thể tạo thư mục lưu trữ.
     */
    public static Path resolve(String fileName) throws IOException {
        return ensureDirectory().resolve(fileName);
    }

    private static synchronized Path ensureDirectory() throws IOException {
        if (!initialized) {
            Files.createDirectories(ROOT_DIRECTORY);
            initialized = true;
        }
        return ROOT_DIRECTORY;
    }

    private static String determineExtension(Part part) {
        String contentType = part.getContentType();
        if (contentType != null) {
            String mapped = CONTENT_TYPE_EXTENSIONS.get(contentType.toLowerCase(Locale.ROOT));
            if (mapped != null) {
                return mapped;
            }
        }
        String submitted = part.getSubmittedFileName();
        if (submitted != null) {
            int dotIndex = submitted.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < submitted.length() - 1) {
                String ext = submitted.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (EXTENSION_WHITELIST.contains(ext)) {
                    return normalizeExtension(ext);
                }
            }
        }
        throw new IllegalArgumentException("Định dạng ảnh không được hỗ trợ. Chỉ chấp nhận PNG, JPG, GIF hoặc WebP.");
    }

    private static String normalizeExtension(String extension) {
        if (".jpeg".equals(extension)) {
            return ".jpg";
        }
        return extension;
    }
}
