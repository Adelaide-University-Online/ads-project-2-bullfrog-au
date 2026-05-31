import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class FileParserTest {

    private Path writeTempFile(String content) throws IOException {
        Path file = Files.createTempFile("degree-parser-test", ".txt");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    public void parseFileBuildsGraphForValidInput() throws IOException {
        Path file = writeTempFile("COMP1000,COMP2000,COMP3000\nCOMP2000,COMP1000\nCOMP3000,COMP2000\n");

        Graph graph = FileParser.parseFile(file.toString());

        Assert.assertEquals(3, graph.getSize());
        Assert.assertTrue(graph.containsCourse("COMP1000"));
        Assert.assertEquals(1, graph.getCourse("COMP2000").getPrerequisites().size());
        Assert.assertEquals("COMP1000", graph.getCourse("COMP2000").getPrerequisites().get(0).getCourseCode());
    }

    @Test
    public void parseFileRejectsEmptyFile() throws IOException {
        Path file = writeTempFile("\n\n");

        try {
            FileParser.parseFile(file.toString());
            Assert.fail("Expected IllegalArgumentException for empty file");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("File is empty"));
        }
    }

    @Test
    public void parseFileRejectsCourseNotInHeader() throws IOException {
        Path file = writeTempFile("COMP1000,COMP2000\nCOMP3000,COMP1000\n");

        try {
            FileParser.parseFile(file.toString());
            Assert.fail("Expected IllegalArgumentException for unknown course");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("not found in the course list on line 1"));
        }
    }

    @Test
    public void parseFileRejectsPrerequisiteNotInHeader() throws IOException {
        Path file = writeTempFile("COMP1000,COMP2000\nCOMP2000,COMP3000\n");

        try {
            FileParser.parseFile(file.toString());
            Assert.fail("Expected IllegalArgumentException for unknown prerequisite");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("not found in the course list"));
        }
    }

    @Test
    public void validateGraphPassesForValidAcyclicGraph() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP3000", "COMP2000");

        FileParser.validateGraph(graph);

        Assert.assertFalse(graph.hasCycle());
    }

    @Test
    public void validateGraphRejectsCycle() {
        Graph graph = new Graph();
        graph.addPrerequisite("COMP2000", "COMP1000");
        graph.addPrerequisite("COMP1000", "COMP2000");

        try {
            FileParser.validateGraph(graph);
            Assert.fail("Expected IllegalArgumentException for cycle");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Circular prerequisite dependency"));
        }
    }

    @Test
    public void equalsHashCodeAndToStringBehaveAsExpected() {
        FileParser first = new FileParser();
        FileParser second = new FileParser();

        Assert.assertTrue(first.equals(second));
        Assert.assertTrue(first.equals(first));
        Assert.assertFalse(first.equals("not a parser"));
        Assert.assertEquals(first.hashCode(), second.hashCode());
        Assert.assertEquals("FileParser{}", first.toString());
    }
}
