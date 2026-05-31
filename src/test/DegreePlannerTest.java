import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class DegreePlannerTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsInvalidConcurrency() {
        Graph graph = new Graph();

        new DegreePlanner(graph, 0);
    }

    @Test
    public void planDegreeSchedulesAllCoursesRespectingPrerequisites() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP3000", "COMP2000");

        DegreePlanner planner = new DegreePlanner(graph, 2);
        List<List<Course>> schedule = planner.planDegree();

        Assert.assertEquals(3, planner.getTotalPeriods());
        Assert.assertEquals(3, schedule.size());
        Assert.assertEquals("COMP1000", schedule.get(0).get(0).getCourseCode());
        Assert.assertEquals("COMP2000", schedule.get(1).get(0).getCourseCode());
        Assert.assertEquals("COMP3000", schedule.get(2).get(0).getCourseCode());
    }

    @Test
    public void planDegreeRespectsMaxConcurrencyAndCompletesAllCourses() {
        Graph graph = new Graph();
        graph.addCourse("COMP1000");
        graph.addCourse("COMP2000");
        graph.addCourse("COMP3000");
        graph.addCourse("COMP4000");

        DegreePlanner planner = new DegreePlanner(graph, 2);
        List<List<Course>> schedule = planner.planDegree();

        Set<String> scheduled = new HashSet<>();
        for (List<Course> period : schedule) {
            Assert.assertTrue(period.size() <= 2);
            for (Course course : period) {
                scheduled.add(course.getCourseCode());
            }
        }

        Assert.assertEquals(4, scheduled.size());
        Assert.assertEquals(2, planner.getMaxConcurrentCourses());
    }

    @Test
    public void planDegreeThrowsWhenNoCourseCanBeScheduled() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP1000", "COMP2000");

        DegreePlanner planner = new DegreePlanner(graph, 2);

        try {
            planner.planDegree();
            Assert.fail("Expected IllegalStateException when scheduling cannot progress");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("No courses can be scheduled"));
        }
    }

    @Test
    public void getScheduleReturnsEmptyBeforePlanning() {
        Graph graph = new Graph();
        graph.addCourse("COMP1000");

        DegreePlanner planner = new DegreePlanner(graph, 1);

        Assert.assertTrue(planner.getSchedule().isEmpty());
        Assert.assertEquals(0, planner.getTotalPeriods());
    }

    @Test
    public void equalsHashCodeAndToStringBehaveAsExpected() {
        Graph firstGraph = new Graph();
        firstGraph.addPrerequisite("COMP2000", "COMP1000");

        Graph secondGraph = new Graph();
        secondGraph.addPrerequisite("COMP2000", "COMP1000");

        DegreePlanner first = new DegreePlanner(firstGraph, 2);
        DegreePlanner second = new DegreePlanner(secondGraph, 2);
        DegreePlanner differentConcurrency = new DegreePlanner(secondGraph, 1);

        Assert.assertTrue(first.equals(second));
        Assert.assertEquals(first.hashCode(), second.hashCode());
        Assert.assertFalse(first.equals(differentConcurrency));
        Assert.assertFalse(first.equals("not planner"));

        first.planDegree();
        String text = first.toString();
        Assert.assertTrue(text.contains("DegreePlanner{"));
        Assert.assertTrue(text.contains("Total Periods"));
    }
}
