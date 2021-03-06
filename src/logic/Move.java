package logic;
import ui.BoardUI;

public class Move
{
	private int colFrom, rowFrom, colTo, rowTo;
	private boolean isJump;

	public Move(int colFrom, int rowFrom, int colTo, int rowTo, boolean isJump)
	{
		this.colFrom = colFrom;
		this.rowFrom = rowFrom;
		this.colTo = colTo;
		this.rowTo = rowTo;
		this.isJump = isJump;
	}

	void makeMove(GameLogic gl, boolean repaint)	//making this move on a given board
	{
		gl.getAvailablePlays(colFrom, rowFrom);
		gl.makeMove(colTo, rowTo, colFrom, rowFrom, repaint);

		if(repaint)
			new Thread(() -> BoardUI.getInstance().repaint()).start();
	}

	void makeMove(GameLogic gl)	//for background checking
	{
		makeMove(gl, false);	//it gets true when finishes the move and repaints the board
	}

	public boolean isJump()
	{
		return isJump;
	}

	public int getColFrom()
	{
		return colFrom;
	}

	public int getRowFrom()
	{
		return rowFrom;
	}

	public int getColTo()
	{
		return colTo;
	}

	public int getRowTo()
	{
		return rowTo;
	}
}