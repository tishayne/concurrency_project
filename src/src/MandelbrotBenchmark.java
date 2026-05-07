/**
 * This is to show that 4 cores are more than twice as fast as 1 core for rendering the Mandelbrot set.
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

        // This can be changed to test different thread counts.
        RenderSettings oneThreadSettings = copyWithThreads(baseSettings, 1);
        RenderSettings fourThreadSettings = copyWithThreads(baseSettings, 4);

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
                speedup
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

        public BenchmarkResult(
                long oneThreadTimeMs,
                long fourThreadTimeMs,
                double speedup
        ) {
            this.oneThreadTimeMs = oneThreadTimeMs;
            this.fourThreadTimeMs = fourThreadTimeMs;
            this.speedup = speedup;
        }


        public void print() {
            System.out.println("===== Mandelbrot Benchmark =====");
            System.out.println("Average render time with 1 thread:  " + oneThreadTimeMs + " ms");
            System.out.println("Average render time with 4 threads: " + fourThreadTimeMs + " ms");
            System.out.printf("Speedup: %.2fx%n", speedup);
        }
    }
}
