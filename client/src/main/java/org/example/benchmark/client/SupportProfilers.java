package org.example.benchmark.client;

import org.openjdk.jmh.profile.ClassloaderProfiler;
import org.openjdk.jmh.profile.CompilerProfiler;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.PausesProfiler;
import org.openjdk.jmh.profile.Profiler;

/**
 * @author icodening
 * @date 2024.02.06
 */
enum SupportProfilers {
    GC("gc", GCProfiler.class),
    CLASSLOADER("classloader", ClassloaderProfiler.class),
    COMPILER("compiler", CompilerProfiler.class),
    Pauses("pauses", PausesProfiler.class),
    MEMORY("memory", HotspotMemoryProfiler.class),
    ;

    private final String name;

    private final Class<? extends Profiler> profiler;

    SupportProfilers(String name, Class<? extends Profiler> profiler) {
        this.name = name;
        this.profiler = profiler;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Profiler> getProfiler() {
        return profiler;
    }
}
