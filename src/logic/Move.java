package logic;
import ui.BoardUI;

public class Move
{
	int colFrom, rowFrom, colTo, rowTo;	//why not private???
	boolean isJump;	//why not private???

	public Move(int colFrom, int rowFrom, int colTo, int rowTo, boolean isJump)
	{
		this.colFrom = colFrom;
		this.rowFrom = rowFrom;
		this.colTo = colTo;
		this.rowTo = rowTo;
		this.isJump = isJump;
	}

	void makeMove(GameLogic gl, boolean repaint)
	{
		gl.getAvailablePlays(colFrom, rowFrom);
		gl.makeMove(colTo, rowTo, colFrom, rowFrom, repaint);

		if(repaint)
			new Thread(()->BoardUI.instance.repaint()).start();
	}

	void makeMove(GameLogic gl)
	{
		makeMove(gl, false);
	}
}