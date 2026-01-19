package view;

import javax.swing.*;
import java.awt.*;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import controller.Game;
import model.Board;
import model.Cell;
import model.Difficulty;
import model.SpecialBoxType;

public class MineSweeper extends JFrame implements GameObserver {
    private static final long serialVersionUID = 1L;

    private JPanel boardPanelA, boardPanelB;
    private JButton[][] buttonsA, buttonsB;

    private JLabel minesLeftALabel;
    private JLabel minesLeftBLabel;
    private JToggleButton flagToggle;
    private JLabel flagLabel;
    private JButton helpButton;

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
    private Icon legendMineIcon;
    private Icon legendQuestionIcon;
    private Icon legendBoxIcon;
    private Icon legendFlagIcon;
    private Icon legendHeartIcon;
    private JLabel legendHeartsCountLabel;
    private JButton hintMineButton;
    private JLabel timerLabel;
    private javax.swing.Timer swingTimer;
    private int elapsedSeconds = 0;
    private Icon legendDiceIcon;
    private JLabel legendDiceCountLabel;
    private JButton openMineButton;

    private final String FRAME_TITLE = "🎮 Minesweeper";
    private int FRAME_WIDTH = 1300;
    private int FRAME_HEIGHT = 750;

    public final Color DARK_NAVY = new Color(8, 22, 30); // Main background
    public final Color BOARD_BG = new Color(13, 42, 56);

    // colors come from PlayerSetupView
    public final Color BOARD_BG_A = PlayerSetupView.getPlayer1BoardColorChoice();
    public final Color BOARD_BG_B = PlayerSetupView.getPlayer2BoardColorChoice();

    // closed cells are derived from board colors (as you had)
    public final Color CELL_HIDDEN_A = PlayerSetupView.getPlayer1BoardColorChoice().darker();
    public final Color CELL_HIDDEN_B = PlayerSetupView.getPlayer2BoardColorChoice().darker();

    // revealed normal cell background
    public final Color CELL_REVEALED = new Color(238, 246, 248);

    public final Color CELL_HOVER = new Color(19, 104, 126);

    // Softer pastel highlights
    public final Color Q_HIGHLIGHT = new Color(255, 236, 179);
    public final Color S_HIGHLIGHT = new Color(187, 222, 251); // pastel blue

    public final Color SUCCESS_COLOR = new Color(0, 191, 165);
    public final Color TEXT_WHITE = new Color(245, 245, 245);
    public final Color TEXT_GRAY = new Color(178, 190, 195);

    private final Color NUM_BLUE = new Color(79, 195, 247);
    private final Color NUM_GREEN = new Color(102, 187, 106);
    private final Color NUM_RED = new Color(239, 83, 80);
    private final Color NUM_PURPLE = new Color(171, 71, 188);

    // mine reveal styling
    private final Color MINE_BG = new Color(183, 28, 28); // soft deep red
    private final Color MINE_BORDER = new Color(120, 20, 30); // darker red border

    // borders for Q/S cells
    private final Color Q_BORDER = new Color(255, 193, 7); // amber border
    private final Color S_BORDER = new Color(66, 165, 245); // blue border
    private JLabel legendMinesCountLabel;
    private JLabel legendQuestionsCountLabel;
    private JLabel legendSurprisesCountLabel;

    private Icon redMine, mine, tile, smallMineIcon;

    private String activeBoard = "A";
    private JPanel boardCardA, boardCardB;

    private Difficulty currentDifficulty;

    private String player1Name;
    private String player2Name;

    // ======= UI SIZE TUNING (sizes only) =======
    private static final int CARD_VGAP = 4; // gap between header and board in card

    private static final int HEADER_PAD_TOP = 8;
    private static final int HEADER_PAD_LR = 12;
    private static final int HEADER_PAD_BOTTOM = 6;

    private static final int BOARD_PAD = 8; // inner padding around grid
    private static final int GRID_GAP = 3;  // cell gap in GridLayout

    private static final int STATUS_PAD_TOP = 10;
    private static final int STATUS_PAD_LR = 18;
    private static final int STATUS_PAD_BOTTOM = 10;
    private static final int STATUS_HGAP = 16;

    private static final int FONT_TITLE = 18;
    private static final int FONT_MINES = 14;
    private static final int FONT_TURN = 13;

    private static final int TURN_PAD_V = 3;
    private static final int TURN_PAD_H = 10;

    private static final int FONT_STATUS = 15;
    private static final int FONT_SCORE = 20;
    private static final int FONT_HEARTS = 18;

    private static final int CELL_SIZE = 46; // was 50

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
        loadLegendIcons();

        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setTitle(FRAME_TITLE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBackground(DARK_NAVY);
        mainContainer.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel headerPanel = createHeaderPanel(); // Minesweeper + Flag mode only
        JPanel keysPanel = createKeysLegendPanel(); // "KEYS" + legend row

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        topWrapper.add(headerPanel);
        topWrapper.add(Box.createVerticalStrut(8)); // small gap
        topWrapper.add(keysPanel);
        JPanel hintPanel = createHintPanel();
        topWrapper.add(hintPanel);

        mainContainer.add(topWrapper, BorderLayout.NORTH);

        JPanel boardsContainer = createBoardsPanel();
        JPanel statusContainer = createBottomStatusPanel();

        JPanel centerWrapper = new JPanel(new BorderLayout(20, 0));
        centerWrapper.setOpaque(false);

        centerWrapper.add(boardsContainer, BorderLayout.CENTER);

        mainContainer.add(centerWrapper, BorderLayout.CENTER);
        mainContainer.add(statusContainer, BorderLayout.SOUTH);

        setContentPane(mainContainer);

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/mine.png")));
        } catch (Exception e) {
        }
    }

    private void loadLegendIcons() {
        legendMineIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/redmine.png")), 22, 22);
        legendQuestionIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/questionMark.png")), 22, 22);
        legendBoxIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/BOX.png")), 22, 22);
        legendFlagIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/flag.png")), 22, 22);
        legendHeartIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/heart.png")), 22, 22);
        legendDiceIcon = resizeIcon(new ImageIcon(getClass().getResource("/resources/dice.png")), 22, 22);
    }

    // ---------- color helpers (UI only) ----------
    private Color darken(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.max(0, Math.round(c.getRed() * (1f - factor)));
        int g = Math.max(0, Math.round(c.getGreen() * (1f - factor)));
        int b = Math.max(0, Math.round(c.getBlue() * (1f - factor)));
        return new Color(r, g, b);
    }

    private Color lighten(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.min(255, Math.round(c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, Math.round(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, Math.round(c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b);
    }

    private boolean isDark(Color c) {
        // perceived luminance
        double lum = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
        return lum < 140;
    }

    private JPanel createHorizontalLegendBar() {

        JPanel bar = new JPanel(new GridLayout(1, 6, 20, 0)); // ✅ 6 now
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));

        legendMinesCountLabel = new JLabel("--");
        legendQuestionsCountLabel = new JLabel("--");
        legendSurprisesCountLabel = new JLabel("--");
        legendHeartsCountLabel = new JLabel("--");
        legendDiceCountLabel = new JLabel("--"); // 🎲 NEW

        Font valueFont = new Font("Segoe UI", Font.BOLD, 16);

        legendMinesCountLabel.setFont(valueFont);
        legendQuestionsCountLabel.setFont(valueFont);
        legendSurprisesCountLabel.setFont(valueFont);
        legendHeartsCountLabel.setFont(valueFont);
        legendDiceCountLabel.setFont(valueFont); // 🎲 NEW

        legendMinesCountLabel.setForeground(TEXT_WHITE);
        legendQuestionsCountLabel.setForeground(TEXT_WHITE);
        legendSurprisesCountLabel.setForeground(TEXT_WHITE);
        legendHeartsCountLabel.setForeground(TEXT_WHITE);
        legendDiceCountLabel.setForeground(TEXT_WHITE); // 🎲 NEW

        bar.add(makeLegendIconStat(legendMineIcon, legendMinesCountLabel, MINE_BG));
        bar.add(makeLegendIconStat(legendQuestionIcon, legendQuestionsCountLabel, Q_HIGHLIGHT));
        bar.add(makeLegendIconStat(legendBoxIcon, legendSurprisesCountLabel, S_HIGHLIGHT));
        bar.add(makeLegendIconStat(legendHeartIcon, legendHeartsCountLabel, new Color(183, 28, 28)));

        bar.add(makeLegendIconStat(legendDiceIcon, legendDiceCountLabel, new Color(25, 38, 56))); // 🎲 NEW

        bar.add(makeLegendIconOnly(legendFlagIcon, new Color(25, 38, 56))); // 🚩 no number

        return bar;
    }

    private void updateLegendByDifficulty(Difficulty diff) {
        if (diff == null)
            return;

        switch (diff) {

            case EASY -> {
                legendMinesCountLabel.setText("10");
                legendQuestionsCountLabel.setText("6");
                legendSurprisesCountLabel.setText("2");
                legendHeartsCountLabel.setText("1");
                legendDiceCountLabel.setText("1");
            }

            case MEDIUM -> {
                legendMinesCountLabel.setText("26");
                legendQuestionsCountLabel.setText("7");
                legendSurprisesCountLabel.setText("3");
                legendHeartsCountLabel.setText("2");
                legendDiceCountLabel.setText("1");
            }

            case HARD -> {
                legendMinesCountLabel.setText("44");
                legendQuestionsCountLabel.setText("11");
                legendSurprisesCountLabel.setText("4");
                legendHeartsCountLabel.setText("2");
                legendDiceCountLabel.setText("2");
            }
        }
    }

    private JPanel makeLegendIconStat(Icon icon, JLabel valueLabel, Color badgeBg) {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inner.setOpaque(false);

        JLabel badge = new JLabel(icon, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(44, 32));
        badge.setOpaque(true);
        badge.setBackground(badgeBg);
        badge.setBorder(null); // no border if you want
        inner.add(badge);

        valueLabel.setBorder(null);
        inner.add(valueLabel);

        return inner; // ✅ no GridBag wrapper
    }

    private JPanel makeLegendIconOnly(Icon icon, Color badgeBg) {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        inner.setOpaque(false);

        JLabel badge = new JLabel(icon, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(44, 32));
        badge.setOpaque(true);
        badge.setBackground(badgeBg);
        badge.setBorder(null);
        inner.add(badge);

        return inner;
    }

    public int showDiceChoiceDialog() {

        final int[] result = {-1};

        JDialog dialog = new JDialog(this, "Dice Box", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(18, 34, 55));
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(90, 120, 170), 2, true),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel(" Dice Box");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel(
                "<html><div style='text-align:center;'>Choose what this dice becomes</div></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 190, 200));
        subtitle.setBorder(new EmptyBorder(10, 0, 14, 0));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton heartBtn = createDiceChoiceButton("  Heart", new Color(190, 60, 70), new Color(230, 90, 100));
        JButton questionBtn = createDiceChoiceButton("  Question", new Color(200, 160, 40), new Color(235, 200, 80));
        JButton surpriseBtn = createDiceChoiceButton("  Surprise", new Color(60, 120, 200), new Color(90, 160, 240));
        JButton cancelBtn = createDiceChoiceButton("Cancel", new Color(45, 55, 70), new Color(70, 85, 105));

        Dimension btnSize = new Dimension(260, 44);
        heartBtn.setMaximumSize(btnSize);
        questionBtn.setMaximumSize(btnSize);
        surpriseBtn.setMaximumSize(btnSize);
        cancelBtn.setMaximumSize(btnSize);

        heartBtn.addActionListener(e -> {
            result[0] = 0;
            dialog.dispose();
        });
        questionBtn.addActionListener(e -> {
            result[0] = 1;
            dialog.dispose();
        });
        surpriseBtn.addActionListener(e -> {
            result[0] = 2;
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> {
            result[0] = 3;
            dialog.dispose();
        });

        buttonsPanel.add(heartBtn);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(questionBtn);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(surpriseBtn);
        buttonsPanel.add(Box.createVerticalStrut(14));
        buttonsPanel.add(cancelBtn);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(subtitle);
        center.add(buttonsPanel);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        dialog.setContentPane(card);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private JButton createDiceChoiceButton(String text, Color bg, Color hoverBg) {
        JButton b = new JButton(text);

        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);

        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(bg);

        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(120, 150, 200), 1, true),
                new EmptyBorder(10, 16, 10, 16)));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });

        return b;
    }

    private JButton createDiceButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBackground(new Color(60, 90, 140));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(140, 180, 255), 2, true));
        b.setPreferredSize(new Dimension(220, 42));
        return b;
    }

    private JPanel createHeaderPanel() {
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
        hintMineButton = new JButton("🎯 HINT");
        hintMineButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hintMineButton.setForeground(Color.WHITE);
        hintMineButton.setFocusPainted(false);
        hintMineButton.setOpaque(true);
        hintMineButton.setContentAreaFilled(true);
        hintMineButton.setBackground(new Color(60, 90, 140));
        hintMineButton.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(140, 180, 255), 2, true),
                new EmptyBorder(6, 12, 6, 12)));
        hintMineButton.addActionListener(e -> {
            if (game != null) {
                game.useMineHint(); // implement in Game (controller)
            }
        });

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

        flagToggle.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 80, 80), 2, true),
                new EmptyBorder(6, 12, 6, 12)));

        flagToggle.addActionListener(e -> {
            boolean on = flagToggle.isSelected();
            if (game != null) {
                game.setFlagMode(on);
            }

            if (on) {
                flagToggle.setText("🚩 ON");
                flagToggle.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0, 200, 100), 2, true),
                        new EmptyBorder(6, 12, 6, 12)));
            } else {
                flagToggle.setText("🚩 OFF");
                flagToggle.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 80, 80), 2, true),
                        new EmptyBorder(6, 12, 6, 12)));
            }

            flagToggle.setBackground(new Color(25, 38, 56));
        });

        rightPanel.add(flagLabel);
        rightPanel.add(flagToggle);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createKeysLegendPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 30, 50));
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel keysTitle = new JLabel("KEYS");
        keysTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        keysTitle.setForeground(TEXT_GRAY);

        JPanel legendRow = createHorizontalLegendBar();

        panel.add(keysTitle, BorderLayout.WEST);
        panel.add(legendRow, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHintPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        hintMineButton = new JButton("Use Hint");
        hintMineButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hintMineButton.setForeground(Color.WHITE);
        hintMineButton.setFocusPainted(false);

        hintMineButton.setOpaque(true);
        hintMineButton.setContentAreaFilled(true);
        hintMineButton.setBackground(new Color(60, 90, 140));

        hintMineButton.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(140, 180, 255), 2, true),
                new EmptyBorder(10, 18, 10, 18)));

        ImageIcon hintIcon = new ImageIcon(getClass().getResource("/resources/search.png"));
        hintMineButton.setIcon(resizeIcon(hintIcon, 20, 20));
        hintMineButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        hintMineButton.setIconTextGap(8);

        hintMineButton.addActionListener(e -> {
            if (game != null)
                game.useMineHint();
        });
        openMineButton = new JButton(" OPEN MINE");
        openMineButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openMineButton.setForeground(Color.WHITE);
        openMineButton.setFocusPainted(false);
        openMineButton.setOpaque(true);
        openMineButton.setContentAreaFilled(true);
        openMineButton.setBackground(new Color(180, 60, 70));

        openMineButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 120, 130), 2, true),
                new EmptyBorder(10, 18, 10, 18)
        ));

        ImageIcon mineIcon = new ImageIcon(getClass().getResource("/resources/where.png"));
        openMineButton.setIcon(resizeIcon(mineIcon, 20, 20));
        openMineButton.setText(" OPEN MINE");
        openMineButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        openMineButton.setIconTextGap(8);

        openMineButton.addActionListener(e -> {
            if (game != null) {
                game.openMineWithButton();
            }
        });

        helpButton = new JButton(" USER GUIDE");
        helpButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        helpButton.setForeground(Color.WHITE);
        helpButton.setFocusPainted(false);
        helpButton.setOpaque(true);
        helpButton.setContentAreaFilled(true);
        helpButton.setBackground(new Color(25, 38, 56)); // dark

        helpButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 202, 40), 2, true),  // gold border
                new EmptyBorder(10, 18, 10, 18)
        ));

        java.net.URL helpUrl = getClass().getResource("/resources/help.png");
        if (helpUrl != null) {
            ImageIcon helpIcon = new ImageIcon(helpUrl);
            helpButton.setIcon(resizeIcon(helpIcon, 20, 20));
            helpButton.setHorizontalTextPosition(SwingConstants.RIGHT);
            helpButton.setIconTextGap(8);
        }

        helpButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            UserGuideFrame f = new UserGuideFrame();
            f.setVisible(true);
        }));

        JPanel timerCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        timerCard.setBackground(new Color(15, 30, 50));
        timerCard.setOpaque(true);
        timerCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(140, 180, 255), 2, true),
                new EmptyBorder(12, 18, 12, 18)));

        JLabel timerTitle = new JLabel("Timer:");
        timerTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timerTitle.setForeground(TEXT_GRAY);

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timerLabel.setForeground(TEXT_WHITE);

        timerCard.add(timerTitle);
        timerCard.add(timerLabel);

        panel.add(helpButton);
        panel.add(hintMineButton);
        panel.add(openMineButton);
        panel.add(timerCard);

        return panel;
    }

    private void startGameTimer() {
        stopGameTimer(); // avoid double timers
        elapsedSeconds = 0;
        updateTimerLabel();

        swingTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            updateTimerLabel();
        });
        swingTimer.start();
    }

    private void stopGameTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    private void updateTimerLabel() {
        if (timerLabel == null)
            return;
        int mins = elapsedSeconds / 60;
        int secs = elapsedSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    private JPanel createBoardsPanel() {
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 40, 0));
        boardsContainer.setOpaque(false);

        String titleA = (player1Name != null && !player1Name.isBlank()) ? player1Name : "Player 1";
        boardCardA = createBoardCard(titleA, true);

        JPanel boardAContainer = new JPanel();
        boardAContainer.setLayout(new OverlayLayout(boardAContainer));
        boardAContainer.setOpaque(false);

        boardPanelA = new JPanel(new GridLayout(rows, cols, GRID_GAP, GRID_GAP));
        boardPanelA.setBackground(BOARD_BG_A);
        boardPanelA.setBorder(new EmptyBorder(BOARD_PAD, BOARD_PAD, BOARD_PAD, BOARD_PAD));
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

        boardPanelB = new JPanel(new GridLayout(rows, cols, GRID_GAP, GRID_GAP));
        boardPanelB.setBackground(BOARD_BG_B);
        boardPanelB.setBorder(new EmptyBorder(BOARD_PAD, BOARD_PAD, BOARD_PAD, BOARD_PAD));
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
        installWindowCloseHandler();

        return boardsContainer;
    }

    public void updateMinesLeft(int minesA, int minesB) {
        if (minesLeftALabel != null) {
            minesLeftALabel.setText("  " + minesA);
        }
        if (minesLeftBLabel != null) {
            minesLeftBLabel.setText("  " + minesB);
        }
    }

    private JPanel createBoardCard(String title, boolean isBoardA) {
        JPanel card = new JPanel(new BorderLayout(0, CARD_VGAP));

        Color cardBg = isBoardA ? BOARD_BG_A : BOARD_BG_B;
        Color headerBg = darken(cardBg, 0.18f);
        Color borderColor = isBoardA ? new Color(0, 191, 165) : new Color(171, 71, 188);

        card.setBackground(cardBg);
        card.setBorder(new LineBorder(borderColor, 2, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(HEADER_PAD_TOP, HEADER_PAD_LR, HEADER_PAD_BOTTOM, HEADER_PAD_LR));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_TITLE));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel minesLabel;
        if (isBoardA) {
            minesLeftALabel = new JLabel(" --", smallMineIcon, JLabel.LEFT);
            minesLeftALabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_MINES));
            minesLeftALabel.setForeground(new Color(255, 230, 120));
            minesLabel = minesLeftALabel;
        } else {
            minesLeftBLabel = new JLabel(" --", smallMineIcon, JLabel.LEFT);
            minesLeftBLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_MINES));
            minesLeftBLabel.setForeground(new Color(255, 230, 120));
            minesLabel = minesLeftBLabel;
        }

        JLabel turnIndicator = new JLabel();
        turnIndicator.setFont(new Font("Segoe UI", Font.BOLD, FONT_TURN));
        turnIndicator.setForeground(SUCCESS_COLOR);
        turnIndicator.setOpaque(true);
        turnIndicator.setBackground(new Color(0, 191, 165, 30));
        turnIndicator.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SUCCESS_COLOR, 2, true),
                new EmptyBorder(TURN_PAD_V, TURN_PAD_H, TURN_PAD_V, TURN_PAD_H)
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

    private JPanel createBottomStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout(STATUS_HGAP, 0));
        statusPanel.setBackground(BOARD_BG);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(20, 80, 100), 2, true),
                new EmptyBorder(STATUS_PAD_TOP, STATUS_PAD_LR, STATUS_PAD_BOTTOM, STATUS_PAD_LR)
        ));

        JPanel livesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        livesPanel.setOpaque(false);

        JLabel livesTextLabel = new JLabel("Lives: ");
        livesTextLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_STATUS));
        livesTextLabel.setForeground(TEXT_WHITE);

        livesLabel = new JLabel("0 / 0");
        livesLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_STATUS));
        livesLabel.setForeground(TEXT_WHITE);

        heartsLabel = new JLabel();
        heartsLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, FONT_HEARTS));
        updateHeartsDisplay(0, 0);

        livesPanel.add(livesTextLabel);
        livesPanel.add(livesLabel);
        livesPanel.add(heartsLabel);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        scorePanel.setOpaque(false);

        JLabel scoreText = new JLabel("Score:");
        scoreText.setFont(new Font("Segoe UI", Font.BOLD, FONT_STATUS));
        scoreText.setForeground(TEXT_WHITE);

        scoreLabel = new JLabel("0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, FONT_SCORE));
        scoreLabel.setForeground(new Color(255, 202, 40));

        scorePanel.add(scoreText);
        scorePanel.add(scoreLabel);

        statusPanel.add(livesPanel, BorderLayout.WEST);
        statusPanel.add(scorePanel, BorderLayout.EAST);

        this.timePassed = 0;
        this.stopTimer = true;

        return statusPanel;
    }

    private void updateHeartsDisplay(int current, int max) {
        String fullHeart = "\u2665"; // ♥
        String emptyHeart = "\u2661"; // ♡

        StringBuilder hearts = new StringBuilder();

        for (int i = 0; i < current; i++) {
            hearts.append(fullHeart).append(" ");
        }

        for (int i = current; i < max; i++) {
            hearts.append(emptyHeart).append(" ");
        }

        if (heartsLabel != null) {
            heartsLabel.setFont(new Font("Segoe UI", Font.PLAIN, FONT_HEARTS));
            heartsLabel.setForeground(Color.RED);
            heartsLabel.setText(hearts.toString());
        }
    }

    private JButton createCell(String board, int x, int y) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));

        boolean isA = "A".equals(board);
        Color bg = isA ? CELL_HIDDEN_A : CELL_HIDDEN_B;

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(bg);

        button.setForeground(isDark(bg) ? TEXT_WHITE : new Color(20, 20, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 24));
        button.setFocusPainted(false);

        Color border = isA ? darken(BOARD_BG_A, 0.20f) : darken(BOARD_BG_B, 0.20f);
        button.setBorder(new LineBorder(border, 1, true));

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
        startGameTimer();
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
            boardCardA.setBorder(new LineBorder(aActive ? SUCCESS_COLOR : new Color(20, 80, 100), aActive ? 3 : 2, true));
        }
        if (boardCardB != null) {
            boardCardB.setBorder(new LineBorder(!aActive ? SUCCESS_COLOR : new Color(20, 80, 100), !aActive ? 3 : 2, true));
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
                buttonsA[x][y].setForeground(isDark(CELL_HIDDEN_A) ? TEXT_WHITE : new Color(20, 20, 20));
                buttonsA[x][y].setIcon(null);
                buttonsA[x][y].setBorder(new LineBorder(darken(BOARD_BG_A, 0.20f), 1, true));

                buttonsB[x][y].setText("");
                buttonsB[x][y].setBackground(CELL_HIDDEN_B);
                buttonsB[x][y].setForeground(isDark(CELL_HIDDEN_B) ? TEXT_WHITE : new Color(20, 20, 20));
                buttonsB[x][y].setIcon(null);
                buttonsB[x][y].setBorder(new LineBorder(darken(BOARD_BG_B, 0.20f), 1, true));
            }
        }
    }

    public void setButtonListeners(Game game) {
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
                    b.setBackground(MINE_BG);
                    b.setBorder(new LineBorder(MINE_BORDER, 2, true));
                    b.setIcon(getIconRedMine());
                    b.setText("");
                    continue;
                }

                SpecialBoxType special = cell.getSpecialBox();
                String content = cell.getContent();
                if (content == null)
                    content = "";

                if (special == SpecialBoxType.SURPRISE && !"USED".equals(content)) {
                    b.setIcon(null);
                    b.setBackground(S_HIGHLIGHT);
                    b.setBorder(new LineBorder(S_BORDER, 2, true));
                    b.setText("🎁");
                    b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
                    b.setForeground(new Color(30, 30, 30));
                    continue;
                }

                if (special == SpecialBoxType.QUESTION && !"USED".equals(content)) {
                    b.setIcon(null);
                    b.setBackground(Q_HIGHLIGHT);
                    b.setBorder(new LineBorder(Q_BORDER, 2, true));
                    b.setText("❓");
                    b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
                    b.setForeground(new Color(30, 30, 30));
                    continue;
                }

                int n = cell.getSurroundingMines();
                b.setBackground(CELL_REVEALED);
                b.setBorder(new LineBorder(new Color(210, 220, 230), 1, true));

                if (n == 0) {
                    b.setText("·");
                    b.setForeground(new Color(160, 170, 200, 110));
                    b.setFont(new Font("Arial", Font.BOLD, 24));
                } else {
                    b.setText(Integer.toString(n));
                    setTextColor(b);
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
        b.setBorder(new LineBorder(Q_BORDER, 2, true));
    }

    public void highlightSCell(JButton b) {
        b.setBackground(S_HIGHLIGHT);
        b.setForeground(new Color(20, 40, 60));
        b.setText("S");
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBorder(new LineBorder(S_BORDER, 2, true));
    }

    public void updateStatus(int sharedScore, int lives) {
        if (scoreLabel != null) {
            scoreLabel.setText(String.valueOf(sharedScore));
        }
        updateLives(lives, 10);
    }

    public void updateLives(int current, int max) {
        if (livesLabel != null) {
            livesLabel.setText(current + " / " + max);
        }
        updateHeartsDisplay(current, max);
    }

    public void initStatus(int lives) {
        int max = 10;

        if (livesLabel != null) {
            livesLabel.setText(lives + " / " + max);
        }

        if (scoreLabel != null) {
            scoreLabel.setText("0");
        }

        updateHeartsDisplay(lives, max);
    }

    public void setDifficulty(Difficulty diff) {
        this.currentDifficulty = diff;
        updateLegendByDifficulty(diff);
    }

    public void showVictoryDialog(int score, int timeSeconds) {
        stopGameTimer();
        String message = String.format("<html><center><h2>You win!</h2><br>" + "Team score: %d points<br>"
                + "Time: %d seconds</center></html>", score, timeSeconds);

        Object[] options = {"Main Page", "New Game", "Exit"};
        int choice = JOptionPane.showOptionDialog(this, message, "Victory!", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        handleEndDialogChoice(choice);
    }

    public void showGameOverDialog(int score) {
        stopGameTimer();
        String message = String.format(
                "<html><center><h2>Game over - no lives left</h2><br>" + "Team score: %d points</center></html>",
                score);

        Object[] options = {"Main Page", "New Game", "Exit"};
        int choice = JOptionPane.showOptionDialog(this, message, "Game Over", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        handleEndDialogChoice(choice);
    }

    private void handleEndDialogChoice(int choice) {
        if (choice == 0) {
            goToMainPage();
        } else if (choice == 1) {
            if (game != null) {
                game.newGame();
            }
        } else {
            System.exit(0);
        }
    }

    public void goToMainPage() {
        stopGameTimer();
        SwingUtilities.invokeLater(() -> {
            MainPage mainPage = new MainPage();
            mainPage.setVisible(true);

            dispose();
        });
    }

    public void showSurpriseDialog(boolean isBonus) {
        String text;
        if (isBonus) {
            text = "You hit a surprise\nThis one is a BONUS ";
        } else {
            text = "You hit a surprise\nThis one is a PENALTY ";
        }

        JOptionPane.showMessageDialog(this, text, "Surprise Box", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showNoMoreQuestionsDialog() {
        JOptionPane.showMessageDialog(this, "No more questions available.", "Question Box",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public Object askQuestion(model.Question q) {
        String[] options = q.getOptions().toArray(new String[0]);

        while (true) {
            Object answer = JOptionPane.showInputDialog(this, q.getText(), "Question Box", JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (answer == null) {
                JOptionPane.showMessageDialog(this, "You must choose an answer to continue.", "Question Box",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }

            return answer;
        }
    }

    public void setHintEnabled(boolean enabled) {
        if (hintMineButton != null) {
            hintMineButton.setEnabled(enabled);
            hintMineButton.setText(enabled ? " HINT" : "USED");
        }
    }

    public void circleCell(String boardTag, int x, int y) {
        JButton[][] btns = "A".equals(boardTag) ? buttonsA : buttonsB;
        if (x < 0 || y < 0 || x >= cols || y >= rows) return;

        JButton b = btns[x][y];
        Border originalBorder = b.getBorder();

        javax.swing.Timer old = (javax.swing.Timer) b.getClientProperty("circleRestoreTimer");
        if (old != null) old.stop();

        b.setBorder(new LineBorder(Color.YELLOW, 3, true));
        b.revalidate();
        b.repaint();

        if (b.getParent() != null) {
            b.getParent().revalidate();
            b.getParent().repaint();
        }

        Toolkit.getDefaultToolkit().sync();

        javax.swing.Timer t = new javax.swing.Timer(5000, e -> {
            b.setBorder(originalBorder);
            b.revalidate();
            b.repaint();

            if (b.getParent() != null) {
                b.getParent().revalidate();
                b.getParent().repaint();
            }

            Toolkit.getDefaultToolkit().sync();
            ((javax.swing.Timer) e.getSource()).stop();
            b.putClientProperty("circleRestoreTimer", null);
        });

        b.putClientProperty("circleRestoreTimer", t);
        t.start();
    }

    public void showCorrectAnswerDialog() {
        JOptionPane.showMessageDialog(this, "Correct answer", "Correct", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWrongAnswerDialog() {
        JOptionPane.showMessageDialog(this, "Wrong answer", "Incorrect", JOptionPane.ERROR_MESSAGE);
    }

    public void showMineHitDialog() {
        JOptionPane.showMessageDialog(this, "Boom You hit a mine.\n1 life lost.", "Mine", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void onStatusChanged(int score, int lives) {
        updateStatus(score, lives);
    }

    public void stopTimerUI() {
        stopGameTimer();
    }

    public void setOpenMineEnabled(boolean enabled) {
        if (openMineButton != null) {
            openMineButton.setEnabled(enabled);
            openMineButton.setText(enabled ? " OPEN MINE" : "USED");
        }
    }

    private void installWindowCloseHandler() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                Object[] options = {"Continue", "Main Page", "Exit"};

                int choice = JOptionPane.showOptionDialog(MineSweeper.this, "What would you like to do?", "Exit Game",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                switch (choice) {
                    case 0 -> {
                        // Continue → do nothing
                    }
                    case 1 -> {
                        if (game != null) {
                            game.logQuit();
                        }
                        stopGameTimer();
                        goToMainPage();
                        dispose();
                    }

                    case 2 -> {
                        if (game != null) {
                            game.logQuit();
                        }
                        stopGameTimer();
                        System.exit(0);
                    }

                    default -> {
                        // Dialog closed → do nothing
                    }
                }
            }
        });
    }
}