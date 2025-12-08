package controller;

import model.Difficulty;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class GameActivationCostTest {

    @Test
    public void testActivationCosts() throws Exception {
        Game gEasy = new Game(Difficulty.EASY, "A", "B");
        Game gMed  = new Game(Difficulty.MEDIUM, "A", "B");
        Game gHard = new Game(Difficulty.HARD, "A", "B");

        Method m = Game.class.getDeclaredMethod("getActivationCost");
        m.setAccessible(true);

        int easyValue = (int) m.invoke(gEasy);
        int medValue  = (int) m.invoke(gMed);
        int hardValue = (int) m.invoke(gHard);

     
        System.out.println("Activation Cost (EASY)   Actual Value = " + easyValue);
        System.out.println("Activation Cost (MEDIUM) Actual Value = " + medValue);
        System.out.println("Activation Cost (HARD)   Actual Value = " + hardValue);

        assertEquals(5,  easyValue);
        assertEquals(8,  medValue);
        assertEquals(12, hardValue);
    }
}
