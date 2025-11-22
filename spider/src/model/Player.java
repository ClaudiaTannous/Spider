package model;

public class Player {
    private final String name;
    private int score = 0;

    public Player(String name){ this.name = name; }
    public String getName(){ return name; }
    public int getScore(){ return score; }
    public void addScore(int delta){ score += delta; }
	public void setScore(int score) {
		this.score = score;
	}
}
