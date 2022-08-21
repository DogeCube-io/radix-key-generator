package io.dogecube.bouncycastle;

import io.dogecube.javax.MacSpi;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE;

import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class BaseMac extends MacSpi implements PBE {
    private final Mac macEngine;

    protected BaseMac(Mac macEngine) {
        this.macEngine = macEngine;
    }

    protected void engineInit(SecretKeySpec key) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }

        final KeyParameter keyParam = new KeyParameter(key.getEncoded());

        try {
            macEngine.init(keyParam);
        } catch (Exception e) {
            throw new InvalidAlgorithmParameterException("cannot initialize MAC: " + e.getMessage());
        }
    }

    protected int engineGetMacLength() {
        return macEngine.getMacSize();
    }

    protected void engineReset() {
        macEngine.reset();
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        macEngine.update(input, offset, len);
    }

    protected byte[] engineDoFinal() {
        byte[] out = new byte[engineGetMacLength()];

        macEngine.doFinal(out, 0);

        return out;
    }

}
