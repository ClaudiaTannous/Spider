package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Board {
	//test//
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

   
    public void createEmptyCells() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y] = new Cell();
                cells[x][y].setContent("");
            }
        }
    }

    
    public void setSpecialBoxes() {
        Random rand = new Random();
        List<String> usedPositions = new ArrayList<>();

     
        int currentSurpriseBoxes = 0;
        while (currentSurpriseBoxes < surpriseBoxes) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String position = x + "," + y;

            if (!usedPositions.contains(position) && cells[x][y].getSpecialBox() == SpecialBoxType.NONE) {
                cells[x][y].setSpecialBox(SpecialBoxType.SURPRISE);
                usedPositions.add(position);
                currentSurpriseBoxes++;
            }
        }

       
        int currentQuestionBoxes = 0;
        while (currentQuestionBoxes < questionBoxes) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String position = x + "," + y;

            if (!usedPositions.contains(position) && cells[x][y].getSpecialBox() == SpecialBoxType.NONE) {
                cells[x][y].setSpecialBox(SpecialBoxType.QUESTION);
                usedPositions.add(position);
                currentQuestionBoxes++;
            }
        }
    }

    
    public void setMines() {
        Random rand = new Random();
        int currentMines = 0;
        List<String> usedPositions = new ArrayList<>();

        while (currentMines < numberOfMines) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            String position = x + "," + y;

            boolean hasMine = cells[x][y].getMine();
            boolean hasSpecialBox = cells[x][y].getSpecialBox() != SpecialBoxType.NONE;

            if (!hasMine && !hasSpecialBox && !usedPositions.contains(position)) {
                cells[x][y].setMine(true);
            
                usedPositions.add(position);
                currentMines++;
            }
        }
    }

   
   

    
    public void setSurroundingMinesNumber() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int calculated = calculateNeighbours(x, y);
                cells[x][y].setSurroundingMines(calculated);
            }
        }
    }

   
    public int calculateNeighbours(int xCo, int yCo) {
        int neighbours = 0;

        for (int x = makeValidCoordinateX(xCo - 1); x <= makeValidCoordinateX(xCo + 1); x++) {
            for (int y = makeValidCoordinateY(yCo - 1); y <= makeValidCoordinateY(yCo + 1); y++) {
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
        if (i < 0) i = 0;
        else if (i > cols - 1) i = cols - 1;
        return i;
    }

    public int makeValidCoordinateY(int i) {
        if (i < 0) i = 0;
        else if (i > rows - 1) i = rows - 1;
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

    public void setNumberOfMines(int numberOfMines) {
        this.numberOfMines = numberOfMines;
    }

    public int getNumberOfMines() {
        return numberOfMines;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
