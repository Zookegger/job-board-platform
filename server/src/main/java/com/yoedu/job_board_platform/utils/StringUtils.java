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
     * Loại bỏ dấu tiếng Việt, ký tự đặc biệt, và chuẩn hoá khoảng trắng.
     * <p>
     * Cơ chế: NFD normalization → xoá dấu kết hợp ({@code \\p{M}}) → xử lý riêng
     * {@code đ/Đ} → lowercase → giữ lại {@code a-z0-9}, dấu cách, dấu gạch ngang
     * → thay khoảng trắng bằng dấu gạch ngang → gộp dấu gạch ngang liên tiếp.
     * <pre>{@code
     * StringUtils.slugify("Công ty TNHH ABC");           // → "cong-ty-tnhh-abc"
     * StringUtils.slugify("Đồng Nai");                   // → "dong-nai"
     * StringUtils.slugify("  Hồ Chí  Minh  ");           // → "ho-chi-minh"
     * StringUtils.slugify("Job #1 (Software Engineer)"); // → "job-1-software-engineer"
     * }</pre>
     *
     * @param input chuỗi đầu vào (có thể có dấu tiếng Việt)
     * @return slug URL, ví dụ: "Công ty ABC" → "cong-ty-abc"
     */
    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{M}", "")
                .replaceAll("[đĐ]", "d")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    /**
     * Tạo slug duy nhất bằng cách thêm UUID suffix (8 ký tự đầu) nếu slug
     * đã tồn tại trong hệ thống.
     * <pre>{@code
     * StringUtils.slugifyUnique("Công ty ABC",
     *         slug -> companyRepository.existsBySlug(slug));
     * // nếu "cong-ty-abc" đã tồn tại → "cong-ty-abc-a1b2c3d4"
     * }</pre>
     *
     * @param input         chuỗi đầu vào
     * @param existsChecker hàm kiểm tra slug đã tồn tại chưa (thường gọi repository)
     * @return slug duy nhất
     */
    public static String slugifyUnique(String input, Predicate<String> existsChecker) {
        String slug = slugify(input);
        if (!existsChecker.test(slug)) {
            return slug;
        }
        String suffix = "-" + UUID.randomUUID().toString().substring(0, 8);
        return slug + suffix;
    }
}
