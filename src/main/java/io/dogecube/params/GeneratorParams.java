package io.dogecube.params;

import io.dogecube.utils.MaskedString;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GeneratorParams {
    private int threads;
    private String keyStoreDir;
    private String keyStoreFile;
    private MaskedString pass;
    private boolean print;

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
                case "-t", "--treads" -> params.setThreads(Integer.parseInt(args[++i]));
                case "-d", "--dir" -> params.setKeyStoreDir(args[++i]);
                case "-f", "--file" -> params.setKeyStoreFile(args[++i]);
                case "-p", "--pass" -> params.setPass(new MaskedString(args[++i]));
                case "-o", "--print" -> params.setPrint(true);
                default -> params.addPattern(args[i]);
            }
        }
        // set defaults:
        if (params.getThreads() == 0) {
            params.setThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        }
        if (params.getKeyStoreDir() == null && params.getKeyStoreFile() == null) {
            throw new RuntimeException("Please specify either a keystore file or a directory where created keystores will be stored!");
        }
        if (params.getCriteria().isEmpty()) {
            throw new RuntimeException("Please specify at least one address pattern.");
        }

        if (params.getPass() == null) {
            params.setPass(new MaskedString(""));
        }

        return params;
    }

    private void addPattern(String pattern) {
        criteria.add(new AddressCriteria(pattern));
    }

    private static GeneratorParams printAndExit() {
        System.out.println("""
                Usage:
                    java -jar radix-key-generator-0.1.jar <params> <patterns>
                    
                Params:
                    -t, --treads    - number of threads to use. By default, half of the CPU cores.
                                      Past a certain point, using more cores may yield no visible increase in speed.
                    -f, --file      - keystore file location. If set, all generated private keys will be
                                      stored in that keystore.
                    -d, --dir       - dir where to create keystores. If set, each generated private key
                                      will be stored in its own keystore.
                                      Either file or dir is required.
                    -p, --pass      - password that should be used to encrypt java key stores.
                    -o, --print     - if set, private keys will be printed to standard output.
                    
                Patterns - a list of address patterns to search for, separated by spaces. A pattern may be:
                    - a suffix (`grandma`)
                    - a prefix (`rdx1qspd0ge3x` or equivalent `d0ge3x*`)
                    - both a prefix and a suffix, separated by a wildcard (`d0ge3x*grandma`).
                Only one wildcard (`*`) is allowed.
                Additionally, you can use a question marks (`?`) to allow any char, for example: `d0?e3x*??a?dma`
                                
                Examples:
                    java -jar radix-key-generator-0.1.jar --dir ./keys --pass qwerty d0ge3x* *d0ge3x grandma
                    java -jar radix-key-generator-0.1.jar --file ./keystore.ks --pass qwerty -t 1 --print d0ge3x* *d0ge3x grandma
                """);
        return null;
    }

}
