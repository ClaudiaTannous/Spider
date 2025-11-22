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