package model;

public class MediumBoardFactory extends BoardFactory {
    @Override
    public Board createBoard() {
        return new Board(Difficulty.MEDIUM);
    }
}
