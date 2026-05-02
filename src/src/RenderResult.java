import java.awt.image.BufferedImage;

public final class RenderResult {

    public final BufferedImage image;
    public final long renderTimeMs;

    public RenderResult(BufferedImage image, long renderTimeMs) {
        this.image = image;
        this.renderTimeMs = renderTimeMs;
    }
}