package view;

import model.SysData;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameHistory extends JDialog {

	
	private static final long serialVersionUID = 1L;
	private static final Color TEXT_PRIMARY = Color.WHITE;
	private static final Color TEXT_MUTED = new Color(180, 190, 210);
	

	public GameHistory(JFrame owner, SysData sysData) {
		super(owner, "Game History", true);

		List<String[]> rows = sysData.getGameHistory();

		int totalGames = rows.size();
		int victories = 0;
		int defeats = 0;

		for (String[] r : rows) {
			String result = getOrDefault(r, 6).toLowerCase();
			if (result.contains("win") || result.contains("victory"))
				victories++;
			else if (result.contains("loss") || result.contains("defeat"))
				defeats++;
		}

		Image bgImage = new ImageIcon(getClass().getResource("/resources/history.png")).getImage();

		JPanel root = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
				g2.dispose();
			}
		};
		root.setOpaque(false);
		setContentPane(root);

		JPanel titlePanel = new JPanel() {
		
		
		};
		titlePanel.setOpaque(false);
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

		titlePanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
		titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel title = new JLabel("Game History");
		title.setForeground(TEXT_PRIMARY);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
		title.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel subtitle = new JLabel("Track your Minesweeper adventures and achievements");
		subtitle.setForeground(TEXT_MUTED);
		subtitle.setFont(subtitle.getFont().deriveFont(13f));
		subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

		titlePanel.add(title);
		titlePanel.add(Box.createVerticalStrut(6));
		titlePanel.add(subtitle);

		JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
		statsPanel.setOpaque(false);

		statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
		statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		statsPanel.add(createStatCard("Total Games", String.valueOf(totalGames)));
		statsPanel.add(createStatCard("Victories", String.valueOf(victories)));
		statsPanel.add(createStatCard("Defeats", String.valueOf(defeats)));

		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		topPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

		topPanel.add(titlePanel);

		topPanel.add(Box.createVerticalStrut(12));
		topPanel.add(statsPanel);

		JPanel cardsContainer = new JPanel();
		cardsContainer.setOpaque(false);
		cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
		cardsContainer.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));

		for (String[] r : rows) {
			cardsContainer.add(createGameCard(r));
			cardsContainer.add(Box.createVerticalStrut(12));
		}

		JScrollPane scrollPane = new JScrollPane(cardsContainer);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		root.add(topPanel, BorderLayout.NORTH);
		root.add(scrollPane, BorderLayout.CENTER);

		setSize(1000, 600);
		setLocationRelativeTo(owner);
	}

	private JPanel createStatCard(String label, String value) {

		JPanel card = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(new Color(10, 10, 15, 100));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

				g2.setColor(new Color(255, 255, 255, 20));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

				g2.setColor(new Color(255, 255, 255, 220));
				g2.setStroke(new BasicStroke(2f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);

				g2.dispose();
				super.paintComponent(g);
			}
		};

		card.setOpaque(false);
		card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setForeground(TEXT_PRIMARY);
		valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 18f));

		JLabel labelTitle = new JLabel(label);
		labelTitle.setForeground(TEXT_MUTED);
		labelTitle.setFont(labelTitle.getFont().deriveFont(12f));

		card.add(valueLabel);
		card.add(Box.createVerticalStrut(4));
		card.add(labelTitle);

		return card;
	}

	private JPanel createGameCard(String[] r) {

		String raw = getOrDefault(r, 0);
		String dateOnly = raw;
		if (raw.contains(" "))
			dateOnly = raw.substring(0, raw.indexOf(" "));
		if (raw.contains("T"))
			dateOnly = raw.substring(0, raw.indexOf("T"));

		String difficulty = getOrDefault(r, 1);
		String player1 = getOrDefault(r, 2);
		String p1Score = getOrDefault(r, 3);
		String player2 = getOrDefault(r, 4);
		String result = getOrDefault(r, 6);

		JPanel card = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(new Color(10, 10, 15, 100));
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

				g2.setColor(new Color(255, 255, 255, 18));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

				g2.setColor(new Color(255, 255, 255, 220));
				g2.setStroke(new BasicStroke(2f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);

				g2.dispose();
				super.paintComponent(g);
			}
		};

		card.setOpaque(false);
		card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

		JPanel left = new JPanel();
		left.setOpaque(false);
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

		JLabel dateLabel = new JLabel(" " + dateOnly);
		dateLabel.setForeground(TEXT_MUTED);
		dateLabel.setFont(dateLabel.getFont().deriveFont(12f));

		JLabel p1Label = new JLabel("Player 1 name: " + player1);
		p1Label.setForeground(TEXT_PRIMARY);
		p1Label.setFont(p1Label.getFont().deriveFont(13f));

		JLabel p2Label = new JLabel("Player 2 name: " + player2);
		p2Label.setForeground(TEXT_PRIMARY);
		p2Label.setFont(p2Label.getFont().deriveFont(13f));

		left.add(dateLabel);
		left.add(Box.createVerticalStrut(8));
		left.add(p1Label);
		left.add(Box.createVerticalStrut(4));
		left.add(p2Label);

		JPanel right = new JPanel();
		right.setOpaque(false);
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

		right.add(makeMiniCard("Score: " + p1Score));
		right.add(Box.createVerticalStrut(10));
		right.add(makeMiniCard("Difficulty: " + difficulty));
		right.add(Box.createVerticalStrut(10));
		right.add(makeMiniCard("Result: " + result));

		card.add(left, BorderLayout.WEST);
		card.add(right, BorderLayout.EAST);

		return card;
	}

	private JPanel makeMiniCard(String text) {

		JPanel box = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				g2.setColor(new Color(10, 10, 15, 100));

				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

				g2.setColor(new Color(255, 255, 255, 18));
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

				g2.setColor(new Color(255, 255, 255, 220));

				g2.setStroke(new BasicStroke(2f));
				g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);

				g2.dispose();
				super.paintComponent(g);
			}
		};

		box.setOpaque(false);
		box.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

		JLabel lbl = new JLabel(text);
		lbl.setForeground(TEXT_PRIMARY);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
		box.add(lbl);

		box.setMaximumSize(new Dimension(180, 45));
		box.setMinimumSize(new Dimension(180, 45));
		box.setPreferredSize(new Dimension(180, 45));

		return box;
	}

	private String getOrDefault(String[] arr, int idx) {
		if (idx >= 0 && idx < arr.length && arr[idx] != null)
			return arr[idx];
		return "";
	}
}
