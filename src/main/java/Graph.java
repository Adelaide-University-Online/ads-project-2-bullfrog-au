/**
 * File: Graph.java
 * Description: Represents the course network as a directed acyclic graph using adjacency list.
 * Author: Tom Jeremiah
 * Student ID: a2970593@adelaide.edu.au
 * Email ID: tom.jeremiah@student.adelaide.edu.au
 * AI Tool Used: 
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 */

import java.util.*;

/**
 * Represents a directed graph of courses and their prerequisite relationships.
 * Uses an adjacency list representation where each course maps to its direct prerequisites.
 */
public class Graph {
    private Map<String, Course> courses;

    /**
     * Constructs an empty graph.
     */
    public Graph() {
        this.courses = new HashMap<>();
    }

    /**
     * Adds a course to the graph. If the course already exists, does nothing.
     *
     * @param courseCode the code of the course to add
     */
    public void addCourse(String courseCode) {
        if (!courses.containsKey(courseCode)) {
            courses.put(courseCode, new Course(courseCode));
        }
    }

    /**
     * Adds a prerequisite relationship between two courses.
     * Both courses are added to the graph if they don't exist.
     *
     * @param courseCode the course code
     * @param prerequisiteCode the code of the prerequisite course
     * @throws IllegalArgumentException if either course code is null or empty
     */
    public void addPrerequisite(String courseCode, String prerequisiteCode) {
        if (courseCode == null || courseCode.isEmpty() || 
            prerequisiteCode == null || prerequisiteCode.isEmpty()) {
            throw new IllegalArgumentException("Course codes cannot be null or empty");
        }

        addCourse(courseCode);
        addCourse(prerequisiteCode);

        Course course = courses.get(courseCode);
        Course prerequisite = courses.get(prerequisiteCode);
        course.addPrerequisite(prerequisite);
    }

    /**
     * Gets a course by its course code.
     *
     * @param courseCode the course code
     * @return the Course object, or null if not found
     */
    public Course getCourse(String courseCode) {
        return courses.get(courseCode);
    }

    /**
     * Gets all courses in the graph.
     *
     * @return a collection of all courses
     */
    public Collection<Course> getAllCourses() {
        return courses.values();
    }

    /**
     * Gets the number of courses in the graph.
     *
     * @return the number of courses
     */
    public int getSize() {
        return courses.size();
    }

    /**
     * Checks if a course exists in the graph.
     *
     * @param courseCode the course code to check
     * @return true if the course exists, false otherwise
     */
    public boolean containsCourse(String courseCode) {
        return courses.containsKey(courseCode);
    }

    /**
     * Detects if there is a cycle in the graph using depth-first search.
     * A cycle indicates invalid prerequisite structure.
     *
     * @return true if a cycle is detected, false otherwise
     */
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String courseCode : courses.keySet()) {
            if (!visited.contains(courseCode)) {
                if (hasCycleDFS(courseCode, visited, recursionStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method for cycle detection using DFS.
     *
     * @param courseCode the current course code being examined
     * @param visited set of already visited courses
     * @param recursionStack set of courses in the current recursion path
     * @return true if a cycle is found, false otherwise
     */
    private boolean hasCycleDFS(String courseCode, Set<String> visited, Set<String> recursionStack) {
        visited.add(courseCode);
        recursionStack.add(courseCode);

        Course course = courses.get(courseCode);
        for (Course prereq : course.getPrerequisites()) {
            if (!visited.contains(prereq.getCourseCode())) {
                if (hasCycleDFS(prereq.getCourseCode(), visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(prereq.getCourseCode())) {
                return true;
            }
        }

        recursionStack.remove(courseCode);
        return false;
    }

    /**
     * Compares two graphs for equality based on their courses and prerequisite structure.
     *
     * @param obj the object to compare with
     * @return true if both graphs have the same courses and prerequisites, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Graph graph = (Graph) obj;
        return Objects.equals(courses, graph.courses);
    }

    /**
     * Generates a hash code for this graph based on its courses.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(courses);
    }

    /**
     * Returns a string representation of the graph.
     *
     * @return a string showing all courses and their prerequisites
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph{\n");
        for (Course course : courses.values()) {
            sb.append("  ").append(course.getCourseCode()).append(" -> ");
            sb.append(course.getPrerequisites()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
