package com.yoedu.job_board_platform.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

public class StringUtils {
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

    public static String toSlug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    public static String slugifyUnique(String input, Predicate<String> existsChecker) {
        return toSlugUnique(input, existsChecker);
    }

    public static String toSlugUnique(String value, Predicate<String> existsChecker) {
        String slug = toSlug(value);
        if (!existsChecker.test(slug)) {
            return slug;
        }
        String suffix = "-" + UUID.randomUUID().toString().substring(0, 8);
        return toSlug(value) + suffix;
    }
}
