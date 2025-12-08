package view;

import controller.Game;
import model.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PlayerSetupView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField player1Field;
    private JTextField player2Field;

    private JPanel easyCard;
    private JPanel mediumCard;
    private JPanel hardCard;

    private JLabel easyLabel;
    private JLabel mediumLabel;
    private JLabel hardLabel;

    private JButton startButton;
    private JButton backButton;

    private Difficulty selectedDifficulty = Difficulty.EASY;

    // Palette 
    private final Color bgMain       = new Color(22, 73, 63);
    private final Color cardBgDark   = new Color(34, 92, 82);
    private final Color cardBgLight  = new Color(233, 220, 199);
    private final Color accentGreen  = new Color(7, 66, 57);
    private final Color accentBorder = new Color(5, 48, 41);
    private final Color textPrimary  = new Color(255, 255, 255);

    // Board color options
    private static final Color[] BOARD_COLORS = {
            new Color(5, 70, 94),
            new Color(32, 26, 73),
            new Color(74, 20, 140)
    };
    private static final String[] BOARD_COLOR_NAMES = {
            "Teal", "Purple", "Deep Blue"
    };

    private int p1ColorIndex = 0;
    private int p2ColorIndex = 1;
    private JButton[] p1ColorButtons;
    private JButton[] p2ColorButtons;

    // Values that MineSweeper or Game can read
    public static Color player1BoardColorChoice = BOARD_COLORS[0];
    public static Color player2BoardColorChoice = BOARD_COLORS[1];

    private Image backgroundImage;

    public PlayerSetupView() {
        loadBackgroundImage();
        initUI();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon(
                    getClass().getResource("/resources/setupBackground.png")
            ).getImage();
        } catch (Exception e) {
            System.out.println("Warning: Could not load /resources/setupBackground.png");
            backgroundImage = null;
        }
    }

    private void initUI() {

        setTitle("Minesweeper - Player Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(bgMain);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        setContentPane(mainPanel);

        // ===== Header =====
        JLabel titleLabel = new JLabel("Enter Players Names And Choose Difficulty", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(textPrimary);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ===== Center: glass container =====
        JPanel centerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 30;
                int w = getWidth();
                int h = getHeight();

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 80),
                        0, h, new Color(255, 255, 255, 20)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc - 4, arc - 4);

                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ===== Top-left back button =====
        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topButtonsPanel.setOpaque(false);

        ImageIcon homeIcon = new ImageIcon(getClass().getResource("/resources/home.png"));
        Image scaledHome = homeIcon.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
        homeIcon = new ImageIcon(scaledHome);

        backButton = new RoundedButton("  Main Menu");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        backButton.setBackground(accentGreen);
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(180, 45));
        backButton.setIcon(homeIcon);
        backButton.setHorizontalAlignment(SwingConstants.LEFT);
        backButton.setIconTextGap(10);

        topButtonsPanel.add(backButton);
        centerPanel.add(topButtonsPanel, BorderLayout.NORTH);

        // ===== Center content (players + difficulty) =====
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        // --- two cards for players ---
        JPanel playersRow = new JPanel();
        playersRow.setOpaque(false);
        playersRow.setLayout(new BoxLayout(playersRow, BoxLayout.X_AXIS));

        JPanel player1Card = createPlayerCard("Player 1", true);
        JPanel player2Card = createPlayerCard("Player 2", false);

        playersRow.add(player1Card);
        playersRow.add(Box.createHorizontalStrut(40));
        playersRow.add(player2Card);

        contentPanel.add(playersRow);
        contentPanel.add(Box.createVerticalStrut(35));

        // --- difficulty label ---
        JLabel difficultyLabel = new JLabel("Choose difficulty level:");
        difficultyLabel.setForeground(textPrimary);
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(difficultyLabel);
        contentPanel.add(Box.createVerticalStrut(12));

        // --- difficulty cards row ---
        JPanel difficultyCardsPanel = new JPanel();
        difficultyCardsPanel.setOpaque(false);
        difficultyCardsPanel.setLayout(new BoxLayout(difficultyCardsPanel, BoxLayout.X_AXIS));

        easyCard   = createDifficultyCard("Easy", Difficulty.EASY);
        mediumCard = createDifficultyCard("Medium", Difficulty.MEDIUM);
        hardCard   = createDifficultyCard("Hard", Difficulty.HARD);

        difficultyCardsPanel.add(easyCard);
        difficultyCardsPanel.add(Box.createHorizontalStrut(25));
        difficultyCardsPanel.add(mediumCard);
        difficultyCardsPanel.add(Box.createHorizontalStrut(25));
        difficultyCardsPanel.add(hardCard);

        difficultyCardsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(difficultyCardsPanel);

        // ===== Bottom: Start Game button =====
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonsPanel.setOpaque(false);

        startButton = new RoundedButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startButton.setBackground(accentGreen);
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(160, 50));

        buttonsPanel.add(startButton);
        centerPanel.add(buttonsPanel, BorderLayout.SOUTH);

        attachListeners();
        updateDifficultySelectionUI();

        getRootPane().setDefaultButton(startButton);
        setLocationRelativeTo(null);
    }

    // ===== Player card =====
    private JPanel createPlayerCard(String title, boolean isPlayer1) {

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 25;
                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 6, w - 2, h - 2, arc, arc);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 110),
                        0, h, new Color(255, 255, 255, 40)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 4, h - 6, arc, arc);

                g2.setColor(new Color(255, 255, 255, 160));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(2, 2, w - 8, h - 10, arc - 4, arc - 4);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(360, 200));
        card.setMaximumSize(new Dimension(360, 200));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 40, 40));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Enter your name");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hintLabel.setForeground(new Color(90, 90, 90));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(hintLabel);

        RoundedTextField nameField = new RoundedTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameField.setBackground(cardBgLight);
        nameField.setForeground(Color.DARK_GRAY);
        nameField.setPreferredSize(new Dimension(280, 36));
        nameField.setMaximumSize(new Dimension(280, 36));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetFieldBorder(nameField);
        installFocusGlow(nameField);

        if (isPlayer1) {
            player1Field = nameField;
        } else {
            player2Field = nameField;
        }

        JLabel colorLabel = new JLabel("Choose Board color:");
        colorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        colorLabel.setForeground(new Color(60, 60, 60));
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel colorsPanel = createColorButtonsPanel(isPlayer1);
        colorsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(titlePanel);
        inner.add(Box.createVerticalStrut(10));
        inner.add(nameField);
        inner.add(Box.createVerticalStrut(12));
        inner.add(colorLabel);
        inner.add(Box.createVerticalStrut(6));
        inner.add(colorsPanel);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createColorButtonsPanel(boolean isPlayer1) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton[] buttons = new JButton[BOARD_COLORS.length];
        int defaultIndex = isPlayer1 ? p1ColorIndex : p2ColorIndex;

        for (int i = 0; i < BOARD_COLORS.length; i++) {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(26, 26));
            b.setBackground(BOARD_COLORS[i]);
            b.setFocusPainted(false);
            b.setContentAreaFilled(true);
            b.setOpaque(true);
            b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
            b.setToolTipText(BOARD_COLOR_NAMES[i]);

            final int index = i;
            b.addActionListener(e -> {
                if (isPlayer1) {
                    if (index == p2ColorIndex) {
                        JOptionPane.showMessageDialog(
                                PlayerSetupView.this,
                                "Player 2 already uses this color.\nPlease choose a different one.",
                                "Color already in use",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                    p1ColorIndex = index;
                    updateColorButtonSelection(p1ColorButtons, p1ColorIndex);
                } else {
                    if (index == p1ColorIndex) {
                        JOptionPane.showMessageDialog(
                                PlayerSetupView.this,
                                "Player 1 already uses this color.\nPlease choose a different one.",
                                "Color already in use",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                    p2ColorIndex = index;
                    updateColorButtonSelection(p2ColorButtons, p2ColorIndex);
                }
            });

            buttons[i] = b;
            panel.add(b);
        }

        if (isPlayer1) {
            p1ColorButtons = buttons;
            updateColorButtonSelection(p1ColorButtons, defaultIndex);
        } else {
            p2ColorButtons = buttons;
            updateColorButtonSelection(p2ColorButtons, defaultIndex);
        }

        return panel;
    }

    private void updateColorButtonSelection(JButton[] buttons, int selectedIndex) {
        if (buttons == null) return;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            buttons[i].setBorder(BorderFactory.createLineBorder(
                    Color.WHITE,
                    (i == selectedIndex) ? 3 : 1,
                    true
            ));
        }
    }

    private void resetFieldBorder(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(accentBorder, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void markFieldAsError(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Color.RED, 2, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void installFocusGlow(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new javax.swing.border.LineBorder(accentBorder.brighter(), 2, true),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                resetFieldBorder(field);
            }
        });
    }

    // ===== Difficulty cards =====
    private JPanel createDifficultyCard(String title, Difficulty difficulty) {

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 20;
                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 6, w - 2, h - 2, arc, arc);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, w - 4, h - 6, arc, arc);

                g2.setColor(getForeground());
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, w - 8, h - 10, arc - 4, arc - 4);

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(220, 105));
        card.setMaximumSize(new Dimension(220, 105));
        card.setMinimumSize(new Dimension(220, 105));
        card.setBackground(cardBgDark);
        card.setForeground(new Color(40, 44, 80));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 35));
        textPanel.setOpaque(false);

        String gridText = switch (difficulty) {
            case EASY -> "9×9";
            case MEDIUM -> "13×13";
            case HARD -> "16×16";
        };

        JLabel label = new JLabel(title + "   " + gridText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(new Color(220, 220, 220));

        textPanel.add(label);
        card.add(textPanel, BorderLayout.CENTER);

        switch (difficulty) {
            case EASY -> easyLabel = label;
            case MEDIUM -> mediumLabel = label;
            case HARD -> hardLabel = label;
        }

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDifficulty = difficulty;
                updateDifficultySelectionUI();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (difficulty != selectedDifficulty) {
                    card.setBackground(cardBgDark.brighter());
                    card.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (difficulty != selectedDifficulty) {
                    card.setBackground(cardBgDark);
                    card.repaint();
                }
            }
        });

        return card;
    }

    private void updateDifficultySelectionUI() {
        styleCard(easyCard, easyLabel, selectedDifficulty == Difficulty.EASY);
        styleCard(mediumCard, mediumLabel, selectedDifficulty == Difficulty.MEDIUM);
        styleCard(hardCard, hardLabel, selectedDifficulty == Difficulty.HARD);
    }

    private void styleCard(JPanel card, JLabel label, boolean selected) {
        if (card == null) return;

        if (selected) {
            card.setBackground(cardBgDark.brighter());
            card.setForeground(new Color(255, 223, 128));
            if (label != null) label.setForeground(new Color(255, 246, 210));
        } else {
            card.setBackground(cardBgDark);
            card.setForeground(new Color(40, 44, 80));
            if (label != null) label.setForeground(new Color(220, 220, 220));
        }
        card.repaint();
    }

    // ===== Listeners / logic =====
    private void attachListeners() {
        startButton.addActionListener(e -> onStartGame());
        backButton.addActionListener(e -> onBackToMainMenu());
    }

    private void onStartGame() {
        String p1Name = player1Field.getText().trim();
        String p2Name = player2Field.getText().trim();

        resetFieldBorder(player1Field);
        resetFieldBorder(player2Field);

        boolean valid = true;

        if (p1Name.isEmpty()) {
            markFieldAsError(player1Field);
            valid = false;
        }
        if (p2Name.isEmpty()) {
            markFieldAsError(player2Field);
            valid = false;
        }

        if (!valid) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter names for both players before starting the game.",
                    "Missing Player Name",
                    JOptionPane.WARNING_MESSAGE
            );
            if (p1Name.isEmpty()) {
                player1Field.requestFocus();
            } else {
                player2Field.requestFocus();
            }
            return;
        }

        Difficulty difficulty = getSelectedDifficulty();
        if (difficulty == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a difficulty level.",
                    "Missing Difficulty",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Save chosen board colors so MineSweeper can use them
        player1BoardColorChoice = BOARD_COLORS[p1ColorIndex];
        player2BoardColorChoice = BOARD_COLORS[p2ColorIndex];

        new Game(difficulty, p1Name, p2Name);
        dispose();
    }

    private void onBackToMainMenu() {
        MainPage mainPage = new MainPage();
        mainPage.setVisible(true);
        dispose();
    }

    private Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }

    // ===== Rounded button with hover glow =====
    private static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;

        private Color hoverColor;
        private boolean hovered = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setFocusPainted(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            if (bg != null) {
                hoverColor = bg.brighter();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 30;
            Color bg = hovered && hoverColor != null ? hoverColor : getBackground();

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            if (hovered) {
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc - 4, arc - 4);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===== Rounded text field =====
    private static class RoundedTextField extends JTextField {
        private static final long serialVersionUID = 1L;

        public RoundedTextField(int columns) {
            super(columns);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 20;
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Optional getters if you want to read colors from outside
    public static Color getPlayer1BoardColorChoice() {
        return player1BoardColorChoice;
    }

    public static Color getPlayer2BoardColorChoice() {
        return player2BoardColorChoice;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlayerSetupView view = new PlayerSetupView();
            view.setVisible(true);
        });
    }
}
