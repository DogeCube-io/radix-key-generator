package io.dogecube;

import com.radixdlt.crypto.ECKeyPair;
import com.radixdlt.crypto.RadixKeyStore;
import com.radixdlt.crypto.exception.KeyStoreException;
import com.radixdlt.crypto.exception.PrivateKeyException;
import com.radixdlt.crypto.exception.PublicKeyException;
import com.radixdlt.identifiers.REAddr;
import com.radixdlt.networks.Addressing;
import com.radixdlt.networks.Network;
import com.radixdlt.utils.Bits;
import com.radixdlt.utils.Pair;
import io.dogecube.generator.AddressQuickGeneratorV2;
import io.dogecube.params.AddressCriteria;
import io.dogecube.params.GeneratorParams;
import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static io.dogecube.utils.Bech32Utils.*;

public class KeyStoreGenerator extends Generator {

    private static final int LOG_INTERVAL = 100_000_000;
    private static final int SMALL_LOG_INTERVAL = 1_000_000;

    public KeyStoreGenerator(GeneratorParams params, List<AddressCriteria> criteria) {
        super(params, criteria, LOG_INTERVAL, SMALL_LOG_INTERVAL);
    }


    private synchronized ECKeyPair readKey(String name, String keyFileName) throws PrivateKeyException, KeyStoreException, PublicKeyException, IOException {
        try (RadixKeyStore keyStore = RadixKeyStore.fromFile(new File(keyFileName), params.getPass().getVal().toCharArray(), false)) {
            return keyStore.readKeyPair(name, false);
        }
    }

    private synchronized void writeKey(String name, String keyFileName, byte[] privateKey) throws KeyStoreException, IOException, PrivateKeyException, PublicKeyException {
        try (RadixKeyStore keyStore = RadixKeyStore.fromFile(new File(keyFileName), params.getPass().getVal().toCharArray(), true)) {
            keyStore.writeKeyPair(name, ECKeyPair.fromPrivateKey(privateKey));
        }
    }

    @Override
    protected void doWorkInternal(int i) throws Exception {
        ECKeyPair keyPair = null;
        if (params.getKeyStoreFile() != null) {
            try {
                Addressing addressing = Addressing.ofNetwork(Network.MAINNET);
                keyPair = readKey("key" + i, params.getKeyStoreFile());
                REAddr addr = REAddr.ofPubKeyAccount(keyPair.getPublicKey());
                String account = addressing.forAccounts().of(addr);
                System.out.println("Existing Account " + i + ": " + account);
            } catch (FileNotFoundException e) {
                // ignore
            } catch (KeyStoreException e) {
                if (!e.getMessage().startsWith("No such entry")) {
                    throw e;
                }
            }
        }
        if (keyPair == null) {
            AddressQuickGeneratorV2 generator = new AddressQuickGeneratorV2();
            char[] alphabet = ALPHABET.toCharArray();

            byte[] pkBytes = new byte[34];
            pkBytes[0] = 4;
            char[] accountChars = new char[3 + 1 + 61];
            accountChars[0] = 'r';
            accountChars[1] = 'd';
            accountChars[2] = 'x';
            accountChars[3] = '1';
            byte[] hrpExpanded = expandHrp("rdx");
            byte[] enc = new byte[hrpExpanded.length + 55 + 6];
            System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
            while (!stopped) {
                Pair<Supplier<byte[]>, byte[]> newKeyPair = generator.generateNewQuick();

                //noinspection NonAtomicOperationOnVolatileField
                genCounter++;
                checkLogProgress();

                System.arraycopy(newKeyPair.getSecond(), 0, pkBytes, 1, 33);
                byte[] converted = Bits.convertBits(pkBytes, 0, pkBytes.length, 8, 5, true);

                for (int idx = 0; idx < converted.length; idx++) {
                    byte b = converted[idx];
                    accountChars[idx + 4] = alphabet[b];
                }

                System.arraycopy(converted, 0, enc, hrpExpanded.length, converted.length);
                int mod = polymod(enc) ^ 1;

                for (int idx = 0; idx < 6; idx++) {
                    byte b = (byte) (mod >>> 5 * (5 - idx) & 31);
                    accountChars[idx + 59] = alphabet[b];
                }


                if (AddressCriteria.containsAny(criteria, accountChars)) {
                    String accountAddress = String.valueOf(accountChars);
                    byte[] privateKey = newKeyPair.getFirst().get();
                    System.out.println("Worker " + i + " found a match: " + accountAddress);
                    if (params.isPrint()) {
                        System.out.println("Private key: " + Hex.toHexString(privateKey));
                    }
                    String fileName = params.getKeyStoreFile() != null ? params.getKeyStoreFile() : new File(new File(params.getKeyStoreDir()), accountAddress + ".ks").getCanonicalPath();
                    String keyName = params.getKeyStoreFile() != null ? "key" + i : "node";
                    writeKey(keyName, fileName, privateKey);
                    break;
                }
            }
        }
    }


}
