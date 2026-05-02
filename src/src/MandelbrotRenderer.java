import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MandelbrotRenderer {

    private final Color[][] colors;

    public MandelbrotRenderer(Color[][] colors) {
        this.colors = colors;
    }

    public RenderResult render(int width, int height, RenderSettings settings) throws InterruptedException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        long startTime = System.nanoTime();

        int threads = Math.max(1, Math.min(settings.numberOfThreads, height));
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        int rowsPerThread = height / threads;

        for (int i = 0; i < threads; i++) {
            int yStart = i * rowsPerThread;
            int yEnd = (i == threads - 1) ? height : yStart + rowsPerThread;

            executor.submit(new MandelbrotTask(yStart, yEnd, width, height, image, settings));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            throw e;
        }

        long endTime = System.nanoTime();
        long renderTimeMs = (endTime - startTime) / 1_000_000;

        return new RenderResult(image, renderTimeMs);
    }

    private final class MandelbrotTask implements Runnable {
        private final int yStart;
        private final int yEnd;
        private final int width;
        private final int height;
        private final BufferedImage image;
        private final RenderSettings settings;

        private MandelbrotTask(
                int yStart,
                int yEnd,
                int width,
                int height,
                BufferedImage image,
                RenderSettings settings
        ) {
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.width = width;
            this.height = height;
            this.image = image;
            this.settings = settings;
        }

        @Override
        public void run() {
            double r = settings.zoom / Math.min(width, height);

            for (int y = yStart; y < yEnd; y++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                for (int x = 0; x < width; x++) {
                    double dx = 2.5 * (x * r + settings.viewX) - 2;
                    double dy = 1.25 - 2.5 * (y * r + settings.viewY);

                    Color color = color(dx, dy, settings);

                    if (settings.antialias) {
                        color = antialiasColor(dx, dy, r, color, settings);
                    }

                    image.setRGB(x, y, color.getRGB());
                }
            }
        }
    }

    private Color antialiasColor(double dx, double dy, double r, Color center, RenderSettings settings) {
        Color c1 = color(dx - 0.25 * r, dy - 0.25 * r, settings);
        Color c2 = color(dx + 0.25 * r, dy - 0.25 * r, settings);
        Color c3 = color(dx + 0.25 * r, dy + 0.25 * r, settings);
        Color c4 = color(dx - 0.25 * r, dy + 0.25 * r, settings);

        int red = (center.getRed() + c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed()) / 5;
        int green = (center.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen()) / 5;
        int blue = (center.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue() + c4.getBlue()) / 5;

        return new Color(red, green, blue);
    }

    private Color color(double x, double y, RenderSettings settings) {
        int count = mandel(0.0, 0.0, x, y, settings.maxCount);

        Color[] palette = colors[settings.paletteIndex];
        int palSize = palette.length;

        Color color = palette[count / 256 % palSize];

        if (settings.smooth) {
            Color color2 = palette[(count / 256 + palSize - 1) % palSize];

            int k1 = count % 256;
            int k2 = 255 - k1;

            int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
            int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
            int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;

            color = new Color(red, green, blue);
        }

        return color;
    }

    private int mandel(double zRe, double zIm, double pRe, double pIm, int maxCount) {
        double zRe2 = zRe * zRe;
        double zIm2 = zIm * zIm;
        double zM2 = 0.0;
        int count = 0;

        while (zRe2 + zIm2 < 4.0 && count < maxCount) {
            zM2 = zRe2 + zIm2;

            zIm = 2.0 * zRe * zIm + pIm;
            zRe = zRe2 - zIm2 + pRe;

            zRe2 = zRe * zRe;
            zIm2 = zIm * zIm;

            count++;
        }

        if (count == 0 || count == maxCount) {
            return 0;
        }

        zM2 += 0.000000001;

        return 256 * count
               + (int) (255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
    }
}