import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class GraphTest {

    // Verifies the graph does not duplicate a course when added twice.
    @Test
    public void addCourseAddsNewAndIgnoresDuplicate() {
        Graph graph = new Graph();

        graph.addCourse("COMP1000");
        graph.addCourse("COMP1000");

        Assert.assertEquals(1, graph.getSize());
        Assert.assertTrue(graph.containsCourse("COMP1000"));
    }

    // Confirms null course codes are rejected before the graph is mutated.
    @Test(expected = IllegalArgumentException.class)
    public void addPrerequisiteRejectsNullCourseCode() {
        Graph graph = new Graph();

        graph.addPrerequisite(null, "COMP1000");
    }

    // Confirms empty prerequisite codes are rejected as invalid input.
    @Test(expected = IllegalArgumentException.class)
    public void addPrerequisiteRejectsEmptyPrerequisiteCode() {
        Graph graph = new Graph();

        graph.addPrerequisite("COMP2000", "");
    }

    // Uses a simple dependency fixture to verify missing courses are created automatically.
    @Test
    public void addPrerequisiteAutoCreatesCoursesAndLinksThem() {
        Graph graph = new Graph();

        graph.addPrerequisite("COMP2000", "COMP1000");

        Assert.assertEquals(2, graph.getSize());
        Course course = graph.getCourse("COMP2000");
        Assert.assertNotNull(course);
        Assert.assertEquals(1, course.getPrerequisites().size());
        Assert.assertEquals("COMP1000", course.getPrerequisites().get(0).getCourseCode());
    }

    // Verifies lookups return null when the course code has never been added.
    @Test
    public void getCourseReturnsNullForMissingCourse() {
        Graph graph = new Graph();

        Assert.assertNull(graph.getCourse("MISSING"));
    }

    // Confirms the graph exposes every inserted course through the collection view.
    @Test
    public void getAllCoursesReturnsAllInsertedCourses() {
        Graph graph = new Graph();
        graph.addCourse("COMP1000");
        graph.addCourse("COMP2000");

        Collection<Course> courses = graph.getAllCourses();
        List<String> codes = new ArrayList<>();
        for (Course course : courses) {
            codes.add(course.getCourseCode());
        }

        Assert.assertEquals(2, courses.size());
        Assert.assertTrue(codes.contains("COMP1000"));
        Assert.assertTrue(codes.contains("COMP2000"));
    }

    // Uses an acyclic prerequisite chain fixture to verify cycle detection stays false.
    @Test
    public void hasCycleReturnsFalseForAcyclicGraph() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP3000", "COMP2000");

        Assert.assertFalse(graph.hasCycle());
    }

    // Uses a two-node cycle fixture to verify cycle detection reports true.
    @Test
    public void hasCycleReturnsTrueForCyclicGraph() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP1000", "COMP2000");

        Assert.assertTrue(graph.hasCycle());
    }

    // Confirms structural equality and hash code behavior for equivalent graphs.
    @Test
    public void equalsAndHashCodeMatchForEquivalentGraphs() {
        Graph first = new Graph();
        first.addPrerequisite("COMP2000", "COMP1000");

        Graph second = new Graph();
        second.addPrerequisite("COMP2000", "COMP1000");

        Assert.assertTrue(first.equals(second));
        Assert.assertEquals(first.hashCode(), second.hashCode());
    }

    // Verifies the string form includes the header and the linked course codes.
    @Test
    public void toStringIncludesGraphHeaderAndCourseCodes() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");

        String text = graph.toString();

        Assert.assertTrue(text.contains("Graph{"));
        Assert.assertTrue(text.contains("COMP2000"));
        Assert.assertTrue(text.contains("COMP1000"));
    }
}
