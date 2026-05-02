import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class MandelbrotApplication {

    private MandelbrotApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Parallel Mandelbrot");
            MandelbrotPanel panel = new MandelbrotPanel();

            frame.add(panel);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            panel.requestFocusInWindow();
            panel.start();
        });
    }
}