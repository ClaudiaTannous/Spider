package view;

import model.Question;
import model.QuestionDifficulty;
import model.SysData;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class QuestionEditorDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final SysData sysData;
    private final Question originalQuestion;

    private JTextField txtId;
    private JTextArea txtQuestion;
    private JTextArea[] txtAnswers;
    private JRadioButton[] rbCorrect;
    private ButtonGroup correctGroup;
    private JComboBox<QuestionDifficulty> cbDifficulty;

    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnCancel;

    // --- Color palette from "Modern Dark Mode Pale" ---
    private static final Color BG_MAIN         = new Color(0x08, 0x13, 0x1E); // Background
    private static final Color CARD_BG         = new Color(0x0D, 0x2A, 0x38); // Board
    private static final Color ROW_ODD         = new Color(0x1F, 0x33, 0x40); // Hidden Cell
    private static final Color ROW_EVEN        = new Color(0x2B, 0x45, 0x53); // Revealed Cell
    private static final Color HEADER_BG       = new Color(0x36, 0x53, 0x69); // Hover
    private static final Color HEADER_FG       = new Color(236, 240, 241);
    private static final Color TEXT_MAIN       = new Color(230, 242, 255);
    private static final Color TEXT_MUTED      = new Color(170, 185, 195);

    // Buttons based on palette
    private static final Color BTN_SAVE_BG     = new Color(0xFF, 0xCF, 0x4A); // Flag
    private static final Color BTN_DELETE_BG   = new Color(0xFF, 0x52, 0x62); // Mine
    private static final Color BTN_CANCEL_BG   = new Color(0x2B, 0x45, 0x53); // Revealed Cell

    public QuestionEditorDialog(Frame owner, SysData sysData, Question questionOrNull) {
        super(owner, true);
        this.sysData = sysData;
        this.originalQuestion = questionOrNull;

        setTitle(questionOrNull == null ? "New Question"
                                        : "Edit Question " + questionOrNull.getId());

        initComponents();
        buildLayout();
        attachListeners();

        if (originalQuestion != null) {
            loadQuestionToForm(originalQuestion);
        } else {
            txtId.setText(sysData.generateNewQuestionId());
        }

        setSize(720, 560);
        setLocationRelativeTo(owner);
    }

    // ----------------------------------------------------
    // Init components
    // ----------------------------------------------------
    private void initComponents() {
        txtId = new JTextField(8);
        txtId.setEditable(false);
        txtId.setBackground(ROW_EVEN);
        txtId.setForeground(TEXT_MAIN);
        txtId.setBorder(new LineBorder(HEADER_BG.darker(), 1, true));
        txtId.setCaretColor(TEXT_MAIN);

        txtQuestion = new JTextArea(3, 30);
        styleTextArea(txtQuestion);

        txtAnswers = new JTextArea[4];
        rbCorrect = new JRadioButton[4];
        correctGroup = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
         
            txtAnswers[i] = new JTextArea(3, 30);
            styleTextArea(txtAnswers[i]);

            rbCorrect[i] = new JRadioButton();
            rbCorrect[i].setActionCommand(String.valueOf(i));
            rbCorrect[i].setOpaque(false);
            rbCorrect[i].setForeground(TEXT_MAIN);
            correctGroup.add(rbCorrect[i]);
        }

        cbDifficulty = new JComboBox<>(QuestionDifficulty.values());
        cbDifficulty.setBackground(ROW_EVEN);
        cbDifficulty.setForeground(TEXT_MAIN);
        cbDifficulty.setBorder(new LineBorder(HEADER_BG.darker(), 1, true));
        cbDifficulty.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btnSave   = new JButton("Save");
        btnDelete = new JButton("Delete");
        btnCancel = new JButton("Cancel");

      
        stylePrimaryButton(btnSave, BTN_SAVE_BG);
        styleDangerButton(btnDelete, BTN_DELETE_BG);
        styleSecondaryButton(btnCancel, BTN_CANCEL_BG);

        if (originalQuestion == null) {
            btnDelete.setEnabled(false);
        }
    }

    // ----------------------------------------------------
    // Layout
    // ----------------------------------------------------
    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(
                        0, 0, BG_MAIN,
                        getWidth(), getHeight(), BG_MAIN.darker()));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // Title bar
        JLabel lblTitle = new JLabel("Question details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Fill in the question text, answers, and difficulty level.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblSubtitle);

        root.add(titlePanel, BorderLayout.NORTH);

        // Card with form
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(CARD_BG);
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(HEADER_BG, 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        int row = 0;

        // --- ID + Difficulty row ---
        GridBagConstraints gbcIdLabel = new GridBagConstraints();
        gbcIdLabel.insets = new Insets(5, 5, 5, 5);
        gbcIdLabel.gridx = 0;
        gbcIdLabel.gridy = row;
        gbcIdLabel.anchor = GridBagConstraints.WEST;
        JLabel lblId = new JLabel("ID:");
        lblId.setForeground(TEXT_MAIN);
        cardPanel.add(lblId, gbcIdLabel);

        GridBagConstraints gbcIdField = new GridBagConstraints();
        gbcIdField.insets = new Insets(5, 5, 5, 5);
        gbcIdField.gridx = 1;
        gbcIdField.gridy = row;
        gbcIdField.fill = GridBagConstraints.HORIZONTAL;
        gbcIdField.weightx = 0.4;
        cardPanel.add(txtId, gbcIdField);

        GridBagConstraints gbcDiffLabel = new GridBagConstraints();
        gbcDiffLabel.insets = new Insets(5, 20, 5, 5);
        gbcDiffLabel.gridx = 2;
        gbcDiffLabel.gridy = row;
        gbcDiffLabel.anchor = GridBagConstraints.EAST;
        JLabel lblDiff = new JLabel("Difficulty:");
        lblDiff.setForeground(TEXT_MAIN);
        cardPanel.add(lblDiff, gbcDiffLabel);

        GridBagConstraints gbcDiffCombo = new GridBagConstraints();
        gbcDiffCombo.insets = new Insets(5, 5, 5, 5);
        gbcDiffCombo.gridx = 3;
        gbcDiffCombo.gridy = row;
        gbcDiffCombo.fill = GridBagConstraints.HORIZONTAL;
        gbcDiffCombo.weightx = 0.6;
        cardPanel.add(cbDifficulty, gbcDiffCombo);

        // --- Question label ---
        row++;
        GridBagConstraints gbcQuestionLabel = new GridBagConstraints();
        gbcQuestionLabel.insets = new Insets(12, 5, 5, 5);
        gbcQuestionLabel.gridx = 0;
        gbcQuestionLabel.gridy = row;
        gbcQuestionLabel.gridwidth = 4;
        gbcQuestionLabel.anchor = GridBagConstraints.WEST;
        JLabel lblQuestion = new JLabel("Question:");
        lblQuestion.setForeground(TEXT_MAIN);
        cardPanel.add(lblQuestion, gbcQuestionLabel);

        // --- Question text area ---
        row++;
        JScrollPane questionScroll = new JScrollPane(txtQuestion);
        questionScroll.setBorder(new LineBorder(HEADER_BG, 1, true));
        questionScroll.getViewport().setBackground(ROW_ODD);

        GridBagConstraints gbcQuestionScroll = new GridBagConstraints();
        gbcQuestionScroll.insets = new Insets(5, 5, 10, 5);
        gbcQuestionScroll.gridx = 0;
        gbcQuestionScroll.gridy = row;
        gbcQuestionScroll.gridwidth = 4;
        gbcQuestionScroll.fill = GridBagConstraints.BOTH;
        gbcQuestionScroll.weightx = 1.0;
        gbcQuestionScroll.weighty = 0.5;
        cardPanel.add(questionScroll, gbcQuestionScroll);

        // --- Answers title ---
        row++;
        JLabel answersTitle = new JLabel("Answers (mark the correct one):");
        answersTitle.setForeground(TEXT_MAIN);
        answersTitle.setBorder(new EmptyBorder(5, 0, 0, 0));

        GridBagConstraints gbcAnswersTitle = new GridBagConstraints();
        gbcAnswersTitle.insets = new Insets(5, 5, 5, 5);
        gbcAnswersTitle.gridx = 0;
        gbcAnswersTitle.gridy = row;
        gbcAnswersTitle.gridwidth = 4;
        gbcAnswersTitle.anchor = GridBagConstraints.WEST;
        cardPanel.add(answersTitle, gbcAnswersTitle);

        // --- 4 answer rows ---
        for (int i = 0; i < 4; i++) {
            row++;

            GridBagConstraints gbcAnsLabel = new GridBagConstraints();
            gbcAnsLabel.insets = new Insets(6, 5, 6, 5);
            gbcAnsLabel.gridx = 0;
            gbcAnsLabel.gridy = row;
            gbcAnsLabel.anchor = GridBagConstraints.NORTHWEST;
            JLabel lblAns = new JLabel("Answer " + (i + 1) + ":");
            lblAns.setForeground(TEXT_MAIN);
            cardPanel.add(lblAns, gbcAnsLabel);

            JScrollPane ansScroll = new JScrollPane(txtAnswers[i]);
            ansScroll.setBorder(new LineBorder(HEADER_BG, 1, true));
            ansScroll.getViewport().setBackground(ROW_ODD);

            GridBagConstraints gbcAnsScroll = new GridBagConstraints();
            gbcAnsScroll.insets = new Insets(6, 5, 6, 5);
            gbcAnsScroll.gridx = 1;
            gbcAnsScroll.gridy = row;
            gbcAnsScroll.gridwidth = 2;
            gbcAnsScroll.fill = GridBagConstraints.BOTH;
            gbcAnsScroll.weightx = 1.0;
            gbcAnsScroll.weighty = 0.18;    
            cardPanel.add(ansScroll, gbcAnsScroll);

            GridBagConstraints gbcAnsRadio = new GridBagConstraints();
            gbcAnsRadio.insets = new Insets(6, 5, 6, 5);
            gbcAnsRadio.gridx = 3;
            gbcAnsRadio.gridy = row;
            gbcAnsRadio.anchor = GridBagConstraints.CENTER;
            cardPanel.add(rbCorrect[i], gbcAnsRadio);
        }

        // --- Buttons row ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnCancel);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnSave);

        root.add(cardPanel, BorderLayout.CENTER);
        root.add(buttonsPanel, BorderLayout.SOUTH);
    }

    // ----------------------------------------------------
    // Listeners
    // ----------------------------------------------------
    private void attachListeners() {
        btnSave.addActionListener(this::onSave);
        btnDelete.addActionListener(this::onDelete);
        btnCancel.addActionListener(e -> dispose());
    }

    // ----------------------------------------------------
    // Actions
    // ----------------------------------------------------
    private void onSave(ActionEvent e) {
        try {
            Question q = buildQuestionFromForm();
            if (q == null) return;

            sysData.saveQuestion(q);

            JOptionPane.showMessageDialog(this,
                    "Question saved.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving question: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete(ActionEvent e) {
        if (originalQuestion == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete question " + originalQuestion.getId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        sysData.deleteQuestion(originalQuestion);

        JOptionPane.showMessageDialog(this,
                "Question deleted.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    // ----------------------------------------------------
    // Form helpers
    // ----------------------------------------------------
    private void loadQuestionToForm(Question q) {
        txtId.setText(q.getId());
        txtQuestion.setText(q.getText());

        List<String> ans = q.getAnswers();
        for (int i = 0; i < 4; i++) {
            String text = (i < ans.size()) ? ans.get(i) : "";
            txtAnswers[i].setText(text);
        }

        correctGroup.clearSelection();
        int idx = q.getCorrectIndex();
        if (idx >= 0 && idx < rbCorrect.length) {
            rbCorrect[idx].setSelected(true);
        }

        cbDifficulty.setSelectedItem(q.getDifficulty());
    }

    private Question buildQuestionFromForm() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            id = sysData.generateNewQuestionId();
            txtId.setText(id);
        }

        String text = txtQuestion.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Question text cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        List<String> answers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            answers.add(txtAnswers[i].getText().trim());
        }

        if (answers.stream().anyMatch(String::isEmpty)) {
            JOptionPane.showMessageDialog(this,
                    "All 4 answers must be filled.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (correctGroup.getSelection() == null) {
            JOptionPane.showMessageDialog(this,
                    "Select the correct answer.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int correctIndex = Integer.parseInt(correctGroup.getSelection().getActionCommand());
        QuestionDifficulty difficulty = (QuestionDifficulty) cbDifficulty.getSelectedItem();

        return new Question(
                id,
                text,
                answers,
                correctIndex,
                difficulty
        );
    }

    // ----------------------------------------------------
    // Styling helpers
    // ----------------------------------------------------
    private void styleTextArea(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(ROW_ODD);
        area.setForeground(TEXT_MAIN);
        area.setCaretColor(TEXT_MAIN);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    private void stylePrimaryButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(6, 18, 6, 18)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleDangerButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(6, 20, 6, 20)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleSecondaryButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(bg.darker(), 1, true),
            new EmptyBorder(6, 18, 6, 18)
        ));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }
}
