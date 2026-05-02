import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public final class MandelbrotPanel extends JPanel
        implements MouseListener, MouseMotionListener, KeyListener, Runnable {

    private int numberOfThreads = 4;
    private long lastRenderTimeMs = 0;

    private int maxCount = 192;
    private boolean smooth = false;
    private boolean antialias = false;

    private boolean toDrag = false;
    private boolean rect = true;

    private int paletteIndex = 0;

    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    private BufferedImage image;
    private int width;
    private int height;

    private volatile Thread thread = null;

    private int mouseX;
    private int mouseY;
    private int dragX;
    private int dragY;

    private final Color[][] colors = ColorFactory.createPalettes();
    private final MandelbrotRenderer renderer = new MandelbrotRenderer(colors);

    public MandelbrotPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    public void start() {
        redraw();
    }

    public void destroy() {
        Thread currentThread = thread;
        thread = null;

        if (currentThread != null) {
            currentThread.interrupt();
        }
    }

    private void redraw() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        } else {
            thread = new Thread(this, "Mandelbrot render thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            while (draw()) {
                // Redraw immediately if the previous render was interrupted.
            }

            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // A redraw request interrupts the waiting render thread.
                }
            }
        }
    }

    private boolean draw() {
        Dimension size = getSize();

        if (size.width <= 0 || size.height <= 0) {
            return false;
        }

        width = size.width;
        height = size.height;

        RenderSettings settings = new RenderSettings(
                maxCount,
                numberOfThreads,
                smooth,
                antialias,
                paletteIndex,
                viewX,
                viewY,
                zoom
        );

        try {
            RenderResult result = renderer.render(width, height, settings);
            image = result.image;
            lastRenderTimeMs = result.renderTimeMs;
        } catch (InterruptedException e) {
            return true;
        }

        repaint();
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) {
            return;
        }

        Dimension size = getSize();

        if (size.width != width || size.height != height) {
            redraw();
            return;
        }

        g.drawImage(image, 0, 0, null);

        if (toDrag) {
            drawDragOverlay(g);
        }

        drawStatusText(g);
    }

    private void drawDragOverlay(Graphics g) {
        g.setColor(Color.BLACK);
        g.setXORMode(Color.WHITE);

        if (rect) {
            int x = Math.min(mouseX, dragX);
            int y = Math.min(mouseY, dragY);

            double w = mouseX + dragX - 2 * x;
            double h = mouseY + dragY - 2 * y;
            double r = Math.max(w / width, h / height);

            g.drawRect(x, y, (int) (width * r), (int) (height * r));
        } else {
            g.drawLine(mouseX, mouseY, dragX, dragY);
        }

        g.setPaintMode();
    }

    private void drawStatusText(Graphics g) {
        String status = "Threads=" + numberOfThreads
                        + " | Iterations=" + maxCount
                        + " | Time=" + lastRenderTimeMs + " ms";

        FontMetrics metrics = g.getFontMetrics();

        int textX = 10;
        int textY = getHeight() - 10;

        int boxX = 5;
        int boxY = getHeight() - 25;
        int boxWidth = metrics.stringWidth(status) + 12;
        int boxHeight = 20;

        g.setColor(Color.WHITE);
        g.fillRect(boxX, boxY, boxWidth, boxHeight);

        g.setColor(Color.BLACK);
        g.drawRect(boxX, boxY, boxWidth, boxHeight);
        g.drawString(status, textX, textY);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();

        mouseX = dragX = e.getX();
        mouseY = dragY = e.getY();

        toDrag = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        toDrag = false;

        int x = e.getX();
        int y = e.getY();

        if (SwingUtilities.isLeftMouseButton(e)) {
            handleLeftMouseRelease(x, y);
            redraw();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            maxCount += maxCount / 4;
            redraw();
        }
    }

    private void handleLeftMouseRelease(int x, int y) {
        double r = zoom / Math.min(width, height);

        if (!rect) {
            viewX += (mouseX - x) * r;
            viewY += (mouseY - y) * r;
        } else if (x == mouseX && y == mouseY) {
            viewX += 0.5 * x * r;
            viewY += 0.5 * y * r;
            zoom *= 0.5;
        } else {
            int mx = Math.min(x, mouseX);
            int my = Math.min(y, mouseY);

            viewX += mx * r;
            viewY += my * r;

            double w = x + mouseX - 2 * mx;
            double h = y + mouseY - 2 * my;

            zoom *= Math.max(w / width, h / height);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            dragX = e.getX();
            dragY = e.getY();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            resetView();
            redraw();
        } else if (keyCode == KeyEvent.VK_I) {
            zoomIn();
            redraw();
        } else if (keyCode == KeyEvent.VK_O) {
            zoomOut();
            redraw();
        } else if (keyCode == KeyEvent.VK_P) {
            changePalette(e.isShiftDown());
            redraw();
        } else if (keyCode == KeyEvent.VK_S) {
            smooth = !smooth;
            redraw();
        } else if (keyCode == KeyEvent.VK_A) {
            antialias = !antialias;
            redraw();
        } else if (keyCode == KeyEvent.VK_T) {
            changeThreadCount(e.isShiftDown());
            redraw();
        } else if (keyCode == KeyEvent.VK_C) {
            changeMaxIterations(e.isShiftDown());
            redraw();
        } else if (keyCode == KeyEvent.VK_SHIFT) {
            rect = false;

            if (toDrag) {
                repaint();
            }
        }
    }

    private void resetView() {
        maxCount = 192;
        viewX = 0.0;
        viewY = 0.0;
        zoom = 1.0;
    }

    private void zoomIn() {
        viewX += 0.25 * zoom;
        viewY += 0.25 * zoom;
        zoom *= 0.5;
    }

    private void zoomOut() {
        viewX -= 0.5 * zoom;
        viewY -= 0.5 * zoom;
        zoom *= 2.0;
    }

    private void changePalette(boolean backwards) {
        if (backwards) {
            paletteIndex = (paletteIndex + colors.length - 1) % colors.length;
        } else {
            paletteIndex = (paletteIndex + 1) % colors.length;
        }
    }

    private void changeThreadCount(boolean decrease) {
        if (decrease) {
            numberOfThreads = Math.max(1, numberOfThreads - 1);
        } else {
            numberOfThreads++;
        }
    }

    private void changeMaxIterations(boolean decrease) {
        if (decrease) {
            maxCount = Math.max(32, maxCount - 32);
        } else {
            maxCount += 32;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            rect = true;

            if (toDrag) {
                repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not used.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Not used.
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }
}