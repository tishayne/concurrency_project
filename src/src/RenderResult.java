import java.awt.image.BufferedImage;

/**
 * Result of rendering the Mandelbrot set, containing the rendered image and the time taken to render it.
 * Immutable and thread-safe, as it only contains final fields and does not allow modification after creation.
 */
public final class RenderResult {

    public final BufferedImage image;
    public final long renderTimeMs;

    public RenderResult(BufferedImage image, long renderTimeMs) {
        this.image = image;
        this.renderTimeMs = renderTimeMs;
    }
}