package io.dogecube.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MaskedString {
    private String val;

    @Override
    public String toString() {
        return "*".repeat(val.length());
    }
}
