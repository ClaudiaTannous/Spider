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