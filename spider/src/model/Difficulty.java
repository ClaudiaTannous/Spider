package model;

public enum Difficulty {
    // rows, cols, mines, lives, surpriseBoxes, questionBoxes, questionPoints, surprisePoints

    // Easy game:
    //  - 10 mines
    //  - 6 question boxes
    //  - 2 surprise boxes
    //  - 10 lives
    //  - questionPoints = 5  (used in scoring logic)
    //  - surprisePoints = 8  (used in scoring logic)
    EASY(9, 9, 10, 10, 2, 6, 5, 8),

    // Medium game:
    //  - 26 mines
    //  - 7 question boxes
    //  - 3 surprise boxes
    //  - 8 lives
    //  - questionPoints = 8
    //  - surprisePoints = 12
    MEDIUM(13, 13, 26, 8, 3, 7, 8, 12),

    // Hard game:
    //  - 44 mines
    //  - 11 question boxes
    //  - 4 surprise boxes
    //  - 6 lives
    //  - questionPoints = 12
    //  - surprisePoints = 16
    HARD(16, 16, 44, 6, 4, 11, 12, 16);

    private final int rows;
    private final int cols;
    private final int mines;
    private final int lives;
    private final int surpriseBoxes;   // S cells
    private final int questionBoxes;   // Q cells
    private final int questionPoints;  // Points used for question-related scoring
    private final int surprisePoints;  // Points for good surprise

    Difficulty(int rows,
               int cols,
               int mines,
               int lives,
               int surpriseBoxes,
               int questionBoxes,
               int questionPoints,
               int surprisePoints) {

        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.lives = lives;
        this.surpriseBoxes = surpriseBoxes;
        this.questionBoxes = questionBoxes;
        this.questionPoints = questionPoints;
        this.surprisePoints = surprisePoints;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }

    public int getLives() {
        return lives;
    }

    public int getSurpriseBoxes() {
        return surpriseBoxes;
    }

    public int getQuestionBoxes() {
        return questionBoxes;
    }

    public int getQuestionPoints() {
        return questionPoints;
    }

    public int getSurprisePoints() {
        return surprisePoints;
    }

    @Override
    public String toString() {
        switch (this) {
            case EASY:   return "Easy";
            case MEDIUM: return "Medium";
            case HARD:   return "Hard";
            default:     return "";
        }
    }
}
