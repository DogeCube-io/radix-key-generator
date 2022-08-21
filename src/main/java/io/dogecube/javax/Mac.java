package io.dogecube.javax;

import io.dogecube.bouncycastle.SHA512;

import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class Mac implements Cloneable {
    private MacSpi spi;
    private final String algorithm;
    private boolean initialized = false;


    protected Mac(MacSpi macSpi, String algorithm) {
        this.spi = macSpi;
        this.algorithm = algorithm;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static Mac getInstance(String algorithm) {
        if (!"HmacSHA512".equals(algorithm)) {
            throw new RuntimeException("Only HmacSHA512 is supported!");
        }

        return new Mac(new SHA512.HashMac(), algorithm);
    }

    public final int getMacLength() {
        return this.spi.engineGetMacLength();
    }

    public final void init(SecretKeySpec key) throws InvalidKeyException {
        try {
            this.spi.engineInit(key);
        } catch (InvalidAlgorithmParameterException var3) {
            throw new InvalidKeyException("init() failed", var3);
        }

        this.initialized = true;
    }

    public final void update(byte[] input) throws IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else {
            if (input != null) {
                this.spi.engineUpdate(input, 0, input.length);
            }

        }
    }

    public final void update(byte[] input, int offset, int len) throws IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else {
            if (input != null) {
                if (offset < 0 || len > input.length - offset || len < 0) {
                    throw new IllegalArgumentException("Bad arguments");
                }

                this.spi.engineUpdate(input, offset, len);
            }

        }
    }

    public final void update(ByteBuffer input) {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else if (input == null) {
            throw new IllegalArgumentException("Buffer must not be null");
        } else {
            this.spi.engineUpdate(input);
        }
    }

    public final byte[] doFinal() throws IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else {
            byte[] mac = this.spi.engineDoFinal();
            this.spi.engineReset();
            return mac;
        }
    }

    public final void doFinal(byte[] output, int outOffset) throws ShortBufferException, IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else {
            int macLen = this.getMacLength();
            if (output != null && output.length - outOffset >= macLen) {
                byte[] mac = this.doFinal();
                System.arraycopy(mac, 0, output, outOffset, macLen);
            } else {
                throw new ShortBufferException("Cannot store MAC in output buffer");
            }
        }
    }

    public final byte[] doFinal(byte[] input) throws IllegalStateException {
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else {
            this.update(input);
            return this.doFinal();
        }
    }

    public final Object clone() throws CloneNotSupportedException {
        Mac that = (Mac) super.clone();
        that.spi = (MacSpi) this.spi.clone();
        return that;
    }
}

