package view;

import model.Question;
import model.SysData;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class QuestionManagerFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final SysData sysData;

    // Top buttons
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnReload;
    private JButton btnClose;

    // Cards area
    private JPanel cardsContainer;
    private JLabel lblSubtitle;

    // Data
    private List<Question> currentQuestions = new ArrayList<>();
    private int selectedIndex = -1;

    // Colors
    private static final Color BG_MAIN    = new Color(0x08, 0x13, 0x1E);
    private static final Color TEXT_MAIN  = new Color(230, 242, 255);
    private static final Color TEXT_MUTED = new Color(170, 185, 195);

    // Buttons palette
    private static final Color BTN_NEW_BG    = new Color(0xFF, 0xCF, 0x4A);
    private static final Color BTN_EDIT_BG   = new Color(0x36, 0x53, 0x69);
    private static final Color BTN_DELETE_BG = new Color(0xFF, 0x52, 0x62);
    private static final Color BTN_CLOSE_BG  = new Color(0x2B, 0x45, 0x53);

    // Background image
    private Image bgImage;

    public QuestionManagerFrame(SysData sysData) {
        super("Question Bank");
        this.sysData = sysData;

        try {
            bgImage = new ImageIcon(
                    getClass().getResource("/resources/questionsBackground.jpg")
            ).getImage();
        } catch (Exception e) {
            System.out.println("Warning: Could not load /resources/questionsBackground.jpg");
            bgImage = null;
        }

        initComponents();
        buildLayout();
        attachListeners();
        reloadQuestions();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
    }

    // ----------------------------------------------------
    // Init components
    // ----------------------------------------------------
    private void initComponents() {
        // Buttons
        btnNew    = new JButton("Add new question");
        btnEdit   = new JButton("Edit");
        btnDelete = new JButton("Delete");
        btnReload = new JButton("Reload");
        btnClose  = new JButton("Close");

        // Style buttons
        stylePrimaryButton(btnNew,   BTN_NEW_BG);
        stylePrimaryButton(btnEdit,  BTN_EDIT_BG);
        styleDangerButton(btnDelete, BTN_DELETE_BG);
        styleGhostButton(btnReload);
        styleSecondaryButton(btnClose);

        // Make side buttons the same size
        JButton[] sideButtons = { btnNew, btnDelete, btnEdit, btnClose };
        Dimension maxSize = new Dimension(0, 0);

        for (JButton b : sideButtons) {
            Dimension pref = b.getPreferredSize();
            maxSize.width  = Math.max(maxSize.width, pref.width);
            maxSize.height = Math.max(maxSize.height, pref.height);
        }
        maxSize.width  += 10;
        maxSize.height += 4;

        for (JButton b : sideButtons) {
            b.setPreferredSize(maxSize);
            b.setMinimumSize(maxSize);
            b.setMaximumSize(maxSize);
        }

        // Cards container for questions (2 columns via GridBagLayout)
        cardsContainer = new JPanel(new GridBagLayout());
        cardsContainer.setOpaque(false);
        cardsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblSubtitle = new JLabel("");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);
    }

    // ----------------------------------------------------
    // Layout
    // ----------------------------------------------------
    private void buildLayout() {
        // Root panel with background image
        JPanel root = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (bgImage != null) {
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2.setPaint(new GradientPaint(
                            0, 0, BG_MAIN,
                            getWidth(), getHeight(), BG_MAIN.darker()
                    ));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        // Title + subtitle + Reload
        JLabel lblTitle = new JLabel("Questions");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_MAIN);

        JPanel titleLeft = new JPanel();
        titleLeft.setOpaque(false);
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.add(lblTitle);
        titleLeft.add(lblSubtitle);

        JPanel titlePanel = new JPanel(new BorderLayout(5, 2));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLeft, BorderLayout.WEST);
        titlePanel.add(btnReload, BorderLayout.EAST);

        // Frosted glass wrapper for cards
        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 28;
                int w = getWidth();
                int h = getHeight();

                // Base glass tint
                Color base = new Color(5, 20, 40, 200);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Vertical highlight for glass effect
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 70),
                        0, h, new Color(255, 255, 255, 15)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Outer soft border
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Inner bright border
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, arc - 4, arc - 4);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        cardWrapper.add(scrollPane, BorderLayout.CENTER);

        // Side buttons panel (stick to bottom)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(new EmptyBorder(20, 10, 20, 0));

        // Glue pushes all buttons to bottom
        buttonsPanel.add(Box.createVerticalGlue());

        buttonsPanel.add(btnNew);
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(Box.createVerticalStrut(12));
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(btnClose);

        root.add(titlePanel, BorderLayout.NORTH);
        root.add(cardWrapper, BorderLayout.CENTER);
        root.add(buttonsPanel, BorderLayout.EAST);
    }

    // ----------------------------------------------------
    // Listeners
    // ----------------------------------------------------
    private void attachListeners() {
        btnReload.addActionListener(this::onReload);
        btnNew.addActionListener(this::onNew);
        btnEdit.addActionListener(this::onEdit);
        btnDelete.addActionListener(this::onDelete);
        btnClose.addActionListener(e -> dispose());
    }

    // ----------------------------------------------------
    // Actions
    // ----------------------------------------------------
    private void onReload(ActionEvent e) {
        reloadQuestions();
    }

    private void onNew(ActionEvent e) {
        openEditor(null);  // null = new question
    }

    private void onEdit(ActionEvent e) {
        Question q = getSelectedQuestion();
        if (q == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a question to edit.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        openEditor(q);
    }

    private void onDelete(ActionEvent e) {
        Question q = getSelectedQuestion();
        if (q == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a question to delete.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete question " + q.getId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        sysData.deleteQuestionById(q.getId());
        sysData.saveQuestionsToCsv();
        reloadQuestions();
    }

    private void openEditor(Question questionOrNull) {
        QuestionEditorDialog dialog = new QuestionEditorDialog(this, sysData, questionOrNull);
        dialog.setVisible(true);
        reloadQuestions();
    }

    private void reloadQuestions() {
        currentQuestions = new ArrayList<>(sysData.findAllQuestions());
        selectedIndex = -1;

        lblSubtitle.setText(currentQuestions.size() + " questions in bank");
        rebuildCardsUI();
    }

    // ----------------------------------------------------
    // Cards UI (2 columns)
    // ----------------------------------------------------
    private void rebuildCardsUI() {
        cardsContainer.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;

        for (int i = 0; i < currentQuestions.size(); i++) {
            Question q = currentQuestions.get(i);
            JPanel card = createQuestionCard(q, i);

            int row = i / 2;
            int col = i % 2;

            gbc.gridx = col;
            gbc.gridy = row;

            cardsContainer.add(card, gbc);
        }

        // Spacer row to push cards to the top
        gbc.gridx = 0;
        gbc.gridy = currentQuestions.size() / 2 + 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        cardsContainer.add(spacer, gbc);

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createQuestionCard(Question q, int index) {
        String id   = String.valueOf(q.getId());
        String text = q.getText();
        String diff = String.valueOf(q.getDifficulty());

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isSelected = (index == selectedIndex);

                Color baseFill   = new Color(10, 10, 15, 140);
                Color borderSoft = new Color(255, 255, 255, 25);
                Color borderHard = new Color(255, 255, 255, 220);

                if (isSelected) {
                    baseFill   = new Color(40, 80, 110, 180);
                    borderSoft = new Color(0, 255, 200, 60);
                    borderHard = new Color(0, 255, 200, 220);
                }

                g2.setColor(baseFill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(borderSoft);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.setColor(borderHard);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Top meta line: ID + Difficulty
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaPanel.setOpaque(false);

        JLabel idLabel = new JLabel("ID: " + id);
        idLabel.setForeground(TEXT_MUTED);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel diffLabel = new JLabel("Difficulty: " + diff);
        diffLabel.setForeground(new Color(255, 202, 40));
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        metaPanel.add(idLabel);
        metaPanel.add(new JLabel("|") {{
            setForeground(new Color(90, 100, 120));
        }});
        metaPanel.add(diffLabel);

        // Question text
        JTextArea questionArea = new JTextArea(text);
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setOpaque(false);
        questionArea.setForeground(TEXT_MAIN);
        questionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionArea.setBorder(null);

        card.add(metaPanel, BorderLayout.NORTH);
        card.add(questionArea, BorderLayout.CENTER);

        // Mouse handling for selection + double-click edit
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelectedIndex(index);
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openEditor(q);
                }
            }
        };

        card.addMouseListener(clickHandler);
        questionArea.addMouseListener(clickHandler);

        return card;
    }

    private void setSelectedIndex(int index) {
        if (index < 0 || index >= currentQuestions.size()) {
            selectedIndex = -1;
        } else {
            selectedIndex = index;
        }
        cardsContainer.repaint();
    }

    private Question getSelectedQuestion() {
        if (selectedIndex < 0 || selectedIndex >= currentQuestions.size()) {
            return null;
        }
        return currentQuestions.get(selectedIndex);
    }

    // ----------------------------------------------------
    // Button styling helpers
    // ----------------------------------------------------
    private void stylePrimaryButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleDangerButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleGhostButton(JButton btn) {
        Color bg = new Color(0x36, 0x53, 0x69);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleSecondaryButton(JButton btn) {
        Color bg = BTN_CLOSE_BG;
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(9, 22, 9, 22)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }
}
