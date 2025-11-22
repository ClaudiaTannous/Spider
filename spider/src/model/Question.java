package model;

import java.util.List;
import java.util.Objects;

public final class Question {

    private final String id;                 
    private final String text;              
    private final List<String> answers;     
    private final int correctIndex;         
    private final QuestionDifficulty difficulty; 

    public Question(String id,
                    String text,
                    List<String> answers,
                    int correctIndex,
                    QuestionDifficulty difficulty) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
        if (answers == null || answers.size() != 4) {
            throw new IllegalArgumentException("exactly 4 answers required");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("correctIndex must be 0..3");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty is required");
        }

        this.id = id;
        this.text = text.trim();
        this.answers = List.copyOf(answers);
        this.correctIndex = correctIndex;
        this.difficulty = difficulty;
    }

    // ===== GETTERS =====

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    /** The 4 possible answers */
    public List<String> getAnswers() {
        return List.copyOf(answers);
    }

    /** Alias, in case some code uses getOptions() */
    public List<String> getOptions() {
        return getAnswers();
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    /** Convenience: returns true if chosenIndex is the correct answer. */
    public boolean isCorrect(int chosenIndex) {
        return this.correctIndex == chosenIndex;
    }

    /** Convenience: the correct answer text itself. */
    public String getCorrectAnswerText() {
        return answers.get(correctIndex);
    }

    // ===== equals / hashCode / toString =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question q = (Question) o;
        return Objects.equals(id, q.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", difficulty=" + difficulty +
                '}';
    }
}
