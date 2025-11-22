package model;

import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.sql.Date;


public class Score {

    private ArrayList<Time> bestTimes;
    private int gamesPlayed;
    private int gamesWon;
    private int longestWinningStreak;
    private int longestLosingStreak;
    private int currentStreak;
    private int currentWinningStreak;
    private int currentLosingStreak;

    // -------------------- CONSTRUCTOR ------------------------ //
    public Score() {
        resetScore();
        bestTimes = new ArrayList<>();
    }

    // -------------------- GETTERS ------------------------ //
    public int getGamesPlayed() { return gamesPlayed; }

    public int getGamesWon() { return gamesWon; }

    public int getWinPercentage() {
        if (gamesPlayed == 0) return 0;
        double percentage = ceil(((double) gamesWon / gamesPlayed) * 100);
        return (int) percentage;
    }

    public int getLongestWinningStreak() { return longestWinningStreak; }

    public int getLongestLosingStreak() { return longestLosingStreak; }

    public int getCurrentStreak() { return currentStreak; }

    public int getCurrentLosingStreak() { return currentLosingStreak; }

    public int getCurrentWinningStreak() { return currentWinningStreak; }

    public ArrayList<Time> getBestTimes() { return bestTimes; }

    // -------------------- INCREMENT FUNCTIONS ------------------------ //
    public void incGamesWon() { gamesWon++; }

    public void incGamesPlayed() { gamesPlayed++; }

    public void incCurrentStreak() { currentStreak++; }

    public void incCurrentLosingStreak() {
        currentLosingStreak++;
        if (longestLosingStreak < currentLosingStreak)
            longestLosingStreak = currentLosingStreak;
    }

    public void incCurrentWinningStreak() {
        currentWinningStreak++;
        if (longestWinningStreak < currentWinningStreak)
            longestWinningStreak = currentWinningStreak;
    }

    public void decCurrentStreak() { currentStreak--; }

    public void resetScore() {
        gamesPlayed = gamesWon = currentStreak =
        longestLosingStreak = longestWinningStreak =
        currentWinningStreak = currentLosingStreak = 0;
        bestTimes = new ArrayList<>();
    }

    // -------------------- BEST TIMES ------------------------ //
    public void addTime(int time, Date date) {
        bestTimes.add(new Time(time, date));
        Collections.sort(bestTimes, new TimeComparator());
        if (bestTimes.size() > 5)
            bestTimes.remove(bestTimes.size() - 1);
    }

   
    public boolean populate() {
        
        return true;
    }

    public void save() {
     
    }

    // -------------------- INNER CLASSES ------------------------ //
    public class TimeComparator implements Comparator<Time> {
        @Override
        public int compare(Time a, Time b) {
            return Integer.compare(a.getTimeValue(), b.getTimeValue());
        }
    }

    public class Time {
        private final Date date;
        private final int time;

        public Time(int t, Date d) {
            this.time = t;
            this.date = d;
        }

        public Date getDateValue() { return date; }

        public int getTimeValue() { return time; }
    }
}
