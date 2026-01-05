package model;

public interface CellActionContext {

    void handleMine(Cell cell);

    void handleQuestion(Cell cell);

    void handleSurprise(Cell cell);

    void handleSafeCell(Cell cell);

	void switchTurn();
}
