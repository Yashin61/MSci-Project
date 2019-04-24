package logic;
import algorithms.Algorithm;
import ui.BoardUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

public class GameLogic
{
	public static final int NUM_TILES_PER_ROW = 8; 	//8 tiles for each row and column
	public Piece[][] gameData = new Piece[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];	//storing piece data in the board
	private Player currentPlayer;	//whose turn is it
	private Player player1 = Player.RED;	//playing first
	private boolean isSelected = false;	//if a piece is selected
	private boolean playFirst;
	public int[][] availablePlays = new int[NUM_TILES_PER_ROW][NUM_TILES_PER_ROW];	//storing available plays in the board
	private int storedRow, storedCol; //which tile is currently selected
	public boolean isKingMove = false;
	private boolean bothAI = false;
	private int difficulty = 0;
	private boolean forceJump;
	private Algorithm algorithm;
	public Move lastMove;
	private boolean hasJump = false;
	public List<List<UndoMove>> undoMoves;


	public enum Piece	//lists all the possible values of all specific types
	{
		RED(1), RED_KING(1.5f), BLUE(1), BLUE_KING(1.5f);

		Piece(float value)
		{
			this.value = value;
		}

		public Player getPlayer()	//returns which player the selected piece belongs to
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

		public void drawPiece(Graphics2D g, int x, int y, boolean isSelected)
		{
			g.setColor(getPlayer().color);

			if(isSelected)
			{
				g.fillOval(x+2, y+2, BoardUI.TILE_SIZE-4, BoardUI.TILE_SIZE-4);

				if(isKing())
				{
					Color c = getPlayer().color.darker();
					g.setStroke(new BasicStroke(2));
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha()*0.75f));

					for(int i = 0; i < 8; i++)
					{
						g.setColor(c);
						c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha()*0.75f));
						g.drawOval(x+i*4+8, y+i*4+8, BoardUI.TILE_SIZE-16-i*8, BoardUI.TILE_SIZE-16-i*8);
					}
				}
			}
			else
			{
				g.setStroke(new BasicStroke(4));
				g.drawOval(x+4, y+4, BoardUI.TILE_SIZE-8, BoardUI.TILE_SIZE-8);

				if(isKing())
				{
					g.setStroke(new BasicStroke(2));
					Color c = getPlayer().color;
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha()*0.75f));

					for(int i = 0; i < 8; i++)
					{
						g.setColor(c);
						c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha()*0.75f));
						g.drawOval(x+i*4+8, y+i*4+8, BoardUI.TILE_SIZE-16-i*8, BoardUI.TILE_SIZE-16-i*8);
					}
				}
			}
		}

		public final float value;
	}

	public enum Player
	{
		RED(new Color(255,50,20)), BLUE(new Color(20,120,255));

		Player(Color c)
		{
			this.color = c;
		}

		public Player getOpposite()	//calling on Red, it gives Blue and vice versa
		{
			if(this == RED)
				return BLUE;
			else
				return RED;
		}

		public final Color color;
	}

	public GameLogic(boolean playFirst, boolean bothAI, Algorithm algorithm, int difficulty, Player player, boolean forceJump)
	{
		this.bothAI = bothAI;
		this.algorithm = algorithm;
		this.difficulty = difficulty;
		player1 = player;
		this.playFirst = playFirst;
		this.forceJump = forceJump;
		this.undoMoves = new ArrayList<>();
		initializeBoard();
	}

	public GameLogic(GameLogic gl, Move m)	//for background simulation (it makes the run of the game efficient)
	{
		gameData = new Piece[NUM_TILES_PER_ROW][];

		for(int i = 0; i < NUM_TILES_PER_ROW; i++)
			gameData[i] = Arrays.copyOf(gl.gameData[i], gl.gameData[i].length);

		currentPlayer = gl.currentPlayer;
		isKingMove = gl.isKingMove;
		undoMoves = gl.undoMoves;
		m.makeMove(this);
	}

	public void initializeBoard()
	{
		lastMove = null;

		for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			for(int row = 0; row < NUM_TILES_PER_ROW; row++)
				gameData[col][row] = null;

		for(int col = 0; col < NUM_TILES_PER_ROW; col++)	//puts appropriate pieces in the appropriate squares
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

		if(playFirst)
		{
			currentPlayer = player1;

			if(bothAI)
			{
				//avoiding hanging of the game
				new Thread(() -> {
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

			new Thread(() -> {
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

	public void mousePressed(int col, int row)
	{
		if(bothAI || currentPlayer != player1)
			return;
		if((!isSelected && gameData[col][row] != null) || (isSelected && checkTeamPiece(col, row)))
		{
			if(!isKingMove || gameData[col][row].isKing()) //if we are dealing with a king move
			{
				resetPlay();
				storedCol = col;
				storedRow = row;	//setting the current click to instance variables to be used elsewhere
				isSelected = true;
				int numMoves = getAvailablePlays(col, row, availablePlays);

				if(numMoves == 0)
					resetPlay();
			}
			else
			{
				resetPlay();
				swapPlayer();
				nextMove(true);
				isKingMove = false;
			}
		}
		else if(isSelected && availablePlays[col][row] != 0)	//if a piece is selected and there is an available move for it, move
			makeMove(col, row, storedCol, storedRow, true);
		else if(isSelected && availablePlays[col][row] == 0)	//if clicked on blank space, then deselect
			resetPlay();
	}

	private void resetPlay()	//deselects
	{
		storedCol = 0;
		storedRow = 0;
		isSelected = false;

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				availablePlays[col][row] = 0;
	}

	private void makeAIMove()
	{
		//it does not hang the program while the AI calculates
		new Thread(() -> {
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

	private void nextMove(boolean useAI)	//checks weather the AI needs to play
	{
		hasJump = false;
		List<Move> moves = getAllMoves();

		for(Move m : moves)
		{
			if(m.isJump())
			{
				hasJump = true;
				break;
			}
		}

		if(useAI)
		{
			if(gameOver())
			{
				Player winner = getWinner();
				JOptionPane.showMessageDialog(null, winner + " wins!");
			}
			if(currentPlayer != player1){
				makeAIMove();
			}
			else if(bothAI && currentPlayer == player1)
				makeAIMove();
		}
	}

	public void undo()
	{
		ArrayList<UndoMove> lastMoves = (ArrayList<UndoMove>)undoMoves.get(undoMoves.size()-1);

		for(int i = 0; i<lastMoves.size();i++)
		{
			int col = lastMoves.get(i).getCol();
			int row = lastMoves.get(i).getRow();
			Piece type = lastMoves.get(i).getType();
			gameData[col][row] = type;
			swapPlayer();
		}
	}

	private void swapPlayer()
	{
		if(currentPlayer == Player.RED)
			currentPlayer = Player.BLUE;
		else
			currentPlayer = Player.RED;
	}

	void makeMove(int col, int row, int storedCol, int storedRow, boolean useAI)
	{
		lastMove = new Move(storedCol, storedRow, col, row, false);
		gameData[col][row] = gameData[storedCol][storedRow];
		gameData[storedCol][storedRow] = null;	//making old piece empty
		ArrayList addToUndo = new ArrayList<UndoMove>();
		addToUndo.add(new UndoMove(storedRow, storedCol, gameData[storedCol][storedRow]));	//before
		addToUndo.add(new UndoMove(col, row, gameData[col][row]));	//after

		if(!gameData[col][row].isKing())
		{
			if(gameData[col][row].getPlayer() == Player.RED && row == 0)	//checking for the last raw to turn the piece to king in correct colour
				gameData[col][row] = Piece.RED_KING;
			else if(gameData[col][row].getPlayer() == Player.BLUE && row == 7)
				gameData[col][row] = Piece.BLUE_KING;
		}
		if(availablePlays[col][row] == 2)	//if it is a jump move, then remove the piece. (1 is a normal move)
		{
			removePiece(col, row, storedCol, storedRow);

			if(gameData[col][row].isKing())	//if the piece just moved is a king, then see for other moves and capturing
			{
				resetPlay();
				getAvailablePlays(col, row, availablePlays);
				isKingMove = canMove();	//keep it as a king move
			}
			else
				isKingMove = false;
		}
		else
			isKingMove = false;
		//end of move
		if(!isKingMove)
		{
			resetPlay();
			swapPlayer();	//not a king move, then swap the players
			nextMove(useAI);
		}
		else	//a king move, then check for available jumps
		{
			if(isJumpAvailable())	//keep the same player and make the jump
			{
				resetPlay();
				isSelected = true;
				this.storedCol = col;
				this.storedRow = row;
				nextMove(useAI);
			}
			else
			{
				resetPlay();
				swapPlayer();
				isKingMove = false;
				nextMove(useAI);
			}
		}

		this.undoMoves.add(addToUndo);
	}

	private boolean canMove()	//says whether a player can move or not
	{
		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				if(availablePlays[col][row] > 0)
					return true;

		return false;
	}

	public boolean canMove(int col, int row)
	{
		int numMoves = getAvailablePlays(col, row, new int[8][8]);
		return numMoves != 0;
	}

	private boolean isJumpAvailable()
	{
		return findJump(availablePlays);
	}

	private boolean findJump(int[][] availablePlays)
	{
		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
				if(availablePlays[col][row] > 1)
					return true;

		return false;
	}

	public boolean isSelected(int col, int row)
	{
		if(!isSelected)
			return false;

		return col == storedCol && row == storedRow;
	}

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

	void getAvailablePlays(int col, int row)
	{
		getAvailablePlays(col, row, availablePlays);
	}

	private int getAvailablePlays(int col, int row, int[][] plays)
	{
		int numMoves = 0;

		if((checkTeamPiece(col, row)))	//checking if the piece is assigned to the current player
		{
			if(gameData[col][row] == Piece.RED)	//going up, checking the row above
				numMoves += getUp(col, row, plays);
			if(gameData[col][row] == Piece.BLUE)	//going down, checking the row below
				numMoves += getDown(col, row, plays);
			if(gameData[col][row] == Piece.RED_KING || gameData[col][row] == Piece.BLUE_KING)	//going up or down 1 row below
			{
				numMoves += getUp(col, row, plays);
				numMoves += getDown(col, row, plays);
			}
		}

		return numMoves;
	}

	private void removePiece(int col, int row, int storedCol, int storedRow)	//detecting the position of opponent piece based on destination and original position
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

	private int getUp(int col, int row, int[][] plays)	//for Red pieces
	{
		if(row == 0)
			return 0;

		int numMoves = 0;
		int currentRow = row - 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)	//if it is empty, then the piece can go there
			{
				if(!forceJump || !hasJump)
				{
					plays[currentCol][currentRow] = 1;
					numMoves ++;
				}
			}
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
				{
					plays[jumpPos[0]][jumpPos[1]] = 2;	//if it is not empty, but the piece can jump over it, then set it to 2 to show that it is a jump
					numMoves++;
				}
			}
		}

		return numMoves;
	}

	private int getDown(int col, int row, int[][] plays)	//for Blue pieces
	{
		if(row == NUM_TILES_PER_ROW-1)
			return 0;

		int numMoves = 0;
		int currentRow = row + 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)
			{
				if(!forceJump || !hasJump)
				{
					plays[currentCol][currentRow] = 1;
					numMoves ++;
				}
			}
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
				{
					plays[jumpPos[0]][jumpPos[1]] = 2;
					numMoves ++;
				}
			}
		}

		return numMoves;
	}

	public boolean checkTeamPiece(int col, int row)	//checking if the specified piece belongs to the current player whose turn it is
	{
		if(gameData[col][row] == null)
			return false;

		return gameData[col][row].getPlayer() == currentPlayer;
	}

	private boolean isLegalPos(int col, int row)	//if the position specifying is on the board
	{
		return !(row < 0 || row >= NUM_TILES_PER_ROW || col < 0 || col >= NUM_TILES_PER_ROW);
	}

	private int[] getJumpPos(int col, int row, int opponentCol, int opponentRow)	//depending on where the current piece and opponent are, what the position is for the jump
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
	public List<Move> getAllMoves()	//returns a list of all possible moves for the player
	{
		List<Move> moves = new LinkedList<>();	//since looping over the full list and it has O(1) for adding

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
		return getPlayerScore(currentPlayer);
	}

	private float getPlayerScore(Player player)
	{
		float positive = 0;
		float total = 0;

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			{
				Piece p = gameData[col][row];

				if(p != null)
				{
					if(p.getPlayer() == player)
						positive+=p.value;

					total+=p.value;
				}
			}
		}

		return positive / total;
	}

	public boolean gameOver()
	{
		List<Move> moves = getAllMoves();
		return moves.size() == 0 || getCurrentPlayerScore() == 0 || getPlayerScore(currentPlayer.getOpposite()) == 0;
	}

	public Player getWinner()	//fining who has won the game
	{
		if(!gameOver())
			return null;

		int current = 0;
		int opp = 0;

		for(int row = 0; row < NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < NUM_TILES_PER_ROW; col++)
			{
				if(gameData[row][col] == null)
					continue;
				if(gameData[row][col].getPlayer() == currentPlayer)
					current ++;
				else
					opp ++;
			}
		}

		if(current == 0)
			return currentPlayer.getOpposite();
		if(opp == 0)
			return currentPlayer;

		List<Move> moves = getAllMoves();

		if(moves.size() == 0)
			return currentPlayer.getOpposite();

		return currentPlayer;
	}

	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}
}