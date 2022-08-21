package io.dogecube;

import io.dogecube.params.AddressCriteria;
import io.dogecube.params.GeneratorParams;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.dogecube.utils.Formatter.shortString;

public abstract class Generator {

    volatile int genCounter = 0;
    int epochs = 0;
    protected long startTime = System.currentTimeMillis();
    protected final AtomicInteger counter = new AtomicInteger(0);
    protected final GeneratorParams params;
    protected final List<AddressCriteria> criteria;
    protected final int logInterval;
    protected final int smallLogInterval;

    public boolean stopped;

    public Generator(GeneratorParams params, List<AddressCriteria> criteria, int logInterval, int smallLogInterval) {
        this.params = params;
        this.criteria = criteria;
        this.logInterval = logInterval;
        this.smallLogInterval = smallLogInterval;
    }

    protected abstract void doWorkInternal(int i) throws Exception;

    protected void checkLogProgress() {
        if (genCounter > logInterval) {
            synchronized (this) {
                if (genCounter > logInterval) {
                    int c = genCounter;
                    epochs++;
                    genCounter = 0;
                    long timeNow = System.currentTimeMillis();
                    double speed = c / ((timeNow - startTime) / 1000.0);
                    startTime = timeNow;
                    System.out.printf("Speed: %.1f/s -> %s iterations%n", speed, shortString(((long) logInterval) * epochs));
                }
            }
        } else if (genCounter % smallLogInterval == 0) {
            int c = genCounter;
            long timeNow = System.currentTimeMillis();
            // yes, the counter will be slightly ahead at this line, need to correct for it
            c = c / smallLogInterval * smallLogInterval;
            double speed = c / ((timeNow - startTime) / 1000.0);
            System.out.printf("speed: %.1f/s -> %s iterations\r", speed, shortString(c));
        }
    }

    public void doWork() {
        try {
            int i = counter.incrementAndGet();

            while (!stopped) {
                doWorkInternal(i);
                i = counter.incrementAndGet();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
