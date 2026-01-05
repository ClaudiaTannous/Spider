package view;

public interface GameObserver {

    void onStatusChanged(int score, int lives);

    void onTurnChanged(String activeBoard);

    void onGameOver(boolean win, int score, int time);


}
