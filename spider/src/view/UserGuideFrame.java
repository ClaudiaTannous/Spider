package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class UserGuideFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // ===== Colors =====
    private static final Color DARK_NAVY   = new Color(8, 22, 30);
    private static final Color TOP_BAR     = new Color(15, 30, 50);
    private static final Color CARD_BG     = new Color(13, 42, 56);

    private static final Color TEXT_WHITE  = new Color(245, 245, 245);
    private static final Color TEXT_GRAY   = new Color(190, 200, 205);

    private static final Color BORDER_TEAL = new Color(0, 191, 165);
    private static final Color BORDER_GOLD = new Color(255, 202, 40);

    public UserGuideFrame() {
        super("User Guide - Minesweeper");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 720);
        setMinimumSize(new Dimension(720, 560));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(DARK_NAVY);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(TOP_BAR);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("USER GUIDE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_WHITE);

        JLabel subtitle = new JLabel(
                "How to play + rules, turns, flags, special boxes, scoring & win/lose"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        JLabel tag = new JLabel("Two Players");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tag.setForeground(TEXT_WHITE);
        tag.setOpaque(true);
        tag.setBackground(new Color(0, 191, 165, 30));
        tag.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_TEAL, 2, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        header.add(titleBox, BorderLayout.WEST);
        header.add(tag, BorderLayout.EAST);
        return header;
    }

    // ================= CONTENT =================
    private JComponent buildContent() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(20, 80, 100), 2, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JEditorPane pane = new JEditorPane("text/html", buildHtml());
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ================= FOOTER =================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel tip = new JLabel(
                "Tip: Start with corners/edges and use the numbers to deduce mines."
        );
        tip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tip.setForeground(TEXT_GRAY);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(true);
        closeBtn.setBackground(new Color(25, 38, 56));
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GOLD, 2, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        closeBtn.addActionListener(e -> dispose());

        footer.add(tip, BorderLayout.WEST);
        footer.add(closeBtn, BorderLayout.EAST);
        return footer;
    }

    // ================= HTML =================
 // ================= HTML =================
    private String buildHtml() {
        return """
        <html>
        <body style="font-family:Segoe UI; color:#F5F5F5; font-size:14px; line-height:1.55;">

        %s
        %s
        %s
        %s
        %s
        %s
        %s
        %s
        %s

        </body>
        </html>
        """.formatted(

            section("How to Play Minesweeper ?",
                "Minesweeper is a logic game: your goal is to open safe cells and avoid mines.<br/><br/>" +
                "<b>Step 1:</b> Click a hidden cell to reveal it.<br/>" +
                "<b>Step 2:</b> If you reveal a <b>number</b>, it tells you how many mines are in the <b>8 surrounding cells</b>.<br/>" +
                "<b>Step 3:</b> Use logic to deduce where mines must be, then mark them using <b>Flag Mode (🚩)</b>.<br/>" +
                "<b>Step 4:</b> Use special bonuses wisely (Dice / Heart / Hint / Open Mine).<br/><br/>" +
                "<div style='margin:8px 0 0; padding:10px; border-radius:12px; background:rgba(255,255,255,0.06);'>" +
                "✅ You should <b>flag</b> cells you are sure are mines.<br/>" +
                "⚠️ Don’t guess early — use the numbers to be sure." +
                "</div>"
            ),

            section("1) Goal of the Game (Two Players)",
                "Each player has their own board (A/B). Players take turns and only the <b>ACTIVE</b> board can be clicked.<br/>" +
                "The game ends when you <b>win</b>, <b>lose</b> (no lives), or <b>quit</b>."
            ),

            section("2) Turns & Boards",
                "Only the <b>ACTIVE</b> board can be clicked. After a <b>valid action</b>, the turn switches to the other player.<br/>" +
                "If a click is ignored (for example: clicking an already revealed cell), the turn does not change."
            ),

            section("3) Basic Cells",
                "<b>Safe cell:</b> reveals a number.<br/>" +
                "<b>Empty cell:</b> opens a safe area automatically.<br/>" +
                "<b>Mine:</b> clicking a mine reduces lives and reveals the mine."
            ),

            section("4) Flag Mode (🚩)",
                "Flag Mode lets you place/remove a flag on a hidden cell.<br/>" +
                "When leaving Flag Mode, the game evaluates all placed flags:<br/>" +
                "• Correct flag on a mine: <b>+1 point</b> and the mine is shown.<br/>" +
                "• Wrong flag (not a mine): <b>-3 points</b> and the cell is marked as wrong."
            ),

            section("5) Question Box (❓)",
                "First click: reveals the ❓ symbol.<br/>" +
                "Second click: asks if you want to answer the question (activation cost applies).<br/>" +
                "Correct / wrong effects depend on the difficulty rules."
            ),

            section("6) Surprise Box (🎁) + Difficulty Effects",
                "First click: reveals the 🎁 symbol.<br/>" +
                "Second click: asks if you want to activate it (activation cost applies).<br/><br/>" +

                "<div style='margin:8px 0 14px; padding:10px; border-radius:12px; background:rgba(255,255,255,0.06);'>" +
                "✅ <b>Good outcome</b>: you <b>gain points</b> and <b>gain +1 life</b>.<br/>" +
                "❌ <b>Bad outcome</b>: you <b>lose points</b> and <b>lose -1 life</b>.<br/>" +
                "<span style='color:#C0C8CD;'>The amount of points gained/lost depends on the chosen difficulty.</span>" +
                "</div>" +

                "<div style='padding:12px; border-radius:12px; border:2px solid rgba(0,191,165,0.55); background:rgba(0,191,165,0.08); margin-bottom:10px;'>" +
                "<b>Easy</b> — Start: <b>8 lives</b><br/>" +
                "Activation cost: <b>-5 points</b><br/>" +
                "Good outcome: <b>+8 points</b> and <b>+1 life</b><br/>" +
                "Bad outcome: <b>-8 points</b> and <b>-1 life</b>" +
                "</div>" +

                "<div style='padding:12px; border-radius:12px; border:2px solid rgba(171,71,188,0.55); background:rgba(171,71,188,0.08); margin-bottom:10px;'>" +
                "<b>Medium</b> — Start: <b>8 lives</b><br/>" +
                "Activation cost: <b>-8 points</b><br/>" +
                "Good outcome: <b>+12 points</b> and <b>+1 life</b><br/>" +
                "Bad outcome: <b>-12 points</b> and <b>-1 life</b>" +
                "</div>" +

                "<div style='padding:12px; border-radius:12px; border:2px solid rgba(255,202,40,0.55); background:rgba(255,202,40,0.08);'>" +
                "<b>Hard</b> — Start: <b>6 lives</b><br/>" +
                "Activation cost: <b>-12 points</b><br/>" +
                "Good outcome: <b>+16 points</b> and <b>+1 life</b><br/>" +
                "Bad outcome: <b>-16 points</b> and <b>-1 life</b>" +
                "</div>"
            ),

            section("7) Dice Cell (🎲) — Choose What It Becomes",
                "A Dice cell is a special bonus cell.<br/>" +
                "When you click it, a dialog appears with these options:<br/>" +
                "• <b>Heart Cell</b><br/>" +
                "• <b>Question Cell</b><br/>" +
                "• <b>Surprise Cell</b><br/>" +
                "• <b>Cancel</b><br/><br/>" +
                "After choosing, the Dice cell behaves <b>exactly like</b> the chosen cell type (same rules, points, and life effects).<br/>" +
                "Choosing <b>Cancel</b> leaves the cell unchanged.<br/><br/>" +

                "<div style='margin:8px 0 0; padding:10px; border-radius:12px; background:rgba(255,255,255,0.06);'>" +
                "<b>Dice cells per difficulty (each board):</b><br/>" +
                "• Easy: <b>1</b> dice<br/>" +
                "• Medium: <b>1</b> dice<br/>" +
                "• Hard: <b>2</b> dice" +
                "</div>"
            ),

            section("8) Heart Cell (♥) — Extra Life or Bonus Points",
                "Heart cells give a valuable bonus, but they require an <b>activation cost</b> (depends on difficulty).<br/><br/>" +
                "When activating a Heart cell:<br/>" +
                "• If you are <b>below max lives</b> → you gain <b>+1 life</b><br/>" +
                "• If you are already at <b>max lives</b> → you gain <b>+5 points</b><br/><br/>" +

                "<div style='margin:8px 0 0; padding:10px; border-radius:12px; background:rgba(255,255,255,0.06);'>" +
                "<b>Heart cells + activation cost (each board):</b><br/>" +
                "• Easy: <b>1</b> heart, cost <b>-5</b> points<br/>" +
                "• Medium: <b>2</b> hearts, cost <b>-8</b> points<br/>" +
                "• Hard: <b>2</b> hearts, cost <b>-12</b> points" +
                "</div>"
            ),

            section("9) Bonus Buttons, Timer, Keys & History",
                "<b>Hint Button</b>: Can be used <b>once per game</b>. It highlights (yellow circle) a hidden mine for <b>5 seconds</b> and then disappears.<br/><br/>" +
                "<b>Open Mine Button</b>: Can be used <b>once per game</b>. It opens a mine cell directly.<br/><br/>" +
                "<b>Timer</b>: Starts when the game begins and stops when you <b>win</b> or <b>lose</b>.<br/><br/>" +
                "<b>Keys Panel</b>: Shows which cell types exist in the boards and helps players recognize special cells.<br/><br/>" +
                "<b>Help Button</b>: Opens this guide window anytime during the game.<br/><br/>" +
                "<b>History Filters</b>: In the history/results screen you can filter between <b>Wins</b> and <b>Losses</b>."
            )
        );
    }


    private String section(String title, String body) {
        return """
        <div style="margin-bottom:14px; padding:12px 14px; border-radius:14px;
                    background:rgba(255,255,255,0.05);
                    border:1px solid rgba(255,255,255,0.06);">
          <div style="font-size:16px; font-weight:700; margin-bottom:6px;">%s</div>
          <div style="color:#E6EEF2;">%s</div>
        </div>
        """.formatted(title, body);
    }
}
