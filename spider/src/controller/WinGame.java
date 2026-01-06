package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class WinGame extends EndGameTemplate {

    private final Game game;

    public WinGame(Game game) {
        this.game = game;
    }

    @Override
    protected void beforeEnd() {
        // Convert remaining lives to points
        game.convertRemainingLivesToPoints();
    }

    @Override
    protected void logResult() {
        // Save WIN result
        game.getSysData().logGameResult(
                game.getDifficulty(),
                game.getPlayer1(),
                game.getSharedScore(),
                game.getPlayer2(),
                game.getSharedScore(),
                "WIN",
                game.getGui().getTimePassed()
        );
    }

    @Override
    protected void afterEnd() {
        // End the game (lock board, stop timer)
        game.endGame();

        // Show win dialog
        showWinDialog();
    }

    private void showWinDialog() {
        SwingUtilities.invokeLater(() -> {

            Object[] options = {"Main Page", "Exit"};

            int choice = JOptionPane.showOptionDialog(
                    null, // independent dialog
                    "🎉 You won the game!\nCongratulations!",
                    "You Win!",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            switch (choice) {
                case 0 -> game.goToMainMenu(); 
                default -> System.exit(0);   
            }
        });
    }
}