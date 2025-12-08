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
import model.Cell;
import model.Difficulty;
import model.Player;
import model.Question;
import model.QuestionDifficulty;
import model.Score;
import model.SpecialBoxType;
import view.MineSweeper;
import model.SysData;

public class Game implements MouseListener, ActionListener, WindowListener {

	private boolean playing;

	private Board boardA;
	private Board boardB;
	private boolean flagMode = false;
	private Player player1;
	private Player player2;
	private Player currentPlayer;

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
		this.gui.setButtonListeners(this);
		this.gui.initStatus(sharedLives);
		this.gui.setDifficulty(currentDifficulty);
	}

	public void createBoards() {// Creates a fresh Board for each player based on the current difficulty and
								// updates the GUI mine count.
		boardA = new Board(currentDifficulty);
		boardB = new Board(currentDifficulty);
		gui.setMines(currentDifficulty.getMines());
	}

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

	private void endGame() { // Marks the game as not playing, reveals all mines, and saves the score state.
		playing = false;
		showAll();
		score.save();
	}

	private void convertRemainingLivesToPoints() {
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

	public void gameWon() {
		// קודם ממירים חיים לנקודות לפי הכלל החדש
		convertRemainingLivesToPoints();

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
		// אם במקרה נשארו חיים (לא סביר אצלך, אבל לפי ההגדרה זה נכון)
		convertRemainingLivesToPoints();

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

		Cell[][] cells = board.getCells();

		for (int tempX = board.makeValidCoordinateX(x - 1); tempX <= board.makeValidCoordinateX(x + 1); tempX++) {

			for (int tempY = board.makeValidCoordinateY(y - 1); tempY <= board.makeValidCoordinateY(y + 1); tempY++) {

				if (tempX == x && tempY == y)
					continue;

				Cell cell = cells[tempX][tempY];
				JButton btn = buttons[tempX][tempY];

				String c = cell.getContent();

				if (!c.equals("") && !c.equals("❓"))
					continue;

				if (cell.getMine())
					continue;

				SpecialBoxType special = cell.getSpecialBox();

				if (special == SpecialBoxType.SURPRISE) {
					cell.setContent("🎁");
					btn.setIcon(null);
					btn.setText("🎁");
					btn.setFont(new Font("Serif", Font.BOLD, 18));
					btn.setForeground(Color.RED);
					btn.setBackground(Color.ORANGE);
					continue;
				}

				if (special == SpecialBoxType.QUESTION) {
					cell.setContent("❓");
					btn.setIcon(null);
					btn.setText("❓");
					btn.setFont(new Font("Serif", Font.BOLD, 18));
					btn.setForeground(Color.RED);
					btn.setBackground(Color.YELLOW);
					continue;
				}

				int neighbours = cell.getSurroundingMines();
				cell.setContent(Integer.toString(neighbours));

				btn.setBackground(gui.CELL_REVEALED);

				if (neighbours == 0) {
					btn.setText("·");
					btn.setForeground(new Color(160, 170, 200, 100));
					btn.setFont(new Font("Arial", Font.BOLD, 24));

					findZeroes(tempX, tempY, board, buttons);

				} else {
					btn.setText(Integer.toString(neighbours));
					gui.setTextColor(btn);
				}
			}
		}
	}

	private void showAll() { // Reveals all mines on both boards at game end
		gui.revealAllMines(boardA, gui.getButtonsA());
		gui.revealAllMines(boardB, gui.getButtonsB());
	}

	private void switchTurn() { // Alternates current player and updates GUI to highlight the active board.
		currentPlayer = (currentPlayer == player1) ? player2 : player1;
		String newBoard = (currentPlayer == player1) ? "A" : "B";
		gui.setActiveBoard(newBoard);
		gui.updateStatus(sharedScore, sharedLives);
	}

	private void handleSurpriseBox(int x, int y, Board board, JButton button) {
		Cell cell = board.getCells()[x][y];
		String content = cell.getContent();

		// FIRST CLICK – reveal, +1 point (like empty)
		if (content.equals("")) {
			button.setBackground(Color.ORANGE);
			button.setIcon(null);
			button.setText("🎁");
			button.setFont(new Font("Serif", Font.BOLD, 18));
			button.setForeground(Color.RED);

			cell.setContent("🎁");

			sharedScore += 1;
			gui.updateStatus(sharedScore, sharedLives);
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
				button.setBackground(Color.GREEN);
			} else {
				deltaPts -= surprisePts; // -8 / -12 / -16
				deltaLives = -1;
				button.setBackground(Color.RED);
			}

			sharedScore += deltaPts;
			sharedLives += deltaLives;
			clampLives();

			button.setIcon(null);
			button.setText("USED");
			button.setFont(new Font("Serif", Font.BOLD, 16));
			button.setForeground(Color.LIGHT_GRAY);

			cell.setContent("USED");
			cell.setSpecialBox(SpecialBoxType.NONE);

			gui.updateStatus(sharedScore, sharedLives);

			// MESSAGE – what happened
			StringBuilder msg = new StringBuilder();
			msg.append("Surprise box result:\n\n");
			msg.append("Activation cost: -").append(activationCost).append(" pts\n");
			int effectPts = deltaPts + activationCost; // רק האפקט (לא העלות)
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

	private void handleQuestionBox(int x, int y, Board board, JButton button) {
		Cell cell = board.getCells()[x][y];
		String content = cell.getContent();

		if (content.equals("")) {
			// Reveal question
			button.setBackground(Color.YELLOW);
			button.setIcon(null);
			button.setText("❓");
			button.setFont(new Font("Serif", Font.BOLD, 18));
			button.setForeground(Color.RED);

			cell.setContent("❓");

			sharedScore += 1;
			gui.updateStatus(sharedScore, sharedLives);

			// 🔥 NEW: expand empty neighbors like normal empty cell
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
				button.setBackground(Color.GREEN);
				gui.showCorrectAnswerDialog();
			} else {
				button.setBackground(Color.RED);
				gui.showWrongAnswerDialog();
			}

			button.setIcon(null);
			button.setText("USED");
			button.setFont(new Font("Serif", Font.BOLD, 16));
			button.setForeground(Color.LIGHT_GRAY);

			cell.setContent("USED");
			cell.setSpecialBox(SpecialBoxType.NONE);

			// טבלת הניקוד (כולל עלות ההפעלה החדשה)
			applyQuestionOutcome(currentDifficulty, q.getDifficulty(), correct, board);
		}
	}

	private void handleMineClick(int x, int y, Board board, JButton button) { // Reduces a life, shows mine explosion,
																				// updates status, and may end the game.

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
        return currentDifficulty.getLives();   // 10 / 8 / 6 from enum
    }

	private void updateMineCounters() { // Updates displayed mine counters for both boards.
		int a = countRemainingMines(boardA);
		int b = countRemainingMines(boardB);
		gui.updateMinesLeft(a, b);
	}

	@Override
	public void windowClosing(WindowEvent e) { // Logs a QUIT result, saves data, interrupts timer, and exits safely.

		if (gui != null) {
			gui.interruptTimer();
		}

		int seconds = (gui != null) ? gui.getTimePassed() : 0;

		sysData.logGameResult(currentDifficulty, player1, sharedScore, player2, sharedScore, "QUIT", seconds);

		score.save();

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

	private void evaluateFlags() {
		if (boardA != null && gui != null) {
			evaluateFlagsOnBoard(boardA, gui.getButtonsA());
		}
		if (boardB != null && gui != null) {
			evaluateFlagsOnBoard(boardB, gui.getButtonsB());
		}

		gui.updateStatus(sharedScore, sharedLives);
	}

	private void evaluateFlagsOnBoard(Board board, JButton[][] buttons) {
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				Cell cell = cells[x][y];
				JButton button = buttons[x][y];

				// רק משבצות שעדיין מסומנות בדגל
				if (!"F".equals(cell.getContent())) {
					continue;
				}

				// אם כבר הערכנו את הדגל הזה (יש LineBorder) – לא מחשבים שוב
				if (button.getBorder() instanceof LineBorder) {
					continue;
				}

				if (cell.getMine()) {
					// ❇ דגל נכון על מוקש:
					// +1 נקודה וחושפים את המוקש
					sharedScore += 1;

					cell.setContent("M"); // עכשיו זה מוקש גלוי
					button.setIcon(gui.getIconMine()); // אייקון המוקש הרגיל
					button.setText(""); // לא צריך "F" יותר
					button.setBackground(Color.DARK_GRAY); // או כל צבע שאתה אוהב
					button.setBorder(new LineBorder(Color.GREEN, 2, true));
				} else {
					// ❌ דגל שגוי (על ריקה / מספר / הפתעה / שאלה): -3 נק'
					sharedScore -= 3;
					button.setBorder(new LineBorder(Color.RED, 2, true));
					// כאן במפורש *לא* חושפים את המשבצת – רק מסמנים שהדגל היה שגוי
					// (החוקים שלך לא דורשים לחשוף במקרה הזה)
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {

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

	private void applyQuestionOutcome(Difficulty gameDiff, QuestionDifficulty qDiff, boolean correct, Board board) {

		int activationCost = getActivationCost(gameDiff);

		int deltaPts = -activationCost; // משלמים עלות הפעלה
		int deltaLives = 0;
		boolean revealMine = false;
		boolean reveal3x3 = false;

		boolean pickFirst = rng.nextBoolean(); // ל־OR בטבלה

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

// הודעת סיכום לשאלה
		StringBuilder msg = new StringBuilder();
		msg.append("Question result (").append(gameDiff.name()).append(" game, ").append(qDiff.name())
				.append(" question)\n\n");

		msg.append(correct ? "✅ Correct answer!\n" : "❌ Wrong answer.\n");

		msg.append("Activation cost: -").append(activationCost).append(" pts\n");

		int effectPts = deltaPts + activationCost; // רק האפקט מעבר לעלות
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
	 private void clampLives() {
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
	 
	  private void revealRandomMine(Board board) {
	        Cell[][] c = board.getCells();
	        JButton[][] btns = (board == boardA) ? gui.getButtonsA() : gui.getButtonsB();

	        for (int x = 0; x < board.getCols(); x++) {
	            for (int y = 0; y < board.getRows(); y++) {
	                if (c[x][y].getMine() && c[x][y].getContent().equals("")) {

	                    c[x][y].setContent("M");
	                    JButton btn = btns[x][y];
	                    btn.setIcon(gui.getIconMine());
	                    btn.setBackground(Color.DARK_GRAY);
	                    return;
	                }
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
