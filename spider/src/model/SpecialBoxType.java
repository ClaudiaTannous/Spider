package model;

public enum SpecialBoxType {
    NONE,
    SURPRISE,    
    QUESTION;    
    
    @Override
    public String toString() {
        switch(this) {
            case SURPRISE: return "S";
            case QUESTION: return "Q";
            default: return "";
        }
    }
}
