package view;

import model.SysData;    

import javax.swing.*;
import java.awt.*;

public class MainPage extends JFrame {

    private static final long serialVersionUID = 1L;

    private Image backgroundImage;

    public MainPage() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 450);
        setLocationRelativeTo(null);

  
        try {
            backgroundImage = new ImageIcon(
                    getClass().getResource("/resources/animated-bg.gif")
            ).getImage();
        } catch (Exception e) {
            backgroundImage = null;
        }

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // ☑ Frosted Glass Panel
        JPanel glassPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                // Darker glass
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                g2.setColor(new Color(0, 0, 0));  // black tint
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.dispose();
            }
        };

        glassPanel.setOpaque(false);
        glassPanel.setPreferredSize(new Dimension(380, 350));
        glassPanel.setLayout(new GridBagLayout());

  
        JLabel title = new JLabel("MineSweeper", SwingConstants.CENTER);
        title.setForeground(Color.white);
        title.setFont(new Font("Segoe UI", Font.BOLD, 44)); // bigger

      
        JLabel subtitle = new JLabel("Welcome to Minesweeper", SwingConstants.CENTER);
        subtitle.setForeground(Color.white);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
 
        ImageIcon startIcon  = loadIcon("/resources/play.png");
        ImageIcon historyIcon = loadIcon("/resources/swords.png");
        ImageIcon manageIcon = loadIcon("/resources/task-management.png");

        // Buttons
        JPanel startBtn = createStyledButton(
                "Start New Game",
                new Color(255, 204, 0),
                startIcon,
                () -> {
                    new PlayerSetupView().setVisible(true);
                    MainPage.this.dispose();
                }
        );

        JPanel historyBtn = createStyledButton(
                "History",
                new Color(51, 102, 255),
                historyIcon,
                () -> {
                    SwingUtilities.invokeLater(() -> {
                        SysData sysData = new SysData();
                        GameHistory historyDialog = new GameHistory(MainPage.this, sysData);
                        historyDialog.setVisible(true);
                    });
                }
        );

        JPanel manageBtn = createStyledButton(
                "Manage Questions",
                new Color(0, 153, 76),
                manageIcon,
                () -> {
                    SwingUtilities.invokeLater(() -> {
                        SysData sysData = new SysData();
                        QuestionManagerFrame qm = new QuestionManagerFrame(sysData);
                        qm.setVisible(true);
                    });
                }
        );

        GridBagConstraints inner = new GridBagConstraints();
        inner.insets = new Insets(10, 10, 10, 10);
        inner.fill = GridBagConstraints.HORIZONTAL;
        inner.gridx = 0;

        inner.gridy = 0;
        glassPanel.add(title, inner);

        inner.gridy = 1;
        glassPanel.add(subtitle, inner);

        inner.gridy = 2;
        glassPanel.add(startBtn, inner);

        inner.gridy = 3;
        glassPanel.add(historyBtn, inner);

        inner.gridy = 4;
        glassPanel.add(manageBtn, inner);

     
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        backgroundPanel.add(glassPanel, gbcMain);

        setContentPane(backgroundPanel);
    }


    private ImageIcon loadIcon(String path) {
        try {
            ImageIcon i = new ImageIcon(getClass().getResource(path));
            return new ImageIcon(i.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel createStyledButton(String text, Color bgColor, ImageIcon icon, Runnable onClick) {

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

               
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 

                g2.dispose();
                super.paintComponent(g);
            }
        };

        panel.setPreferredSize(new Dimension(260, 50));
        panel.setBackground(bgColor);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setOpaque(false); 

        panel.setLayout(new BorderLayout());

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        textLabel.setForeground(
                bgColor.getRed() > 200 && bgColor.getGreen() > 180 ? Color.BLACK : Color.WHITE
        );
        panel.add(textLabel, BorderLayout.CENTER);

        // Hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(bgColor);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (onClick != null) onClick.run();
            }
        });

        return panel;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainPage().setVisible(true));
    }
}