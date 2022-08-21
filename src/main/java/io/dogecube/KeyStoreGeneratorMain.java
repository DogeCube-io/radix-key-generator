package io.dogecube;

import io.dogecube.params.AddressCriteria;
import io.dogecube.params.GeneratorParams;
import io.dogecube.params.Validator;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.dogecube.utils.Formatter.shortNumber;
import static io.dogecube.utils.Formatter.withNumberGroups;

public class KeyStoreGeneratorMain {


    public static void main(String[] args) throws InterruptedException {
        GeneratorParams params = GeneratorParams.parseArguments(args);
        if (params == null) {
            return;
        }
        System.out.println(params);

        List<AddressCriteria> criteria = params.getCriteria();
        Validator validator = new Validator();

        validator.validate(criteria);
        printExpectations(criteria);

        Generator generator = params.getMode() == GeneratorParams.Mode.GEN_KEY
                ? new KeyStoreGenerator(params, criteria)
                : new MnemonicGenerator(params, criteria);
        if (params.getKeyStoreDir() != null) {
            new File(params.getKeyStoreDir()).mkdirs();
        }

        int threadsCount = params.getThreads();
        List<Thread> threads = new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            generator.stopped = true;
            System.out.println("Shutting down gracefully...");
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Done.");
        }));

        for (int i = 0; i < threadsCount; i++) {
            Thread t = new Thread(generator::doWork);
            threads.add(t);
            t.start();
            Thread.sleep(100);
        }
    }

    private static void printExpectations(List<AddressCriteria> criterias) {
        criterias.sort(Comparator.comparingLong(AddressCriteria::getDifficulty).thenComparing(AddressCriteria::toString));

        System.out.println("Pattern -> difficulty (iterations)");
        BigDecimal frequency = BigDecimal.ZERO;
        for (AddressCriteria criteria : criterias) {
            long difficulty = criteria.getDifficulty();

            System.out.println(criteria + " -> " + withNumberGroups(difficulty));
            frequency = frequency.add(BigDecimal.ONE.divide(BigDecimal.valueOf(difficulty), 25, RoundingMode.HALF_EVEN));
        }
        BigDecimal result = divide(BigDecimal.ONE, frequency);
        System.out.println("Expecting a match every " + shortNumber(Math.round(result.doubleValue())) + " iterations.");
    }

    private static BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, 24, RoundingMode.HALF_EVEN);
    }
}
