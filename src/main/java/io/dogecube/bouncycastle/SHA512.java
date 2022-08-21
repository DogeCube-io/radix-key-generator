package io.dogecube.bouncycastle;

public class SHA512 {
    private SHA512() {
    }

    public static class HashMac extends BaseMac {
        public HashMac() {
            super(new HMac(new SHA512Digest()));
        }
    }

}
