package io.dogecube.bitcoinj;

import io.dogecube.javax.Mac;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * clone of PBKDF2SHA512 with:
 * 1. no passphrase support, so hardcoded salt "mnemonic"
 * 2. not static, so arrays can be reused (per-thread)
 */
public class PBKDF2SHA512 {
    final byte[] baS = "mnemonic".getBytes(StandardCharsets.UTF_8);

    final byte[][] baU_cache = new byte[8][baS.length + 4];

    public PBKDF2SHA512() {
        for (int i = 0; i < 8; i++) {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.putInt(i);
            byte[] baI = bb.array();

            byte[] baU = baU_cache[i];

            System.arraycopy(baS, 0, baU, 0, baS.length);
            System.arraycopy(baI, 0, baU, baS.length, baI.length);
        }
    }


    public byte[] derive(String P, int c, int dkLen) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            int hLen = 20;

            if (dkLen > ((Math.pow(2, 32)) - 1) * hLen) {
                throw new IllegalArgumentException("derived key too long");
            } else {
                int l = (int) Math.ceil((double) dkLen / (double) hLen);
                // int r = dkLen - (l-1)*hLen;

                SecretKeySpec key = new SecretKeySpec(P.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
                Mac mac = Mac.getInstance(key.getAlgorithm());
                mac.init(key);

                for (int i = 1; i <= l; i++) {
                    byte[] T = F(mac, c, i);
                    baos.write(T);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] baDerived = new byte[dkLen];
        System.arraycopy(baos.toByteArray(), 0, baDerived, 0, baDerived.length);

        return baDerived;
    }

    private byte[] F(Mac mac, int c, int i) {
        byte[] U_LAST = null;
        byte[] U_XOR = null;

        for (int j = 0; j < c; j++) {
            if (j == 0) {
                U_XOR = mac.doFinal(baU_cache[i]);
                U_LAST = U_XOR;
            } else {
                byte[] baU = mac.doFinal(U_LAST);

                for (int k = 0; k < U_XOR.length; k++) {
                    U_XOR[k] = (byte) (U_XOR[k] ^ baU[k]);
                }

                U_LAST = baU;
            }
        }

        return U_XOR;
    }

}
