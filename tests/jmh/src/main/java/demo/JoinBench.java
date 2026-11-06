package demo;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/** Конкатенация в цикле против StringBuilder: тот же пример, что в журнале оптимизаций пайплайна. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class JoinBench {
    @Param({"100", "1000"})
    int size;

    String[] words;

    @Setup
    public void setup() {
        Random rnd = new Random(42);
        words = new String[size];
        for (int i = 0; i < size; i++) {
            words[i] = Integer.toString(rnd.nextInt(1_000_000));
        }
    }

    @Benchmark
    public String concat() {
        String s = "";
        for (String w : words) {
            s += w + ",";
        }
        return s;
    }

    @Benchmark
    public String builder() {
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(w).append(',');
        }
        return sb.toString();
    }
}
