import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TestHarness {

    private static final String TEST_FOLDER = "jlox/src/test/resources/test/";
    private static final String LOX_EXTENSION = ".lox";
    private static final String RUNTIME_ERROR = "expect runtime error: ";
    private static final String SYNTAX_ERROR = "Error at '";
    private static final String EXPECT = "expect: ";
    private static final String START = "// ";
    String regexExpect = "^//\\s*expect:\\s*(.*)$";


    private String code;
    private int currentPos = 0;

    public static void main(String[] arg) {
        new TestHarness().runTest();
    }

    public void runTest() {

        try (Stream<Path> paths = Files.walk(new File(TEST_FOLDER).toPath())) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(LOX_EXTENSION))
                    .forEach(this::runTest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void runTest(Path path) {
        try {
            this.code = Files.readString(path);
            this.currentPos = 0;
            StringBuilder sb = new StringBuilder();
            while (!end()) {
                String s = scan();
                if (!s.isEmpty()) sb.append(s).append('\n');
                advance();
            }
            String expected = sb.toString();

            Process process = new ProcessBuilder("jlox/src/test/java/run.sh", path.toAbsolutePath().toString())
                    .start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            check(output, expected, path.toAbsolutePath().toString());

        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    private void check(String actual, String expected, String testFile) {
        if (!actual.contains(expected)) {
            System.out.println("FAIL: " + testFile);
            System.out.println("Expected:\n" + expected);
            System.out.println("Actual:\n" + actual);
            return;
        }
        System.out.println("PASS: " + testFile);
    }

    private void advance() {
        if (end()) return;
        currentPos++;
    }

    private boolean end() {
        return code.length() == currentPos;
    }

    private boolean check(char c) {
        return !end() && (code.charAt(currentPos) == c);
    }

    private String consume() {
        StringBuilder sb = new StringBuilder();
        while (!end() && code.charAt(currentPos) != '\n') {
            sb.append(code.charAt(currentPos));
            advance();
        }
        return sb.toString();
    }

    private String found(String s, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String scan() {
        char c = code.charAt(currentPos);
        if (c == '/' && check('/')) {
            String expectation = consume();
            if (expectation.contains(SYNTAX_ERROR)) {
//                return found(expectation, regexSyntax);
                return expectation.replaceFirst("^// ", "");
            } else if (expectation.contains(RUNTIME_ERROR)) {
//                return found(expectation, regexRuntime);
                return expectation.replaceFirst("^// ", "");
            } else if (expectation.contains(EXPECT)) {
                return found(expectation, regexExpect);
            }
        }
        return "";
    }

    class TestOutput {
        private final String compileError;
        private final String runtimeError;
        private final String stderr;
        private final String stdout;

        public TestOutput(String stdout, String stderr, String runtimeError, String compileError) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.runtimeError = runtimeError;
            this.compileError = compileError;
        }
    }
}
