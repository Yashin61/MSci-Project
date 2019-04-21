package ui;
import algorithms.Algorithm;
import logic.GameLogic;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BoardUI extends JPanel implements MouseListener
{
	private static BoardUI instance;	//for singleton design pattern
	private static final double GOLDEN_RATIO = 1.618;
	private static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private static int monitorHeight = gd.getDisplayMode().getHeight();
	private static int width = ((int)Math.round(monitorHeight / GOLDEN_RATIO) / 8) * 8;
	private static int height = width;
	public static final int TILE_SIZE = width / GameLogic.NUM_TILES_PER_ROW;
	private GameLogic gl;
	private final Color BLACK = new Color(220,220,220);
	private final Color WHITE = new Color(240,240,240);
	private final Color PREVIOUS = new Color(240,180,20,150);
	private final Color MOVABLE = new Color(250,190,25,80);
	private final Color MOVE = new Color(20,240,20,150);

	public static BoardUI getBoardUI(boolean playFirst, boolean bothAI, Algorithm algorithm, int difficulty, boolean forceJump)
	{
		if(instance == null)
			return instance = new BoardUI(playFirst, bothAI, algorithm, difficulty, forceJump);

		return instance;
	}

	public static BoardUI getInstance()
	{
		return instance;
	}

	private BoardUI(boolean playFirst, boolean bothAI, Algorithm algorithm, int difficulty, boolean forceJump)
	{
		window(width, height, this);	//setting the width and the height
		this.gl = new GameLogic(playFirst, bothAI, algorithm, difficulty, GameLogic.Player.RED, forceJump);
	}

	//abstract methods
	public void mouseClicked(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }

	public void mousePressed(java.awt.event.MouseEvent evt)	//this is called by java when mouse clicked (event)
	{
		int col = (evt.getX() - 8) / TILE_SIZE;	//8 is left frame length
		int row = (evt.getY() - 30) / TILE_SIZE;	//30 is top frame length
		gl.mousePressed(col, row);
		new Thread(()->repaint()).start();
	}

	private void window(int width, int height, BoardUI b)
	{
		this.setPreferredSize(new Dimension(width, height));
	}

	public void paint(Graphics g)	//this method must be public, painting the board and the pieces in proper colours
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for(int row = 0; row < GameLogic.NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < GameLogic.NUM_TILES_PER_ROW; col++)
			{
				if((row%2 == 0 && col%2 == 0) || (row%2 != 0 && col%2 != 0))	//assigning board pattern
					g2d.setColor(WHITE);
				else
					g2d.setColor(BLACK);

				g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

				if(gl.canMove(col, row))	//if piece can move, make square different colour
					g2d.setColor(MOVABLE);

				g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

				if(gl.availablePlays[col][row] != 0)	//Draw available moves
				{
					g.setColor(MOVE);
					g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				else if(gl.lastMove != null && gl.lastMove.getColFrom() == col && gl.lastMove.getRowFrom() == row)
				{
					g.setColor(PREVIOUS);
					g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if(gl.gameData[col][row] != null)	//Draw pieces
					gl.gameData[col][row].drawPiece(g2d, col*TILE_SIZE, row*TILE_SIZE, gl.isSelected(col, row));
			}
		}
	}

	public void undo()
	{
		gl.undo();
	}
}