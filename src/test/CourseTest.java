import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CourseTest {

    // Verifies the constructor preserves the course code and starts with no prerequisites.
    @Test
    public void constructorInitializesCodeAndEmptyPrerequisites() {
        Course course = new Course("COMP1000");

        Assert.assertEquals("COMP1000", course.getCourseCode());
        Assert.assertTrue(course.getPrerequisites().isEmpty());
        Assert.assertTrue(course.hasNoPrerequisites());
    }

    // Uses a single prerequisite fixture to confirm duplicates are ignored.
    @Test
    public void addPrerequisiteAddsOnlyOnce() {
        Course course = new Course("COMP2000");
        Course prerequisite = new Course("COMP1000");

        course.addPrerequisite(prerequisite);
        course.addPrerequisite(prerequisite);

        List<Course> prerequisites = course.getPrerequisites();
        Assert.assertEquals(1, prerequisites.size());
        Assert.assertEquals(prerequisite, prerequisites.get(0));
    }

    // Confirms prerequisite state flips once at least one dependency is added.
    @Test
    public void hasNoPrerequisitesIsFalseAfterAddingPrerequisite() {
        Course course = new Course("COMP2000");
        course.addPrerequisite(new Course("COMP1000"));

        Assert.assertFalse(course.hasNoPrerequisites());
    }

    // Exercises equals for self-comparison, null, and unrelated object types.
    @Test
    public void equalsHandlesReferenceNullAndDifferentType() {
        Course course = new Course("COMP1000");

        Assert.assertTrue(course.equals(course));
        Assert.assertFalse(course.equals(null));
        Assert.assertFalse(course.equals("COMP1000"));
    }

    // Confirms equal course codes produce both equality and matching hash codes.
    @Test
    public void equalsAndHashCodeMatchForSameCourseCode() {
        Course first = new Course("COMP1000");
        Course second = new Course("COMP1000");

        Assert.assertTrue(first.equals(second));
        Assert.assertEquals(first.hashCode(), second.hashCode());
    }

    // Verifies different course codes remain distinct.
    @Test
    public void equalsIsFalseForDifferentCourseCode() {
        Course first = new Course("COMP1000");
        Course second = new Course("COMP2000");

        Assert.assertFalse(first.equals(second));
    }

    // Confirms the display form is just the course code.
    @Test
    public void toStringReturnsCourseCode() {
        Course course = new Course("COMP3000");

        Assert.assertEquals("COMP3000", course.toString());
    }
}
