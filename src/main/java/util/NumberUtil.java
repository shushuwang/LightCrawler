package util;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {
    public static boolean isZeroOrNull(Long value) {
        return value == null || value.longValue() == 0;
    }

    public static boolean isNotZeroAndNull(Long value) {
        return !isZeroOrNull(value);
    }

    public static boolean isZeroOrNull(Integer value) {
        return value == null || value.intValue() == 0;
    }

    public static boolean isNotZeroAndNull(Integer value) {
        return !isZeroOrNull(value);
    }

    public static long getValue(Long value) {
        if (value == null) {
            return 0;
        }
        return value.longValue();
    }

    public static int getValue(Integer value) {
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    public static float getValue(Float value) {
        if (value == null) {
            return 0f;
        }
        return value.floatValue();
    }

    public static int compare(Integer first, Integer second) {
        int valueFirst = getValue(first);
        int valueSecond = getValue(second);
        return (valueFirst < valueSecond ? -1 : (valueFirst == valueSecond ? 0 : 1));
    }
    public static int compare(Long first, Long second) {
        long valueFirst = getValue(first);
        long valueSecond = getValue(second);
        return (valueFirst < valueSecond ? -1 : (valueFirst == valueSecond ? 0 : 1));
    }

    public static double save2Decimal(double value) {
        BigDecimal bg = new BigDecimal(value);
        double result = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return result;
    }

    public static long parseLong(String value) {
        long l = 0;
        try {
            if (!Strings.isNullOrEmpty(value)) {
                l = Long.parseLong(value);
            }
        } catch (NumberFormatException e) {

        }
        return l;
    }

    public static int parseInt(String value) {
        int l = 0;
        try {
            if (!Strings.isNullOrEmpty(value)) {
                l = Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {

        }
        return l;
    }

    public static float parseFloat(String value) {
        float l = 0f;
        try {
            if (!Strings.isNullOrEmpty(value)) {
                l = Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {

        }
        return l;
    }
    public static double parseDouble(String value) {
        double l = 0d;
        try {
            if (!Strings.isNullOrEmpty(value)) {
                l = Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {

        }
        return l;
    }
    
    public static String getNumberFromString(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(source);
        return matcher.replaceAll("").trim();
    }
}
