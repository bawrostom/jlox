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
    private final static String[] regex = {"^/\\s*expect:\\s*(.*)$", "^/\\s*expect runtime error:\\s*(.*?)(?:\\.)?$", "^//\\s*(Error at '.*?': .*)$"};
    static StringBuilder report = new StringBuilder();


    private String code;
    private int currentPos = 0;

    public static void main(String[] args) throws IOException {
        new TestHarness().runTests();
        writeTestsuiteReport(report.toString());
    }

    public void runTests() {
        try (Stream<Path> paths = Files.walk(new File(TEST_FOLDER).toPath())) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(LOX_EXTENSION))
                    .forEach(this::runOneTest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runOneTest(Path path) {
        Result testResult;
        try {
            this.code = Files.readString(path);
            this.currentPos = 0;

            StringBuilder sb = new StringBuilder();
            while (!end()) sb.append(scan());

            String expected = sb.toString();

            Process process = new ProcessBuilder("jlox/src/test/java/run.sh", path.toAbsolutePath().toString())
                    .start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            testResult = new Result(path.toAbsolutePath().toString(), expected, output);

        } catch (IOException e) {
            testResult = new Result(path.toAbsolutePath().toString(), null, e.toString());
        }
        String result = testResult.recordResult();
        System.out.println(result);
        report.append(result);
    }

    private String scan() {
        char c = advance();
        if (c == '/' && check('/')) {
            return find(consume());
        }
        return "";
    }

    public static String find(String s) {
        StringBuilder match = new StringBuilder();
        for (String regex : regex) {
            Matcher matcher = Pattern.compile(regex).matcher(s);
            if (matcher.find()) {
                match.append(matcher.group(1)).append('\n');
            }
        }
        // here none of the expected output is found
        return match.toString();
    }

    private char advance() {
        return code.charAt(currentPos++);
    }

    private boolean end() {
        return code.length() == currentPos;
    }

    private boolean check(char c) {
        return !end() && (code.charAt(currentPos) == c);
    }

    private String consume() {
        int start = currentPos;
        while (!end() && !check('\n')) advance();
        return code.substring(start, currentPos);
    }

    static void writeTestsuiteReport(String content) throws IOException {
        Path outputDir = Path.of("output~");
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve("Testsuite-rport.txt");

        Files.writeString(file, content);
    }

    record Result(String test, String expected, String actual) {
        public final static String PASS = "PASS";
        public final static String FAIL = "FAIL";

        public String getResult() {
            return expected.equals(actual) ? PASS : FAIL;
        }

        public String recordResult() {
            String result = this.getResult();
            String report = test + " " + result + '\n';
            if (result.equals(FAIL)) {
                report += "Expected: \n" + this.expected;
                report += "Got: \n" + this.actual;
            }
            return report + "==============================================================";
        }
    }
}
