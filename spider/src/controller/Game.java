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
	    // points used by surprise good/bad effect (±8 / ±12 / ±16)
	    private int getSurprisePoints(Difficulty diff) {
	        return switch (diff) {
	            case EASY   -> 8;
	            case MEDIUM -> 12;
	            case HARD   -> 16;
	        };
	    }

	    private int getSurprisePoints() {
	        return getSurprisePoints(currentDifficulty);
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
	 public void setFlagMode(boolean flagMode) {
	        boolean wasFlagMode = this.flagMode;
	        this.flagMode = flagMode;

	        // When leaving flag mode → evaluate all flags once
	        if (wasFlagMode && !flagMode) {
	            evaluateFlags();      // scores + borders
	            updateMineCounters(); // now remaining mines changes
	            checkGame();          // now we can also check win condition
	        }
	    }

	    public boolean isFlagMode() {
	        return flagMode;
	    }
	    
	    private void handleFlagClick(int x, int y, Board board, JButton button) {
	        Cell cell = board.getCells()[x][y];
	        String content = cell.getContent();
	        if (content == null) content = "";

	    
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
            int choice = JOptionPane.showConfirmDialog(
                    gui,
                    "Do you want to activate the surprise box?\n" +
                    "(Activation will cost " + getActivationCost() + " points.)",
                    "Activate Surprise?",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            int activationCost = getActivationCost();
            int surprisePts    = getSurprisePoints();

            boolean isBonus = rng.nextBoolean();

            int deltaPts  = -activationCost;
            int deltaLives = 0;

            if (isBonus) {
                deltaPts  += surprisePts; // +8 / +12 / +16
                deltaLives = +1;
                button.setBackground(Color.GREEN);
            } else {
                deltaPts  -= surprisePts; // -8 / -12 / -16
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
            msg.append("Surprise effect points: ")
               .append(effectPts >= 0 ? "+" : "")
               .append(effectPts)
               .append(" pts");

            if (deltaLives != 0) {
                msg.append("\nLife change: ")
                   .append(deltaLives > 0 ? "+" : "")
                   .append(deltaLives)
                   .append(" ♥");
            }

            msg.append("\n\nTotal points change: ")
               .append(deltaPts >= 0 ? "+" : "")
               .append(deltaPts)
               .append(" pts");

            msg.append("\nTotal score: ").append(sharedScore)
               .append("\nTotal lives: ").append(sharedLives);

            JOptionPane.showMessageDialog(
                    gui,
                    msg.toString(),
                    "Surprise Box",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (sharedLives <= 0) {
                gameLost();
            }
        }
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
	
	private void evaluateFlags() {
	    if (boardA != null && gui != null) {
	        evaluateFlagsOnBoard(boardA, gui.getButtonsA());
	    }
	    if (boardB != null && gui != null) {
	        evaluateFlagsOnBoard(boardB, gui.getButtonsB());
	    }

	    gui.updateStatus(sharedScore, sharedLives);
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
        if (content == null) content = "";

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

        // if cell already has content (number, USED, etc.) – only allow clicking special boxes
        if (!content.equals("")) {
            boolean isClickableSpecial =
                    (specialBox == SpecialBoxType.SURPRISE && content.equals("🎁")) ||
                    (specialBox == SpecialBoxType.QUESTION && content.equals("❓"));

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

	
