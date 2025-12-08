package controller;

import model.Board;
import model.Cell;
import model.Difficulty;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class GameWinConditionTest {

    @Test
    public void testWinWhenAllMinesFlagged() throws Exception {
        Game game = new Game(Difficulty.EASY, "A", "B");

        Field boardField = Game.class.getDeclaredField("boardA");
        boardField.setAccessible(true);
        Board board = (Board) boardField.get(game);

        Cell[][] grid = board.getCells();

       
        for (int x = 0; x < board.getCols(); x++) {
            for (int y = 0; y < board.getRows(); y++) {
                if (grid[x][y].getMine()) {
                    grid[x][y].setContent("F");
                }
            }
        }

        Method win = Game.class.getDeclaredMethod("checkWinCondition", Board.class);
        win.setAccessible(true);

        boolean result = (boolean) win.invoke(game, board);

        
        System.out.println("Win Condition Result  = " + result);

        assertTrue(result);
    }
}
