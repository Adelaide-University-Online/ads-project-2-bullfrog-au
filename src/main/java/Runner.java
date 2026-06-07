/**
 * File: Runner.java
 * Description: Main entry point for the OptiTime degree planning optimization tool.
 * Author: Tom Jeremiah
 * Student ID: a2970593@adelaide.edu.au
 * Email ID: tom.jeremiah@student.adelaide.edu.au
 * AI Tool Used: 
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 */

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * OptiTime - Optimizes degree course scheduling to minimize total study periods.
 * Interactive CLI tool that reads a degree definition file, constructs a course dependency graph,
 * and produces an optimal schedule respecting prerequisite constraints.
 */
public class Runner {
    private final Scanner scanner;
    private final PrintStream out;
    private final PrintStream err;
    private boolean running;

    /**
     * Constructs a Runner with the given I/O streams.
     * Keeping streams injectable rather than hard-wiring System.in/out allows
     * test code to capture output and supply scripted input without spawning
     * a real process.
     *
     * @param scanner the input source for user responses
     * @param out the stream for normal program output
     * @param err the stream for error messages (kept separate so callers can
     *            redirect errors independently of standard output)
     */
    public Runner(Scanner scanner, PrintStream out, PrintStream err) {
        this.scanner = scanner;
        this.out = out;
        this.err = err;
        this.running = true;
    }

    /**
     * Main method to run the degree planner with interactive CLI.
     * Prompts user for file path and concurrency limit, with error recovery and retry options.
     *
     * @param args command-line arguments (not used; interactive input only)
     */
    public static void main(String[] args) {
        Runner runner = new Runner(new Scanner(System.in), System.out, System.err);
        runner.run();
    }

    /**
     * Drives the interactive session loop.
     * Each iteration collects a file path and concurrency limit, runs the planner,
     * then asks whether the user wants another run — allowing exploration of different
     * concurrency settings without restarting the program.
     * The loop exits cleanly on user request or on an unrecoverable error.
     */
    void run() {
        displayWelcome();

        while (running) {
            try {
                String filePath = promptForFilePath();
                if (filePath == null) {
                    break;
                }
                
                int maxConcurrent = promptForConcurrency();
                if (maxConcurrent <= 0) {
                    break;
                }
                
                runDegreePlanner(filePath, maxConcurrent);
                
                if (!promptToContinue()) {
                    running = false;
                }

            } catch (Exception e) {
                err.println("\n[ERROR] Unexpected error: " + e.getMessage());
                if (!handleError()) {
                    running = false;
                }
            }
        }

        displayGoodbye();
        scanner.close();
    }

    /**
     * Displays the welcome message.
     */
    void displayWelcome() {
        out.println("\n╔════════════════════════════════════════════╗");
        out.println("║     OptiTime - Degree Planner v1.0         ║");
        out.println("║   Optimize Your Degree Schedule             ║");
        out.println("╚════════════════════════════════════════════╝\n");
    }

    /**
     * Displays the goodbye message.
     */
    void displayGoodbye() {
        out.println("\n╔════════════════════════════════════════════╗");
        out.println("║          Thank you for using OptiTime!      ║");
        out.println("║        Goodbye and good luck!               ║");
        out.println("╚════════════════════════════════════════════╝\n");
    }

    /**
     * Prompts user for the degree file path.
     *
     * @return the file path entered by the user, or null if user chooses to exit
     */
    String promptForFilePath() {
        while (true) {
            out.println("\n--- Step 1: Input File Path ---");
            out.print("Enter the path to your degree file: ");
            String filePath = scanner.nextLine().trim();
            
            if (filePath.isEmpty()) {
                out.println("[WARNING] File path cannot be empty.");
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            // Verify file exists
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                out.println("[ERROR] File not found: " + filePath);
                out.println("        Please check the file path and try again.");
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            if (!file.isFile()) {
                out.println("[ERROR] Path is not a file: " + filePath);
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            out.println("✓ File path accepted: " + filePath);
            return filePath;
        }
    }

    /**
     * Prompts user for the maximum number of concurrent courses.
     *
     * @return the concurrency limit, or 0 if user chooses to exit
     */
    int promptForConcurrency() {
        while (true) {
            out.println("\n--- Step 2: Maximum Concurrent Courses ---");
            out.print("Enter the maximum number of courses you can take at once: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                out.println("[WARNING] Input cannot be empty.");
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            int maxConcurrent;
            try {
                maxConcurrent = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                out.println("[ERROR] Invalid input: '" + input + "' is not a valid integer.");
                out.println("        Please enter a whole number.");
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            if (maxConcurrent < 1) {
                out.println("[ERROR] Concurrency must be at least 1.");
                out.println("        You entered: " + maxConcurrent);
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            if (maxConcurrent > 50) {
                out.println("[WARNING] Very high concurrency value (" + maxConcurrent + ").");
                out.print("Do you want to proceed anyway? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes") && !response.equals("y")) {
                    if (!promptToRetry("Enter concurrency")) {
                        return 0;
                    }
                    continue;
                }
            }
            
            out.println("✓ Concurrency limit accepted: " + maxConcurrent + " course(s)");
            return maxConcurrent;
        }
    }

    /**
     * Runs the degree planner with the specified file and concurrency limit.
     *
     * @param filePath path to the degree file
     * @param maxConcurrent maximum concurrent courses
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the course structure is invalid
     * @throws IllegalStateException if scheduling fails
     */
    void runDegreePlanner(String filePath, int maxConcurrent) 
            throws IOException, IllegalArgumentException, IllegalStateException {
        
        out.println("\n--- Processing Your Degree ---");
        
        // File parsing builds the graph structure; done first so any format errors
        // surface with line-number context before we attempt scheduling.
        out.println("▶ Reading course data from: " + filePath);
        Graph graph = FileParser.parseFile(filePath);
        out.println("  ✓ File parsed successfully");

        // Cycle detection must happen before the planner runs — a cycle would cause
        // planDegree() to loop indefinitely waiting for prerequisites that never clear.
        out.println("▶ Validating course structure...");
        FileParser.validateGraph(graph);
        out.println("  ✓ No circular dependencies detected");
        out.println("  ✓ All prerequisites are valid");

        // Surface key metrics so the user can confirm the file was interpreted correctly
        // before committing to a potentially long scheduling run.
        out.println("\n--- Degree Summary ---");
        out.println("  Total Courses: " + graph.getSize());
        out.println("  Max Concurrent: " + maxConcurrent);

        // DegreePlanner applies greedy topological scheduling; the result minimises
        // total study periods subject to the user-supplied concurrency constraint.
        out.println("\n▶ Planning optimal degree schedule...");
        DegreePlanner planner = new DegreePlanner(graph, maxConcurrent);
        List<List<Course>> schedule = planner.planDegree();
        out.println("  ✓ Schedule generated successfully");

        // Display the schedule
        out.println("\n╔════════════════════════════════════════════╗");
        out.println("║      OPTIMIZED DEGREE SCHEDULE              ║");
        out.println("╚════════════════════════════════════════════╝\n");
        
        out.println("Total Study Periods Required: " + planner.getTotalPeriods() + "\n");

        for (int period = 0; period < schedule.size(); period++) {
            List<Course> coursesInPeriod = schedule.get(period);
            out.println("Study Period " + (period + 1) + " (" + 
                               coursesInPeriod.size() + " course" + 
                               (coursesInPeriod.size() != 1 ? "s" : "") + "):");
            for (Course course : coursesInPeriod) {
                out.println("  • " + course.getCourseCode());
            }
            out.println();
        }

        out.println("═══════════════════════════════════════════════");
        out.println("All " + graph.getSize() + " courses scheduled across " + 
                         planner.getTotalPeriods() + " study period" +
                         (planner.getTotalPeriods() != 1 ? "s" : "") + ".");
        out.println("═══════════════════════════════════════════════");
    }

    /**
     * Handles an error by offering the user options to retry or terminate.
     *
     * @return true if user wants to continue, false if user wants to terminate
     */
    boolean handleError() {
        out.println("\nWould you like to:");
        out.println("  1. Try again");
        out.println("  2. Terminate");
        out.print("Enter your choice (1 or 2): ");
        
        String choice = scanner.nextLine().trim();
        return choice.equals("1");
    }

    /**
     * Prompts the user to retry an operation.
     *
     * @param operation the operation description (e.g., "Enter file path")
     * @return true if user wants to retry, false otherwise
     */
    boolean promptToRetry(String operation) {
        out.print("Would you like to " + operation + " again? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Prompts the user to continue with another degree plan or exit.
     *
     * @return true if user wants to continue, false if user wants to exit
     */
    boolean promptToContinue() {
        out.println("\nWould you like to plan another degree? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }
}
