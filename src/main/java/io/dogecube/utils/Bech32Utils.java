package io.dogecube.utils;

/**
 * Private functions extracted from the Bech32 library.
 */
public class Bech32Utils {

    public static final String ALPHABET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    public static byte[] expandHrp(String hrp) {
        int hrpLength = hrp.length();
        byte[] ret = new byte[hrpLength * 2 + 1];

        for (int i = 0; i < hrpLength; ++i) {
            int c = hrp.charAt(i) & 127;
            ret[i] = (byte) (c >>> 5 & 7);
            ret[i + hrpLength + 1] = (byte) (c & 31);
        }

        ret[hrpLength] = 0;
        return ret;
    }

    public static int polymod(byte[] values) {
        int c = 1;

        for (byte v_i : values) {
            int c0 = c >>> 25 & 255;
            c = (c & 33554431) << 5 ^ v_i & 255;
            if ((c0 & 1) != 0) {
                c ^= 996825010;
            }

            if ((c0 & 2) != 0) {
                c ^= 642813549;
            }

            if ((c0 & 4) != 0) {
                c ^= 513874426;
            }

            if ((c0 & 8) != 0) {
                c ^= 1027748829;
            }

            if ((c0 & 16) != 0) {
                c ^= 705979059;
            }
        }

        return c;
    }
}
