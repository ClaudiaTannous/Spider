package view;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import controller.Game;
import model.Board;
import model.Cell;
import model.Difficulty;
import model.SpecialBoxType;

public class MineSweeper extends JFrame {
    private static final long serialVersionUID = 1L;

    private JPanel boardPanelA, boardPanelB;
    private JButton[][] buttonsA, buttonsB;

    private JLabel minesLeftALabel;
    private JLabel minesLeftBLabel;
    private JToggleButton flagToggle;
    private JLabel flagLabel;

    private int rows;
    private int cols;
    private Game game;

    private JLabel minesLabel;
    private int mines;

    private JLabel timePassedLabel;
    private Thread timer;
    private int timePassed;
    private boolean stopTimer;

    private JLabel livesLabel;
    private JLabel heartsLabel;
    private JLabel scoreLabel;
    private JLabel turnIndicatorA;
    private JLabel turnIndicatorB;
    private JPanel overlayA;
    private JPanel overlayB;

    private final String FRAME_TITLE = "🎮 Minesweeper";
    private int FRAME_WIDTH = 1400;
    private int FRAME_HEIGHT = 900;

    public final Color DARK_NAVY = new Color(8, 22, 30); // Main background

    public final Color BOARD_BG = new Color(13, 42, 56);

    // colors come from PlayerSetupView 
    public final Color BOARD_BG_A = PlayerSetupView.getPlayer1BoardColorChoice();
    public final Color BOARD_BG_B = PlayerSetupView.getPlayer2BoardColorChoice();

    public final Color CELL_HIDDEN_A   = PlayerSetupView.getPlayer1BoardColorChoice().darker();
    public final Color CELL_HIDDEN_B   = PlayerSetupView.getPlayer2BoardColorChoice().darker();
    public final Color CELL_REVEALED   = new Color(230, 245, 248);
    public final Color CELL_HOVER      = new Color(19, 104, 126);
    public final Color Q_HIGHLIGHT     = new Color(255, 183, 77);
    public final Color S_HIGHLIGHT     = new Color(129, 212, 250);

    public final Color SUCCESS_COLOR = new Color(0, 191, 165);
    public final Color TEXT_WHITE = new Color(245, 245, 245);
    public final Color TEXT_GRAY = new Color(178, 190, 195);

    private final Color NUM_BLUE = new Color(79, 195, 247);
    private final Color NUM_GREEN = new Color(102, 187, 106);
    private final Color NUM_RED = new Color(239, 83, 80);
    private final Color NUM_PURPLE = new Color(171, 71, 188);

    private Icon redMine, mine, tile, smallMineIcon;

    private String activeBoard = "A";
    private JPanel boardCardA, boardCardB;

    private Difficulty currentDifficulty;

    private String player1Name;
    private String player2Name;

    public MineSweeper() {
        this(null, 9, 9, 10, "Player 1", "Player 2");
    }

    public MineSweeper(Game game, int r, int c, int m, String player1Name, String player2Name) {
        this.game = game;

        this.rows = (r <= 0) ? 9 : r;
        this.cols = (c <= 0) ? 9 : c;
        this.mines = m;

        this.player1Name = (player1Name == null || player1Name.isBlank()) ? "Player 1" : player1Name;
        this.player2Name = (player2Name == null || player2Name.isBlank()) ? "Player 2" : player2Name;

        buttonsA = new JButton[cols][rows];
        buttonsB = new JButton[cols][rows];

        setIcons();

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setTitle(FRAME_TITLE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBackground(DARK_NAVY);
        mainContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel topBar = createTopBar();
        mainContainer.add(topBar, BorderLayout.NORTH);

        JPanel boardsContainer = createBoardsPanel();
        JPanel statusContainer = createBottomStatusPanel();

        mainContainer.add(boardsContainer, BorderLayout.CENTER);
        mainContainer.add(statusContainer, BorderLayout.SOUTH);

        setContentPane(mainContainer);

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/mine.png")));
        } catch (Exception e) {
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 30, 50));
        topBar.setBorder(new EmptyBorder(5, 15, 5, 15));

        JLabel titleLabel = new JLabel("Minesweeper");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        flagLabel = new JLabel("FLAG MODE");
        flagLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        flagLabel.setForeground(TEXT_WHITE);

        flagToggle = new JToggleButton("🚩 OFF");
        flagToggle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        flagToggle.setForeground(Color.WHITE);

        flagToggle.setOpaque(true);
        flagToggle.setContentAreaFilled(false);
        flagToggle.setFocusPainted(false);
        flagToggle.setBackground(new Color(25, 38, 56));

        flagToggle.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 80, 80), 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        flagToggle.addActionListener(e -> {
            boolean on = flagToggle.isSelected();
            if (game != null) {
                game.setFlagMode(on);
            }

            if (on) {
                flagToggle.setText("🚩 ON");
                flagToggle.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(0, 200, 100), 2, true),
                        new EmptyBorder(6, 12, 6, 12)
                ));
            } else {
                flagToggle.setText("🚩 OFF");
                flagToggle.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 80, 80), 2, true),
                        new EmptyBorder(6, 12, 6, 12)
                ));
            }

            flagToggle.setBackground(new Color(25, 38, 56));
        });

        rightPanel.add(flagLabel);
        rightPanel.add(flagToggle);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createBoardsPanel() {
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 40, 0));
        boardsContainer.setOpaque(false);

        String titleA = (player1Name != null && !player1Name.isBlank()) ? player1Name : "Player 1";
        boardCardA = createBoardCard(titleA, true);

        JPanel boardAContainer = new JPanel();
        boardAContainer.setLayout(new OverlayLayout(boardAContainer));
        boardAContainer.setOpaque(false);

        boardPanelA = new JPanel(new GridLayout(rows, cols, 3, 3));
        boardPanelA.setBackground(BOARD_BG_A);
        boardPanelA.setBorder(new EmptyBorder(15, 15, 15, 15));
        boardPanelA.setAlignmentX(0.5f);
        boardPanelA.setAlignmentY(0.5f);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                buttonsA[x][y] = createCell("A", x, y);
                boardPanelA.add(buttonsA[x][y]);
            }
        }

        overlayA = new JPanel(new GridBagLayout());
        overlayA.setBackground(new Color(8, 22, 30, 200));
        overlayA.setAlignmentX(0.5f);
        overlayA.setAlignmentY(0.5f);
        overlayA.setVisible(false);

        JLabel waitingLabelA = new JLabel("WAITING...");
        waitingLabelA.setFont(new Font("Segoe UI", Font.BOLD, 32));
        waitingLabelA.setForeground(new Color(255, 255, 255, 210));
        overlayA.add(waitingLabelA);

        boardAContainer.add(overlayA);
        boardAContainer.add(boardPanelA);

        boardCardA.add(boardAContainer, BorderLayout.CENTER);

        String titleB = (player2Name != null && !player2Name.isBlank()) ? player2Name : "Player 2";
        boardCardB = createBoardCard(titleB, false);

        JPanel boardBContainer = new JPanel();
        boardBContainer.setLayout(new OverlayLayout(boardBContainer));
        boardBContainer.setOpaque(false);

        boardPanelB = new JPanel(new GridLayout(rows, cols, 3, 3));
        boardPanelB.setBackground(BOARD_BG_B);
        boardPanelB.setBorder(new EmptyBorder(15, 15, 15, 15));
        boardPanelB.setAlignmentX(0.5f);
        boardPanelB.setAlignmentY(0.5f);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                buttonsB[x][y] = createCell("B", x, y);
                boardPanelB.add(buttonsB[x][y]);
            }
        }

        overlayB = new JPanel(new GridBagLayout());
        overlayB.setBackground(new Color(8, 22, 30, 200));
        overlayB.setAlignmentX(0.5f);
        overlayB.setAlignmentY(0.5f);
        overlayB.setVisible(false);

        JLabel waitingLabelB = new JLabel("WAITING...");
        waitingLabelB.setFont(new Font("Segoe UI", Font.BOLD, 32));
        waitingLabelB.setForeground(new Color(255, 255, 255, 210));
        overlayB.add(waitingLabelB);

        boardBContainer.add(overlayB);
        boardBContainer.add(boardPanelB);

        boardCardB.add(boardBContainer, BorderLayout.CENTER);

        boardsContainer.add(boardCardA);
        boardsContainer.add(boardCardB);

        return boardsContainer;
    }

    //updates the number of mines left to reveal on the boards
    public void updateMinesLeft(int minesA, int minesB) {
        if (minesLeftALabel != null) {
            minesLeftALabel.setText("  " + minesA);
        }
        if (minesLeftBLabel != null) {
            minesLeftBLabel.setText("  " + minesB);
        }
    }

    private JPanel createBoardCard(String title, boolean isBoardA) {
        JPanel card = new JPanel(new BorderLayout(0, 10));

        Color cardBg = isBoardA ? BOARD_BG_A : BOARD_BG_B;
        Color borderColor = isBoardA ? new Color(0, 191, 165) : new Color(171, 71, 188);

        card.setBackground(cardBg);
        card.setBorder(new LineBorder(borderColor, 2, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(cardBg);
        header.setBorder(new EmptyBorder(15, 15, 10, 15));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel minesLabel;
        if (isBoardA) {
            minesLeftALabel = new JLabel(" --", smallMineIcon, JLabel.LEFT);
            minesLeftALabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            minesLeftALabel.setForeground(new Color(255, 230, 120));
            minesLabel = minesLeftALabel;
        } else {
            minesLeftBLabel = new JLabel(" --", smallMineIcon, JLabel.LEFT);
            minesLeftBLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            minesLeftBLabel.setForeground(new Color(255, 230, 120));
            minesLabel = minesLeftBLabel;
        }

        JLabel turnIndicator = new JLabel();
        turnIndicator.setFont(new Font("Segoe UI", Font.BOLD, 15));
        turnIndicator.setForeground(SUCCESS_COLOR);
        turnIndicator.setOpaque(true);
        turnIndicator.setBackground(new Color(0, 191, 165, 30));
        turnIndicator.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SUCCESS_COLOR, 2, true),
                new EmptyBorder(5, 15, 5, 15)
        ));
        turnIndicator.setVisible(false);

        if (isBoardA) {
            turnIndicatorA = turnIndicator;
        } else {
            turnIndicatorB = turnIndicator;
        }

        leftPanel.add(titleLabel);
        leftPanel.add(turnIndicator);

        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        iconsPanel.setOpaque(false);
        iconsPanel.add(minesLabel);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(iconsPanel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        return card;
    }

    private JLabel createIconLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(color.darker());
        label.setOpaque(true);
        label.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        label.setBorder(
                BorderFactory.createCompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(5, 10, 5, 10)));

        return label;
    }

    private JPanel createBottomStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout(30, 0));
        statusPanel.setBackground(BOARD_BG);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(20, 80, 100), 2, true),
                new EmptyBorder(20, 30, 20, 30))
        );

        JPanel livesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        livesPanel.setOpaque(false);

        JLabel livesTextLabel = new JLabel("Lives: ");
        livesTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        livesTextLabel.setForeground(TEXT_WHITE);

        livesLabel = new JLabel("0 / 0");
        livesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        livesLabel.setForeground(TEXT_WHITE);

        heartsLabel = new JLabel();
        heartsLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        updateHeartsDisplay(0, 0);

        livesPanel.add(livesTextLabel);
        livesPanel.add(livesLabel);
        livesPanel.add(heartsLabel);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        scorePanel.setOpaque(false);

        JLabel scoreText = new JLabel("Score:");
        scoreText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreText.setForeground(TEXT_WHITE);

        scoreLabel = new JLabel("0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(255, 202, 40));

        scorePanel.add(scoreText);
        scorePanel.add(scoreLabel);

        statusPanel.add(livesPanel, BorderLayout.WEST);
        statusPanel.add(scorePanel, BorderLayout.EAST);

        this.timePassed = 0;
        this.stopTimer = true;

        return statusPanel;
    }

    //updates lives display
    private void updateHeartsDisplay(int current, int max) {
        String fullHeart = "\u2665";  // ♥
        String emptyHeart = "\u2661"; // ♡

        StringBuilder hearts = new StringBuilder();

        for (int i = 0; i < current; i++) {
            hearts.append(fullHeart).append(" ");
        }

        for (int i = current; i < max; i++) {
            hearts.append(emptyHeart).append(" ");
        }

        if (heartsLabel != null) {
            heartsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            heartsLabel.setForeground(Color.RED);
            heartsLabel.setText(hearts.toString());
        }
    }

    private JButton createCell(String board, int x, int y) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(50, 50));

        boolean isA = "A".equals(board);
        button.setBackground(isA ? CELL_HIDDEN_A : CELL_HIDDEN_B);

        button.setForeground(TEXT_WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(isA ? BOARD_BG_A : BOARD_BG_B, 1, true));
        button.setName(board + ":" + x + "," + y);
        button.putClientProperty("board", board);

        return button;
    }

    public void startTimer() {
        stopTimer = false;
        timer = new Thread(() -> {
            while (!stopTimer) {
                timePassed++;
                if (timePassedLabel != null) {
                    timePassedLabel.setText(String.valueOf(timePassed));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        });
        timer.start();
    }

    public void interruptTimer() {
        stopTimer = true;
        try {
            if (timer != null)
                timer.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void resetTimer() {
        timePassed = 0;
        if (timePassedLabel != null) {
            timePassedLabel.setText("0");
        }
    }

    public void setTimePassed(int t) {
        timePassed = t;
        if (timePassedLabel != null) {
            timePassedLabel.setText(String.valueOf(t));
        }
    }

    public void initGame() {
        hideAll();
        enableBothBoards();
    }

    public void enableBothBoards() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                buttonsA[x][y].setEnabled(true);
                buttonsB[x][y].setEnabled(true);
            }
        }
    }

    public void setActiveBoard(String boardTag) {
        activeBoard = boardTag != null ? boardTag : "A";
        boolean aActive = "A".equals(activeBoard);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                buttonsA[x][y].setEnabled(aActive);
                buttonsB[x][y].setEnabled(!aActive);
            }
        }

        if (turnIndicatorA != null) {
            turnIndicatorA.setText(aActive ? "YOUR TURN" : "");
            turnIndicatorA.setVisible(aActive);
        }
        if (turnIndicatorB != null) {
            turnIndicatorB.setText(!aActive ? "YOUR TURN" : "");
            turnIndicatorB.setVisible(!aActive);
        }

        if (overlayA != null) {
            overlayA.setVisible(!aActive);
        }
        if (overlayB != null) {
            overlayB.setVisible(aActive);
        }

        if (boardCardA != null) {
            boardCardA.setBorder(
                    new LineBorder(aActive ? SUCCESS_COLOR : new Color(20, 80, 100),
                            aActive ? 3 : 2, true));
        }
        if (boardCardB != null) {
            boardCardB.setBorder(
                    new LineBorder(!aActive ? SUCCESS_COLOR : new Color(20, 80, 100),
                            !aActive ? 3 : 2, true));
        }

        boardCardA.repaint();
        boardCardB.repaint();
    }

    public String getActiveBoard() {
        return activeBoard;
    }

    public void hideAll() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                buttonsA[x][y].setText("");
                buttonsA[x][y].setBackground(CELL_HIDDEN_A);
                buttonsA[x][y].setIcon(null);

                buttonsB[x][y].setText("");
                buttonsB[x][y].setBackground(CELL_HIDDEN_B);
                buttonsB[x][y].setIcon(null);
            }
        }
    }

    public void setButtonListeners(Game game) {
        addWindowListener(game);
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                buttonsA[x][y].addMouseListener(game);
                buttonsB[x][y].addMouseListener(game);
            }
        }
    }

    public JButton[][] getButtonsA() {
        return buttonsA;
    }

    public JButton[][] getButtonsB() {
        return buttonsB;
    }

    public int getTimePassed() {
        return timePassed;
    }

    public static void setLook(String look) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    public void setMines(int m) {
        mines = m;
        if (minesLabel != null) {
            minesLabel.setText(String.valueOf(m));
        }
    }

    public void incMines() {
        setMines(++mines);
    }

    public void decMines() {
        setMines(--mines);
    }

    public int getMines() {
        return mines;
    }

    private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        if (icon == null)
            return null;
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public void setIcons() {
        int bOffset = 2, bWidth = 40, bHeight = 40;
        try {
            if (buttonsA[0][0] != null) {
                bOffset = buttonsA[0][0].getInsets().left;
                bWidth = buttonsA[0][0].getWidth();
                bHeight = buttonsA[0][0].getHeight();
            }
        } catch (Exception ignored) {
        }

        try {
            redMine = resizeIcon(new ImageIcon(getClass().getResource("/resources/redmine.png")),
                    Math.max(20, bWidth - bOffset), Math.max(20, bHeight - bOffset));
            mine = resizeIcon(new ImageIcon(getClass().getResource("/resources/mine.png")),
                    Math.max(20, bWidth - bOffset), Math.max(20, bHeight - bOffset));

            tile = resizeIcon(new ImageIcon(getClass().getResource("/resources/tile.png")),
                    Math.max(20, bWidth - bOffset), Math.max(20, bHeight - bOffset));
            smallMineIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/mine.png")), 30, 30);
        } catch (Exception e) {
            System.out.println("Warning: Could not load icons from /resources/");
        }
    }

    public void revealAllBoard(Board board, JButton[][] buttons) {
        Cell[][] cells = board.getCells();

        for (int x = 0; x < board.getCols(); x++) {
            for (int y = 0; y < board.getRows(); y++) {
                JButton b = buttons[x][y];
                Cell cell = cells[x][y];

                b.setEnabled(false);

                if (cell.getMine()) {
                    b.setIcon(getIconMine());
                    b.setBackground(Color.BLACK);
                } else {
                    SpecialBoxType special = cell.getSpecialBox();
                    String content = cell.getContent();
                    if (content == null) content = "";

                    if (special == SpecialBoxType.SURPRISE && !"USED".equals(content)) {
                        b.setIcon(null);
                        b.setBackground(Color.ORANGE);
                        b.setText("🎁");
                        b.setFont(new Font("Serif", Font.BOLD, 18));
                        b.setForeground(Color.RED);
                    } else if (special == SpecialBoxType.QUESTION && !"USED".equals(content)) {
                        b.setIcon(null);
                        b.setBackground(Color.YELLOW);
                        b.setText("❓");
                        b.setFont(new Font("Serif", Font.BOLD, 18));
                        b.setForeground(Color.RED);
                    } else {
                        int n = cell.getSurroundingMines();
                        b.setBackground(CELL_REVEALED);

                        if (n == 0) {
                            b.setText("·");
                            b.setForeground(new Color(160, 170, 200, 100));
                            b.setFont(new Font("Arial", Font.BOLD, 24));
                        } else {
                            b.setText(Integer.toString(n));
                            setTextColor(b);
                        }
                    }
                }
            }
        }
    }

    public Icon getIconMine() {
        return mine;
    }

    public Icon getIconRedMine() {
        return redMine;
    }

    public Icon getIconTile() {
        return tile;
    }

    public void setTextColor(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 24));

        switch (b.getText()) {
            case "1" -> b.setForeground(NUM_BLUE);
            case "2" -> b.setForeground(NUM_GREEN);
            case "3" -> b.setForeground(NUM_RED);
            case "4" -> b.setForeground(NUM_PURPLE);
            case "5" -> b.setForeground(new Color(255, 202, 40));
            case "6" -> b.setForeground(new Color(38, 198, 218));
            case "7" -> b.setForeground(new Color(144, 164, 174));
            case "8" -> b.setForeground(new Color(176, 190, 197));
        }
    }

    public void highlightQCell(JButton b) {
        b.setBackground(Q_HIGHLIGHT);
        b.setForeground(new Color(40, 40, 40));
        b.setText("Q");
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
    }

    public void highlightSCell(JButton b) {
        b.setBackground(S_HIGHLIGHT);
        b.setForeground(new Color(20, 40, 60));
        b.setText("S");
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
    }

    public void updateStatus(int sharedScore, int lives) {
        if (scoreLabel != null) {
            scoreLabel.setText(String.valueOf(sharedScore));
        }
        int maxLives = (currentDifficulty != null) ? currentDifficulty.getLives() : lives;
        updateLives(lives, maxLives);
    }

    public void updateLives(int current, int max) {
        if (livesLabel != null) {
            livesLabel.setText(current + " / " + max);
        }
        updateHeartsDisplay(current, max);
    }

    public void initStatus(int lives) {
        if (livesLabel != null) {
            livesLabel.setText(lives + " / " + lives);
        }
        if (scoreLabel != null) {
            scoreLabel.setText("0");
        }
        updateHeartsDisplay(lives, lives);
    }

    public void setDifficulty(Difficulty diff) {
        this.currentDifficulty = diff;
    }

    public void showVictoryDialog(int score, int timeSeconds) {
        String message = String.format(
                "<html><center><h2>You win!</h2><br>" +
                        "Team score: %d points<br>" +
                        "Time: %d seconds</center></html>",
                score, timeSeconds
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Victory!",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showGameOverDialog(int score) {
        String message = String.format(
                "<html><center><h2>Game over - no lives left!</h2><br>" +
                        "Team score: %d points</center></html>",
                score
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showSurpriseDialog(boolean isBonus) {
        String text;
        if (isBonus) {
            text = "You hit a surprise!\nThis one is a BONUS 🙂";
        } else {
            text = "You hit a surprise!\nThis one is a PENALTY 😈";
        }

        JOptionPane.showMessageDialog(
                this,
                text,
                "Surprise Box",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showNoMoreQuestionsDialog() {
        JOptionPane.showMessageDialog(
                this,
                "No more questions available.",
                "Question Box",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public Object askQuestion(model.Question q) {
        String[] options = q.getOptions().toArray(new String[0]);

        while (true) {
            Object answer = JOptionPane.showInputDialog(
                    this,
                    q.getText(),
                    "Question Box",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]   // default selected
            );

            // If user closes the dialog or presses Cancel → answer == null
            if (answer == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "You must choose an answer to continue.",
                        "Question Box",
                        JOptionPane.WARNING_MESSAGE
                );
                // loop again – show the dialog once more
                continue;
            }

            // Valid selection → return it
            return answer;
        }
    }


    public void showCorrectAnswerDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Correct answer!",
                "Correct",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showWrongAnswerDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Wrong answer!",
                "Incorrect",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void showMineHitDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Boom! You hit a mine.\n1 life lost.",
                "Mine!",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
