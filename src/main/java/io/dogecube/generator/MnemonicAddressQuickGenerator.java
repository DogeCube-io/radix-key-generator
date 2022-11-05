package io.dogecube.generator;

import com.radixdlt.crypto.ECKeyUtils;
import com.radixdlt.utils.Pair;
import io.dogecube.bitcoinj.PBKDF2SHA512;
import io.dogecube.bouncycastle.HMac;
import io.dogecube.bouncycastle.SHA512Digest;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.*;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;


public class MnemonicAddressQuickGenerator {
    private static final int ENTROPY_BITS = 128;

    public static final int FORCE_RESEED_INTERVAL = 10_000;

    private final ECDomainParameters params = ECKeyUtils.domain();
    private final CachedECMult mult = new CachedECMult(params.getG());
    private final SecureRandom random = new SecureRandom();
    private final PBKDF2SHA512 pbk = new PBKDF2SHA512();


    private final byte[] seed = new byte[ENTROPY_BITS / 8];
    private final List<ChildNumber> path = Arrays.asList(
            new ChildNumber(44, true),
            new ChildNumber(1022, true),
            ChildNumber.ZERO_HARDENED,
            ChildNumber.ZERO,
            null
    );

    private int addIterations;

    public MnemonicAddressQuickGenerator() {
        this.generateRandomSeed();
    }

    /**
     * pair: mnemonic, list of public key (ECPoint) bytes
     */
    public Pair<String, List<byte[]>> generateNewQuick(int count) {
        try {
            updateOrRegenerateSeed();

            List<String> words = MnemonicCode.INSTANCE.toMnemonic(seed);
            String mnemonic = Utils.SPACE_JOINER.join(words);

            byte[] seedBytes = pbk.derive(mnemonic, 2048, 64);
            // compare with the canonical BouncyCastle+BitcoinJ implementation
//            byte[] seedOld = org.bitcoinj.crypto.PBKDF2SHA512.derive(mnemonic, "mnemonic", 2048, 64);
//            if (!Arrays.equals(seedOld, seedBytes)) {
//                System.out.println("OLD: " + Arrays.toString(seedOld));
//                System.out.println("NEW: " + Arrays.toString(seedBytes));
//            }

            DeterministicKey rootKey = createMasterPrivateKey(seedBytes);
            rootKey.setCreationTimeSeconds(0);
            DeterministicHierarchy hierarchy = new DeterministicHierarchy(rootKey);


            List<byte[]> publicKeys = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                path.set(4, new ChildNumber(i, true));
                DeterministicKey keyByPath = hierarchy.get(path, false, true);
                BigInteger privKey = keyByPath.getPrivKey();

                byte[] pkBytes = mult.toPublicKey(privKey);
                publicKeys.add(pkBytes);
            }

            return Pair.of(mnemonic, publicKeys);
        } catch (Exception var7) {
            throw new IllegalStateException("Failed to generate ECKeyPair", var7);
        }
    }

    public static DeterministicKey createMasterPrivateKey(byte[] seed) throws HDDerivationException {
        checkArgument(seed.length > 8, "Seed is too short and could be brute forced");
        // Calculate I = HMAC-SHA512(key="Bitcoin seed", msg=S)
        byte[] i = hmacSha512(createHmacSha512Digest("Bitcoin seed".getBytes()), seed);
        // Split I into two 32-byte sequences, Il and Ir.
        // Use Il as master secret key, and Ir as master chain code.
        checkState(i.length == 64, i.length);
        byte[] il = Arrays.copyOfRange(i, 0, 32);
        byte[] ir = Arrays.copyOfRange(i, 32, 64);
        DeterministicKey masterPrivKey = HDKeyDerivation.createMasterPrivKeyFromBytes(il, ir);
        // Child deterministic keys will chain up to their parents to find the keys.
        masterPrivKey.setCreationTimeSeconds(Utils.currentTimeSeconds());
        return masterPrivKey;
    }

    static byte[] hmacSha512(HMac hmacSha512, byte[] input) {
        hmacSha512.reset();
        hmacSha512.update(input, 0, input.length);
        byte[] out = new byte[64];
        hmacSha512.doFinal(out, 0);
        return out;
    }

    static HMac createHmacSha512Digest(byte[] key) {
        SHA512Digest digest = new SHA512Digest();
        HMac hMac = new HMac(digest);
        hMac.init(new KeyParameter(key));
        return hMac;
    }

    private void updateOrRegenerateSeed() {
        if (addIterations < FORCE_RESEED_INTERVAL) {
            seed[addIterations % seed.length]++;
            addIterations++;
        } else {
            generateRandomSeed();
        }
    }

    private void generateRandomSeed() {
        random.nextBytes(seed);
        addIterations = 1;
    }

}
