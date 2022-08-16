package io.dogecube.generator;

import io.dogecube.bouncycastle.ECPointSimple;
import org.bouncycastle.math.ec.*;
import org.bouncycastle.math.raw.Nat;

import java.math.BigInteger;

/**
 * Performs EC point multiplications to convert a generated private key into the public key.
 */
public class CachedECMult {
    private final ECPoint p;
    private final FixedPointPreCompInfo info;
    private final ECPoint[] lookupArr = new ECPoint[64];
    private final int d;
    private final int fullComb;

    private final SimpleIdentityHashMap<ECPoint, ECPoint>[] twicePlusCache = new SimpleIdentityHashMap[64];

    public CachedECMult(ECPoint p) {
        this.p = p;
        info = FixedPointUtil.precompute(p);
        ECLookupTable lookupTable = info.getLookupTable();
        for (int i = 0; i < lookupArr.length; i++) {
            lookupArr[i] = lookupTable.lookup(i);
        }

        ECCurve c = p.getCurve();
        int size = FixedPointUtil.getCombSize(c);
        int width = info.getWidth();
        d = (size + width - 1) / width;
        fullComb = d * width;

        for (int i = 0; i < twicePlusCache.length; i++) {
            twicePlusCache[i] = new SimpleIdentityHashMap<>(16 * 16);
        }
    }

    public byte[] toPublicKey(BigInteger privateKey) {
        ECPoint Q = multiplyPositive(privateKey);
        ECPoint q = normalizeJacobian(Q);

        return q.getEncoded(true);
    }

    protected ECPoint multiplyPositive(BigInteger k) {
        ECPoint R = p.getCurve().getInfinity();
        int[] K = Nat.fromBigInteger(fullComb, k);

        int top = fullComb - 1;
        for (int i = 0; i < d; ++i) {
            int secretIndex = 0;

            for (int j = top - i; j >= 0; j -= d) {
                int secretBit = K[j >>> 5] >>> (j & 0x1F);
                secretIndex ^= secretBit >>> 1;
                secretIndex <<= 1;
                secretIndex ^= secretBit;
            }

            SimpleIdentityHashMap<ECPoint, ECPoint> cache = twicePlusCache[secretIndex];

            ECPoint val = cache.get(R);
            if (val != null) {
                R = val;
            } else {
                ECPoint add = lookupArr[secretIndex];
                ECPoint res = R.twicePlus(add);
                cache.put(R, res);
                R = res;
            }
        }
        return R.add(info.getOffset());
    }

    private ECPoint normalizeJacobian(ECPoint pp) {
        ECFieldElement z = pp.getZCoord(0);
        if (z.isOne()) {
            return pp;
        }

        ECFieldElement zInv = z.invert();

        ECFieldElement zInv2 = zInv.square(), zInv3 = zInv2.multiply(zInv);
        ECFieldElement x = pp.getRawXCoord().multiply(zInv2);
        ECFieldElement y = pp.getRawYCoord().multiply(zInv3);
        return new ECPointSimple(pp.getCurve(), x, y);
    }
}
