package controller;

import model.Board;
import model.Cell;
import model.Difficulty;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GameCountMinesTest {

    @Test
    public void testCountRemainingMines() throws Exception {
        Game game = new Game(Difficulty.EASY, "A", "B");

        Field boardField = Game.class.getDeclaredField("boardA");
        boardField.setAccessible(true);
        Board board = (Board) boardField.get(game);

        Cell[][] grid = board.getCells();

      
        for (int x = 0; x < board.getCols(); x++) {
            for (int y = 0; y < board.getRows(); y++) {
                grid[x][y].setMine(false);
                grid[x][y].setContent("");
            }
        }

       
        grid[0][0].setMine(true);
        grid[1][1].setMine(true);
        grid[2][2].setMine(true);

       
        grid[1][1].setContent("M");
 
        Method count = Game.class.getDeclaredMethod("countRemainingMines", Board.class);
        count.setAccessible(true);

        int result = (int) count.invoke(game, board);

      
        System.out.println("Actual Remaining Mines Count = " + result);

        assertEquals(2, result);
    }
}
