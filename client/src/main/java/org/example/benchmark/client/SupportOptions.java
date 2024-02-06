package org.example.benchmark.client;

import org.apache.commons.cli.CommandLine;
import org.openjdk.jmh.profile.Profiler;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * @author icodening
 * @date 2024.02.06
 */
public enum SupportOptions {

    WARMUP_ITERATIONS("warmupIterations", "3", Integer::parseInt),
    WARMUP_TIME("warmupTime", "10", Integer::parseInt),
    MEASUREMENT_ITERATIONS("measurementIterations", "3", Integer::parseInt),
    MEASUREMENT_TIME("measurementTime", "10", Integer::parseInt),
    FORKS("forks", "1", Integer::parseInt),
    THREADS("threads", "32", Integer::parseInt),
    RESULT_FILE("resultFile", "result.json", (arg) -> arg),
    PROFILER("profilers", "", SupportOptions::parseProfilers),
    ;

    private final String name;

    private final String defaultValue;

    private final Function<String, Object> parser;

    SupportOptions(String name, String defaultValue, Function<String, Object> parser) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public <T> T getParsedValue(CommandLine line) {
        String arg = line.getOptionValue(getName(), getDefaultValue());
        return (T) parser.apply(arg);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static SupportOptions fromOptionName(String name) {
        for (SupportOptions options : SupportOptions.values()) {
            if (options.getName().equals(name)) {
                return options;
            }
        }
        throw new IllegalArgumentException("Unsupported option name: " + name);
    }

    private static Class<? extends Profiler>[] parseProfilers(String args) {
        ArrayList<Class<? extends Profiler>> classes = new ArrayList<>();
        for (String p : args.split(",")) {
            Class<? extends Profiler> profiler = SupportProfilers.valueOf(p.toUpperCase()).getProfiler();
            classes.add(profiler);
        }
        return classes.toArray(new Class[0]);
    }


}
