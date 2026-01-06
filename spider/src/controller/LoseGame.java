package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import controller.Game;


public class LoseGame extends EndGameTemplate {

    private final Game game;

    public LoseGame(Game game) {
        this.game = game;
    }

    @Override
    protected void beforeEnd() {
        // convert remaining lives to points
        game.convertRemainingLivesToPoints();
    }

    @Override
    protected void logResult() {
        // Save LOST result to SysData
        game.getSysData().logGameResult(
                game.getDifficulty(),
                game.getPlayer1(),
                game.getSharedScore(),
                game.getPlayer2(),
                game.getSharedScore(),
                "LOST",
                game.getGui().getTimePassed()
        );
    }

    @Override
    protected void afterEnd() {
        // End the game (lock board, stop timer)
        game.endGame();

        // Show lose dialog
        showLoseDialog();
    }

    private void showLoseDialog() {
        SwingUtilities.invokeLater(() -> {

            Object[] options = {"Main Page", "Exit"};

            int choice = JOptionPane.showOptionDialog(
                    game.getGui(),
                    "💥 You lost the game!\nWhat would you like to do?",
                    "Game Over",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            switch (choice) {
            case 0 -> { 
                game.goToMainMenu();
            }
            case 1 -> { 
                System.exit(0);
            }
            case JOptionPane.CLOSED_OPTION -> {
             
                System.exit(0);
            }
            default -> {
                System.exit(0);
            }
        }

            
        });
    }
}