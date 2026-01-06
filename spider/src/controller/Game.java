package controller;

import javax.swing.*;
import javax.swing.border.LineBorder;

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
import model.BoardFactory;
import model.Cell;

import model.Difficulty;
import model.EasyBoardFactory;
import model.HardBoardFactory;
import model.MediumBoardFactory;
import model.Player;
import model.Question;
import model.QuestionDifficulty;
import model.Score;
import model.SpecialBoxType;
import view.GameObserver;
import view.MineSweeper;
import model.SysData;

public class Game implements MouseListener, ActionListener, WindowListener {

	private boolean playing;
	private BoardFactory boardFactory;
	private GameObserver observer;



	private Board boardA;
	private Board boardB;
	private boolean flagMode = false;
	private Player player1;
	private Player player2;
	private Player currentPlayer;
	private boolean gameOver = false;


	private int sharedLives;
	private int sharedScore;

	private MineSweeper gui;

	private Score score;
	private SysData sysData;

	private Difficulty currentDifficulty;
	private final Random rng = new Random();

	public Game(Difficulty difficulty, String player1Name, String player2Name) {// Sets up a new game: loads scores and
																				// questions, creates players and
																				// boards, initializes the GUI and game
																				// state.
		score = new Score();
		score.populate();

		

		this.currentDifficulty = difficulty;
		this.sysData = new SysData();
		this.sharedScore = 0;
		

		this.playing = false;
		this.boardFactory = switch (difficulty) {
	    case EASY -> new EasyBoardFactory();
	    case MEDIUM -> new MediumBoardFactory();
	    case HARD -> new HardBoardFactory();
	};


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
	public void setObserver(GameObserver observer) {
	    this.observer = observer;
	}


	private int getActivationCost(Difficulty diff) {
		return switch (diff) {
		case EASY -> 5;
		case MEDIUM -> 8;
		case HARD -> 12;
		};
	}

	private int getActivationCost() {
		return getActivationCost(currentDifficulty);
	}

	// points used by surprise good/bad effect (±8 / ±12 / ±16)
	private int getSurprisePoints(Difficulty diff) {
		return switch (diff) {
		case EASY -> 8;
		case MEDIUM -> 12;
		case HARD -> 16;
		};
	}

	private int getSurprisePoints() {
		return getSurprisePoints(currentDifficulty);
	}

	private void initializePlayers(String name1, String name2) { // Ensures valid player names, creates the Player
																	// objects, initializes shared lives/score, and
																	// constructs the MineSweeper GUI.
		if (name1 == null || name1.trim().isEmpty())
			name1 = "Player A";
		if (name2 == null || name2.trim().isEmpty())
			name2 = "Player B";

		player1 = new Player(name1);
		player2 = new Player(name2);
		currentPlayer = player1;

		sharedLives = currentDifficulty.getLives();
		sharedScore = 0;

		this.gui = new MineSweeper(this, currentDifficulty.getRows(), currentDifficulty.getCols(),
				currentDifficulty.getMines(), player1.getName(), player2.getName());
		this.setObserver(this.gui);

		this.gui.setButtonListeners(this);
		this.gui.initStatus(sharedLives);
		this.gui.setDifficulty(currentDifficulty);
	}

	public void createBoards() {
	    boardA = boardFactory.createBoard();
	    boardB = boardFactory.createBoard();
	    gui.setMines(currentDifficulty.getMines());
	}


	// Enables or disables flag mode
	public void setFlagMode(boolean flagMode) {
		boolean wasFlagMode = this.flagMode;
		this.flagMode = flagMode;

		// When leaving flag mode → evaluate all flags once
		if (wasFlagMode && !flagMode) {
			evaluateFlags(); // scores + borders
			updateMineCounters(); // now remaining mines changes
			checkGame(); // now we can also check win condition
		}
	}

	public boolean isFlagMode() {
		return flagMode;
	}

	 // Handles placing/removing a flag on a given cell while in flag mode.
	private void handleFlagClick(int x, int y, Board board, JButton button) {
		Cell cell = board.getCells()[x][y];
		String content = cell.getContent();
		if (content == null)
			content = "";

		if ("F".equals(content)) {
			cell.setContent("");
			button.setText("");
			button.setIcon(null);
			button.setBorder(UIManager.getBorder("Button.border"));
			gui.updateStatus(sharedScore, sharedLives);
			return;
		}

		// If the cell is already revealed (number, M, USED, etc.) → do nothing
		if (!content.equals("")) {
			return;
		}

		// Place a NEW flag – but no hint if correct or not
		cell.setContent("F");

		button.setIcon(null);
		button.setText("🚩");
		button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
		button.setForeground(Color.RED);
		button.setBorder(UIManager.getBorder("Button.border")); // neutral border

		// no score change here!
		gui.updateStatus(sharedScore, sharedLives);
	}

			public void newGame() { // Resets the game state (players, boards, lives, score, questions, timer) and
				// restarts the GUI for a new match.
		this.playing = false;
		gameOver = false;
		
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
			
	void endGame() { // Marks the game as not playing, reveals all mines, and saves the score state.
		playing = false;
		showAll();
		score.save();
	}

	void convertRemainingLivesToPoints() {  // Converts remaining lives into bonus points
		if (sharedLives <= 0) {
			return;
		}

		int lifeValue = getActivationCost();
		int bonus = sharedLives * lifeValue;

		sharedScore += bonus;
		sharedLives = 0;

		if (gui != null) {
			gui.updateStatus(sharedScore, sharedLives);
		}
	}

	// Handles a win: converts lives to points, updates status, logs and shows dialog.
	public void gameLost() {
	    if (gameOver) return;
	    gameOver = true;

	    new LoseGame(this).execute();
	}

	public void gameWon() {
	    if (gameOver) return;
	    gameOver = true;

	    new WinGame(this).execute();
	}

	 // Checks win/lose conditions and calls gameWon/gameLost accordingly.
	private void checkGame() {
	    if (!playing) return;   // if the game is no longer active, skip further win/lose checks
	                           // This prevents multiple end-game dialogs from being shown
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

	// Returns true if all mines on a board are either flagged or revealed.
	private boolean checkWinCondition(Board board) {
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				Cell cell = cells[x][y];
				String content = cell.getContent();
				boolean isMine = cell.getMine();

				if (content == null) {
					content = "";
				}

				if (isMine) {
					if (!"F".equals(content) && !"M".equals(content)) {
						return false;
					}
				}
			}
		}

		return true;
	}
	private void findZeroes(int x, int y, Board board, JButton[][] buttons) {
	    boolean[][] visited = new boolean[board.getCols()][board.getRows()];
	    findZeroes(x, y, board, buttons, visited);
	}


	private void findZeroes(
	        int x,
	        int y,
	        Board board,
	        JButton[][] buttons,
	        boolean[][] visited
	) {
	    // אם כבר ביקרנו כאן – עוצרים
	    if (visited[x][y]) return;
	    visited[x][y] = true;

	    Cell[][] cells = board.getCells();

	    for (int tempX = board.makeValidCoordinateX(x - 1);
	         tempX <= board.makeValidCoordinateX(x + 1);
	         tempX++) {

	        for (int tempY = board.makeValidCoordinateY(y - 1);
	             tempY <= board.makeValidCoordinateY(y + 1);
	             tempY++) {

	            if (tempX == x && tempY == y)
	                continue;

	            Cell cell = cells[tempX][tempY];
	            JButton btn = buttons[tempX][tempY];

	            String content = cell.getContent();
	            if (content == null) content = "";

	            // מוקש – לא ממשיכים
	            if (cell.getMine())
	                continue;

	            // אם התא כבר נפתח כמספר או USED – לא ממשיכים
	            if (!content.equals("") && !content.equals("❓"))
	                continue;

	            SpecialBoxType special = cell.getSpecialBox();

	            // 🎁 Surprise – נחשף אבל לא ממשיך קסקדה
	            if (special == SpecialBoxType.SURPRISE && content.equals("")) {
	                cell.setContent("🎁");
	                btn.setIcon(null);
	                btn.setText("🎁");
	                btn.setFont(new Font("Serif", Font.BOLD, 18));
	                btn.setForeground(new Color(30, 30, 30));
	                btn.setBackground(gui.S_HIGHLIGHT);
	                findZeroes(tempX, tempY, board, buttons, visited);
	                continue;
	            }

	            // ❓ Question – מתנהג כמו תא ריק וממשיך קסקדה
	            if (special == SpecialBoxType.QUESTION) {
	                if (!content.equals("❓")) {
	                    cell.setContent("❓");
	                    btn.setIcon(null);
	                    btn.setText("❓");
	                    btn.setFont(new Font("Serif", Font.BOLD, 18));
	                    btn.setForeground(new Color(30, 30, 30));
	                    btn.setBackground(gui.Q_HIGHLIGHT);
	                }

	                // ממשיכים קסקדה דרך השאלה
	                findZeroes(tempX, tempY, board, buttons, visited);
	                continue;
	            }

	            // תא רגיל
	            int neighbours = cell.getSurroundingMines();
	            cell.setContent(Integer.toString(neighbours));
	            btn.setBackground(gui.CELL_REVEALED);

	            if (neighbours == 0) {
	                btn.setText("·");
	                btn.setForeground(new Color(160, 170, 200, 100));
	                btn.setFont(new Font("Arial", Font.BOLD, 24));

	                findZeroes(tempX, tempY, board, buttons, visited);
	            } else {
	                btn.setText(Integer.toString(neighbours));
	                gui.setTextColor(btn);
	            }
	        }
	    }
	}


	  // Reveals all cells on both boards using GUI helper methods.
	 private void showAll() {
	        gui.revealAllBoard(boardA, gui.getButtonsA());
	        gui.revealAllBoard(boardB, gui.getButtonsB());
	    }
	 private void switchTurn() { // Alternates current player and updates GUI to highlight the active board.
			currentPlayer = (currentPlayer == player1) ? player2 : player1;
			String newBoard = (currentPlayer == player1) ? "A" : "B";
			gui.setActiveBoard(newBoard);
			if (observer != null) {
			    observer.onStatusChanged(sharedScore, sharedLives);
			}
		}


	private void handleSurpriseBox(int x, int y, Board board, JButton button) {  // Handles clicks on a surprise box and applies random effect.
		Cell cell = board.getCells()[x][y];
		String content = cell.getContent();

		// FIRST CLICK – reveal, +1 point (like empty)
		if (content.equals("")) {
			button.setBackground(gui.S_HIGHLIGHT);
			button.setIcon(null);
			button.setText("🎁");
			button.setFont(new Font("Serif", Font.BOLD, 18));
			button.setForeground(new Color(30, 30, 30));

			cell.setContent("🎁");

			sharedScore += 1;
			if (observer != null) {
			    observer.onStatusChanged(sharedScore, sharedLives);
			}

			return;
		}

		if (content.equals("USED")) {
			return;
		}

		// SECOND CLICK – activation with cost and good/bad effect
		if (content.equals("🎁")) {
			int choice = JOptionPane
					.showConfirmDialog(gui,
							"Do you want to activate the surprise box?\n" + "(Activation will cost "
									+ getActivationCost() + " points.)",
							"Activate Surprise?", JOptionPane.YES_NO_OPTION);

			if (choice != JOptionPane.YES_OPTION) {
				return;
			}

			int activationCost = getActivationCost();
			int surprisePts = getSurprisePoints();

			boolean isBonus = rng.nextBoolean();

			int deltaPts = -activationCost;
			int deltaLives = 0;

			if (isBonus) {
				deltaPts += surprisePts; // +8 / +12 / +16
				deltaLives = +1;
				button.setBackground(new Color(167, 214, 167)); 
			} else {
				deltaPts -= surprisePts; // -8 / -12 / -16
				deltaLives = -1;
				button.setBackground(new Color(167, 214, 167)); 
			}

			sharedScore += deltaPts;
			sharedLives += deltaLives;
			clampLives();

			button.setIcon(null);
			button.setText("USED");
			button.setFont(new Font("Serif", Font.BOLD, 12));
			button.setForeground(Color.BLACK);

			cell.setContent("USED");
			cell.setSpecialBox(SpecialBoxType.NONE);

			gui.updateStatus(sharedScore, sharedLives);

			// MESSAGE – what happened
			StringBuilder msg = new StringBuilder();
			msg.append("Surprise box result:\n\n");
			msg.append("Activation cost: -").append(activationCost).append(" pts\n");
			int effectPts = deltaPts + activationCost; 
			msg.append("Surprise effect points: ").append(effectPts >= 0 ? "+" : "").append(effectPts).append(" pts");

			if (deltaLives != 0) {
				msg.append("\nLife change: ").append(deltaLives > 0 ? "+" : "").append(deltaLives).append(" ♥");
			}

			msg.append("\n\nTotal points change: ").append(deltaPts >= 0 ? "+" : "").append(deltaPts).append(" pts");

			msg.append("\nTotal score: ").append(sharedScore).append("\nTotal lives: ").append(sharedLives);

			JOptionPane.showMessageDialog(gui, msg.toString(), "Surprise Box", JOptionPane.INFORMATION_MESSAGE);

			if (sharedLives <= 0) {
				gameLost();
			}
		}
	}

	private void handleQuestionBox(int x, int y, Board board, JButton button) {  // Handles clicks on a question box and applies question outcome.
		Cell cell = board.getCells()[x][y];
		String content = cell.getContent();

		if (content.equals("")) {
			// Reveal question
			button.setBackground(gui.Q_HIGHLIGHT);
			button.setIcon(null);
			button.setText("❓");
			button.setFont(new Font("Serif", Font.BOLD, 18));
			button.setForeground(new Color(30, 30, 30));

			cell.setContent("❓");

			sharedScore += 1;
			gui.updateStatus(sharedScore, sharedLives);

			//expand empty neighbors like normal empty cell
			findZeroes(x, y, board, (board == boardA ? gui.getButtonsA() : gui.getButtonsB()));

			return;
		}

		if (content.equals("USED")) {
			return;
		}

		// SECOND CLICK – answer question
		if (content.equals("❓")) {
			int choice = JOptionPane.showConfirmDialog(gui, "Do you want to answer the question now?\n"
					+ "(Activation will cost " + getActivationCost() + " points.)", "Answer Question?",
					JOptionPane.YES_NO_OPTION);

			if (choice != JOptionPane.YES_OPTION) {
				return;
			}

			var opt = sysData.nextQuestion();
			if (opt.isEmpty()) {
				gui.showNoMoreQuestionsDialog();
				return;
			}

			Question q = opt.get();

			Object answer = gui.askQuestion(q);

			int selectedIndex = q.getOptions().indexOf(answer.toString());
			boolean correct = (selectedIndex == q.getCorrectIndex());

			if (correct) {
				button.setBackground(new Color(167, 214, 167));
				gui.showCorrectAnswerDialog();
			} else {
				button.setBackground(new Color(167, 214, 167)); 
				gui.showWrongAnswerDialog();
			}

			button.setIcon(null);
			button.setText("USED");
			button.setFont(new Font("Serif", Font.BOLD, 12));
			button.setForeground(Color.BLACK);

			cell.setContent("USED");
			cell.setSpecialBox(SpecialBoxType.NONE);

			// Apply scoring & lives
			applyQuestionOutcome(currentDifficulty, q.getDifficulty(), correct, board);
		}
	}

	private void handleMineClick(int x, int y, Board board, JButton button) { // Reduces a life, shows mine explosion,
																				// updates status, and may end the game.

		Cell cell = board.getCells()[x][y];

		sharedLives -= 1;

		button.setIcon(gui.getIconRedMine());
		button.setBackground(new Color(183, 28, 28));
		cell.setContent("M");

		gui.showMineHitDialog();

		gui.updateStatus(sharedScore, sharedLives);

		if (sharedLives <= 0) {
			gameLost();
			return;
		}

		switchTurn();
	}

	private int countRemainingMines(Board board) { // Counts unrevealed mines on a specific board
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
	
	private int getMaxLives() {
	    return 10;
	}


	private void updateMineCounters() { // Updates displayed mine counters for both boards.
		int a = countRemainingMines(boardA);
		int b = countRemainingMines(boardB);
		gui.updateMinesLeft(a, b);
	}
	public void logQuit() {
	    if (gui != null) {
	        gui.interruptTimer();
	    }

	    int seconds = (gui != null) ? gui.getTimePassed() : 0;

	    sysData.logGameResult(
	        currentDifficulty,
	        player1,
	        sharedScore,
	        player2,
	        sharedScore,
	        "QUIT",
	        seconds
	    );

	    score.save();
	}


	@Override
	public void windowClosing(WindowEvent e) {
	    logQuit();
	    System.exit(0);
	}


	@Override
	public void actionPerformed(ActionEvent e) { // Handles menu actions like starting a new game.

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

	private void evaluateFlags() { // Evaluates flags on both boards and updates score and status.
		if (boardA != null && gui != null) {
			evaluateFlagsOnBoard(boardA, gui.getButtonsA());
		}
		if (boardB != null && gui != null) {
			evaluateFlagsOnBoard(boardB, gui.getButtonsB());
		}

		gui.updateStatus(sharedScore, sharedLives);
	}

	private void evaluateFlagsOnBoard(Board board, JButton[][] buttons) {  // Evaluates all flags on a single board and marks correct/incorrect flags.
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				Cell cell = cells[x][y];
				JButton button = buttons[x][y];

				 // Only cells that are still flagged
				if (!"F".equals(cell.getContent())) {
					continue;
				}

				// Skip flags already evaluated (have LineBorder)
				if (button.getBorder() instanceof LineBorder) {
					continue;
				}

				if (cell.getMine()) {
					 // Correct flag on a mine (+1 and reveal)
					sharedScore += 1;

					cell.setContent("M");  
					button.setIcon(gui.getIconRedMine());
					button.setText("");
					button.setBackground(new Color(183, 28, 28));
					button.setBorder(new LineBorder(Color.GREEN, 2, true));
				} else {
					// Wrong flag on non-mine (-3, red border)
					sharedScore -= 3;
					button.setBorder(new LineBorder(Color.RED, 2, true));
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {  // Handles all mouse clicks on cells (flags, specials, mines, normal cells).

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
		if (content == null)
			content = "";

		SpecialBoxType specialBox = cell.getSpecialBox();

		boolean isLeft = SwingUtilities.isLeftMouseButton(e);
		if (!isLeft)
			return;

		// -------- FLAG MODE --------
		if (flagMode) {
			handleFlagClick(x, y, board, button);

			return;
		}

		// ignore click on a flagged cell
		if (content.equals("F"))
			return;

		boolean isMine = cell.getMine();
		int neighbours = cell.getSurroundingMines();

		// ⭐ FIX: if this is a mine that is already revealed ("M"), ignore click
		if (isMine && "M".equals(content)) {
			return;
		}

		// ⭐ FIX: only clear icon if this is NOT an already-revealed mine
		if (!"M".equals(content)) {
			button.setIcon(null);
		}

		// if cell already has content (number, USED, etc.) – only allow clicking
		// special boxes
		if (!content.equals("")) {
			boolean isClickableSpecial = (specialBox == SpecialBoxType.SURPRISE && content.equals("🎁"))
					|| (specialBox == SpecialBoxType.QUESTION && content.equals("❓"));

			if (!isClickableSpecial)
				return;
		}

		// -------- SPECIAL BOXES / MINES / NORMAL CELLS --------
		if (specialBox == SpecialBoxType.SURPRISE) {

			String before = cell.getContent();
			handleSurpriseBox(x, y, board, button);
			String after = cell.getContent();

			if (before.equals("") || "USED".equals(after))
				switchTurn();

		} else if (specialBox == SpecialBoxType.QUESTION) {

			String before = cell.getContent();
			handleQuestionBox(x, y, board, button);
			String after = cell.getContent();

			if (before.equals("") || "USED".equals(after))
				switchTurn();

		} else if (isMine) {
			// first-time click on hidden mine
			handleMineClick(x, y, board, button);

		} else {
			// safe cell
			sharedScore += 1;
			gui.updateStatus(sharedScore, sharedLives);

			cell.setContent(Integer.toString(neighbours));
			button.setBackground(gui.CELL_REVEALED);

			if (neighbours == 0) {
				button.setText("·");
				button.setForeground(new Color(160, 170, 200, 100));
				button.setFont(new Font("Arial", Font.BOLD, 24));

				findZeroes(x, y, board, buttons);
			} else {
				button.setText(Integer.toString(neighbours));
				gui.setTextColor(button);
			}

			switchTurn();
		}

		updateMineCounters();
		checkGame();
	}


	 // Applies question outcome (points, lives, reveals) based on difficulty and answer.
	private void applyQuestionOutcome(Difficulty gameDiff, QuestionDifficulty qDiff, boolean correct, Board board) {

		int activationCost = getActivationCost(gameDiff);

		int deltaPts = -activationCost; // pay activation cost
		int deltaLives = 0;
		boolean revealMine = false;
		boolean reveal3x3 = false;

		boolean pickFirst = rng.nextBoolean(); // for OR cases in table

// EASY game
		if (gameDiff == Difficulty.EASY) {
			switch (qDiff) {
			case EASY -> {
				if (correct) {
					deltaPts += 3;
					deltaLives += 1;
				} else {
					if (pickFirst)
						deltaPts -= 3;
				}
			}
			case MEDIUM -> {
				if (correct) {
					deltaPts += 6;
					revealMine = true;
				} else {
					if (pickFirst)
						deltaPts -= 6;
				}
			}
			case HARD -> {
				if (correct) {
					deltaPts += 10;
					reveal3x3 = true;
				} else {
					deltaPts -= 10;
				}
			}
			case EXPERT -> {
				if (correct) {
					deltaPts += 15;
					deltaLives += 2;
				} else {
					deltaPts -= 15;
					deltaLives -= 1;
				}
			}
			}
		}

// MEDIUM game
		if (gameDiff == Difficulty.MEDIUM) {
			switch (qDiff) {
			case EASY -> {
				if (correct) {
					deltaPts += 8;
					deltaLives += 1;
				} else {
					deltaPts -= 8;
				}
			}
			case MEDIUM -> {
				if (correct) {
					deltaPts += 10;
					deltaLives += 1;
				} else {
					if (pickFirst) {
						deltaPts -= 10;
						deltaLives -= 1;
					}
				}
			}
			case HARD -> {
				if (correct) {
					deltaPts += 15;
					deltaLives += 1;
				} else {
					deltaPts -= 15;
					deltaLives -= 1;
				}
			}
			case EXPERT -> {
				if (correct) {
					deltaPts += 20;
					deltaLives += 2;
				} else {
					deltaPts -= 20;
					deltaLives -= (pickFirst ? 1 : 2);
				}
			}
			}
		}

// HARD game
		if (gameDiff == Difficulty.HARD) {
			switch (qDiff) {
			case EASY -> {
				if (correct) {
					deltaPts += 10;
					deltaLives += 1;
				} else {
					deltaPts -= 10;
					deltaLives -= 1;
				}
			}
			case MEDIUM -> {
				if (correct) {
					deltaPts += 15;
					deltaLives += (pickFirst ? 1 : 2);
				} else {
					deltaPts -= 15;
					deltaLives -= (pickFirst ? 1 : 2);
				}
			}
			case HARD -> {
				if (correct) {
					deltaPts += 20;
					deltaLives += 2;
				} else {
					deltaPts -= 20;
					deltaLives -= 2;
				}
			}
			case EXPERT -> {
				if (correct) {
					deltaPts += 40;
					deltaLives += 3;
				} else {
					deltaPts -= 40;
					deltaLives -= 3;
				}
			}
			}
		}

		sharedScore += deltaPts;
		sharedLives += deltaLives;
		clampLives();

		if (revealMine) {
			revealRandomMine(board);
		}
		if (reveal3x3) {
			revealRandom3x3(board);
		}

		gui.updateStatus(sharedScore, sharedLives);

		 // Summary message for question result
		StringBuilder msg = new StringBuilder();
		msg.append("Question result (").append(gameDiff.name()).append(" game, ").append(qDiff.name())
				.append(" question)\n\n");

		msg.append(correct ? "✅ Correct answer!\n" : "❌ Wrong answer.\n");

		msg.append("Activation cost: -").append(activationCost).append(" pts\n");

		int effectPts = deltaPts + activationCost; // pure effect beyond activation cost
		msg.append("Question effect points: ").append(effectPts >= 0 ? "+" : "").append(effectPts).append(" pts");

		if (deltaLives != 0) {
			msg.append("\nLife change: ").append(deltaLives > 0 ? "+" : "").append(deltaLives).append(" ♥");
		}

		if (revealMine) {
			msg.append("\n\nA random mine on your board was revealed.");
		}
		if (reveal3x3) {
			msg.append("\nA 3×3 area on your board was revealed.");
		}

		msg.append("\n\nTotal points change: ").append(deltaPts >= 0 ? "+" : "").append(deltaPts).append(" pts");

		msg.append("\nTotal score: ").append(sharedScore).append("\nTotal lives: ").append(sharedLives);

		JOptionPane.showMessageDialog(gui, msg.toString(), "Question Outcome", JOptionPane.INFORMATION_MESSAGE);

		if (sharedLives <= 0) {
			gameLost();
		}
	}
	 private void clampLives() { // Clamps lives between 0 and max and converts extra lives to points.
	        int max = getMaxLives();

	        if (sharedLives > max) {
	            int extraLives = sharedLives - max;

	            int activationCost = getActivationCost();

	            int bonusPoints = extraLives * activationCost;

	            sharedScore += bonusPoints;  
	            sharedLives = max;           

	        }

	        if (sharedLives < 0) {
	            sharedLives = 0;
	        }
	    }
	 
	  private void revealRandomMine(Board board) { // Reveals one hidden mine on the given board (first one found).
	        Cell[][] c = board.getCells();
	        JButton[][] btns = (board == boardA) ? gui.getButtonsA() : gui.getButtonsB();

	        for (int x = 0; x < board.getCols(); x++) {
	            for (int y = 0; y < board.getRows(); y++) {
	                if (c[x][y].getMine() && c[x][y].getContent().equals("")) {

	                    c[x][y].setContent("M");
	                    JButton btn = btns[x][y];
	                    btn.setIcon(gui.getIconRedMine());
	                    btn.setBackground(new Color(220, 53, 69)); 
	                    return;
	                }
	            }
	        }
	    }
	  
	  private void revealRandom3x3(Board board) { 
		    int cols = board.getCols();
		    int rows = board.getRows();

		    JButton[][] btns = (board == boardA) ? gui.getButtonsA() : gui.getButtonsB();
		    Cell[][] cells = board.getCells();

		    int bestCenterX = -1;
		    int bestCenterY = -1;
		    int bestClosedCount = -1;

		    // 1) Try to find a 3x3 where ALL 9 cells are closed
		    for (int cx = 0; cx < cols; cx++) {
		        for (int cy = 0; cy < rows; cy++) {

		            int closedCount = 0;

		            for (int x = Math.max(0, cx - 1); x <= Math.min(cols - 1, cx + 1); x++) {
		                for (int y = Math.max(0, cy - 1); y <= Math.min(rows - 1, cy + 1); y++) {

		                    String content = cells[x][y].getContent();
		                    if (content == null) content = "";

		                    if (content.equals("")) {
		                        closedCount++;
		                    }
		                }
		            }

		            // if the 3x3 is fully closed (9 closed cells) -> pick it immediately
		            if (closedCount == 9) {
		                bestCenterX = cx;
		                bestCenterY = cy;
		                bestClosedCount = 9;
		                break;
		            }

		            // otherwise keep the best 3x3 (max closed cells)
		            if (closedCount > bestClosedCount) {
		                bestClosedCount = closedCount;
		                bestCenterX = cx;
		                bestCenterY = cy;
		            }
		        }

		        if (bestClosedCount == 9) break;
		    }

		    // If bestClosedCount <= 0 => no closed cells in any 3x3, nothing to reveal
		    if (bestClosedCount <= 0) return;

		    int centerX = bestCenterX;
		    int centerY = bestCenterY;

		    // 2) Reveal as many as possible in that 3x3 (ONLY closed cells)
		    for (int x = Math.max(0, centerX - 1); x <= Math.min(cols - 1, centerX + 1); x++) {
		        for (int y = Math.max(0, centerY - 1); y <= Math.min(rows - 1, centerY + 1); y++) {

		            Cell cell = cells[x][y];
		            JButton btn = btns[x][y];

		            String content = cell.getContent();
		            if (content == null) content = "";

		            // Only reveal cells that are still closed
		            if (!content.equals("")) continue;

		            if (cell.getMine()) {
		            	btn.setIcon(gui.getIconRedMine());
		            	btn.setBackground(new Color(220, 53, 69)); 
		            	cell.setContent("M");
		                continue;
		            }

		            int n = cell.getSurroundingMines();
		            cell.setContent(Integer.toString(n));

		            btn.setBackground(gui.CELL_REVEALED);
		            if (n == 0) {
		                btn.setText("·");
		            } else {
		                btn.setText(Integer.toString(n));
		                gui.setTextColor(btn);
		            }
		        }
		    }
		}
	  private JButton getButtonForCell(Cell cell) {
		    for (int x = 0; x < boardA.getCols(); x++) {
		        for (int y = 0; y < boardA.getRows(); y++) {
		            if (boardA.getCells()[x][y] == cell) {
		                return gui.getButtonsA()[x][y];
		            }
		            if (boardB.getCells()[x][y] == cell) {
		                return gui.getButtonsB()[x][y];
		            }
		        }
		    }
		    return null;
		}
	  private Board getBoardForCell(Cell cell) {
		    for (int x = 0; x < boardA.getCols(); x++) {
		        for (int y = 0; y < boardA.getRows(); y++) {
		            if (boardA.getCells()[x][y] == cell) return boardA;
		            if (boardB.getCells()[x][y] == cell) return boardB;
		        }
		    }
		    return null;
		}
	  private int[] getCellCoordinates(Cell cell, Board board) {
		    for (int x = 0; x < board.getCols(); x++) {
		        for (int y = 0; y < board.getRows(); y++) {
		            if (board.getCells()[x][y] == cell) {
		                return new int[]{x, y};
		            }
		        }
		    }
		    return null;
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
	




	
	
	public SysData getSysData() {
	    return sysData;
	}

	public MineSweeper getGui() {
	    return gui;
	}

	public Difficulty getDifficulty() {
	    return currentDifficulty;
	}

	public Player getPlayer1() {
	    return player1;
	}

	public Player getPlayer2() {
	    return player2;
	}

	public int getSharedScore() {
	    return sharedScore;
	}
	
	
	
	public void goToMainMenu() {
	    // Stop the timer safely
	    gui.interruptTimer();

	    // Reset game state
	    gameOver = true;

	    // Switch back to main menu UI
	    gui.goToMainPage();
	}
	
  public boolean isGameOver() {
	    return gameOver;
	}

	public void setGameOver(boolean gameOver) {
	    this.gameOver = gameOver;
	}

}
