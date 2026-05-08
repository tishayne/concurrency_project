# Parallel Mandelbrot Application

This project is a Java Swing application that computes and displays the Mandelbrot set.  
The application supports parallel rendering with multiple threads and includes a small benchmark to compare rendering performance with different thread counts.

## Requirements

- Java JDK 17 or newer
- A Java IDE such as IntelliJ IDEA, Eclipse, or VS Code

## Project Structure

```text
src/
 ├── MandelbrotApplication.java
 ├── MandelbrotPanel.java
 ├── MandelbrotRenderer.java
 ├── MandelbrotBenchmark.java
 ├── RenderSettings.java
 └── RenderResult.java
```
## Running the Application
1. Clone the repository or download the source code.
2. Open the project in your Java IDE.
3. Run the `MandelbrotApplication` class to start the application.

## Running Benchmark
To run the benchmark:

1. Start the Mandelbrot application as above.
2. Wait until the application window is visible.
3. Press the **B** key.
4. The benchmark result is printed in the console.

The benchmark compares the render time with **1 thread** against current number of threads in the application. However this
setting can be changed in the code where indicated. 

Example console output:

```text
===== Mandelbrot Benchmark =====
Average render time with 1 thread:  1800 ms
Average render time with 4 threads: 620 ms
Speedup: 2.90x
```
## Shortcuts

| Shortcut          | Action                                    |
| ----------------- | ----------------------------------------- |
| **B**             | Run the benchmark                         |
| **S**             | Toggle smooth coloring                    |
| **A**             | Toggle antialiasing                       |
| **P**             | Change the color palette                  |
| **+**             | Increase the maximum number of iterations |
| **-**             | Decrease the maximum number of iterations |
| **Arrow keys**    | Move the visible area                     |
| **R**             | Reset the view                            |
| **Esc**           | Cancel the current selection or action    |
| **Mouse drag**    | Select an area or move the view           |
| **Mouse release** | Apply the selected zoom or movement       |
