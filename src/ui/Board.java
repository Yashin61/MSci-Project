package ui;	// How can I avoid having it?
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;

public class Board extends JPanel implements ActionListener, MouseListener
{
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

		private boolean isKing() { return this == RED_KING || this == BLUE_KING; }
	}

	public enum Player { RED, BLUE }
//	private static final double GOLDEN_RATIO = 1.618;
//	private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//	private int monitorWidth = gd.getDisplayMode().getWidth();
//	private static int width = Math.round(monitorWidth / goldenRatio), height = width;
	private static int width = 720, height = width;	// square parameters
	private static final int numTilesPerRow = 8; 	// 8 tiles for each row and column
	private static final int tileSize = width / numTilesPerRow;
	private static Piece[][] baseGameData = new Piece[numTilesPerRow][numTilesPerRow];	// storing 8x8 board layout
	private static Piece[][] gameData = new Piece[numTilesPerRow][numTilesPerRow];	// storing piece data in the board
	private Player currentPlayer = Player.RED;
	private Player computer = Player.BLUE;
	private boolean isSelected = false;	// indicating if there is a moving function processing
	private static int[][] availablePlays = new int[numTilesPerRow][numTilesPerRow];	// storing available plays in the board
	private int storedRow, storedCol;
	private static BufferedImage crownImage = null;

	public Board()
	{
		window(width, height, this);
		initializeBoard();
		repaint();
	}

	// abstract methods
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void actionPerformed(ActionEvent e) {}

	public void mousePressed(java.awt.event.MouseEvent evt)	// this method must be public
	{
		int col = (evt.getX() - 8) / tileSize;	// 8 is left frame length
		int row = (evt.getY() - 30) / tileSize;	// 30 is top frame length

		if(!isSelected && gameData[col][row] != null || isSelected && checkTeamPiece(col, row))
		{
			resetPlay();
			storedCol = col;
			storedRow = row;	// setting the current click to instance variables to be used elsewhere?
			isSelected = true;
			getAvailablePlays(col, row);
//			if(gameData[col][row].isKing()){
//				TODO: Additional jumps for king
//			}
		}
		else if(isSelected && availablePlays[col][row] != 0)
			makeMove(col, row, storedCol, storedRow);
		else if(isSelected && availablePlays[col][row] == 0)
			resetPlay();
	}

	private static BufferedImage imageReader()
	{
		try
		{
			crownImage = ImageIO.read(new File("Crown.png"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return crownImage;
	}

	private boolean gameOver() { return gameOverInternal(0, 0, 0, 0); }

	private boolean gameOverInternal(int col, int row, int red, int blue)
	{
		if(gameData[col][row] != null && gameData[col][row].getPlayer() == Player.RED)
			red += 1;
		if(gameData[col][row] != null && gameData[col][row].getPlayer() == Player.BLUE)
			blue += 1;
		if(col == numTilesPerRow-1 && row == numTilesPerRow-1)
		{
			if(red == 0 || blue == 0)
				return true;
			else
				return false;
		}
		if(col == numTilesPerRow-1)
		{
			row += 1;
			col = -1;
		}

		return gameOverInternal(col+1, row, red, blue);
	}

	private void window(int width, int height, Board game)
	{
		JFrame frame = new JFrame();
		frame.setSize(width, height);
//		frame.setIconImage(crownImage);	// I also changed crownImage to imageReader() in other places. How to do it without method calling?
		frame.setIconImage(imageReader());
		frame.setBackground(Color.green);
		frame.setLocationRelativeTo(null);
		frame.pack();
		Insets insets = frame.getInsets();
		int frameLeftBorder = insets.left;
		int frameRightBorder = insets.right;
		int frameTopBorder = insets.top;
		int frameBottomBorder = insets.bottom;
		frame.setPreferredSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setMaximumSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setMinimumSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addMouseListener(this);
		frame.requestFocus();
		frame.setVisible(true);
		frame.add(game);
	}

	private void initializeBoard()
	{
		for(int col = 0; col < numTilesPerRow; col++)
		{
			if(col%2 == 0)	// even
			{
				gameData[col][1] = Piece.BLUE;
				gameData[col][5] = Piece.RED;
				gameData[col][7] = Piece.RED;
			}
			else	// odd
			{
				gameData[col][0] = Piece.BLUE;
				gameData[col][2] = Piece.BLUE;
				gameData[col][6] = Piece.RED;
			}
		}
	}

	private static void drawPiece(int x, int y, Graphics g, Color color)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(color);
		g.fillOval(x, y, tileSize-4, tileSize-4);
	}

	public void paint(Graphics g)	// this method must be public
	{
		super.paintComponent(g);	// painting the board and the pieces

		for(int row = 0; row < numTilesPerRow; row++)
		{
			for(int col = 0; col < numTilesPerRow; col++)
			{
				if((row%2 == 0 && col%2 == 0) || (row%2 != 0 && col%2 != 0))	// assigning board pattern
				{
					baseGameData[col][row] = null;
					g.setColor(Color.black);
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				else
				{
					baseGameData[col][row] = Piece.RED;
					g.setColor(Color.white);
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(checkTeamPiece(col, row))
				{
					g.setColor(Color.white.darker());
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(availablePlays[col][row] != 0)
				{
					g.setColor(Color.GREEN.darker());
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(gameData[col][row] == Piece.BLUE)
				{
					drawPiece((col*tileSize)+2, (row*tileSize)+2, g, Color.blue);
					g.drawOval((col*tileSize)+1, (row*tileSize)+1, tileSize-4, tileSize-4);
				}
				else if(gameData[col][row] == Piece.BLUE_KING)
				{
					drawPiece((col*tileSize)+2, (row*tileSize)+2, g, Color.blue);
					g.drawImage(crownImage, (col*tileSize)+6, (row*tileSize)+6, tileSize-12, tileSize-12, null);
				}

				else if(gameData[col][row] == Piece.RED)
				{
					drawPiece((col*tileSize)+1, (row*tileSize)+1, g, Color.red);
					g.drawOval((col*tileSize)+2, (row*tileSize)+2, tileSize-4, tileSize-4);
				}
				else if(gameData[col][row] == Piece.RED_KING)
				{
					drawPiece((col*tileSize)+1, (row*tileSize)+1, g, Color.red);
					g.drawImage(crownImage, (col*tileSize)+6, (row*tileSize)+6, tileSize-12, tileSize-12, null);
				}
			}
		}

		if(gameOver() == true)
			gameOverDisplay();
	}

	private void gameOverDisplay()	// once game ends, displaying a message
	{
		JOptionPane.showMessageDialog(null,"You won :)","END OF THE GAME!", JOptionPane.WARNING_MESSAGE);
	}

	private void resetPlay()
	{
		storedCol = 0;
		storedRow = 0;
		isSelected = false;

		for(int row = 0; row < numTilesPerRow; row++)
			for(int col = 0; col < numTilesPerRow; col++)
				availablePlays[col][row] = 0;

		repaint();
	}

	private void swapPlayer()
	{
		if(currentPlayer == Player.RED)
			currentPlayer = Player.BLUE;
		else currentPlayer = Player.RED;
	}

	private void makeMove(int col, int row, int storedCol, int storedRow)
	{
		gameData[col][row] = gameData[storedCol][storedRow];
		gameData[storedCol][storedRow] = null;	// making old piece empty

		if(!gameData[col][row].isKing())
		{
			if(gameData[col][row].getPlayer() == Player.RED && row == 0)
				gameData[col][row] = Piece.RED_KING;
			else if(gameData[col][row].getPlayer() == Player.BLUE && row == 7)
				gameData[col][row] = Piece.BLUE_KING;
		}
		if(availablePlays[col][row] == 2)
			removePiece(col, row, storedCol, storedRow);

		resetPlay();
		swapPlayer();
	}

	// might be a better way to do this, but detects position of opponent piece based on destination and original position?
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

	private void getAvailablePlays(int col, int row)
	{

		if((checkTeamPiece(col, row)))	// checking if the piece is assigned to the current player
		{
			if(gameData[col][row] == Piece.RED)	// going up, checking the row above it's own
				getUp(col, row);
			if(gameData[col][row] == Piece.BLUE)	// going down, checking the row below it's own
				getDown(col, row);
			if(gameData[col][row] == Piece.RED_KING || gameData[col][row] == Piece.BLUE_KING)	// going up or down 1 row below it's own
			{
				getUp(col, row);
				getDown(col, row);
			}

			repaint();
		}
	}

	private void getUp(int col, int row)	// getting up availability
	{
		if(row == 0)
			return;

		int currentRow = row - 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)
				availablePlays[currentCol][currentRow] = 1;
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
					availablePlays[jumpPos[0]][jumpPos[1]] = 2;
			}
		}
	}

	private void getDown(int col, int row)
	{
		if(row == numTilesPerRow-1)
			return;

		int currentRow = row + 1;

		for(int currentCol = col-1; currentCol < col+2; currentCol +=2)
		{
			if(!isLegalPos(currentCol, currentRow))
				continue;
			if(gameData[currentCol][currentRow] == null)
				availablePlays[currentCol][currentRow] = 1;
			else if(canJump(col, row, currentCol, currentRow))
			{
				int[] jumpPos = getJumpPos(col, row, currentCol, currentRow);

				if(jumpPos != null)
					availablePlays[jumpPos[0]][jumpPos[1]] = 2;
			}
		}
	}

	private boolean checkTeamPiece(int col, int row)
	{
		if(currentPlayer == Player.RED && (gameData[col][row] == Piece.RED || gameData[col][row] == Piece.RED_KING))	// bottom
			return true;
		if(currentPlayer == Player.BLUE && (gameData[col][row] == Piece.BLUE || gameData[col][row] == Piece.BLUE_KING))	// top
			return true;
		else
			return false;
	}

	private boolean isLegalPos(int col, int row)
	{
		if(row < 0 || row >= numTilesPerRow || col < 0 || col >= numTilesPerRow)
			return false;
		else
			return true;
	}

	/*
	checking if canJump is true: determine piece within movement. then check if its an opponent piece, then if the space
	behind it is empty and in bounds 4 conditions based on column and row relations to the other piece
	*/
	private boolean canJump(int col, int row, int opponentCol, int opponentRow)
	{
		if(gameData[col][row].getPlayer() != gameData[opponentCol][opponentRow].getPlayer())
		{
			if(opponentCol == 0 || opponentCol == numTilesPerRow-1 || opponentRow == 0 || opponentRow == numTilesPerRow-1)
				return false;

			return true;
//			int[] toData = getJumpPos(col, row, opponentCol, opponentRow);
//
//			if(gameData[toData[0]][toData[1]] == null)
//			{
//				isJump = true; //TODO: why
//				return true;
//			}
		}

		return false;
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

	private Player checkOpponent(int col, int row)
	{
		if(gameData[col][row].getPlayer() == Player.RED)
			return Player.BLUE;
		else
			return Player.RED;
	}

	private void checkExtraJumps(int col, int row)
	{
		Player opponent = checkOpponent(col, row);

		if(gameData[col-1][row-1].getPlayer() == opponent)
			availablePlays[col-2][row-2] = 1;
		else if(gameData[col+1][row-1].getPlayer() == opponent)
			availablePlays[col+2][row-2] = 1;
		else if(gameData[col-1][row+1].getPlayer() == opponent)
			availablePlays[col-2][row+2] = 1;
		else if(gameData[col+1][row+1].getPlayer() == opponent)
			availablePlays[col+2][row+2] = 1;

		repaint();
	}

//	private void checkKing(int col, int row)
//	{
//		if(gameData[col][row] == RED && row == 0)
//			gameData[col][row] = RED_KING;
//		else if(gameData[col][row] == BLUE && row == numTilesPerRow-1)
//			gameData[col][row] = BLUE_KING;
//		else return;
//	}

//	private void gameOverDisplay(Graphics g)
//	{
//		String msg = "Game Over!";
//		Font format = new Font("Helvetica", Font.BOLD, 20);
//		FontMetrics metr = getFontMetrics(format);
//		g.setColor(Color.black);
//		g.setFont(format);
//		g.drawString(msg, (width - metr.stringWidth(msg)) / 2, width / 2);
//	}
}