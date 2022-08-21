package io.dogecube.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class Formatter {

    public static String shortString(long num) {
        if (num == 0) {
            return "0";
        }
        if (num % 1_000_000_000L == 0) {
            return (num / 1_000_000_000L) + "B";
        }
        if (num % 1_000_000 == 0) {
            if (num > 1_000_000_000L) {
                return withNumberGroups(num / 1_000_000) + "M";
            }
            return (num / 1_000_000) + "M";
        }
        if (num % 1_000 == 0) {
            return (num / 1_000) + "k";
        }
        return String.valueOf(num);
    }

    public static String shortNumber(double num) {
        if (num == 0) {
            return "0";
        }
        if (num / 1_000_000_000L > 1) {
            return roundTo3(num / 1_000_000_000L) + "B";
        }
        if (num / 1_000_000 > 1) {
            return roundTo3(num / 1_000_000) + "M";
        }
        if (num / 1_000 > 1) {
            return roundTo3(num / 1_000) + "k";
        }
        return String.valueOf(num);
    }

    private static String roundTo3(double num) {
        BigDecimal amt = BigDecimal.valueOf(num);
        return withNumberGroups(amt.setScale(amt.scale() + 3 - amt.precision(), RoundingMode.HALF_EVEN).doubleValue());
    }

    public static String withNumberGroups(long number) {
        NumberFormat usdCostFormat = NumberFormat.getNumberInstance(Locale.US);
        usdCostFormat.setMinimumFractionDigits(0);
        usdCostFormat.setMaximumFractionDigits(0);
        return usdCostFormat.format(number);
    }

    public static String withNumberGroups(double number) {
        NumberFormat usdCostFormat = NumberFormat.getNumberInstance(Locale.US);
        usdCostFormat.setMinimumFractionDigits(0);
        usdCostFormat.setMaximumFractionDigits(2);
        return usdCostFormat.format(number);
    }
}
