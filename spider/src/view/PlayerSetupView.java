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
    private JLabel easyTitleLabel;
    private JLabel mediumTitleLabel;
    private JLabel hardTitleLabel;
    private JLabel easySubtitleLabel;
    private JLabel mediumSubtitleLabel;
    private JLabel hardSubtitleLabel;
    private JLabel easyHeartsLabel;
    private JLabel mediumHeartsLabel;
    private JLabel hardHeartsLabel;
    private JLabel easyCheckLabel;
    private JLabel mediumCheckLabel;
    private JLabel hardCheckLabel;

    private JButton startButton;
    private JButton exitButton;
    private JButton backButton;

    private Difficulty selectedDifficulty = Difficulty.EASY;

    // Colors (palette)
    private final Color bgMain        = new Color(6, 40, 61);
    private final Color bgCard       = new Color(15, 76, 92);
    private final Color accentGreen  = new Color(46, 204, 113);   
    private final Color accentBorder = new Color(0, 120, 140);
    private final Color cardTextMain = new Color(33, 33, 33);
    private final Color cardTextSub  = new Color(120, 120, 120);
    private final Color cardBgNormal = new Color(250, 250, 252);
    private final Color textPrimary   = new Color(240, 244, 248);
    private final Color textSecondary = new Color(189, 204, 220);
    private final Color heartsRed     = new Color(200, 0, 60);

    
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
        // Window settings
        setTitle("Minesweeper - Player Setup");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));

        // Main container with background image
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

        // Header (title + subtitle)
        JLabel titleLabel = new JLabel("Minesweeper", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(textPrimary);

        JLabel subtitleLabel = new JLabel("Enter player names and choose difficulty", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(textSecondary);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center form panel
        JPanel centerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 2, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        GridBagConstraints gbc;

        // Player 1 label
        JLabel p1Label = new JLabel("Player 1 name:");
        p1Label.setForeground(textPrimary);
        p1Label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(p1Label, gbc);

        // Player 1 field
        player1Field = new JTextField();
        player1Field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        player1Field.setBackground(new Color(224, 242, 241));
        player1Field.setForeground(Color.DARK_GRAY);
        resetFieldBorder(player1Field);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(player1Field, gbc);

        // Player 2 label
        JLabel p2Label = new JLabel("Player 2 name:");
        p2Label.setForeground(textPrimary);
        p2Label.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(p2Label, gbc);

        // Player 2 field
        player2Field = new JTextField();
        player2Field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        player2Field.setBackground(new Color(224, 242, 241));
        player2Field.setForeground(Color.DARK_GRAY);
        resetFieldBorder(player2Field);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        gbc.gridx = 1;
        gbc.gridy = 1;
        centerPanel.add(player2Field, gbc);

        // Difficulty label
        JLabel difficultyLabel = new JLabel("Choose difficulty level");
        difficultyLabel.setForeground(textPrimary);
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.4;
        gbc.gridx = 0;
        gbc.gridy = 2;
        centerPanel.add(difficultyLabel, gbc);

        // Difficulty cards container
        JPanel difficultyCardsPanel = new JPanel();
        difficultyCardsPanel.setOpaque(false);
        difficultyCardsPanel.setLayout(new GridLayout(3, 1, 12, 12));

        easyCard = createDifficultyCard("Easy", "9x9 grid", Difficulty.EASY);
        mediumCard = createDifficultyCard("Medium", "13x13 grid", Difficulty.MEDIUM);
        hardCard = createDifficultyCard("Hard", "16x16 grid", Difficulty.HARD);

        difficultyCardsPanel.add(easyCard);
        difficultyCardsPanel.add(mediumCard);
        difficultyCardsPanel.add(hardCard);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.6;
        gbc.gridx = 1;
        gbc.gridy = 2;
        centerPanel.add(difficultyCardsPanel, gbc);

        // Bottom area with back button on the left and start/exit on the right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Left side: Back button
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftButtonsPanel.setOpaque(false);

        backButton = new JButton("Main Menu");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(52, 73, 94));
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(180, 40));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(44, 62, 80), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        leftButtonsPanel.add(backButton);

        // Right side: Exit + Start buttons
        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightButtonsPanel.setOpaque(false);

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        exitButton.setFocusPainted(false);
        exitButton.setBackground(new Color(189, 57, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setPreferredSize(new Dimension(120, 40));
        exitButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(244, 143, 177), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startButton.setFocusPainted(false);
        startButton.setBackground(accentGreen);
        startButton.setForeground(Color.WHITE);
        startButton.setPreferredSize(new Dimension(180, 45));
        startButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 174, 96), 1, true),
                BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));

        rightButtonsPanel.add(exitButton);
        rightButtonsPanel.add(startButton);

        bottomPanel.add(leftButtonsPanel, BorderLayout.WEST);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        attachListeners();
        updateDifficultySelectionUI(); // initial selection = EASY

        setLocationRelativeTo(null);
    }

    // Restores normal border style
    private void resetFieldBorder(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentBorder, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    // Applies red border for missing input
    private void markFieldAsError(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    // Builds hearts string according to difficulty lives
    private String buildHeartsForDifficulty(Difficulty difficulty) {
        int lives = difficulty.getLives(); // assumes Difficulty has getLives()
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lives; i++) {
            sb.append("\u2764 "); // red heart Unicode
        }
        return sb.toString();
    }

    // Creates a single difficulty card
    private JPanel createDifficultyCard(String title, String subtitle, Difficulty difficulty) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardBgNormal);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 2, true));
        card.setPreferredSize(new Dimension(260, 70));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Left text area
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(cardTextMain);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(cardTextSub);

        JLabel heartsLabel = new JLabel(buildHeartsForDifficulty(difficulty));
        heartsLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        heartsLabel.setForeground(heartsRed);

     // Create a horizontal panel for title + hearts
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        titleRow.add(titleLabel);
        titleRow.add(heartsLabel);

        textPanel.add(titleRow);
        textPanel.add(subtitleLabel);

        // Check mark on the right
        JLabel checkLabel = new JLabel("✓", SwingConstants.CENTER);
        checkLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        checkLabel.setForeground(Color.WHITE);
        checkLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));
        checkLabel.setVisible(false);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(checkLabel, BorderLayout.EAST);

        // Store references for later style updates
        switch (difficulty) {
            case EASY -> {
                easyTitleLabel = titleLabel;
                easySubtitleLabel = subtitleLabel;
                easyHeartsLabel = heartsLabel;
                easyCheckLabel = checkLabel;
            }
            case MEDIUM -> {
                mediumTitleLabel = titleLabel;
                mediumSubtitleLabel = subtitleLabel;
                mediumHeartsLabel = heartsLabel;
                mediumCheckLabel = checkLabel;
            }
            case HARD -> {
                hardTitleLabel = titleLabel;
                hardSubtitleLabel = subtitleLabel;
                hardHeartsLabel = heartsLabel;
                hardCheckLabel = checkLabel;
            }
        }

        // Mouse click listener to select difficulty
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDifficulty = difficulty;
                updateDifficultySelectionUI();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (difficulty != selectedDifficulty) {
                    card.setBackground(new Color(245, 245, 250));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (difficulty != selectedDifficulty) {
                    card.setBackground(cardBgNormal);
                }
            }
        });

        return card;
    }

    // Update cards UI according to selectedDifficulty
    private void updateDifficultySelectionUI() {
        styleCard(easyCard, easyTitleLabel, easySubtitleLabel, easyHeartsLabel, easyCheckLabel,
                selectedDifficulty == Difficulty.EASY);
        styleCard(mediumCard, mediumTitleLabel, mediumSubtitleLabel, mediumHeartsLabel, mediumCheckLabel,
                selectedDifficulty == Difficulty.MEDIUM);
        styleCard(hardCard, hardTitleLabel, hardSubtitleLabel, hardHeartsLabel, hardCheckLabel,
                selectedDifficulty == Difficulty.HARD);
    }

    private void styleCard(JPanel card,
                           JLabel titleLabel,
                           JLabel subtitleLabel,
                           JLabel heartsLabel,
                           JLabel checkLabel,
                           boolean selected) {
        if (card == null) return;

        if (selected) {
            card.setBackground(accentGreen);
            card.setBorder(BorderFactory.createLineBorder(new Color(27, 163, 84), 3, true));
            if (titleLabel != null) titleLabel.setForeground(Color.WHITE);
            if (subtitleLabel != null) subtitleLabel.setForeground(new Color(230, 255, 240));
            if (heartsLabel != null) heartsLabel.setForeground(new Color(255, 240, 240));
            if (checkLabel != null) checkLabel.setVisible(true);
        } else {
            card.setBackground(cardBgNormal);
            card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 2, true));
            if (titleLabel != null) titleLabel.setForeground(cardTextMain);
            if (subtitleLabel != null) subtitleLabel.setForeground(cardTextSub);
            if (heartsLabel != null) heartsLabel.setForeground(heartsRed);
            if (checkLabel != null) checkLabel.setVisible(false);
        }
    }

    private void attachListeners() {
        startButton.addActionListener(e -> onStartGame());
        exitButton.addActionListener(e -> System.exit(0));
        backButton.addActionListener(e -> onBackToMainMenu());
    }

    private void onStartGame() {
        String p1Name = player1Field.getText().trim();
        String p2Name = player2Field.getText().trim();

        // Reset borders before validation
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PlayerSetupView view = new PlayerSetupView();
            view.setVisible(true);
        });
    }
}
