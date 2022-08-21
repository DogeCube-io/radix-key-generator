# radix-key-generator
Radix Vanity Address Generator

A multithreaded address generator with a few optimizations that increase generation speed.

### Usage
```bash
java -jar radix-key-generator-0.2.jar [mode] <params> <patterns>
```
#### Modes
    --gen-key       - (default) generates a private key and stores it into a Java keystore.
                      The key can then be used to send transactions from the address via scripts
                      (Python, Java, Javascript, etc).
                      Unlike mnemonic, it can't be imported into a wallet but generation is 100 times faster.
    --mnemonic      - generates a 12-words mnemonic phrase that can be imported into Radix wallets.
                      The phrase is saved into "./wallets.txt" plaintext file.

#### Params (common)
    -t, --treads    - number of threads to use. By default, half of the CPU cores.
                      Past a certain point, using more cores may yield no visible increase in speed.
    -o, --print     - if set, private keys will be printed to standard output.

#### Params (private keys Mode only)
    -f, --file      - keystore file location. If set, all generated private keys will be
                      stored in that keystore.
    -d, --dir       - dir where to create keystores. If set, each generated private key
                      will be stored in its own keystore.
                      Either file or dir is required.
    -p, --pass      - password that should be used to encrypt java key stores.

#### Params (mnemonic phrase Mode only)
    -c, --count     - number of addresses per mnemonic phrase to try (by default - 5).
                      Increasing it will improve generation speed (almost linearly),
                      but you will need to import more addresses into the wallet.

    Patterns - a list of address patterns to search for, separated by spaces. A pattern may be:
        - a suffix (`grandma`)
        - a prefix (`rdx1qspd0ge3x` or equivalent `d0ge3x*`)
        - both a prefix and a suffix, separated by a wildcard (`d0ge3x*grandma`).
    Only one wildcard (`*`) is allowed.
    Additionally, you can use a question marks (`?`) to allow any char, for example: `d0?e3x*??a?dma`
                                
#### Examples
- private keys Mode:
    ```bash
    java -jar radix-key-generator-0.2.jar --dir ./keys --pass qwerty d0ge3x* *d0ge3x grandma
    java -jar radix-key-generator-0.2.jar --file ./keystore.ks --pass qwerty -t 1 --print d0ge3x* *d0ge3x grandma
    ```
- mnemonic phrase Mode:
    ```bash
    java -jar radix-key-generator-0.2.jar --mnemonic -t 4 --count 5 d0ge3x* *d0ge3x grandma
    ```


### Building from sources
Requires Maven 3, Java 17

```bash
mvn clean package
```

You can now run the packaged jar with:
```bash
java -jar ./target/radix-key-generator-0.2.jar [mode] <params> <patterns>
```
