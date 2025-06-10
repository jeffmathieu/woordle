package org.example.ui;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class WordleSubmitDemo extends JFrame {
    private static final int WORD_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 6;

    private final List<String> wordList;
    private final JLabel[][] cells = new JLabel[MAX_ATTEMPTS][WORD_LENGTH];
    private int currentRow = 0;

    private String answer;

    public WordleSubmitDemo() throws IOException {
        super("Wordle Submit Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        wordList = Files.readAllLines(Paths.get("src/main/resources/woordle.txt"));
        wordList.removeIf(w -> w.length() != WORD_LENGTH);
        wordList.replaceAll(String::toUpperCase);
        answer = wordList.get(new Random().nextInt(wordList.size()));

        JPanel grid = new JPanel(new GridLayout(MAX_ATTEMPTS, WORD_LENGTH, 5, 5));
        for (int r = 0; r < MAX_ATTEMPTS; r++) {
            for (int c = 0; c < WORD_LENGTH; c++) {
                JLabel lbl = new JLabel(" ", SwingConstants.CENTER);
                lbl.setPreferredSize(new Dimension(60,60));
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 24f));
                lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
                cells[r][c] = lbl;
                grid.add(lbl);
            }
        }
        add(grid, BorderLayout.CENTER);

        JTextField input = new JTextField();
        input.setFont(input.getFont().deriveFont(Font.PLAIN, 18f));
        input.setHorizontalAlignment(SwingConstants.CENTER);
        ((AbstractDocument)input.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override
            public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null) return;
                str = str.replaceAll("[^a-zA-Z]", "").toUpperCase();
                if (fb.getDocument().getLength() + str.length() <= WORD_LENGTH)
                    super.insertString(fb, offs, str, a);
            }
            @Override
            public void replace(FilterBypass fb, int offs, int len, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null) return;
                str = str.replaceAll("[^a-zA-Z]", "").toUpperCase();
                int newLen = fb.getDocument().getLength() - len + str.length();
                if (newLen <= WORD_LENGTH)
                    super.replace(fb, offs, len, str, a);
            }
        });

        input.addActionListener(e -> {
            String guess = input.getText().trim().toUpperCase();
            if (guess.length() < WORD_LENGTH) {
                Toolkit.getDefaultToolkit().beep();
                Color original = input.getForeground();
                input.setForeground(Color.RED);
                Timer timer = new Timer(300, evt -> input.setForeground(original));
                timer.setRepeats(false);
                timer.start();
                return;
            }
            if (!wordList.contains(guess)) {
                Toolkit.getDefaultToolkit().beep();
                Color original = input.getForeground();
                input.setForeground(Color.RED);
                Timer timer = new Timer(300, evt -> input.setForeground(original));
                timer.setRepeats(false);
                timer.start();
                return;
            } else {
                Color defaultBg = Color.WHITE;
                Color green = new Color(0x6aaa64);
                Color yellow = new Color(0xc9b458);
                Color gray = new Color(0x787c7e);

                boolean[] answerUsed = new boolean[WORD_LENGTH];
                boolean[] guessGreen = new boolean[WORD_LENGTH];

                // Green markings
                for (int c = 0; c < WORD_LENGTH; c++) {
                    JLabel lbl = cells[currentRow][c];
                    char g = guess.charAt(c);
                    lbl.setText(String.valueOf(g));
                    if (g == answer.charAt(c)) {
                        lbl.setOpaque(true);
                        lbl.setBackground(green);
                        guessGreen[c] = true;
                        answerUsed[c] = true;
                    } else {
                        lbl.setOpaque(true);
                        lbl.setBackground(gray);
                    }
                    lbl.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
                }
                // Yellow markings
                for (int c = 0; c < WORD_LENGTH; c++) {
                    if (guessGreen[c]) continue;
                    char g = guess.charAt(c);
                    for (int a = 0; a < WORD_LENGTH; a++) {
                        if (!answerUsed[a] && g == answer.charAt(a)) {
                            cells[currentRow][c].setBackground(yellow);
                            answerUsed[a] = true;
                            break;
                        }
                    }
                }
                boolean allGreen = true;
                for (int c = 0; c < WORD_LENGTH; c++) {
                    if (!guessGreen[c]) {
                        allGreen = false;
                        break;
                    }
                }
                if (allGreen) {
                    showWinAndRestart(input);
                    return;
                }
            }
            input.setForeground(Color.BLACK);
            for (int c = 0; c < WORD_LENGTH; c++) {
                JLabel lbl = cells[currentRow][c];
                lbl.setText( String.valueOf(guess.charAt(c)) );
                lbl.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
            }
            currentRow++;
            input.setText("");
            if (currentRow >= MAX_ATTEMPTS) {
                input.setEnabled(false);
                showLoseAndRestart(input);
            }
        });

        add(input, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showWinAndRestart(JTextField input) {
        JOptionPane.showMessageDialog(this, "Congratulations! You won!", "Wordle", JOptionPane.INFORMATION_MESSAGE);
        // Reset grid
        for (int r = 0; r < MAX_ATTEMPTS; r++) {
            for (int c = 0; c < WORD_LENGTH; c++) {
                JLabel lbl = cells[r][c];
                lbl.setText(" ");
                lbl.setBackground(null);
                lbl.setOpaque(false);
                lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
            }
        }
        // Pick new answer
        answer = wordList.get(new Random().nextInt(wordList.size()));
        currentRow = 0;
        input.setText("");
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    private void showLoseAndRestart(JTextField input) {
        JOptionPane.showMessageDialog(this, "Loser! You lost!\n The word was " + answer, "Wordle", JOptionPane.INFORMATION_MESSAGE);
        // Reset grid
        for (int r = 0; r < MAX_ATTEMPTS; r++) {
            for (int c = 0; c < WORD_LENGTH; c++) {
                JLabel lbl = cells[r][c];
                lbl.setText(" ");
                lbl.setBackground(null);
                lbl.setOpaque(false);
                lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
            }
        }
        // Pick new answer
        answer = wordList.get(new Random().nextInt(wordList.size()));
        currentRow = 0;
        input.setText("");
        input.setEnabled(true);
        input.requestFocusInWindow();
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            try { new WordleSubmitDemo(); }
            catch (IOException ex) { ex.printStackTrace(); }
        });
    }
}
