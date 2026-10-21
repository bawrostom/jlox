import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TestHarness {

    private static String cmd = "";
    private static boolean DEBUG;
    private static final String TEST_FOLDER = "jlox/src/test/resources/test/";
    private static final String REPORT_FOLDER = "jlox/build/reports/tests";
    private static final String REPORT_NAME = "Testsuite-rport.txt";
    private static final String RUNNER_SCRIPT = "jlox/src/test/java/run.sh";
    private static final String EXCLUDE_LIST = "exclude.list";
    private static final String INCLUDE_LIST = "include.list";
    private static final String LOX_EXTENSION = ".lox";
    private final static String[] regex = {"^/\\s*expect:\\s*(.*)$", "^/\\s*expect runtime error:\\s*(.*)$", "^/\\s*(?:\\[line \\d+\\]\\s*)?(Error at '.*?': [^\\r\\n]*)$"};
    static StringBuilder report = new StringBuilder();
    private final List<Result> results = new ArrayList<>();
    private int testsCounter = 0;

    private String code;
    private int currentPos = 0;


    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            cmd = args[0];
            DEBUG = Objects.equals(args[0], "debug");
        }
        TestHarness testHarness = new TestHarness();
        testHarness.runTests();
        testHarness.testsSummary();
        writeTestsuiteReport(report.toString());
    }

    public void runTests() {
        List<PathMatcher> excludeMatchers = getMatchers(EXCLUDE_LIST);
        List<PathMatcher> includeMatchers = getMatchers(INCLUDE_LIST);

        try (Stream<Path> paths = Files.walk(new File(TEST_FOLDER).toPath())) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            excludeMatchers.stream().noneMatch(matcher -> matcher.matches(path))
                                    && includeMatchers.stream().anyMatch(matcher -> matcher.matches(path))
                                    && path.toString().endsWith(LOX_EXTENSION))
                    .forEach(this::runOneTest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runOneTest(Path path) {
        Result testResult;
        testsCounter++;
        try {
            this.code = Files.readString(path);
            this.currentPos = 0;

            StringBuilder sb = new StringBuilder();
            while (!end()) sb.append(scan());

            String expected = sb.toString();

            Process process = new ProcessBuilder(RUNNER_SCRIPT, cmd, path.toAbsolutePath().toString())
                    .start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            if (DEBUG) {
                System.out.println(output);
            }

            testResult = new Result(path.toAbsolutePath().toString(), expected, output);

        } catch (IOException e) {
            testResult = new Result(path.toAbsolutePath().toString(), null, e.toString());
        }
        String result = testResult.recordResult();
        System.out.println(result);
        report.append(result);
        results.add(testResult);
    }

    public ArrayList<String> getPattern(String patternListFile) {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(patternListFile)) {

            if (input == null) {
                throw new FileNotFoundException(patternListFile);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return new ArrayList<>(reader.lines().toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<PathMatcher> getMatchers(String listFile) {
        return getPattern(listFile).stream().map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern)).toList();
    }

    public void testsSummary() {
        int pass = 0;
        for (Result result : results) {
            String res = result.getResult();
            System.out.println(result.test + " " + res);
            pass += res.equals(Result.PASS) ? 1 : 0;
        }
        System.out.println("PASSED: " + pass + "/" + testsCounter + " FAILED: " + (testsCounter - pass) + "/" + testsCounter);
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
        Path outputDir = Path.of(REPORT_FOLDER);
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve(REPORT_NAME);

        Files.writeString(file, content);
    }

    record Result(String test, String expected, String actual) {
        public final static String PASS = "PASS";
        public final static String FAIL = "FAIL";

        public String getResult() {
            return actual.contains(expected) ? PASS : FAIL;
        }

        public String recordResult() {
            String result = this.getResult();
            String report = test + " " + result + '\n';
            report += "Expected: \n" + this.expected;
            report += "Got: \n" + this.actual;
            return report + "==============================================================";
        }
    }
}
