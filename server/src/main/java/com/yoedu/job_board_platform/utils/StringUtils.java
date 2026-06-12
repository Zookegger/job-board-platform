package com.yoedu.job_board_platform.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Tiện ích xử lý chuỗi, tập trung vào chuyển đổi văn bản tiếng Việt
 * thành slug URL không dấu. Hỗ trợ tạo slug duy nhất với UUID suffix.
 */
public class StringUtils {
    /**
     * Chuyển đổi chuỗi đầu vào thành slug URL (dạng thân thiện với SEO).
     * Loại bỏ dấu tiếng Việt và ký tự đặc biệt.
     *
     * @param input chuỗi đầu vào (có thể có dấu tiếng Việt)
     * @return slug URL, ví dụ: "Công ty ABC" → "cong-ty-abc"
     */
    public static String slugify(String input) {
        input = input.trim();
        input = input.toLowerCase();

        input = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[á|à|ả|ạ|ã|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ]", "a")
                .replaceAll("[é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ]", "e")
                .replaceAll("[i|í|ì|ỉ|ĩ|ị]", "i")
                .replaceAll("[ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ]", "o")
                .replaceAll("[ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự]", "u")
                .replaceAll("[ý|ỳ|ỷ|ỹ|ỵ]", "y")
                .replaceAll("[đ|Đ]", "d")
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^\\w+]", "-")
                .replaceAll("\\s+", "-")
                .replaceAll("[-]+", "-")
                .replaceAll("^-", "")
                .replaceAll("-$", "");
        return input;
    }

    /**
     * Chuyển đổi chuỗi thành slug bằng cách loại bỏ dấu Unicode (cách tiếp cận khác).
     *
     * @param value chuỗi đầu vào
     * @return slug URL
     */
    public static String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    /**
     * Tạo slug duy nhất bằng cách thêm UUID suffix nếu slug đã tồn tại.
     *
     * @param input         chuỗi đầu vào
     * @param existsChecker hàm kiểm tra slug đã tồn tại chưa
     * @return slug duy nhất
     */
    public static String slugifyUnique(String input, Predicate<String> existsChecker) {
        return toSlugUnique(input, existsChecker);
    }

    /**
     * Tạo slug duy nhất (phiên bản sử dụng toSlug).
     *
     * @param value         chuỗi đầu vào
     * @param existsChecker hàm kiểm tra slug đã tồn tại chưa
     * @return slug duy nhất
     */
    public static String toSlugUnique(String value, Predicate<String> existsChecker) {
        String slug = toSlug(value);
        if (!existsChecker.test(slug)) {
            return slug;
        }
        String suffix = "-" + UUID.randomUUID().toString().substring(0, 8);
        return toSlug(value) + suffix;
    }
}
