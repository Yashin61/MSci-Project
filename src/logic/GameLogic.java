package logic;
import algorithms.Algorithm;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GameLogic
{
	public static final int NUM_TILES_PER_ROW = 8; 	//8 tiles for each row and column
	public Piece[][] baseGameData = new Piece[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];	//storing 8x8 board layout
	public Piece[][] gameData = new Piece[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];	//storing piece data in the board
	private Player currentPlayer;
	private Player player1 = Player.RED;
	private boolean isSelected = false;	//indicating if there is a moving function processing
	public int[][] availablePlays = new int[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];	//storing available plays in the board
	private int storedRow, storedCol;
	public boolean isKingMove = false;
	private boolean bothAI = false;
	private int difficulty = 0;
	private Algorithm algorithm;

	public enum Piece
	{
		RED, RED_KING, BLUE, BLUE_KING;

		public Player getPlayer()
		{
			if(this == RED || this == RED_KING)
				return Player.RED;
			else
				return Player.BLUE;
		}

		private boolean isKing()
		{
			return this == RED_KING || this == BLUE_KING;
		}
	}

	public enum Player
	{
		RED, BLUE;

		public Player getOpposite()
		{
			if(this == RED)
				return BLUE;
			else return RED;
		}
	}

	public GameLogic(boolean playFirst, boolean bothAI, Algorithm algorithm, int difficulty, Player player)
	{
		this.bothAI = bothAI;
		this.algorithm = algorithm;
		this.difficulty = difficulty;
		initializeBoard();
		player1 = player;

		if(playFirst)
		{
			currentPlayer = player1;

			if(bothAI)
			{
				//avoiding hanging of the game
				new Thread(()->{
					try
					{
						Thread.sleep(100);
						Move m = algorithm.getAIMove(this, difficulty);
						m.makeMove(this, true);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}).start();
			}
		}
		else
		{
			currentPlayer = player1.getOpposite();
			new Thread(()->{
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				Move m = algorithm.getAIMove(this, difficulty);
				m.makeMove(this, true);
			}).start();
		}
	}

	public GameLogic(GameLogic gl, Move m)	//2 constructors???
	{
		baseGameData = new Piece[NUM_TILES_PER_ROW][];

		for(int i = 0; i < NUM_TILES_PER_ROW; i++)
			baseGameData[i] = Arrays.copyOf(gl.baseGameData[i], gl.baseGameData[i].length);

		gameData = new Piece[NUM_TILES_PER_ROW][];

		for(int i = 0; i < NUM_TILES_PER_ROW; i++)
			gameData[i] = Arrays.copyOf(gl.gameData[i], gl.gameData[i].length);

		currentPlayer = gl.currentPlayer;
		isKingMove = gl.isKingMove;
		m.makeMove(this);
	}

	public void initializeBoard()
	{
		for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			for(int row = 0; row < NUM_TILES_PER_ROW; row++)
				gameData[col][row] = null;

		for(int col = 0; col < NUM_TILES_PER_ROW; col++)
		{
			if(col%2 == 0)	//even
			{
				gameData[col][1] = Piece.BLUE;
				gameData[col][5] = Piece.RED;
				gameData[col][7] = Piece.RED;
			}
			else	//odd
			{
				gameData[col][0] = Piece.BLUE;
				gameData[col][2] = Piece.BLUE;
				gameData[col][6] = Piece.RED;
			}
		}
	}

	public void mousePressed(int col, int row)
	{
		if(currentPlayer != player1)
			return;
		if(!isSelected && gameData[col][row] != null || isSelected && checkTeamPiece(col, row))
		{
			if(!isKingMove || gameData[col][row].isKing())
			{
				resetPlay();
				storedCol = col;
				storedRow = row;	//setting the current click to instance variables to be used elsewhere???
				isSelected = true;
				getAvailablePlays(col, row, availablePlays);
			}
			else if(isKingMove)	//Warning:(153, 12) Condition 'isKingMove' is always 'true'???
			{
				resetPlay();
				swapPlayer(true);
				isKingMove = false;
			}
		}
		else if(isSelected && availablePlays[col][row] != 0)
			makeMove(col, row, storedCol, storedRow, true);
		else if(isSelected && availablePlays[col][row] == 0)
			resetPlay();
	}

	private void resetPlay()
	{
		storedCol = 0;
		storedRow = 0;
		isSelected = false;

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				availablePlays[col][row] = 0;
	}

	private void swapPlayer(boolean useAI)
	{
		if(currentPlayer == Player.RED)
			currentPlayer = Player.BLUE;
		else
			currentPlayer = Player.RED;
		if(useAI && currentPlayer != player1)
		{
			new Thread(()->{
//				long start = System.currentTimeMillis();
//				Move m = Minimax.getAIMove(this);
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				Move m = algorithm.getAIMove(this, difficulty);
//				long end = System.currentTimeMillis();
//				System.out.println(end-start);
				m.makeMove(this, true);
			}).start();  //dosent hang the prog
		}
		else if(bothAI && useAI && currentPlayer == player1)	//Warning:(202, 30) Condition 'currentPlayer == player1' is always 'true'???
		{
			new Thread(()->{
				try
				{
					Thread.sleep(100);
					Move m = algorithm.getAIMove(this, difficulty);
					m.makeMove(this, true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}).start();
		}
	}

	void makeMove(int col, int row, int storedCol, int storedRow, boolean useAI)
	{
		gameData[col][row] = gameData[storedCol][storedRow];
		gameData[storedCol][storedRow] = null;	//making old piece empty

		if(!gameData[col][row].isKing())
		{
			if(gameData[col][row].getPlayer() == Player.RED && row == 0)
				gameData[col][row] = Piece.RED_KING;
			else if(gameData[col][row].getPlayer() == Player.BLUE && row == 7)
				gameData[col][row] = Piece.BLUE_KING;
		}
		if(availablePlays[col][row] == 2)
		{
			removePiece(col, row, storedCol, storedRow);

			if(gameData[col][row].isKing())
			{
				resetPlay();
				getAvailablePlays(col, row, availablePlays);
				isKingMove = canMove();
			}
			else
				isKingMove = false;
		}
		else
			isKingMove = false;
		if(!isKingMove)
		{
			resetPlay();
			swapPlayer(useAI);
		}
		else
		{
			if(canJump())
			{
				isSelected = true;
				this.storedCol = col;
				this.storedRow = row;
			}
			else
			{
				resetPlay();
				swapPlayer(useAI);
				isKingMove = false;
			}
		}
	}

	private boolean canMove()
	{
		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				if(availablePlays[col][row] > 0)
					return true;

		return false;
	}

	private boolean canJump()	// 2 of this???
	{
		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				if(availablePlays[col][row] > 1)
					return true;

		return false;
	}

	/*
	checking if canJump is true. determine piece within movement. then check if it's an opponent piece, then if the space
	behind it is empty and in bounds 4 conditions based on column and row relations to the other piece
	*/
	private boolean canJump(int col, int row, int opponentCol, int opponentRow)
	{
		if(gameData[col][row].getPlayer() != gameData[opponentCol][opponentRow].getPlayer())
		{
			if(opponentCol == 0 || opponentCol == NUM_TILES_PER_ROW-1 || opponentRow == 0 || opponentRow == NUM_TILES_PER_ROW-1)
				return false;

			return true;
		}

		return false;
	}

	private void getAvailablePlays(int col, int row)	// 2 of this???
	{
		getAvailablePlays(col, row, availablePlays);
	}

	private void getAvailablePlays(int col, int row, int[][] plays)
	{
		if((checkTeamPiece(col, row)))	//checking if the piece is assigned to the current player
		{
			if(gameData[col][row] == Piece.RED)	//going up, checking the row above
				getUp(col, row, plays);
			if(gameData[col][row] == Piece.BLUE)	//going down, checking the row below
				getDown(col, row, plays);
			if(gameData[col][row] == Piece.RED_KING || gameData[col][row] == Piece.BLUE_KING)	//going up or down 1 row below
			{
				getUp(col, row, plays);
				getDown(col, row, plays);
			}
		}
	}

	//might be a better way to do this, but detects position of opponent piece based on destination and original position???
	private void removePiece(int col, int row, int storedCol, int storedRow)
	{
		int pieceRow = -1;
		int pieceCol = -1;

		if(col > storedCol && row > storedRow)
		{
			pieceRow = row - 1;
			pieceCol = col - 1;
		}
		if(col > storedCol && row < storedRow)
		{
			pieceRow = row + 1;
			pieceCol = col - 1;
		}
		if(col < storedCol && row > storedRow)
		{
			pieceRow = row - 1;
			pieceCol = col + 1;
		}
		if(col < storedCol && row < storedRow)
		{
			pieceRow = row + 1;
			pieceCol = col + 1;
		}

		gameData[pieceCol][pieceRow] = null;
	}

	private void getUp(int col, int row, int[][] plays)	//getting up availability
	{
		if(row == 0)
			return;

		int currentRow = row - 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)
				plays[currentCol][currentRow] = 1;
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
					plays[jumpPos[0]][jumpPos[1]] = 2;
			}
		}
	}

	private void getDown(int col, int row, int[][] plays)
	{
		if(row == NUM_TILES_PER_ROW-1)
			return;

		int currentRow = row + 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)
				plays[currentCol][currentRow] = 1;
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
					plays[jumpPos[0]][jumpPos[1]] = 2;
			}
		}
	}

	public boolean checkTeamPiece(int col, int row)
	{
		if(currentPlayer == Player.RED && (gameData[col][row] == Piece.RED || gameData[col][row] == Piece.RED_KING))	//bottom
			return true;
		if(currentPlayer == Player.BLUE && (gameData[col][row] == Piece.BLUE || gameData[col][row] == Piece.BLUE_KING))	//top
			return true;
		else
			return false;
	}

	private boolean isLegalPos(int col, int row)	//Warning:(412, 18) Boolean method 'isLegalPos' is always inverted???
	{
		if(row < 0 || row >= NUM_TILES_PER_ROW || col < 0 || col >= NUM_TILES_PER_ROW)
			return false;
		else
			return true;
	}

	private int[] getJumpPos(int col, int row, int opponentCol, int opponentRow)
	{
		if(col > opponentCol && row > opponentRow && gameData[col-2][row-2] == null)
			return new int[] {col-2, row-2};
		else if(col > opponentCol && row < opponentRow && gameData[col-2][row+2] == null)
			return new int[] {col-2, row+2};
		else if(col < opponentCol && row > opponentRow && gameData[col+2][row-2] == null)
			return new int[] {col+2, row-2};
		else if(col < opponentCol && row < opponentRow && gameData[col+2][row+2] == null)
			return new int[] {col+2, row+2};
		else
			return null;
	}

	public List<Move> getAllMoves()
	{
		List<Move> moves = new LinkedList<>();	//since looping over the full list, LinkedList is more efficient because and it has O(1) add??? ArrayList might need to be resized, so can be slower

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			{
				if(gameData[col][row] != null && gameData[col][row].getPlayer() == currentPlayer)
				{
					if(isKingMove && !gameData[col][row].isKing())
						continue;

					int[][] availableMoves = new int[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];
					getAvailablePlays(col, row, availableMoves);

					for(int moveRow = 0; moveRow < NUM_TILES_PER_ROW; moveRow++)
						for(int moveCol = 0; moveCol < NUM_TILES_PER_ROW; moveCol++)
							if(availableMoves[moveCol][moveRow] > 0)
								moves.add(new Move(col, row, moveCol, moveRow, availableMoves[moveCol][moveRow]==2));
				}
			}
		}

		return moves;
	}

	public float getCurrentPlayerScore()
	{
		int positive = 0;
		int negative = 0;

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			{
				Piece p = gameData[col][row];

				if(p != null)
					if(p.getPlayer() == currentPlayer)
						positive++;
				else	//think else should be inside the if???
					negative++;
			}
		}

		return (float)positive / negative;
	}

	public boolean gameOver()
	{
		return gameOverInternal(0, 0, 0, 0);
	}

	private boolean gameOverInternal(int col, int row, int red, int blue)
	{
		if(gameData[col][row] != null && gameData[col][row].getPlayer() == Player.RED)
			red += 1;
		if(gameData[col][row] != null && gameData[col][row].getPlayer() == Player.BLUE)
			blue += 1;
		if(col == NUM_TILES_PER_ROW-1 && row == NUM_TILES_PER_ROW-1)
		{
			if(red == 0 || blue == 0)	//Warning:(496, 4) 'if' statement can be simplified??
				return true;
			else
				return false;
		}
		if(col == NUM_TILES_PER_ROW-1)
		{
			row += 1;
			col = -1;
		}

		return gameOverInternal(col+1, row, red, blue);
	}


//	public int getCurrentPlayerScore(){
//		int score = 0;
//		for(int row = 0; row < NUM_TILES_PER_ROW; row++){
//			for(int col = 0; col < NUM_TILES_PER_ROW; col++){
//				Piece p = gameData[col][row];
//				if(p != null)
//					if(p.getPlayer() == currentPlayer)
//						score++;
//					else
//						score--;
//			}
//		}
//		return score;
//	}
//
//	private Player checkOpponent(int col, int row)
//	{
//		if(gameData[col][row].getPlayer() == Player.RED)
//			return Player.BLUE;
//		else
//			return Player.RED;
//	}
//
//	private void kingsExtraJumps(int col, int row)
//	{
//		Player opponent = checkOpponent(col, row);
//
//		if(gameData[col-1][row-1].getPlayer() == opponent)
//			availablePlays[col-2][row-2] = 1;
//		else if(gameData[col+1][row-1].getPlayer() == opponent)
//			availablePlays[col+2][row-2] = 1;
//		else if(gameData[col-1][row+1].getPlayer() == opponent)
//			availablePlays[col-2][row+2] = 1;
//		else if(gameData[col+1][row+1].getPlayer() == opponent)
//			availablePlays[col+2][row+2] = 1;
//
//		new Thread(()->BoardUI.instance.repaint()).start();
//	}
}