package model;

public enum Difficulty {
    // rows, cols, mines, lives, surpriseBoxes, questionBoxes, questionPoints, surprisePoints, heartBoxes, diceBoxes

    // Easy game:
    //  - 10 mines
    //  - 6 question boxes
    //  - 2 surprise boxes
    //  - 1 heart box
    //  - 1 dice box
    //  - 10 lives
    //  - questionPoints = 5
    //  - surprisePoints = 8
    EASY(9, 9, 10, 10, 2, 6, 5, 8, 1, 1),

    // Medium game:
    //  - 26 mines
    //  - 7 question boxes
    //  - 3 surprise boxes
    //  - 2 heart boxes
    //  - 1 dice box
    //  - 8 lives
    //  - questionPoints = 8
    //  - surprisePoints = 12
    MEDIUM(13, 13, 26, 8, 3, 7, 8, 12, 2, 1),

    // Hard game:
    //  - 44 mines
    //  - 11 question boxes
    //  - 4 surprise boxes
    //  - 2 heart boxes
    //  - 2 dice boxes
    //  - 6 lives
    //  - questionPoints = 12
    //  - surprisePoints = 16
    HARD(16, 16, 44, 6, 4, 11, 12, 16, 2, 2);

    private final int rows;
    private final int cols;
    private final int mines;
    private final int lives;
    private final int surpriseBoxes;   // S cells
    private final int questionBoxes;   // Q cells
    private final int questionPoints;  // Points used for question-related scoring
    private final int surprisePoints;  // Points for good surprise
    private final int heartBoxes;      //  HEART cells
    private final int diceBoxes;       // DICE cells

    Difficulty(int rows,
               int cols,
               int mines,
               int lives,
               int surpriseBoxes,
               int questionBoxes,
               int questionPoints,
               int surprisePoints,
               int heartBoxes,
               int diceBoxes) {

        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.lives = lives;
        this.surpriseBoxes = surpriseBoxes;
        this.questionBoxes = questionBoxes;
        this.questionPoints = questionPoints;
        this.surprisePoints = surprisePoints;
        this.heartBoxes = heartBoxes;
        this.diceBoxes = diceBoxes;
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

    public int getHeartBoxes() {
        return heartBoxes;
    }

    public int getDiceBoxes() {
        return diceBoxes;
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
