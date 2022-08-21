package io.dogecube.params;

import lombok.Data;

import java.util.List;

@Data
public class AddressCriteria {
    // sneaky zero byte location (index) in the public key: right-to-left and left-to-right
    public static final int PK_END_RTL = 6;
    public static final int PK_END_LTR = (58 - 1) - PK_END_RTL;

    private String pattern;
    private char[] prefix;
    private char[] suffix;
    private long difficulty;

    public AddressCriteria(String pattern) {
        boolean hasPrefix = false;
        if (pattern.startsWith("rdx1")) {
            hasPrefix = true;
            if (!pattern.startsWith("rdx1qsp")) {
                throw new RuntimeException("Pattern is invalid: '" + pattern + "'! Radix addresses can only start with 'rdx1qsp'.");
            } else {
                pattern = pattern.substring(7);
            }
        }
        this.pattern = pattern;

        int astIdx = pattern.indexOf('*');
        if (astIdx == -1) {
            if (hasPrefix) {
                prefix = pattern.toCharArray();
            } else {
                suffix = pattern.toCharArray();
            }
        } else {
            if (astIdx > 0) {
                prefix = pattern.substring(0, astIdx).toCharArray();
            }
            if (astIdx < pattern.length() - 1) {
                suffix = pattern.substring(astIdx + 1).toCharArray();
            }
        }

        this.difficulty = calculateDifficulty();
    }

    public static boolean containsAny(List<AddressCriteria> criteria, char[] accountChars) {
        for (AddressCriteria criterion : criteria) {
            if (criterion.isMatchedBy(accountChars)) {
                return true;
            }
        }

        return false;
    }

    public boolean isMatchedBy(char[] accountChars) {
        if (suffix != null) {
            for (int i = 0; i < suffix.length; i++) {
                char c = suffix[i];
                if (c != '?' && c != accountChars[accountChars.length - suffix.length + i]) {
                    return false;
                }
            }
        }
        if (prefix != null) {
            for (int i = 0; i < prefix.length; i++) {
                char c = prefix[i];
                if (c != '?' && c != accountChars[i + 7]) {
                    return false;
                }
            }
        }

        return true;
    }

    private long calculateDifficulty() {
        long difficulty = 1;
        if (prefix != null) {
            for (int i = 0; i < prefix.length; i++) {
                char c = prefix[i];
                if (c != '?') {
                    difficulty *= (i != PK_END_LTR) ? 32 : 4;
                }
            }
        }
        if (suffix != null) {
            for (int i = 0; i < suffix.length; i++) {
                char c = suffix[suffix.length - 1 - i];
                if (c != '?') {
                    difficulty *= (i != PK_END_RTL) ? 32 : 4;
                }
            }
        }
        return difficulty;
    }

    @Override
    public String toString() {
        return (prefix != null ? new String(prefix) : "") + "*" + ((suffix != null ? new String(suffix) : ""));
    }
}
