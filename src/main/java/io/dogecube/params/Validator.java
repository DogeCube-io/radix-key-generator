package io.dogecube.params;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Validator {
    public static final String LETTERS_AT_POS_7 = "cgqs";
    public static final String ADDRESS_STRING = "023456789acdefghjklmnpqrstuvwxyz";

    public void validate(List<AddressCriteria> criterias) {
        validate(criterias.stream().map(AddressCriteria::getSuffix).filter(Objects::nonNull).map(String::new).collect(Collectors.toList()), true);
        validate(criterias.stream().map(AddressCriteria::getPrefix).filter(Objects::nonNull).map(String::new).collect(Collectors.toList()), false);
    }

    private void validate(List<String> words, boolean isSuffix) {
        boolean hasErrors = false;
        for (String word : words) {
            char[] chars = word.toCharArray();
            for (char c : chars) {
                if (!ADDRESS_STRING.contains(String.valueOf(c)) && c != '?') {
                    hasErrors = true;
                    System.err.println("Illegal char " + c + " in word:" + word);
                }
            }
            if (isSuffix) {
                if (chars.length > 6) {
                    char char7 = chars[chars.length - 7];
                    if (!LETTERS_AT_POS_7.contains(String.valueOf(char7)) && char7 != '?') {
                        hasErrors = true;
                        System.err.println("7th letter from end should be one of: [" + LETTERS_AT_POS_7 +
                                "] but found: " + char7 + " in word:" + word);
                    }
                }
            }
        }
        if (hasErrors) {
            throw new RuntimeException("See the above printed errors");
        }
    }
}
