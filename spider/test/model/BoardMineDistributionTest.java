package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardMineDistributionTest {

	private int countMines(Board board) {
		Cell[][] cells = board.getCells();
		int mines = 0;

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				if (cells[x][y].getMine()) {
					mines++;
				}
			}
		}
		return mines;
	}

	@Test
	public void testMineCountMatchesDifficulty_Easy() {
		Difficulty diff = Difficulty.EASY;
		Board board = new Board(diff);

		int minesOnBoard = countMines(board);

		assertEquals("Wrong number of mines for EASY difficulty", diff.getMines(), minesOnBoard);
	}

	@Test
	public void testMineCountMatchesDifficulty_Medium() {
		Difficulty diff = Difficulty.MEDIUM;
		Board board = new Board(diff);

		int minesOnBoard = countMines(board);

		assertEquals("Wrong number of mines for MEDIUM difficulty", diff.getMines(), minesOnBoard);
	}

	@Test
	public void testMineCountMatchesDifficulty_Hard() {
		Difficulty diff = Difficulty.HARD;
		Board board = new Board(diff);

		int minesOnBoard = countMines(board);

		assertEquals("Wrong number of mines for HARD difficulty", diff.getMines(), minesOnBoard);
	}

	@Test
	public void testNoNegativeMines() {
		Board board = new Board(Difficulty.EASY);
		Cell[][] cells = board.getCells();

		for (int x = 0; x < board.getCols(); x++) {
			for (int y = 0; y < board.getRows(); y++) {
				assertNotNull("Cell should not be null", cells[x][y]);
			}
		}
	}
}
