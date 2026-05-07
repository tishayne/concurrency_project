import java.awt.Color;

/**
 * Factory for creating color palettes.
 */
public final class ColorFactory {
    private static final int[][][] COLPAL = {
            {{12, 0, 10, 20}, {12, 50, 100, 240}, {12, 20, 3, 26}, {12, 230, 60, 20},
                    {12, 25, 10, 9}, {12, 230, 170, 0}, {12, 20, 40, 10}, {12, 0, 100, 0},
                    {12, 5, 10, 10}, {12, 210, 70, 30}, {12, 90, 0, 50}, {12, 180, 90, 120},
                    {12, 0, 20, 40}, {12, 30, 70, 200}},
            {{10, 70, 0, 20}, {10, 100, 0, 100}, {14, 255, 0, 0}, {10, 255, 200, 0}},
            {{8, 40, 70, 10}, {9, 40, 170, 10}, {6, 100, 255, 70}, {8, 255, 255, 255}},
            {{12, 0, 0, 64}, {12, 0, 0, 255}, {10, 0, 255, 255}, {12, 128, 255, 255}, {14, 64, 128, 255}},
            {{16, 0, 0, 0}, {32, 255, 255, 255}},
    };

    private ColorFactory() {}

    public static Color[][] createPalettes() {
        Color[][] colors = new Color[COLPAL.length][];

        for (int p = 0; p < COLPAL.length; p++) {
            int n = 0;

            for (int i = 0; i < COLPAL[p].length; i++) {
                n += COLPAL[p][i][0];
            }

            colors[p] = new Color[n];
            n = 0;

            for (int i = 0; i < COLPAL[p].length; i++) {
                int[] c1 = COLPAL[p][i];
                int[] c2 = COLPAL[p][(i + 1) % COLPAL[p].length];

                for (int j = 0; j < c1[0]; j++) {
                    colors[p][n + j] = new Color(
                            (c1[1] * (c1[0] - 1 - j) + c2[1] * j) / (c1[0] - 1),
                            (c1[2] * (c1[0] - 1 - j) + c2[2] * j) / (c1[0] - 1),
                            (c1[3] * (c1[0] - 1 - j) + c2[3] * j) / (c1[0] - 1)
                    );
                }

                n += c1[0];
            }
        }

        return colors;
    }

}
