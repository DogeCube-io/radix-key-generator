package io.dogecube.generator;

import com.radixdlt.crypto.ECKeyUtils;
import com.radixdlt.utils.Pair;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.WNafUtil;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.Supplier;


public class AddressQuickGeneratorV2 {

    public static final int FORCE_RESEED_INTERVAL = 10_000_000;
    private final ECDomainParameters params = ECKeyUtils.domain();
    private final CachedECMult mult = new CachedECMult(params.getG());
    private final SecureRandom random = new SecureRandom();

    private int addIterations;
    private BigInteger prev;

    /**
     * pair: private key bytes, public key (ECPoint) bytes
     */
    public Pair<Supplier<byte[]>, byte[]> generateNewQuick() {
        try {
            BigInteger d = getOrGenerateD();

            byte[] pkBytes = mult.toPublicKey(d);
            return Pair.of(() -> {
                ECPrivateKeyParameters privParams = new ECPrivateKeyParameters(d, params);
                return ECKeyUtils.adjustArray(privParams.getD().toByteArray(), 32);
            }, pkBytes);
        } catch (Exception var7) {
            throw new IllegalStateException("Failed to generate ECKeyPair", var7);
        }
    }

    private BigInteger getOrGenerateD() {
        BigInteger d;
        if (prev != null && addIterations < FORCE_RESEED_INTERVAL) {
            addIterations++;
            d = prev.add(BigInteger.ONE);
            if (d.compareTo(BigInteger.ONE) < 0 || (d.compareTo(params.getN()) >= 0)) {
                d = generateRandomBigInt();
                addIterations = 1;
            }
        } else {
            d = generateRandomBigInt();
            addIterations = 1;
        }
        prev = d;
        return d;
    }

    private BigInteger generateRandomBigInt() {
        BigInteger n = params.getN();
        int nBitLength = n.bitLength();
        int minWeight = nBitLength >>> 2;

        BigInteger d;
        do {
            d = BigIntegers.createRandomBigInteger(nBitLength, random);
        } while (WNafUtil.getNafWeight(d) < minWeight);

        return d;
    }


}
