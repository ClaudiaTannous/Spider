package controller;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Random;
import java.sql.Date;
import model.Board;
import model.Cell;
import model.Difficulty;
import model.Player;
import model.Score;
import model.SpecialBoxType;
import view.MineSweeper;
import model.SysData;

public class Game implements MouseListener, ActionListener, WindowListener {

	private boolean playing;

	private Board boardA;
	private Board boardB;

	private Player player1;
	private Player player2;
	private Player currentPlayer;

	private int sharedLives;
	private int sharedScore;

	private MineSweeper gui;

	private Score score;
	private SysData sysData;

	private Difficulty currentDifficulty;
	
	
	public Game(Difficulty difficulty, String player1Name, String player2Name) {// Sets up a new game: loads scores and questions, creates players and boards, initializes the GUI and game state.
		score = new Score();
		score.populate();

		MineSweeper.setLook("Nimbus");

		this.currentDifficulty = difficulty;
		this.sysData = new SysData();
		this.sharedScore = 0;

		this.playing = false;

		initializePlayers(player1Name, player2Name);
		createBoards();
		updateMineCounters();

		gui.initGame();
		gui.setMines(currentDifficulty.getMines());
		gui.setActiveBoard("A");
		gui.initStatus(sharedLives);
		gui.updateStatus(sharedScore, sharedLives);

		gui.setVisible(true);
		gui.setIcons();
		gui.hideAll();

	}
	
	 private int getActivationCost(Difficulty diff) {
	        return switch (diff) {
	            case EASY   -> 5;
	            case MEDIUM -> 8;
	            case HARD   -> 12;
	        };
	    }

	    private int getActivationCost() {
	        return getActivationCost(currentDifficulty);
	    }

	private void initializePlayers(String name1, String name2) {  //Ensures valid player names, creates the Player objects, initializes shared lives/score, and constructs the MineSweeper GUI.
		if (name1 == null || name1.trim().isEmpty())
			name1 = "Player A";
		if (name2 == null || name2.trim().isEmpty())
			name2 = "Player B";

		player1 = new Player(name1);
		player2 = new Player(name2);
		currentPlayer = player1;

		sharedLives = currentDifficulty.getLives();
		sharedScore = 0;

		this.gui = new MineSweeper(this,currentDifficulty.getRows(), currentDifficulty.getCols(),
				currentDifficulty.getMines(), player1.getName(), player2.getName());
		this.gui.setButtonListeners(this);
		this.gui.initStatus(sharedLives);
		this.gui.setDifficulty(currentDifficulty);
	}
	public void createBoards() {//Creates a fresh Board for each player based on the current difficulty and updates the GUI mine count.
		boardA = new Board(currentDifficulty);
		boardB = new Board(currentDifficulty);
		gui.setMines(currentDifficulty.getMines());
	}

	public void newGame() { //Resets the game state (players, boards, lives, score, questions, timer) and restarts the GUI for a new match.
		this.playing = false;

		if (sysData != null) {
			sysData.resetMatchUsage();
		}

		player1 = new Player(player1.getName());
		player2 = new Player(player2.getName());
		currentPlayer = player1;

		sharedLives = currentDifficulty.getLives();
		sharedScore = 0;

		createBoards();
		updateMineCounters();

		gui.interruptTimer();
		gui.resetTimer();
		gui.initGame();
		gui.setMines(currentDifficulty.getMines());
		gui.setActiveBoard("A");
		gui.initStatus(sharedLives);
		gui.updateStatus(sharedScore, sharedLives);
	}
	
	private void endGame() { //Marks the game as not playing, reveals all mines, and saves the score state.
		playing = false;
		showAll();
		score.save();
	}

	public void gameWon() { //Calculates bonus points for remaining lives, updates score/streaks, logs a WIN, shows victory dialog, and saves score/times.
		int lifeValue = currentDifficulty.getQuestionPoints();
		int bonus = sharedLives * lifeValue;
		sharedScore += bonus;

		score.incCurrentStreak();
		score.incCurrentWinningStreak();
		score.incGamesWon();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();

		sysData.logGameResult(currentDifficulty, player1, sharedScore, player2, sharedScore, "WIN",
				gui.getTimePassed());

		gui.showVictoryDialog(sharedScore, gui.getTimePassed());

		score.addTime(gui.getTimePassed(), new Date(System.currentTimeMillis()));
		score.save();
	}

	public void gameLost() { //Updates losing streak and games count, logs a LOST result, shows game-over dialog, and saves the score.
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();

		sysData.logGameResult(currentDifficulty, player1, sharedScore, player2, sharedScore, "LOST",
				gui.getTimePassed());

		gui.showGameOverDialog(sharedScore);

		score.save();
	}

	

	private void checkGame() { //Checks if either board is fully cleared or lives reach zero, then decides win or loss.

		boolean aDone = checkWinCondition(boardA);
		boolean bDone = checkWinCondition(boardB);

		if (aDone || bDone || sharedLives <= 0) {
			if (sharedLives > 0) {
				gameWon();
			} else {
				gameLost();
			}
		}
	}

	
	private boolean checkWinCondition(Board board) { //Returns true if all non-mine, non-special cells are revealed
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				String content = cells[x][y].getContent();
				boolean isMine = cells[x][y].getMine();
				SpecialBoxType specialBox = cells[x][y].getSpecialBox();

				if (!isMine && specialBox == SpecialBoxType.NONE && content.equals("")) {
					return false;
				}
			}
		}
		return true;
	}
	
	private void findZeroes(int x, int y, Board board, JButton[][] buttons) { //Recursively reveals adjacent empty cells surrounding a zero-mine tile.


		Cell[][] cells = board.getCells();

		for (int tempX = board.makeValidCoordinateX(x - 1); tempX <= board.makeValidCoordinateX(x + 1); tempX++) {

			for (int tempY = board.makeValidCoordinateY(y - 1); tempY <= board.makeValidCoordinateY(y + 1); tempY++) {

				Cell cell = cells[tempX][tempY];

				if (cell.getContent().equals("") && !cell.getMine() && cell.getSpecialBox() == SpecialBoxType.NONE) {

					cell.setContent(Integer.toString(cell.getSurroundingMines()));

					buttons[tempX][tempY].setText(Integer.toString(cell.getSurroundingMines()));
					gui.setTextColor(buttons[tempX][tempY]);
					buttons[tempX][tempY].setBackground(gui.CELL_REVEALED);

					if (cell.getSurroundingMines() == 0) {
						buttons[tempX][tempY].setText("·");
						buttons[tempX][tempY].setForeground(new Color(160, 170, 200, 100));
						buttons[tempX][tempY].setFont(new Font("Arial", Font.BOLD, 24));
						findZeroes(tempX, tempY, board, buttons);
					}
				}
			}
		}
	}
	
	private void showAll() { //Reveals all mines on both boards at game end
		gui.revealAllMines(boardA, gui.getButtonsA());
		gui.revealAllMines(boardB, gui.getButtonsB());
	}

	private void switchTurn() { // Alternates current player and updates GUI to highlight the active board.
		currentPlayer = (currentPlayer == player1) ? player2 : player1;
		String newBoard = (currentPlayer == player1) ? "A" : "B";
		gui.setActiveBoard(newBoard);
		gui.updateStatus(sharedScore, sharedLives);
	}
	
	private void handleSurpriseBox(int x, int y, Board board, JButton button) { //Randomly gives bonus/penalty feedback when stepping on a surprise box.

		Random rand = new Random();
		boolean isBonus = rand.nextBoolean();

		if (isBonus) {
			button.setBackground(Color.green);
		} else {
			button.setBackground(Color.orange);
		}

		button.setIcon(null);
		button.setText("S");
		button.setFont(new Font("Serif", Font.BOLD, 18));
		button.setForeground(Color.RED);

		board.getCells()[x][y].setContent("S");

		
		gui.showSurpriseDialog(isBonus);
	}

	private void handleQuestionBox(int x, int y, Board board, JButton button) { //Shows a quiz question and applies feedback for correct/incorrect answers.


		var opt = sysData.nextQuestion();
		if (opt.isEmpty()) {
			
			gui.showNoMoreQuestionsDialog();
			return;
		}

		model.Question q = opt.get();

		
		Object answer = gui.askQuestion(q);
		if (answer == null) {
			
			return;
		}

		int selectedIndex = q.getOptions().indexOf(answer.toString());
		boolean correct = (selectedIndex == q.getCorrectIndex());

		if (correct) {
			button.setBackground(Color.green);
			gui.showCorrectAnswerDialog();
		} else {
			button.setBackground(Color.red);
			gui.showWrongAnswerDialog();
		}

		button.setIcon(null);
		button.setText("Q");
		button.setFont(new Font("Serif", Font.BOLD, 18));
		button.setForeground(Color.RED);

		board.getCells()[x][y].setContent("Q");
	}

	private void handleMineClick(int x, int y, Board board, JButton button) { // Reduces a life, shows mine explosion, updates status, and may end the game.

		Cell cell = board.getCells()[x][y];

		sharedLives -= 1; 

		button.setIcon(gui.getIconRedMine());
		button.setBackground(Color.RED);
		cell.setContent("M");

		
		gui.showMineHitDialog();

		gui.updateStatus(sharedScore, sharedLives);

		if (sharedLives <= 0) {
			gameLost();
			return;
		}

		switchTurn();
	}

	@Override
	public void mouseClicked(MouseEvent e) { // Handles cell clicks: reveals content, triggers events, switches turns, updates mine counts, and checks win/loss.


	  
	    if (!playing) {
	        gui.startTimer();
	        playing = true;
	    }
	    if (!playing)
	        return;

	    JButton button = (JButton) e.getSource();
	    String boardTag = (String) button.getClientProperty("board");
	    if (!boardTag.equals(gui.getActiveBoard()))
	        return;

	    Board board = boardTag.equals("A") ? boardA : boardB;
	    JButton[][] buttons = boardTag.equals("A") ? gui.getButtonsA() : gui.getButtonsB();

	    String[] parts = button.getName().split(":");
	    if (parts.length < 2)
	        return;
	    String[] co = parts[1].split(",");
	    int x = Integer.parseInt(co[0]);
	    int y = Integer.parseInt(co[1]);

	    Cell cell = board.getCells()[x][y];
	    String content = cell.getContent();

	  
	    if (!SwingUtilities.isLeftMouseButton(e)) {
	        return;
	    }

	   
	    if (!content.equals("")) {
	        return;
	    }

	    button.setIcon(null);

	    boolean isMine = cell.getMine();
	    int neighbours = cell.getSurroundingMines();
	    SpecialBoxType specialBox = cell.getSpecialBox();

	    if (specialBox == SpecialBoxType.SURPRISE) {
	        handleSurpriseBox(x, y, board, button);
	        switchTurn();
	    } else if (specialBox == SpecialBoxType.QUESTION) {
	        handleQuestionBox(x, y, board, button);
	        switchTurn();
	    } else if (isMine) {
	        handleMineClick(x, y, board, button);
	    } else {
	     
	        cell.setContent(Integer.toString(neighbours));
	        button.setText(Integer.toString(neighbours));
	        gui.setTextColor(button);
	        button.setBackground(gui.CELL_REVEALED);

	        
	        if (neighbours == 0) {
	            sharedScore += 1;
	            gui.updateStatus(sharedScore, sharedLives);

	            button.setText("·");
	            button.setForeground(new Color(160, 170, 200, 100));
	            button.setFont(new Font("Arial", Font.BOLD, 24));
	            findZeroes(x, y, board, buttons);
	        }

	        switchTurn();
	    }

	    updateMineCounters();
	    checkGame();
	}
	private int countRemainingMines(Board board) { //Counts unrevealed mines on a specific board
		int count = 0;
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				Cell c = cells[x][y];

				if (c.getMine() && !c.getContent().equals("M")) {
					count++;
				}
			}
		}
		return count;
	}
	
	private void updateMineCounters() { //Updates displayed mine counters for both boards.
		int a = countRemainingMines(boardA);
		int b = countRemainingMines(boardB);
		gui.updateMinesLeft(a, b);
	}
	@Override
	public void windowClosing(WindowEvent e) { //Logs a QUIT result, saves data, interrupts timer, and exits safely.


		if (gui != null) {
			gui.interruptTimer();
		}

		int seconds = (gui != null) ? gui.getTimePassed() : 0;

		sysData.logGameResult(currentDifficulty, player1, sharedScore, player2, sharedScore, "QUIT", seconds);

		score.save();

		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) { //Handles menu actions like starting a new game.

		JMenuItem menuItem = (JMenuItem) e.getSource();

		if (menuItem.getName().equals("New Game")) {
			if (playing) {
				Object[] options = { "Start new game", "Continue playing" };

				int startNew = JOptionPane.showOptionDialog(null, "What would you like to do?", "New Game",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

				if (startNew == JOptionPane.YES_OPTION) {
					newGame();
					score.incGamesPlayed();
					score.save();
				}
			} else {
			  newGame();
			}
		}
			
	
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}

	
