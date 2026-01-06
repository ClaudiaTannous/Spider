package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SysData {

    private static SysData instance;

    public static synchronized SysData getInstance() {
        if (instance == null) {
            instance = new SysData();
        }
        return instance;
    }


    // ------------------ FILE NAMES ------------------ //

    private static final String QUESTIONS_CSV_FILE_NAME = "EX_Question Template.csv";
    private static final String HISTORY_CSV_FILE_NAME   = "history.csv";

   
    private static final Path QUESTIONS_CSV_PATH;
    private static final Path HISTORY_CSV_PATH;

    static {
        Path questionsPath;
        Path historyPath;

        try {
            // Try to resolve the folder where the JAR is located
            var codeSource = SysData.class.getProtectionDomain().getCodeSource();
            File jarFile = (codeSource != null) ? new File(codeSource.getLocation().toURI()) : null;

            File baseDir;
            if (jarFile != null && jarFile.isFile()) {
                // Running from a JAR: use its parent folder
                baseDir = jarFile.getParentFile();
            } else if (jarFile != null && jarFile.isDirectory()) {
                // Running from classes folder (e.g. IDE): use that folder
                baseDir = jarFile;
            } else {
                // Fallback: use current working directory
                baseDir = new File(System.getProperty("user.dir"));
            }

            questionsPath = baseDir.toPath().resolve(QUESTIONS_CSV_FILE_NAME);
            historyPath   = baseDir.toPath().resolve(HISTORY_CSV_FILE_NAME);

        } catch (Exception ex) {
            ex.printStackTrace();
            // Hard fallback if something goes wrong with URI/JAR detection
            String userDir = System.getProperty("user.dir");
            questionsPath = Path.of(userDir, QUESTIONS_CSV_FILE_NAME);
            historyPath   = Path.of(userDir, HISTORY_CSV_FILE_NAME);
        }

        QUESTIONS_CSV_PATH = questionsPath;
        HISTORY_CSV_PATH   = historyPath;

        System.out.println("Using questions CSV path: " + QUESTIONS_CSV_PATH.toAbsolutePath());
        System.out.println("Using history CSV path:   " + HISTORY_CSV_PATH.toAbsolutePath());
    }

    // ------------------ HISTORY CSV HEADER ------------------ //

    private static final List<String> HISTORY_HEADER = List.of(
            "timestamp",
            "difficulty",
            "player1", "player1Score",
            "player2", "player2Score",
            "result",
            "seconds"
    );

    // ------------------ QUESTIONS & WEIGHTS ------------------ //

    private final Map<String, Question> questions = new LinkedHashMap<>();
    private final Set<String> usedThisMatch = new HashSet<>();

    private final EnumMap<QuestionDifficulty, Integer> weights =
            new EnumMap<>(QuestionDifficulty.class);

    // ------------------ CONSTRUCTOR ------------------ //

    public SysData() {
        weights.put(QuestionDifficulty.EASY,   50);
        weights.put(QuestionDifficulty.MEDIUM, 35);
        weights.put(QuestionDifficulty.HARD,   12);
        weights.put(QuestionDifficulty.EXPERT, 3);

        loadQuestionsFromCsv();
        initHistoryCsv();
    }

    // ------------------ HISTORY INIT / APPEND ------------------ //

    private void initHistoryCsv() {
        try {
            if (HISTORY_CSV_PATH.getParent() != null) {
                Files.createDirectories(HISTORY_CSV_PATH.getParent());
            }

            if (Files.notExists(HISTORY_CSV_PATH) || Files.size(HISTORY_CSV_PATH) == 0L) {
                try (BufferedWriter w = Files.newBufferedWriter(
                        HISTORY_CSV_PATH,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )) {
                    w.write(String.join(",", HISTORY_HEADER));
                    w.newLine();
                }
            }

            System.out.println("History CSV ready at: " + HISTORY_CSV_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error initializing history CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void appendHistoryRow(Object... values) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(
                HISTORY_CSV_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sb.append(',');
                String s = (values[i] == null) ? "" : values[i].toString();
                sb.append(escapeCsvField(s));
            }
            w.write(sb.toString());
            w.newLine();
        }
    }

    public void logGameResult(Difficulty difficulty,
                              Player player1, int player1Score,
                              Player player2, int player2Score,
                              String result,
                              int seconds) {
        try {
            appendHistoryRow(
                    LocalDateTime.now(),
                    difficulty,
                    player1 != null ? player1.getName() : "",
                    player1Score,
                    player2 != null ? player2.getName() : "",
                    player2Score,
                    result,
                    seconds
            );
        } catch (IOException e) {
            System.err.println("Error writing game history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String[]> getGameHistory() {
        if (Files.notExists(HISTORY_CSV_PATH)) {
            return List.of();
        }

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(HISTORY_CSV_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = r.readLine()) != null) {
                // skip header
                if (first) {
                    first = false;
                    continue;
                }
                if (line.isBlank()) continue;

                List<String> cols = parseCsvLine(line);
                rows.add(cols.toArray(new String[0]));
            }
        } catch (IOException e) {
            System.err.println("Error reading game history: " + e.getMessage());
            e.printStackTrace();
        }
        return rows;
    }

    // ------------------ MATCH QUESTIONS STATE ------------------ //

    public void resetMatchUsage() {
        usedThisMatch.clear();
    }

    public void setWeights(Map<QuestionDifficulty, Integer> newWeights) {
        weights.clear();
        weights.putAll(newWeights);
    }

    // ------------------ LOAD / PARSE QUESTIONS CSV ------------------ //

    private void loadQuestionsFromCsv() {
        if (Files.notExists(QUESTIONS_CSV_PATH)) {
            System.err.println("Could not find questions CSV at: " + QUESTIONS_CSV_PATH.toAbsolutePath());
            return;
        }

        questions.clear();

        try (BufferedReader br = Files.newBufferedReader(QUESTIONS_CSV_PATH, StandardCharsets.UTF_8)) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;   // skip header
                    continue;
                }

                if (line.isBlank())
                    continue;

                List<String> fields = parseCsvLine(line);

                if (fields.size() < 8) {
                    System.err.println("Invalid line (expected >= 8 columns): " + line);
                    continue;
                }

                String questionText = fields.get(0).trim();
                String difficultyRaw = fields.get(1).trim();

                List<String> answers = List.of(
                        fields.get(2).trim(),
                        fields.get(3).trim(),
                        fields.get(4).trim(),
                        fields.get(5).trim()
                );

                String correctAnswerText = fields.get(6).trim();

                int correctIndex = -1;
                for (int i = 0; i < answers.size(); i++) {
                    if (answers.get(i).equalsIgnoreCase(correctAnswerText)) {
                        correctIndex = i;
                        break;
                    }
                }

                if (correctIndex == -1) {
                    System.err.println("Could not match correct answer for line: " + line);
                    continue;
                }

                QuestionDifficulty difficulty = parseDifficulty(difficultyRaw);

                String id = "Q" + String.format("%03d", questions.size() + 1);

                Question q = new Question(
                        id,
                        questionText,
                        answers,
                        correctIndex,
                        difficulty
                );

                upsertQuestion(q);
            }

            System.out.println("Successfully loaded " + questions.size() + " questions from CSV.");

        } catch (IOException e) {
            System.err.println("Error reading questions CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.isEmpty()) return result;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        result.add(sb.toString());
        return result;
    }

    private QuestionDifficulty parseDifficulty(String raw) {
        if (raw == null) return QuestionDifficulty.EASY;
        String v = raw.trim().toLowerCase(Locale.ROOT);

        return switch (v) {
            case "easy" -> QuestionDifficulty.EASY;
            case "medium", "meduim" -> QuestionDifficulty.MEDIUM; // handle typo
            case "hard" -> QuestionDifficulty.HARD;
            case "expert" -> QuestionDifficulty.EXPERT;
            default -> {
                System.err.println("Unknown difficulty '" + raw + "', defaulting to EASY");
                yield QuestionDifficulty.EASY;
            }
        };
    }

    // ------------------ SAVE QUESTIONS TO CSV ------------------ //

    public void saveQuestionsToCsv() {
        try (BufferedWriter w = Files.newBufferedWriter(
                QUESTIONS_CSV_PATH,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            // Header
            w.write("question,difficulty,answer1,answer2,answer3,answer4,correct_answer,author");
            w.newLine();

            for (Question q : questions.values()) {
                List<String> a = q.getAnswers();
                String answer1 = a.get(0);
                String answer2 = a.get(1);
                String answer3 = a.get(2);
                String answer4 = a.get(3);
                String correct = q.getCorrectAnswerText();

                String author = "";

                String line = String.join(",",
                        escapeCsvField(q.getText()),
                        escapeCsvField(q.getDifficulty().name().toLowerCase()),
                        escapeCsvField(answer1),
                        escapeCsvField(answer2),
                        escapeCsvField(answer3),
                        escapeCsvField(answer4),
                        escapeCsvField(correct),
                        escapeCsvField(author)
                );
                w.write(line);
                w.newLine();
            }

            System.out.println("Saved " + questions.size() + " questions to CSV at "
                    + QUESTIONS_CSV_PATH.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error writing questions CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeCsvField(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }

    // ------------------ QUESTION CRUD ------------------ //

    public void upsertQuestion(Question q) {
        questions.put(q.getId(), q);
    }

    public boolean deleteQuestionById(String id) {
        return questions.remove(id) != null;
    }

    public Optional<Question> findQuestionById(String id) {
        return Optional.ofNullable(questions.get(id));
    }

    public List<Question> findAllQuestions() {
        return List.copyOf(questions.values());
    }

    public List<Question> findQuestionsByDifficulty(QuestionDifficulty d) {
        return questions.values().stream()
                .filter(q -> q.getDifficulty() == d)
                .collect(Collectors.toList());
    }

    public boolean questionIdExists(String id) {
        return questions.containsKey(id);
    }

    // ------------------ QUESTION SELECTION ------------------ //

    private QuestionDifficulty pickDifficultyWeighted() {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return QuestionDifficulty.EASY;

        int r = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;

        for (var e : weights.entrySet()) {
            acc += e.getValue();
            if (r < acc) return e.getKey();
        }
        return QuestionDifficulty.EASY;
    }

    public Optional<Question> nextQuestion() {

        // If there are NO questions at all → really no questions
        if (questions.isEmpty()) {
            return Optional.empty();
        }

        QuestionDifficulty target = pickDifficultyWeighted();

        List<Question> candidates = findQuestionsByDifficulty(target).stream()
                .filter(q -> !usedThisMatch.contains(q.getId()))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            candidates = findAllQuestions().stream()
                    .filter(q -> !usedThisMatch.contains(q.getId()))
                    .collect(Collectors.toList());
        }

        // ⭐ NEW: if all questions were used → reset and reuse
        if (candidates.isEmpty()) {
            usedThisMatch.clear();
            candidates = findAllQuestions();
        }

        Question q = candidates.get(
                ThreadLocalRandom.current().nextInt(candidates.size())
        );

        usedThisMatch.add(q.getId());
        return Optional.of(q);
    }


    public String generateNewQuestionId() {
        int i = questions.size() + 1;
        String id;
        do {
            id = "Q" + String.format("%03d", i++);
        } while (questionIdExists(id));
        return id;
    }

    /**
     * Save or update a question in memory and persist all questions to CSV.
     */
    public void saveQuestion(Question q) {
        upsertQuestion(q);
        saveQuestionsToCsv();
    }

    /**
     * Delete a question by ID and persist changes to CSV.
     */
    public boolean deleteQuestion(Question q) {
        boolean removed = deleteQuestionById(q.getId());
        if (removed) {
            saveQuestionsToCsv();
        }
        return removed;
    }
}