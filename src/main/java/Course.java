/**
 * File: Course.java
 * Description: Represents a single course in the degree program with its prerequisites.
 * Author: Tom Jeremiah
 * Student ID: a2970593@adelaide.edu.au
 * Email ID: tom.jeremiah@student.adelaide.edu.au
 * AI Tool Used: 
 * This is my own work as defined by
 *    the University's Academic Integrity Policy.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a course in the degree planning system.
 * Each course has a unique course code and a list of prerequisite courses.
 */
public class Course {
    private String courseCode;
    private List<Course> prerequisites;

    /**
     * Constructs a Course with the specified course code.
     *
     * @param courseCode the unique identifier for the course (e.g., "COMP1043")
     */
    public Course(String courseCode) {
        this.courseCode = courseCode;
        this.prerequisites = new ArrayList<>();
    }

    /**
     * Gets the course code.
     *
     * @return the course code
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Gets the list of prerequisite courses.
     *
     * @return list of prerequisite courses
     */
    public List<Course> getPrerequisites() {
        return prerequisites;
    }

    /**
     * Adds a prerequisite course to this course.
     *
     * @param prerequisite the course that must be taken before this course
     */
    public void addPrerequisite(Course prerequisite) {
        if (!prerequisites.contains(prerequisite)) {
            prerequisites.add(prerequisite);
        }
    }

    /**
     * Checks if this course has no prerequisites.
     *
     * @return true if this course has no prerequisites, false otherwise
     */
    public boolean hasNoPrerequisites() {
        return prerequisites.isEmpty();
    }

    /**
     * Compares two courses based on their course code.
     *
     * @param obj the object to compare with
     * @return true if both courses have the same course code, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Course course = (Course) obj;
        return Objects.equals(courseCode, course.courseCode);
    }

    /**
     * Generates a hash code for this course based on its course code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    /**
     * Returns a string representation of the course.
     *
     * @return the course code as a string
     */
    @Override
    public String toString() {
        return courseCode;
    }
}
