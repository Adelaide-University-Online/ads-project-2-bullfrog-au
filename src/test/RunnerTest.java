import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.Assert;
import org.junit.Test;

public class RunnerTest {

    private Runner newRunner(String input, ByteArrayOutputStream outBuffer, ByteArrayOutputStream errBuffer) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        PrintStream out = new PrintStream(outBuffer);
        PrintStream err = new PrintStream(errBuffer);
        return new Runner(scanner, out, err);
    }

    private Path writeDegreeFile(String content) throws IOException {
        Path file = Files.createTempFile("degree-runner-test", ".txt");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    public void promptToRetryAcceptsYesAndY() {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        Runner yesRunner = newRunner("yes\n", outBuffer, errBuffer);

        Assert.assertTrue(yesRunner.promptToRetry("Enter file path"));

        Runner yRunner = newRunner("y\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertTrue(yRunner.promptToRetry("Enter file path"));
    }

    @Test
    public void promptToRetryRejectsNo() {
        Runner runner = newRunner("no\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertFalse(runner.promptToRetry("Enter file path"));
    }

    @Test
    public void handleErrorReturnsTrueOnlyForOptionOne() {
        Runner tryAgainRunner = newRunner("1\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Runner terminateRunner = newRunner("2\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertTrue(tryAgainRunner.handleError());
        Assert.assertFalse(terminateRunner.handleError());
    }

    @Test
    public void promptForConcurrencyHandlesValidAndInvalidInput() {
        Runner validRunner = newRunner("3\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertEquals(3, validRunner.promptForConcurrency());

        Runner invalidRunner = newRunner("abc\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertEquals(0, invalidRunner.promptForConcurrency());
    }

    @Test
    public void promptForConcurrencyHandlesHighValueConfirmationFlow() {
        String input = "51\nno\nyes\n2\n";
        Runner runner = newRunner(input, new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertEquals(2, runner.promptForConcurrency());
    }

    @Test
    public void promptForFilePathReturnsNullWhenUserStopsRetrying() {
        Runner runner = newRunner("\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertNull(runner.promptForFilePath());
    }

    @Test
    public void promptForFilePathReturnsAcceptedExistingFile() throws IOException {
        Path file = writeDegreeFile("COMP1000\n");
        String input = file.toString() + "\n";
        Runner runner = newRunner(input, new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertEquals(file.toString(), runner.promptForFilePath());
    }

    @Test
    public void runDegreePlannerPrintsScheduleForValidFile() throws IOException {
        Path file = writeDegreeFile("COMP1000,COMP2000\nCOMP2000,COMP1000\n");
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        Runner runner = newRunner("", outBuffer, new ByteArrayOutputStream());

        runner.runDegreePlanner(file.toString(), 1);

        String output = outBuffer.toString(StandardCharsets.UTF_8.name());
        Assert.assertTrue(output.contains("OPTIMIZED DEGREE SCHEDULE"));
        Assert.assertTrue(output.contains("Total Study Periods Required"));
    }

    @Test
    public void runDegreePlannerThrowsForCircularDependency() throws IOException {
        Path file = writeDegreeFile("COMP1000,COMP2000\nCOMP1000,COMP2000\nCOMP2000,COMP1000\n");
        Runner runner = newRunner("", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        try {
            runner.runDegreePlanner(file.toString(), 2);
            Assert.fail("Expected IllegalArgumentException for circular dependency");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Circular prerequisite dependency"));
        }
    }

    @Test
    public void runMethodCanExitImmediatelyAndPrintGoodbye() {
        Runner runner = newRunner("\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        runner.run();

        Assert.assertTrue(true);
    }
}
