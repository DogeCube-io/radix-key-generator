package io.dogecube.javax;

import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public abstract class MacSpi {
    public MacSpi() {
    }

    protected abstract int engineGetMacLength();

    protected abstract void engineInit(SecretKeySpec var1) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void engineUpdate(byte[] var1, int var2, int var3);

    protected void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining()) {
            int chunk;
            if (input.hasArray()) {
                byte[] b = input.array();
                int ofs = input.arrayOffset();
                chunk = input.position();
                int lim = input.limit();
                this.engineUpdate(b, ofs + chunk, lim - chunk);
                input.position(lim);
            } else {
                int len = input.remaining();

                for (byte[] b = new byte[Math.min(4096, len)]; len > 0; len -= chunk) {
                    chunk = Math.min(len, b.length);
                    input.get(b, 0, chunk);
                    this.engineUpdate(b, 0, chunk);
                }
            }

        }
    }

    protected abstract byte[] engineDoFinal();

    protected abstract void engineReset();

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
