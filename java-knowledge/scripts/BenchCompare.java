// Сравнивает прогоны JMH «до» и «после» по их JSON-файлам.
//
// Запуск (JDK 17+, без сборки и зависимостей):
//   java BenchCompare.java <base> <diff> [--threshold 5]
// <base> и <diff> — JSON-файл одного прогона или папка с JSON нескольких прогонов.
// JSON получают так: java -jar target/benchmarks.jar -rf json -rff bench/before/run1.json
//
// Шум между прогонами одного и того же кода (отдельными запусками JVM) на обычной машине
// доходит до 5-10%: JIT компилирует по-разному, меняются частота CPU и раскладка памяти.
// Статистика одного прогона этот шум не видит, поэтому снимайте по 5 прогонов на сторону,
// чередуя «до» и «после» в порядке ABBA. Вердикт ставится так:
// - несколько прогонов на сторону: «faster»/«slower», только если медиана оценок сдвинулась
//   больше порога и оценки всех прогонов одной стороны лежат строго по одну сторону от другой;
// - по одному прогону: то же по порогу и непересечению 99,9% доверительных интервалов JMH,
//   с предупреждением, что такому вердикту доверять нельзя.
// Иначе «same» (в пределах шума). Бенчмарк только с одной стороны — «only-base»/«only-diff».
// Скрипт печатает вероятность, с которой одинаковый код случайно разделился бы полностью
// при таком числе прогонов (3/3 — 10%, 5/5 — 0,8%), и предупреждает, если ошибка JMH
// внутри прогона больше 10% оценки.
// Направление учитывается по режиму: в thrpt больше — лучше, в avgt/sample/ss меньше — лучше.
// Если прогоны сняты с -prof gc, печатается и gc.alloc.rate.norm (байт на операцию).
// Бенчмарк с разными единицами по сторонам (-tu, @OutputTimeUnit) или с NaN в оценке
// не сравнивается: вердикт «unit-mismatch» или «invalid» и код выхода 2.
// Код выхода: 0 — регрессий нет, 1 — есть хотя бы одно «slower», 2 — ошибка ввода
// или нет ни одного бенчмарка, который есть с обеих сторон.

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

public class BenchCompare {

    record Run(double score, double low, double high, String unit, OptionalDouble allocNorm) {}

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        List<String> positional = new ArrayList<>();
        double threshold = 0.05;
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--threshold")) {
                    threshold = Double.parseDouble(args[++i].replace("%", "")) / 100;
                } else if (args[i].startsWith("--threshold=")) {
                    threshold = Double.parseDouble(args[i].substring(12).replace("%", "")) / 100;
                } else {
                    positional.add(args[i]);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("bad --threshold value");
            return 2;
        }
        if (!Double.isFinite(threshold) || threshold < 0) {
            System.err.println("--threshold must be a non-negative number of percent");
            return 2;
        }
        if (positional.size() != 2) {
            System.err.println("usage: java BenchCompare.java <base> <diff> [--threshold 5]");
            return 2;
        }

        Map<String, List<Run>> base;
        Map<String, List<Run>> diff;
        try {
            base = loadRuns(Path.of(positional.get(0)));
            diff = loadRuns(Path.of(positional.get(1)));
        } catch (IOException | RuntimeException e) {
            System.err.println("cannot read input: " + e
                    + " (expected JMH JSON from -rf json -rff <file>)");
            return 2;
        }
        if (base.isEmpty() || diff.isEmpty()) {
            System.err.println("no benchmarks found");
            return 2;
        }
        return report(base, diff, threshold);
    }

    // ---- сравнение и вывод ----

    static int report(Map<String, List<Run>> base, Map<String, List<Run>> diff, double threshold) {
        TreeSet<String> names = new TreeSet<>(base.keySet());
        names.addAll(diff.keySet());
        int width = Math.max("Benchmark".length(), names.stream().mapToInt(String::length).max().orElse(0));
        boolean anyAlloc = Stream.concat(base.values().stream(), diff.values().stream())
                .flatMap(List::stream).anyMatch(r -> r.allocNorm().isPresent());

        String header = String.format(Locale.ROOT, "%-" + width + "s  %5s  %14s  %14s  %-10s  %8s  %-7s",
                "Benchmark", "Runs", "Base score", "Diff score", "Unit", "Change", "Verdict");
        System.out.println(anyAlloc ? header + "  Alloc B/op base -> diff" : header);

        int regressions = 0;
        int invalid = 0;
        boolean singleRun = false;
        Map<String, Integer> runCounts = new TreeMap<>();
        TreeSet<String> noisy = new TreeSet<>();
        TreeSet<String> oneSided = new TreeSet<>();
        for (String name : names) {
            List<Run> b = base.get(name);
            List<Run> d = diff.get(name);
            if (b == null || d == null) {
                oneSided.add(name);
                System.out.printf(Locale.ROOT, "%-" + width + "s  %5s  %14s  %14s  %-10s  %8s  %s%n",
                        name, "", "", "", "", "", b == null ? "only-diff" : "only-base");
                continue;
            }
            List<Run> both = Stream.concat(b.stream(), d.stream()).toList();
            String verdict;
            if (both.stream().map(Run::unit).distinct().count() > 1) {
                verdict = "unit-mismatch";
            } else if (both.stream().anyMatch(r -> !Double.isFinite(r.score()))) {
                verdict = "invalid";
            } else {
                singleRun |= b.size() == 1 || d.size() == 1;
                if (b.size() > 1 && d.size() > 1) {
                    runCounts.merge(b.size() + "/" + d.size(), 1, Integer::sum);
                }
                if (both.stream().anyMatch(r -> relativeError(r) > NOISY_ERROR)) {
                    noisy.add(name);
                }
                verdict = verdict(b, d, threshold, higherIsBetter(name));
            }
            if (verdict.equals("slower")) {
                regressions++;
            } else if (!verdict.equals("faster") && !verdict.equals("same")) {
                invalid++;
            }
            String line = String.format(Locale.ROOT, "%-" + width + "s  %5s  %14s  %14s  %-10s  %8s  %-7s",
                    name, b.size() + "/" + d.size(), num(center(b)), num(center(d)),
                    String.join(",", both.stream().map(Run::unit).distinct().toList()),
                    changeText(center(b), center(d)), verdict);
            System.out.println(anyAlloc ? line + "  " + alloc(b) + " -> " + alloc(d) : line);
        }

        int compared = names.size() - oneSided.size();
        System.out.printf(Locale.ROOT, "%nthreshold %.0f%%; compared: %d; regressions: %d%n",
                threshold * 100, compared, regressions);
        System.out.println("Change is the change of the JMH score: for thrpt higher is better, "
                + "for avgt/sample/ss lower is better.");
        runCounts.forEach((counts, k) -> {
            String[] nm = counts.split("/");
            double p = 2 / binomial(Integer.parseInt(nm[0]) + Integer.parseInt(nm[1]), Integer.parseInt(nm[0]));
            System.out.printf(Locale.ROOT, "runs %s: identical code separates fully by chance with p = %.1f%% "
                    + "per benchmark, up to %.1f%% for at least one of %d%s%n",
                    counts, p * 100, (1 - Math.pow(1 - p, k)) * 100, k,
                    p > 0.05 ? " - too few runs, use 5 per side" : "");
        });
        if (!noisy.isEmpty()) {
            System.err.printf(Locale.ROOT, "warning: JMH error above %.0f%% of the score inside a run for %s; "
                    + "check warmup, forks and background load before trusting the verdict%n",
                    NOISY_ERROR * 100, String.join(", ", noisy));
        }
        if (singleRun) {
            System.err.println("warning: one run per side cannot separate a change from run-to-run noise; "
                    + "repeat 5 interleaved runs per side (ABBA order)");
        }
        if (!oneSided.isEmpty()) {
            System.err.println("warning: present on one side only (renamed benchmark, changed @Param or mode?): "
                    + String.join(", ", oneSided));
        }
        if (compared == 0) {
            System.err.println("error: no benchmark is present on both sides");
            return 2;
        }
        if (invalid > 0) {
            System.err.println("error: " + invalid + " benchmark(s) cannot be compared: different units "
                    + "(-tu, @OutputTimeUnit) or a NaN/infinite score; rerun with the same settings");
            return 2;
        }
        return regressions > 0 ? 1 : 0;
    }

    static String changeText(double base, double diff) {
        if (base == 0 && diff == 0) {
            return "+0.0%";
        }
        double change = (diff / base - 1) * 100;
        return Double.isFinite(change) ? String.format(Locale.ROOT, "%+7.1f%%", change) : "n/a";
    }

    /** Доля ошибки JMH (полуширина 99,9% интервала) от оценки. */
    static final double NOISY_ERROR = 0.10;

    static double relativeError(Run r) {
        return r.score() == 0 ? 0 : (r.high() - r.low()) / 2 / Math.abs(r.score());
    }

    static double binomial(int n, int k) {
        double c = 1;
        for (int i = 1; i <= k; i++) {
            c = c * (n - k + i) / i;
        }
        return c;
    }

    static String verdict(List<Run> base, List<Run> diff, double threshold, boolean higherIsBetter) {
        if (center(base) == 0 && center(diff) == 0) {
            return "same";
        }
        double change = center(diff) / center(base) - 1;
        boolean up;
        boolean down;
        if (base.size() > 1 && diff.size() > 1) {
            up = min(diff, Run::score) > max(base, Run::score);
            down = max(diff, Run::score) < min(base, Run::score);
        } else {
            // один прогон хотя бы с одной стороны: сравниваем объединения 99,9% интервалов JMH
            up = min(diff, Run::low) > max(base, Run::high);
            down = max(diff, Run::high) < min(base, Run::low);
        }
        boolean better = higherIsBetter ? change > threshold && up : change < -threshold && down;
        boolean worse = higherIsBetter ? change < -threshold && down : change > threshold && up;
        return better ? "faster" : worse ? "slower" : "same";
    }

    static boolean higherIsBetter(String name) {
        return name.contains("[thrpt]");
    }

    static double center(List<Run> runs) {
        return median(runs.stream().mapToDouble(Run::score).sorted().toArray());
    }

    static double median(double[] sorted) {
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2;
    }

    static double min(List<Run> runs, java.util.function.ToDoubleFunction<Run> f) {
        return runs.stream().mapToDouble(f).min().orElseThrow();
    }

    static double max(List<Run> runs, java.util.function.ToDoubleFunction<Run> f) {
        return runs.stream().mapToDouble(f).max().orElseThrow();
    }

    static String alloc(List<Run> runs) {
        double[] values = runs.stream().map(Run::allocNorm).filter(OptionalDouble::isPresent)
                .mapToDouble(OptionalDouble::getAsDouble).sorted().toArray();
        return values.length == 0 ? "-" : num(median(values));
    }

    static String num(double v) {
        double a = Math.abs(v);
        if (a == 0) {
            return "0";
        }
        if (a >= 1e6 || a < 1e-3) {
            return String.format(Locale.ROOT, "%.3e", v);
        }
        return String.format(Locale.ROOT, a >= 100 ? "%.1f" : "%.3f", v);
    }

    // ---- чтение JMH JSON ----

    static Map<String, List<Run>> loadRuns(Path path) throws IOException {
        List<Path> files;
        if (Files.isDirectory(path)) {
            try (Stream<Path> s = Files.list(path)) {
                files = s.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            }
            if (files.isEmpty()) {
                throw new IOException("no *.json in " + path);
            }
        } else {
            files = List.of(path);
        }
        Map<String, List<Run>> runs = new TreeMap<>();
        for (Path file : files) {
            String text = Files.readString(file);
            if (text.startsWith("﻿")) {
                text = text.substring(1); // BOM от редакторов Windows
            }
            Object root = new Json(text).parse();
            if (!(root instanceof List<?> benchmarks)) {
                throw new IllegalArgumentException(file + ": top level is not a JSON array");
            }
            for (Object item : benchmarks) {
                Map<?, ?> bench = (Map<?, ?>) item;
                Map<?, ?> primary = (Map<?, ?>) bench.get("primaryMetric");
                double score = toDouble(primary.get("score"));
                double low = score;
                double high = score;
                if (primary.get("scoreConfidence") instanceof List<?> ci && ci.size() == 2) {
                    double lo = toDouble(ci.get(0));
                    double hi = toDouble(ci.get(1));
                    if (!Double.isNaN(lo) && !Double.isNaN(hi)) {
                        low = lo;
                        high = hi;
                    }
                }
                runs.computeIfAbsent(key(bench), k -> new ArrayList<>())
                        .add(new Run(score, low, high, String.valueOf(primary.get("scoreUnit")), allocNorm(bench)));
            }
        }
        return runs;
    }

    /** Полное имя бенчмарка: метод, параметры @Param и режим, например {@code a.B.m(size=100)[avgt]}. */
    static String key(Map<?, ?> bench) {
        StringBuilder sb = new StringBuilder(String.valueOf(bench.get("benchmark")));
        if (bench.get("params") instanceof Map<?, ?> params && !params.isEmpty()) {
            StringBuilder p = new StringBuilder();
            new TreeMap<>(params).forEach((k, v) -> p.append(p.isEmpty() ? "" : ",").append(k).append('=').append(v));
            sb.append('(').append(p).append(')');
        }
        return sb.append('[').append(bench.get("mode")).append(']').toString();
    }

    static OptionalDouble allocNorm(Map<?, ?> bench) {
        if (bench.get("secondaryMetrics") instanceof Map<?, ?> secondary) {
            for (Map.Entry<?, ?> e : secondary.entrySet()) {
                if (String.valueOf(e.getKey()).endsWith("gc.alloc.rate.norm") && e.getValue() instanceof Map<?, ?> m) {
                    return OptionalDouble.of(toDouble(m.get("score")));
                }
            }
        }
        return OptionalDouble.empty();
    }

    /** JMH пишет NaN и бесконечности строками, например {@code "NaN"}. */
    static double toDouble(Object o) {
        if (o instanceof Double d) {
            return d;
        }
        if (o instanceof String s) {
            return Double.parseDouble(s);
        }
        throw new IllegalArgumentException("expected a number, got " + o);
    }

    /** Минимальный разбор JSON: объекты в LinkedHashMap, массивы в ArrayList, числа в Double. */
    static final class Json {
        private final String s;
        private int pos;

        Json(String s) {
            this.s = s;
        }

        Object parse() {
            Object v = value();
            skipWs();
            if (pos != s.length()) {
                throw error("trailing data");
            }
            return v;
        }

        private Object value() {
            skipWs();
            if (pos >= s.length()) {
                throw error("unexpected end");
            }
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> number();
            };
        }

        private Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            pos++;
            skipWs();
            if (peek() == '}') {
                pos++;
                return m;
            }
            while (true) {
                skipWs();
                String k = string();
                skipWs();
                expect(':');
                m.put(k, value());
                skipWs();
                if (peek() == ',') {
                    pos++;
                } else {
                    expect('}');
                    return m;
                }
            }
        }

        private List<Object> array() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWs();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(value());
                skipWs();
                if (peek() == ',') {
                    pos++;
                } else {
                    expect(']');
                    return list;
                }
            }
        }

        private String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (pos >= s.length()) {
                    throw error("unterminated string");
                }
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = s.charAt(pos++);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> sb.append(e);
                }
            }
        }

        private Double number() {
            int start = pos;
            char first = s.charAt(pos);
            if (first != '-' && (first < '0' || first > '9')) {
                throw error("unexpected character '" + first + "'");
            }
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            return Double.parseDouble(s.substring(start, pos));
        }

        private Object literal(String word, Object v) {
            if (!s.startsWith(word, pos)) {
                throw error("expected " + word);
            }
            pos += word.length();
            return v;
        }

        private void expect(char c) {
            if (peek() != c) {
                throw error("expected '" + c + "'");
            }
            pos++;
        }

        private char peek() {
            return pos < s.length() ? s.charAt(pos) : '\0';
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        private IllegalArgumentException error(String msg) {
            return new IllegalArgumentException("JSON " + msg + " at offset " + pos);
        }
    }
}
