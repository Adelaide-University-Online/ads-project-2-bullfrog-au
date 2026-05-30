/**
 * File: DegreePlanner.java
 * Description: Schedules courses into study periods using topological sort and greedy packing.
 * Author: Tom Jeremiah
 * Student ID: a2970593@adelaide.edu.au
 * Email ID: tom.jeremiah@student.adelaide.edu.au
 * AI Tool Used: 
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 */

import java.util.*;

/**
 * Schedules courses across study periods while respecting prerequisite dependencies.
 * Uses topological sorting followed by greedy packing to minimize total study periods.
 */
public class DegreePlanner {
    private Graph graph;
    private int maxConcurrentCourses;
    private List<List<Course>> schedule;

    /**
     * Constructs a DegreePlanner for the given graph and concurrency limit.
     *
     * @param graph the graph representing course dependencies
     * @param maxConcurrentCourses the maximum number of courses that can be taken simultaneously
     * @throws IllegalArgumentException if maxConcurrentCourses is less than 1
     */
    public DegreePlanner(Graph graph, int maxConcurrentCourses) {
        if (maxConcurrentCourses < 1) {
            throw new IllegalArgumentException("Maximum concurrent courses must be at least 1");
        }
        this.graph = graph;
        this.maxConcurrentCourses = maxConcurrentCourses;
        this.schedule = new ArrayList<>();
    }

    /**
     * Plans the degree by scheduling courses across study periods.
     * Ensures all prerequisite constraints are satisfied while minimizing total periods.
     *
     * @return a list of study periods, each containing the courses to take that period
     */
    public List<List<Course>> planDegree() {
        schedule = new ArrayList<>();

        // Track which courses have been completed
        Set<Course> completed = new HashSet<>();

        // Continue until all courses are scheduled
        while (completed.size() < graph.getSize()) {
            List<Course> currentPeriod = new ArrayList<>();

            // Find courses that can be taken this period (all prerequisites completed)
            for (Course course : graph.getAllCourses()) {
                if (!completed.contains(course) && 
                    arePrerequisitesCompleted(course, completed) &&
                    currentPeriod.size() < maxConcurrentCourses) {
                    currentPeriod.add(course);
                }
            }

            if (currentPeriod.isEmpty()) {
                // This shouldn't happen if the graph is valid (no cycles)
                throw new IllegalStateException(
                    "No courses can be scheduled. Check for circular dependencies.");
            }

            // Sort courses in this period by name for consistent output
            currentPeriod.sort(Comparator.comparing(Course::getCourseCode));
            schedule.add(currentPeriod);
            completed.addAll(currentPeriod);
        }

        return schedule;
    }

    /**
     * Checks if all prerequisites for a given course have been completed.
     *
     * @param course the course to check
     * @param completed set of already completed courses
     * @return true if all prerequisites are completed, false otherwise
     */
    private boolean arePrerequisitesCompleted(Course course, Set<Course> completed) {
        for (Course prerequisite : course.getPrerequisites()) {
            if (!completed.contains(prerequisite)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the current schedule.
     *
     * @return the list of study periods with scheduled courses
     */
    public List<List<Course>> getSchedule() {
        return schedule;
    }

    /**
     * Gets the total number of study periods in the schedule.
     *
     * @return the number of study periods
     */
    public int getTotalPeriods() {
        return schedule.size();
    }

    /**
     * Gets the maximum number of courses that can be taken concurrently.
     *
     * @return the concurrency limit
     */
    public int getMaxConcurrentCourses() {
        return maxConcurrentCourses;
    }

    /**
     * Compares two DegreePlanners for equality based on graph and concurrency limit.
     *
     * @param obj the object to compare with
     * @return true if both planners have the same graph and concurrency limit, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DegreePlanner planner = (DegreePlanner) obj;
        return maxConcurrentCourses == planner.maxConcurrentCourses &&
               Objects.equals(graph, planner.graph);
    }

    /**
     * Generates a hash code for this DegreePlanner.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(graph, maxConcurrentCourses);
    }

    /**
     * Returns a string representation of the DegreePlanner.
     *
     * @return a string showing the schedule details
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DegreePlanner{\n");
        sb.append("  Total Periods: ").append(getTotalPeriods()).append("\n");
        sb.append("  Max Concurrent: ").append(maxConcurrentCourses).append("\n");
        sb.append("  Total Courses: ").append(graph.getSize()).append("\n");
        for (int i = 0; i < schedule.size(); i++) {
            sb.append("  Period ").append(i + 1).append(": ").append(schedule.get(i)).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
