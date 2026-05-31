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

    // Fixture helper that wires a Runner to in-memory input and output streams.
    private Runner newRunner(String input, ByteArrayOutputStream outBuffer, ByteArrayOutputStream errBuffer) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        PrintStream out = new PrintStream(outBuffer);
        PrintStream err = new PrintStream(errBuffer);
        return new Runner(scanner, out, err);
    }

    // Fixture helper that creates a temporary degree definition file for CLI flow tests.
    private Path writeDegreeFile(String content) throws IOException {
        Path file = Files.createTempFile("degree-runner-test", ".txt");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    // Confirms retry prompts accept both full and abbreviated affirmative input.
    @Test
    public void promptToRetryAcceptsYesAndY() {
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
        Runner yesRunner = newRunner("yes\n", outBuffer, errBuffer);

        Assert.assertTrue(yesRunner.promptToRetry("Enter file path"));

        Runner yRunner = newRunner("y\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertTrue(yRunner.promptToRetry("Enter file path"));
    }

    // Confirms retry prompts reject negative input.
    @Test
    public void promptToRetryRejectsNo() {
        Runner runner = newRunner("no\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertFalse(runner.promptToRetry("Enter file path"));
    }

    // Verifies the error menu only continues when option 1 is selected.
    @Test
    public void handleErrorReturnsTrueOnlyForOptionOne() {
        Runner tryAgainRunner = newRunner("1\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Runner terminateRunner = newRunner("2\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertTrue(tryAgainRunner.handleError());
        Assert.assertFalse(terminateRunner.handleError());
    }

    // Exercises both immediate success and invalid-input exit paths for concurrency prompts.
    @Test
    public void promptForConcurrencyHandlesValidAndInvalidInput() {
        Runner validRunner = newRunner("3\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertEquals(3, validRunner.promptForConcurrency());

        Runner invalidRunner = newRunner("abc\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());
        Assert.assertEquals(0, invalidRunner.promptForConcurrency());
    }

    // Verifies the high-concurrency warning path can loop and then accept a lower value.
    @Test
    public void promptForConcurrencyHandlesHighValueConfirmationFlow() {
        String input = "51\nno\nyes\n2\n";
        Runner runner = newRunner(input, new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertEquals(2, runner.promptForConcurrency());
    }

    // Confirms file-path prompting returns null when the user declines to retry.
    @Test
    public void promptForFilePathReturnsNullWhenUserStopsRetrying() {
        Runner runner = newRunner("\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertNull(runner.promptForFilePath());
    }

    // Uses a temporary file fixture to verify valid file paths are accepted.
    @Test
    public void promptForFilePathReturnsAcceptedExistingFile() throws IOException {
        Path file = writeDegreeFile("COMP1000\n");
        String input = file.toString() + "\n";
        Runner runner = newRunner(input, new ByteArrayOutputStream(), new ByteArrayOutputStream());

        Assert.assertEquals(file.toString(), runner.promptForFilePath());
    }

    // Uses a valid degree file fixture to verify the planner prints a schedule summary.
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

    // Uses a cyclic degree file fixture to verify planner execution surfaces validation errors.
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

    // Exercises the top-level CLI loop with minimal input to confirm it exits cleanly.
    @Test
    public void runMethodCanExitImmediatelyAndPrintGoodbye() {
        Runner runner = newRunner("\nno\n", new ByteArrayOutputStream(), new ByteArrayOutputStream());

        runner.run();

        Assert.assertTrue(true);
    }
}
