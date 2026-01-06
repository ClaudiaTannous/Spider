package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {

    private int numberOfMines;
    private Cell[][] cells;
    private int rows;
    private int cols;
    private int surpriseBoxes;
    private int questionBoxes;
    private Difficulty difficulty;

    public Board(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.numberOfMines = difficulty.getMines();
        this.surpriseBoxes = difficulty.getSurpriseBoxes();
        this.questionBoxes = difficulty.getQuestionBoxes();

        cells = new Cell[cols][rows];

        createEmptyCells();
        setSpecialBoxes();        
        setMines();               
        setSurroundingMinesNumber();
    }

    
    private void createEmptyCells() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y] = new Cell();
                cells[x][y].setContent("");
            }
        }
    }

   
    private void setSpecialBoxes() {
        Random rand = new Random();
        List<String> usedPositions = new ArrayList<>();

        int currentSurprise = 0;
        while (currentSurprise < surpriseBoxes) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String pos = x + "," + y;

            if (!usedPositions.contains(pos)
                    && cells[x][y].getSpecialBox() == SpecialBoxType.NONE) {

                cells[x][y].setSpecialBox(SpecialBoxType.SURPRISE);
                usedPositions.add(pos);
                currentSurprise++;
            }
        }

        int currentQuestion = 0;
        while (currentQuestion < questionBoxes) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String pos = x + "," + y;

            if (!usedPositions.contains(pos)
                    && cells[x][y].getSpecialBox() == SpecialBoxType.NONE) {

                cells[x][y].setSpecialBox(SpecialBoxType.QUESTION);
                usedPositions.add(pos);
                currentQuestion++;
            }
        }
    }

    
    private void setMines() {
        Random rand = new Random();
        int currentMines = 0;
        List<String> usedPositions = new ArrayList<>();

        while (currentMines < numberOfMines) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String pos = x + "," + y;

            boolean hasMine = cells[x][y].getMine();
            boolean hasSpecial = cells[x][y].getSpecialBox() != SpecialBoxType.NONE;
            boolean nearSpecial = hasSpecialNeighbor(x, y);

            if (!hasMine && !hasSpecial && !nearSpecial && !usedPositions.contains(pos)) {
                cells[x][y].setMine(true);
                usedPositions.add(pos);
                currentMines++;
            }
        }
    }

   
    private boolean hasSpecialNeighbor(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                int nx = x + dx;
                int ny = y + dy;

                if (nx < 0 || ny < 0 || nx >= cols || ny >= rows)
                    continue;

                SpecialBoxType s = cells[nx][ny].getSpecialBox();
                if (s == SpecialBoxType.QUESTION || s == SpecialBoxType.SURPRISE) {
                    return true;
                }
            }
        }
        return false;
    }

    
    private void setSurroundingMinesNumber() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y].setSurroundingMines(calculateNeighbours(x, y));
            }
        }
    }

    private int calculateNeighbours(int xCo, int yCo) {
        int neighbours = 0;

        for (int x = makeValidCoordinateX(xCo - 1);
             x <= makeValidCoordinateX(xCo + 1); x++) {

            for (int y = makeValidCoordinateY(yCo - 1);
                 y <= makeValidCoordinateY(yCo + 1); y++) {

                if (x != xCo || y != yCo) {
                    if (cells[x][y].getMine()) {
                        neighbours++;
                    }
                }
            }
        }
        return neighbours;
    }

   
    public int makeValidCoordinateX(int i) {
        if (i < 0) return 0;
        if (i > cols - 1) return cols - 1;
        return i;
    }

    public int makeValidCoordinateY(int i) {
        if (i < 0) return 0;
        if (i > rows - 1) return rows - 1;
        return i;
    }

   
    public void resetBoard() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y].setContent("");
                cells[x][y].setMine(false);
                cells[x][y].setSpecialBox(SpecialBoxType.NONE);
                cells[x][y].setSurroundingMines(0);
            }
        }

        setSpecialBoxes();
        setMines();
        setSurroundingMinesNumber();
    }

    
    public Cell[][] getCells() {
        return cells;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getNumberOfMines() {
        return numberOfMines;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
