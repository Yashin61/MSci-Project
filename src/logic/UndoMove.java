package logic;

class UndoMove
{
	private int col;
	private int row;
	private GameLogic.Piece type;

	public UndoMove(int col, int row, GameLogic.Piece type)
	{
		this.col = col;
		this.row = row;
		this.type = type;
	}

	public int getCol()
	{
		return col;
	}

	public int getRow()
	{
		return row;
	}

	public GameLogic.Piece getType()
	{
		return type;
	}
}