package model;

public class HardBoardFactory extends BoardFactory {
    @Override
    public Board createBoard() {
        return new Board(Difficulty.HARD);
    }
}
