/**
 * Rendering Time and Speedup between two Thread Counts.
 * Between 1 thread and configured number of threads when running the benchmark in the application..
 */
public class MandelbrotBenchmark {

    private final MandelbrotRenderer renderer;
    private final int width;
    private final int height;
    private final RenderSettings baseSettings;

    public MandelbrotBenchmark(
            MandelbrotRenderer renderer,
            int width,
            int height,
            RenderSettings baseSettings
    ) {
        this.renderer = renderer;
        this.width = width;
        this.height = height;
        this.baseSettings = baseSettings;
    }

    public BenchmarkResult run() throws InterruptedException {
        int warmupRuns = 2;
        int measuredRuns = 5;

        // This could also be changed to test different thread counts.
        RenderSettings oneThreadSettings = copyWithThreads(baseSettings, 1);
        RenderSettings fourThreadSettings = copyWithThreads(baseSettings, baseSettings.numberOfThreads);

        long oneThreadAverage = averageRenderTime(
                oneThreadSettings,
                warmupRuns,
                measuredRuns
        );

        long fourThreadAverage = averageRenderTime(
                fourThreadSettings,
                warmupRuns,
                measuredRuns
        );

        double speedup = (double) oneThreadAverage / fourThreadAverage;

        return new BenchmarkResult(
                oneThreadAverage,
                fourThreadAverage,
                speedup,
                baseSettings
        );
    }

    private long averageRenderTime(
            RenderSettings settings,
            int warmupRuns,
            int measuredRuns
    ) throws InterruptedException {

        for (int i = 0; i < warmupRuns; i++) {
            renderer.render(width, height, settings);
        }

        long totalTime = 0;

        for (int i = 0; i < measuredRuns; i++) {
            RenderResult result = renderer.render(width, height, settings);
            totalTime += result.renderTimeMs;
        }

        return totalTime / measuredRuns;
    }

    private RenderSettings copyWithThreads(RenderSettings settings, int numberOfThreads) {
        return new RenderSettings(
                settings.maxCount,
                numberOfThreads,
                settings.smooth,
                settings.antialias,
                settings.paletteIndex,
                settings.viewX,
                settings.viewY,
                settings.zoom
        );
    }

    public static final class BenchmarkResult {

        private final long oneThreadTimeMs;
        private final long fourThreadTimeMs;
        private final double speedup;
        private final RenderSettings settings;

        public BenchmarkResult(
                long oneThreadTimeMs,
                long fourThreadTimeMs,
                double speedup,
                RenderSettings settings
        ) {
            this.oneThreadTimeMs = oneThreadTimeMs;
            this.fourThreadTimeMs = fourThreadTimeMs;
            this.speedup = speedup;
            this.settings = settings;
        }


        public void print() {
            System.out.println("===== Mandelbrot Benchmark =====");
            System.out.println("Average render time with " + 1 + " thread:  " + oneThreadTimeMs + " ms");
            System.out.println("Average render time with "  + settings.numberOfThreads + " threads: " + fourThreadTimeMs + " ms");
            System.out.printf("Speedup: %.2fx%n", speedup);
            System.out.println("Iterations: " + settings.maxCount);
        }
    }
}
