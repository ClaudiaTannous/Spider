package model;

public class EasyBoardFactory extends BoardFactory {
    @Override
    public Board createBoard() {
        return new Board(Difficulty.EASY);
    }
}
