package model;

public enum SpecialBoxType {
    NONE,
    SURPRISE,
    QUESTION,
    HEART,
    DICE;

    @Override
    public String toString() {
        return switch (this) {
            case SURPRISE -> "S";
            case QUESTION -> "Q";
            case HEART   -> "H";
            case DICE    -> "D";
            default      -> "";
        };
    }
}
