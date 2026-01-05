package model;

public class Cell {

    private boolean mine;
    private SpecialBoxType specialBox;
    private String content;
    private int surroundingMines;

    // ---------------- TEMPLATE METHOD ----------------

    /**
     * Template Method – defines the general flow of a cell click
     */
    public final void onClick(CellActionContext ctx) {

        if ("F".equals(content) || "USED".equals(content)) {
            return;
        }

        // MINE – always a move
        if (mine) {
            ctx.handleMine(this);
            return;
        }

        // SURPRISE
        if (specialBox == SpecialBoxType.SURPRISE) {
            boolean wasEmpty = content.equals("");
            ctx.handleSurprise(this);
            if (wasEmpty || "USED".equals(content)) {
                ctx.switchTurn();
            }
            return;
        }

        // QUESTION
        if (specialBox == SpecialBoxType.QUESTION) {
            boolean wasEmpty = content.equals("");
            ctx.handleQuestion(this);
            if (wasEmpty || "USED".equals(content)) {
                ctx.switchTurn();
            }
            return;
        }

        // SAFE CELL – always a move
        ctx.handleSafeCell(this);
    }


    // ---------------- HOOK METHODS ----------------

    protected void reveal() {
        content = ""; // still hidden visually, but revealed logically
    }

    protected void handleFirstReveal(CellActionContext ctx) {
        // default behavior – can be overridden logically by context
        ctx.handleSafeCell(this);
    }

    protected void handleSecondClick(CellActionContext ctx) {
        // default: do nothing
    }

    // ---------------- EXISTING CODE ----------------
    public Cell() {
        mine = false;
        specialBox = SpecialBoxType.NONE;
        content = "";
        surroundingMines = 0;
    }

    public boolean getMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }

    public SpecialBoxType getSpecialBox() { return specialBox; }
    public void setSpecialBox(SpecialBoxType specialBox) { this.specialBox = specialBox; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getSurroundingMines() { return surroundingMines; }
    public void setSurroundingMines(int surroundingMines) {
        this.surroundingMines = surroundingMines;
    }
}
