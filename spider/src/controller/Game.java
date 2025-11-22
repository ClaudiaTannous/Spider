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
import view.MainPage;
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

	public Game() {
		this(Difficulty.EASY, "Player A", "Player B");
	}
	public Game(Difficulty difficulty, String player1Name, String player2Name) {
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

	private void initializePlayers(String name1, String name2) {
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
	public void createBoards() {
		boardA = new Board(currentDifficulty);
		boardB = new Board(currentDifficulty);
		gui.setMines(currentDifficulty.getMines());
	}

	public void newGame() {
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
	
	private void endGame() {
		playing = false;
		showAll();
		score.save();
	}

	public void gameWon() {
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

	public void gameLost() {
		score.incCurrentLosingStreak();
		score.incGamesPlayed();

		gui.interruptTimer();
		endGame();

		sysData.logGameResult(currentDifficulty, player1, sharedScore, player2, sharedScore, "LOST",
				gui.getTimePassed());

		gui.showGameOverDialog(sharedScore);

		score.save();
	}

	

	private void checkGame() {
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

	private boolean checkWinCondition(Board board) {
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
	
	private void findZeroes(int x, int y, Board board, JButton[][] buttons) {
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