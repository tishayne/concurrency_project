/**
 * Each rendering thread can have its own settings, and we can easily pass them around without worrying about synchronization.
 * Immutable and thread-safe, as it only contains final fields and does not allow modification after creation.
 */
public final class RenderSettings {

    public final int maxCount;
    public final int numberOfThreads;
    public final boolean smooth;
    public final boolean antialias;
    public final int paletteIndex;
    public final double viewX;
    public final double viewY;
    public final double zoom;

    public RenderSettings(
            int maxCount,
            int numberOfThreads,
            boolean smooth,
            boolean antialias,
            int paletteIndex,
            double viewX,
            double viewY,
            double zoom
    ) {
        this.maxCount = maxCount;
        this.numberOfThreads = numberOfThreads;
        this.smooth = smooth;
        this.antialias = antialias;
        this.paletteIndex = paletteIndex;
        this.viewX = viewX;
        this.viewY = viewY;
        this.zoom = zoom;
    }
}