package io.dogecube;

import com.radixdlt.utils.Bits;
import com.radixdlt.utils.Pair;
import io.dogecube.generator.MnemonicAddressQuickGenerator;
import io.dogecube.params.AddressCriteria;
import io.dogecube.params.GeneratorParams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static io.dogecube.utils.Bech32Utils.*;

public class MnemonicGenerator extends Generator {

    private static final int LOG_INTERVAL = 100_000;
    private static final int SMALL_LOG_INTERVAL = 1_000;

    public MnemonicGenerator(GeneratorParams params, List<AddressCriteria> criteria) {
        super(params, criteria, LOG_INTERVAL, SMALL_LOG_INTERVAL);
    }

    private synchronized void saveWallet(String line) throws IOException {
        File file = new File("./wallets.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Files.write(file.toPath(), Collections.singletonList(line), StandardOpenOption.APPEND);
    }

    @Override
    protected void doWorkInternal(int i) throws Exception {
        MnemonicAddressQuickGenerator generator = new MnemonicAddressQuickGenerator();
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
        boolean found = false;
        while (!stopped && !found) {
            int count = params.getCount();
            Pair<String, List<byte[]>> newKeyPairs = generator.generateNewQuick(count);

            //noinspection NonAtomicOperationOnVolatileField
            genCounter += count;
            checkLogProgress();

            List<byte[]> publicKeys = newKeyPairs.getSecond();
            for (int j = 0; j < publicKeys.size(); j++) {
                byte[] publicKey = publicKeys.get(j);

                System.arraycopy(publicKey, 0, pkBytes, 1, 33);
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
                    int num = j + 1;
                    String mnemonic = newKeyPairs.getFirst();
                    System.out.println("Worker " + i + " found a match: " + accountAddress + " (address num: " + num + ")");

                    saveWallet(accountAddress + " (#" + num + ") -> " + mnemonic);

                    if (params.isPrint()) {
                        System.out.println("Mnemonic: " + mnemonic);
                    }
                    found = true;
                }
            }
        }
    }


}
