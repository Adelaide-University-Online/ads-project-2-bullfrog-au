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
import java.util.List;
import java.util.Scanner;

/**
 * OptiTime - Optimizes degree course scheduling to minimize total study periods.
 * Interactive CLI tool that reads a degree definition file, constructs a course dependency graph,
 * and produces an optimal schedule respecting prerequisite constraints.
 */
public class Runner {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean running = true;

    /**
     * Main method to run the degree planner with interactive CLI.
     * Prompts user for file path and concurrency limit, with error recovery and retry options.
     *
     * @param args command-line arguments (not used; interactive input only)
     */
    public static void main(String[] args) {
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
                System.err.println("\n[ERROR] Unexpected error: " + e.getMessage());
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
    private static void displayWelcome() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║     OptiTime - Degree Planner v1.0         ║");
        System.out.println("║   Optimize Your Degree Schedule             ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }

    /**
     * Displays the goodbye message.
     */
    private static void displayGoodbye() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║          Thank you for using OptiTime!      ║");
        System.out.println("║        Goodbye and good luck!               ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }

    /**
     * Prompts user for the degree file path.
     *
     * @return the file path entered by the user, or null if user chooses to exit
     */
    private static String promptForFilePath() {
        while (true) {
            System.out.println("\n--- Step 1: Input File Path ---");
            System.out.print("Enter the path to your degree file: ");
            String filePath = scanner.nextLine().trim();
            
            if (filePath.isEmpty()) {
                System.out.println("[WARNING] File path cannot be empty.");
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            // Verify file exists
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                System.out.println("[ERROR] File not found: " + filePath);
                System.out.println("        Please check the file path and try again.");
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            if (!file.isFile()) {
                System.out.println("[ERROR] Path is not a file: " + filePath);
                if (!promptToRetry("Enter file path")) {
                    return null;
                }
                continue;
            }
            
            System.out.println("✓ File path accepted: " + filePath);
            return filePath;
        }
    }

    /**
     * Prompts user for the maximum number of concurrent courses.
     *
     * @return the concurrency limit, or 0 if user chooses to exit
     */
    private static int promptForConcurrency() {
        while (true) {
            System.out.println("\n--- Step 2: Maximum Concurrent Courses ---");
            System.out.print("Enter the maximum number of courses you can take at once: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("[WARNING] Input cannot be empty.");
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            int maxConcurrent;
            try {
                maxConcurrent = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid input: '" + input + "' is not a valid integer.");
                System.out.println("        Please enter a whole number.");
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            if (maxConcurrent < 1) {
                System.out.println("[ERROR] Concurrency must be at least 1.");
                System.out.println("        You entered: " + maxConcurrent);
                if (!promptToRetry("Enter concurrency")) {
                    return 0;
                }
                continue;
            }
            
            if (maxConcurrent > 50) {
                System.out.println("[WARNING] Very high concurrency value (" + maxConcurrent + ").");
                System.out.print("Do you want to proceed anyway? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes") && !response.equals("y")) {
                    if (!promptToRetry("Enter concurrency")) {
                        return 0;
                    }
                    continue;
                }
            }
            
            System.out.println("✓ Concurrency limit accepted: " + maxConcurrent + " course(s)");
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
    private static void runDegreePlanner(String filePath, int maxConcurrent) 
            throws IOException, IllegalArgumentException, IllegalStateException {
        
        System.out.println("\n--- Processing Your Degree ---");
        
        // Parse the input file
        System.out.println("▶ Reading course data from: " + filePath);
        Graph graph = FileParser.parseFile(filePath);
        System.out.println("  ✓ File parsed successfully");

        // Validate the graph
        System.out.println("▶ Validating course structure...");
        FileParser.validateGraph(graph);
        System.out.println("  ✓ No circular dependencies detected");
        System.out.println("  ✓ All prerequisites are valid");

        // Display graph information
        System.out.println("\n--- Degree Summary ---");
        System.out.println("  Total Courses: " + graph.getSize());
        System.out.println("  Max Concurrent: " + maxConcurrent);

        // Plan the degree
        System.out.println("\n▶ Planning optimal degree schedule...");
        DegreePlanner planner = new DegreePlanner(graph, maxConcurrent);
        List<List<Course>> schedule = planner.planDegree();
        System.out.println("  ✓ Schedule generated successfully");

        // Display the schedule
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║      OPTIMIZED DEGREE SCHEDULE              ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        System.out.println("Total Study Periods Required: " + planner.getTotalPeriods() + "\n");

        for (int period = 0; period < schedule.size(); period++) {
            List<Course> coursesInPeriod = schedule.get(period);
            System.out.println("Study Period " + (period + 1) + " (" + 
                               coursesInPeriod.size() + " course" + 
                               (coursesInPeriod.size() != 1 ? "s" : "") + "):");
            for (Course course : coursesInPeriod) {
                System.out.println("  • " + course.getCourseCode());
            }
            System.out.println();
        }

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("All " + graph.getSize() + " courses scheduled across " + 
                         planner.getTotalPeriods() + " study period" +
                         (planner.getTotalPeriods() != 1 ? "s" : "") + ".");
        System.out.println("═══════════════════════════════════════════════");
    }

    /**
     * Handles an error by offering the user options to retry or terminate.
     *
     * @return true if user wants to continue, false if user wants to terminate
     */
    private static boolean handleError() {
        System.out.println("\nWould you like to:");
        System.out.println("  1. Try again");
        System.out.println("  2. Terminate");
        System.out.print("Enter your choice (1 or 2): ");
        
        String choice = scanner.nextLine().trim();
        return choice.equals("1");
    }

    /**
     * Prompts the user to retry an operation.
     *
     * @param operation the operation description (e.g., "Enter file path")
     * @return true if user wants to retry, false otherwise
     */
    private static boolean promptToRetry(String operation) {
        System.out.print("Would you like to " + operation + " again? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    /**
     * Prompts the user to continue with another degree plan or exit.
     *
     * @return true if user wants to continue, false if user wants to exit
     */
    private static boolean promptToContinue() {
        System.out.println("\nWould you like to plan another degree? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }
}
