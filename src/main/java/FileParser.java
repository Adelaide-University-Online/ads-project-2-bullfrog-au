/**
 * File: FileParser.java
 * Description: Parses degree course files and constructs a Graph of course dependencies.
 * Author: Tom Jeremiah
 * Student ID: a2970593@adelaide.edu.au
 * Email ID: tom.jeremiah@student.adelaide.edu.au
 * AI Tool Used: 
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 */

import java.io.*;
import java.util.*;

/**
 * Parses text files containing course information and prerequisite structures.
 * File format:
 * - Line 1: Comma-separated list of all course codes
 * - Lines 2+: Each line starts with a course code followed by its prerequisites (comma-separated)
 */
public class FileParser {

    /**
     * Parses a file and constructs a Graph from the course data.
     *
     * @param filePath the path to the input file
     * @return a Graph representing the course network
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the file format is invalid
     */
    public static Graph parseFile(String filePath) throws IOException {
        Graph graph = new Graph();
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Parse first line: all courses in the degree
        String[] allCourses = lines.get(0).split(",");
        for (String courseCode : allCourses) {
            courseCode = courseCode.trim();
            if (!courseCode.isEmpty()) {
                graph.addCourse(courseCode);
            }
        }

        // Parse remaining lines: prerequisites for each course
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length > 0) {
                String courseCode = parts[0].trim();

                if (!graph.containsCourse(courseCode)) {
                    throw new IllegalArgumentException(
                        "Course " + courseCode + " on line " + (i + 1) + 
                        " not found in the course list on line 1");
                }

                // Add prerequisites (all remaining parts on this line)
                for (int j = 1; j < parts.length; j++) {
                    String prerequisiteCode = parts[j].trim();
                    if (!prerequisiteCode.isEmpty()) {
                        if (!graph.containsCourse(prerequisiteCode)) {
                            throw new IllegalArgumentException(
                                "Prerequisite " + prerequisiteCode + " for course " + 
                                courseCode + " not found in the course list");
                        }
                        graph.addPrerequisite(courseCode, prerequisiteCode);
                    }
                }
            }
        }

        return graph;
    }

    /**
     * Validates that all courses in the prerequisite lines are defined in line 1.
     *
     * @param graph the graph to validate
     * @throws IllegalArgumentException if prerequisites reference undefined courses
     */
    public static void validateGraph(Graph graph) {
        for (Course course : graph.getAllCourses()) {
            for (Course prereq : course.getPrerequisites()) {
                if (!graph.containsCourse(prereq.getCourseCode())) {
                    throw new IllegalArgumentException(
                        "Prerequisite " + prereq.getCourseCode() + 
                        " is not in the course list");
                }
            }
        }

        if (graph.hasCycle()) {
            throw new IllegalArgumentException(
                "Circular prerequisite dependency detected in the course graph");
        }
    }

    /**
     * Returns a string representation of the parser (metadata only).
     *
     * @return a string describing the FileParser
     */
    @Override
    public String toString() {
        return "FileParser{}";
    }

    /**
     * Compares two FileParser instances for equality (always equal as they have no state).
     *
     * @param obj the object to compare with
     * @return true if obj is a FileParser, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileParser;
    }

    /**
     * Generates a hash code for this FileParser.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(FileParser.class);
    }
}
