package io.dogecube.params;

import io.dogecube.utils.MaskedString;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GeneratorParams {
    private Mode mode;
    private int threads;
    private boolean print;

    private String keyStoreDir;
    private String keyStoreFile;
    private MaskedString pass;

    private int count;


    private List<AddressCriteria> criteria = new ArrayList<>();

    public static GeneratorParams parseArguments(String[] args) {
        if (args.length == 0) {
            return printAndExit();
        }
        GeneratorParams params = new GeneratorParams();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            switch (arg) {
                case "-h", "--help" -> {
                    return printAndExit();
                }
                case "--mnemonic" -> params.setMode(Mode.MNEMONIC);
                case "--gen-key" -> params.setMode(Mode.GEN_KEY);
                case "-t", "--treads" -> params.setThreads(Integer.parseInt(args[++i]));
                case "-o", "--print" -> params.setPrint(true);
                case "-d", "--dir" -> params.setKeyStoreDir(args[++i]);
                case "-f", "--file" -> params.setKeyStoreFile(args[++i]);
                case "-p", "--pass" -> params.setPass(new MaskedString(args[++i]));
                case "-c", "--count" -> params.setCount(Integer.parseInt(args[++i]));
                default -> params.addPattern(args[i]);
            }
        }
        // set defaults:
        if (params.getMode() == null) {
            params.setMode(Mode.GEN_KEY);
        }
        if (params.getThreads() == 0) {
            params.setThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        }

        if (params.getCriteria().isEmpty()) {
            throw new RuntimeException("Please specify at least one address pattern.");
        }

        if (params.getMode() == Mode.GEN_KEY) {
            if (params.getKeyStoreDir() == null && params.getKeyStoreFile() == null) {
                throw new RuntimeException("Please specify either a keystore file or a directory where created keystores will be stored!");
            }

            if (params.getPass() == null) {
                params.setPass(new MaskedString(""));
            }
        } else {
            if (params.getCount() == 0) {
                params.setCount(5);
            }
        }

        return params;
    }

    private void addPattern(String pattern) {
        criteria.add(new AddressCriteria(pattern));
    }

    private static GeneratorParams printAndExit() {
        System.out.println("""
                Usage:
                    java -jar radix-key-generator-0.2.jar [mode] <params> <patterns>
                    
                Mode (optional) should be on of:
                    --gen-key       - (default) generates a private key and stores it into a Java keystore.
                                      The key can then be used to send transactions from the address via scripts
                                      (Python, Java, Javascript, etc).
                                      Unlike mnemonic, it can't be imported into a wallet but generation is 100 times faster.
                    --mnemonic      - generates a 12-words mnemonic phrase that can be imported into Radix wallets.
                                      The phrase is saved into "./wallets.txt" plaintext file.
                Params (common):
                    -t, --treads    - number of threads to use. By default, half of the CPU cores.
                                      Past a certain point, using more cores may yield no visible increase in speed.
                    -o, --print     - if set, private keys will be printed to standard output.
                                
                Params (private keys Mode only):
                    -f, --file      - keystore file location. If set, all generated private keys will be
                                      stored in that keystore.
                    -d, --dir       - dir where to create keystores. If set, each generated private key
                                      will be stored in its own keystore.
                                      Either file or dir is required.
                    -p, --pass      - password that should be used to encrypt java key stores.
                    
                Params (mnemonic phrase Mode only):
                    -c, --count     - number of addresses per mnemonic phrase to try (by default - 5).
                                      Increasing it will improve generation speed (almost linearly),
                                      but you will need to import more addresses into the wallet.
                    
                Patterns - a list of address patterns to search for, separated by spaces. A pattern may be:
                    - a suffix (`grandma`)
                    - a prefix (`rdx1qspd0ge3x` or equivalent `d0ge3x*`)
                    - both a prefix and a suffix, separated by a wildcard (`d0ge3x*grandma`).
                Only one wildcard (`*`) is allowed.
                Additionally, you can use a question marks (`?`) to allow any char, for example: `d0?e3x*??a?dma`
                                
                Examples:
                    - private keys Mode:
                        java -jar radix-key-generator-0.2.jar --dir ./keys --pass qwerty d0ge3x* *d0ge3x grandma
                        java -jar radix-key-generator-0.2.jar --file ./keystore.ks --pass qwerty -t 1 --print d0ge3x* *d0ge3x grandma
                    - mnemonic phrase Mode:
                        java -jar radix-key-generator-0.2.jar --mnemonic -t 4 --count 5 d0ge3x* *d0ge3x grandma
                """);
        return null;
    }

    public enum Mode {
        MNEMONIC,
        GEN_KEY
    }

}
