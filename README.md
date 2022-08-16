# radix-key-generator
Radix Vanity Address Generator

A multithreaded address generator with a few optimizations that increase generation speed.

### Usage
```bash
java -jar radix-key-generator-0.1.jar <params> <patterns>
```
#### Params
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
                                
#### Examples
```bash
java -jar radix-key-generator-0.1.jar --dir ./keys --pass qwerty d0ge3x* *d0ge3x grandma
java -jar radix-key-generator-0.1.jar --file ./keystore.ks --pass qwerty -t 1 --print d0ge3x* *d0ge3x grandma
```


### Building from sources
Requires Maven 3, Java 17

```bash
mvn clean package
```

You can now run the packaged jar with:
```bash
java -jar ./target/radix-key-generator-0.1.jar <params> <patterns>
```
