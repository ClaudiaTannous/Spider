package controller;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import javax.swing.JButton;

import org.junit.Before;
import org.junit.Test;

import model.Board;
import model.Cell;
import model.Difficulty;
import model.SpecialBoxType;
import view.MineSweeper;

public class CheckFindZeroes {

    private Game game;
    private MineSweeper gui;
    private Board board;
    private JButton[][] buttons;

    @Before
    public void setup() {
        // Create Game + GUI
        game = new Game(Difficulty.EASY, "A", "B");
        gui = new MineSweeper(game, 9, 9, 10, "A", "B");

        // Setup the board manually
        board = new Board(Difficulty.EASY);
        buttons = gui.getButtonsA();

        gui.initGame(); 
    }

    @Test
    public void testFindZeroesRevealConnected() throws Exception {

        // 1️⃣ Set entire board to 0-surrounding cells
        for (int x = 0; x < board.getCols(); x++) {
            for (int y = 0; y < board.getRows(); y++) {
                Cell c = board.getCells()[x][y];
                c.setMine(false);
                c.setSpecialBox(SpecialBoxType.NONE);
                c.setSurroundingMines(0);
                c.setContent("");    
            }
        }

      
        int cx = 4, cy = 4;

       
        board.getCells()[cx][cy].setContent("0");
        buttons[cx][cy].setText("·");
        buttons[cx][cy].setBackground(gui.CELL_REVEALED);

     
        Method m = Game.class.getDeclaredMethod(
                "findZeroes",
                int.class, int.class, Board.class, JButton[][].class
        );
        m.setAccessible(true);
        m.invoke(game, cx, cy, board, buttons);

      
        for (int x = 0; x < board.getCols(); x++) {
            for (int y = 0; y < board.getRows(); y++) {

                String text = buttons[x][y].getText();

                assertFalse(
                    "Cell (" + x + "," + y + ") was NOT revealed!",
                    text.isEmpty()
                );

                assertEquals("·", text);
                assertEquals(gui.CELL_REVEALED, buttons[x][y].getBackground());
            }
        }
    }
}
