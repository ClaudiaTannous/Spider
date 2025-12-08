package controller;

import model.Difficulty;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GameClampLivesTest {

    @Test
    public void testClampLivesAboveMax() throws Exception {
        Game game = new Game(Difficulty.EASY, "A", "B");

        Field livesField = Game.class.getDeclaredField("sharedLives");
        Field scoreField = Game.class.getDeclaredField("sharedScore");
        livesField.setAccessible(true);
        scoreField.setAccessible(true);

   
        livesField.set(game, 13); 

        Method clamp = Game.class.getDeclaredMethod("clampLives");
        clamp.setAccessible(true);
        clamp.invoke(game);

        int lives = (int) livesField.get(game);
        int score = (int) scoreField.get(game);

       
        System.out.println("Actual Lives After Clamp = " + lives);
        System.out.println("Actual Score After Clamp = " + score);

        assertEquals(10, lives);
        assertEquals(15, score); 
    }
}
