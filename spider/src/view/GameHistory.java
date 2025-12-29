package view;

import model.SysData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHistory extends JDialog {

    private static final long serialVersionUID = 1L;

    // ---------- Theme ----------
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_MUTED   = new Color(180, 190, 210);

    private static final Color GLASS_BG    = new Color(10, 10, 15, 110);
    private static final Color BORDER_SOFT = new Color(255, 255, 255, 35);

    // Result colors
    private static final Color ACCENT_WIN  = new Color(0, 200, 120);
    private static final Color ACCENT_LOST = new Color(239, 83, 80);
    private static final Color ACCENT_QUIT = new Color(255, 202, 40);
    private static final Color ACCENT_NEUTRAL = new Color(180, 190, 210);

    // ---------- Data ----------
    private final SysData sysData;
    private final List<String[]> allRows;
    private List<String[]> filteredRows;

    // ---------- UI ----------
    private JComboBox<String> resultFilterBox;
    private JComboBox<String> difficultyFilterBox;

    private JPanel cardsContainer;

    public GameHistory(JFrame owner, SysData sysData) {
        super(owner, "Game History", true);
        this.sysData = sysData;

        allRows = new ArrayList<>(sysData.getGameHistory());
        Collections.reverse(allRows); // newest first
        filteredRows = new ArrayList<>(allRows);

        Image bgImage = null;
        try {
            bgImage = new ImageIcon(
                    getClass().getResource("/resources/historyBackGround.png")
            ).getImage();
        } catch (Exception ignored) {}

        Image finalBg = bgImage;

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalBg != null) {
                    g.drawImage(finalBg, 0, 0, getWidth(), getHeight(), null);
                    g.setColor(new Color(0, 0, 0, 150)); // dark overlay
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        setSize(1050, 680);
        setLocationRelativeTo(owner);
    }

    // ================= TOP =================

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(16, 24, 12, 24));

        JLabel title = new JLabel("Game History");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Track your Minesweeper games");
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(14));

        Stats s = computeStats(allRows);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(createStatCard("Total Games", String.valueOf(s.total)));
        statsPanel.add(createStatCard("Victories", String.valueOf(s.wins)));
        statsPanel.add(createStatCard("Defeats", String.valueOf(s.losses)));

        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(14));

        // ---- Controls row (filters + reset) ----
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new GridBagLayout());
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 10);
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        // Result filter
        JLabel resultLbl = new JLabel("Result:");
        styleLabel(resultLbl);

        resultFilterBox = new JComboBox<>(new String[]{"All", "WIN", "LOST", "QUIT"});
        styleCombo(resultFilterBox);
        resultFilterBox.addActionListener(e -> applyFilter());

        gc.gridx = 0;
        controls.add(resultLbl, gc);
        gc.gridx = 1;
        controls.add(resultFilterBox, gc);

        // Difficulty filter
        JLabel diffLbl = new JLabel("Difficulty:");
        styleLabel(diffLbl);

        difficultyFilterBox = new JComboBox<>(new String[]{"All", "EASY", "MEDIUM", "HARD"});
        styleCombo(difficultyFilterBox);
        difficultyFilterBox.addActionListener(e -> applyFilter());

        gc.gridx = 2;
        controls.add(diffLbl, gc);
        gc.gridx = 3;
        controls.add(difficultyFilterBox, gc);

        // Reset button
        JButton reset = new JButton("Reset");
        styleButton(reset);
        reset.addActionListener(e -> resetFilters());

        gc.gridx = 4;
        gc.insets = new Insets(0, 10, 0, 0);
        controls.add(reset, gc);

        panel.add(controls);

        return panel;
    }

    // ================= CENTER =================

    private JScrollPane buildCenter() {
        cardsContainer = new JPanel();
        cardsContainer.setOpaque(false);
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBorder(new EmptyBorder(10, 24, 10, 24));

        rebuildCards();

        JScrollPane sp = new JScrollPane(cardsContainer);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ================= BOTTOM =================

    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, 24, 12, 24));

        JButton mainPage = new JButton("Main Page");
        styleButton(mainPage);
        mainPage.addActionListener(e -> {
            dispose();
            new MainPage().setVisible(true);
        });

        JButton close = new JButton("Close");
        styleButton(close);
        close.addActionListener(e -> dispose());

        bottom.add(mainPage);
        bottom.add(close);
        return bottom;
    }

    // ================= CARDS =================

    private void rebuildCards() {
        cardsContainer.removeAll();

        if (filteredRows.isEmpty()) {
            JLabel empty = new JLabel("No games found");
            empty.setForeground(TEXT_MUTED);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 18));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardsContainer.add(Box.createVerticalStrut(80));
            cardsContainer.add(empty);
        } else {
            for (String[] r : filteredRows) {
                cardsContainer.add(createGameCard(r));
                cardsContainer.add(Box.createVerticalStrut(10));
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createGameCard(String[] r) {
        String date = formatDate(safeGet(r, 0));
        String difficulty = safeGet(r, 1);
        String p1 = safeGet(r, 2);
        String p1Score = safeGet(r, 3);
        String p2 = safeGet(r, 4);
        String result = safeGet(r, 6);
        String seconds = safeGet(r, 7);

        JPanel card = new GlassPanel(22);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel dateLbl = new JLabel(date);
        dateLbl.setForeground(TEXT_MUTED);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel together = new JLabel(p1 + "  &  " + p2);
        together.setForeground(TEXT_PRIMARY);
        together.setFont(new Font("Segoe UI", Font.BOLD, 15));

        left.add(dateLbl);
        left.add(Box.createVerticalStrut(6));
        left.add(together);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        right.add(makeChip("Score: " + p1Score));
        right.add(makeChip("Difficulty: " + difficulty));
        right.add(makeChip("Time: " + seconds + "s"));
        right.add(makeResultChip(result));

        card.add(left, BorderLayout.WEST);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    // ================= FILTER LOGIC =================

    private void applyFilter() {
        String resultF = (String) resultFilterBox.getSelectedItem();
        String diffF   = (String) difficultyFilterBox.getSelectedItem();

        filteredRows = new ArrayList<>();
        for (String[] r : allRows) {

            // Result filter
            String res = safeGet(r, 6).toLowerCase();
            if (!"All".equals(resultF)) {
                if ("WIN".equals(resultF) && !res.contains("win")) continue;
                if ("LOST".equals(resultF) && !(res.contains("lost") || res.contains("defeat"))) continue;
                if ("QUIT".equals(resultF) && !res.contains("quit")) continue;
            }

            // Difficulty filter
            if (!"All".equals(diffF)) {
                String d = safeGet(r, 1);
                if (!d.equalsIgnoreCase(diffF)) continue;
            }

            filteredRows.add(r);
        }

        rebuildCards();
    }

    private void resetFilters() {
        resultFilterBox.setSelectedItem("All");
        difficultyFilterBox.setSelectedItem("All");
        filteredRows = new ArrayList<>(allRows);
        rebuildCards();
    }

    // ================= SMALL UI =================

    private JPanel createStatCard(String label, String value) {
        JPanel card = new GlassPanel(22);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel v = new JLabel(value);
        v.setForeground(TEXT_PRIMARY);
        v.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel l = new JLabel(label);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(v);
        card.add(Box.createVerticalStrut(4));
        card.add(l);
        return card;
    }

    private JPanel makeChip(String text) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(new Color(10, 10, 15, 140));
        p.setBorder(new LineBorder(BORDER_SOFT, 1, true));

        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lbl);

        return p;
    }

    private JPanel makeResultChip(String result) {
        Color c = ACCENT_NEUTRAL;
        String rr = (result == null ? "" : result).toLowerCase();

        if (rr.contains("win")) c = ACCENT_WIN;
        else if (rr.contains("lost") || rr.contains("defeat")) c = ACCENT_LOST;
        else if (rr.contains("quit")) c = ACCENT_QUIT;

        JPanel p = makeChip("Result: " + result);
        p.setBorder(new LineBorder(c, 2, true));
        return p;
    }

    private void styleLabel(JLabel l) {
        l.setForeground(TEXT_PRIMARY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(10, 10, 15, 160));
        c.setForeground(TEXT_PRIMARY);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(TEXT_PRIMARY);
        b.setBackground(new Color(10, 10, 15, 180));
        b.setFocusPainted(false);
    }

    // ================= HELPERS =================

    private String formatDate(String raw) {
        if (raw == null) return "";
        if (raw.contains("T")) return raw.substring(0, raw.indexOf("T"));
        if (raw.contains(" ")) return raw.substring(0, raw.indexOf(" "));
        return raw;
    }

    private String safeGet(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) return "";
        return arr[idx] == null ? "" : arr[idx];
    }

    private Stats computeStats(List<String[]> rows) {
        Stats s = new Stats();
        s.total = rows.size();
        for (String[] r : rows) {
            String res = safeGet(r, 6).toLowerCase();
            if (res.contains("win")) s.wins++;
            else if (res.contains("lost") || res.contains("defeat")) s.losses++;
        }
        return s;
    }

    private static class Stats {
        int total, wins, losses;
    }

    private static class GlassPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int arc;

        GlassPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(GLASS_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(BORDER_SOFT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
    }
}
