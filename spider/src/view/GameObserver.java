package view;

public interface GameObserver {

    // ===== Game state =====
    void onStatusChanged(int score, int lives);

    void onTurnChanged(String activeBoard);

    void onGameOver(boolean win, int score, int time);

    // ===== UI dialogs / notifications =====
    void onMineHit();

    void onCorrectAnswer();

    void onWrongAnswer();

    void onNoMoreQuestions();
}
