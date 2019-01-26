package ui;
import algorithms.Algorithm;
import logic.GameLogic;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.*;

public class BoardUI extends JPanel implements ActionListener, MouseListener
{
	public static BoardUI instance;
	private static final double GOLDEN_RATIO = 1.618;
	private static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	private static int monitorHeight = gd.getDisplayMode().getHeight();
	private static int width = ((int)Math.round(monitorHeight / GOLDEN_RATIO) / 8) * 8;
	private static int height = width;
	private static final int TILE_SIZE = width / GameLogic.NUM_TILES_PER_ROW;
	private static BufferedImage crownImage = null;
	private GameLogic gl;

	public BoardUI(boolean playFirst, boolean bothAI, Algorithm algorithm, int difficulty)
	{
		window(width, height, this);
		this.gl = new GameLogic(playFirst, bothAI, algorithm, difficulty, GameLogic.Player.RED);
	}

	//abstract methods
	public void mouseClicked(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void actionPerformed(ActionEvent e) { }

	public void mousePressed(java.awt.event.MouseEvent evt)	//this method must be public
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

	static BufferedImage imageReader()
	{
		try { crownImage = ImageIO.read(new File("Crown.png")); }
		catch(IOException e) { e.printStackTrace(); }
		return crownImage;
	}

	private static void drawPiece(int x, int y, Graphics g, Color c)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(c);
		g.fillOval(x, y, TILE_SIZE - 4, TILE_SIZE - 4);
	}

	public void paint(Graphics g)	//this method must be public
	{
		super.paintComponent(g);	//painting the board and the pieces

		for(int row = 0; row < GameLogic.NUM_TILES_PER_ROW; row++)
		{
			for(int col = 0; col < GameLogic.NUM_TILES_PER_ROW; col++)
			{
				if((row%2 == 0 && col%2 == 0) || (row%2 != 0 && col%2 != 0))	//assigning board pattern
				{
					gl.baseGameData[col][row] = null;
					g.setColor(Color.black);
					g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				else
				{
					gl.baseGameData[col][row] = GameLogic.Piece.RED;
					g.setColor(Color.white);
					g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if(gl.checkTeamPiece(col, row))
				{
					g.setColor(Color.white.darker());
					g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if(gl.availablePlays[col][row] != 0)
				{
					g.setColor(Color.GREEN.darker());
					g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if(gl.gameData[col][row] == GameLogic.Piece.BLUE)
				{
					drawPiece((col * TILE_SIZE) + 2, (row * TILE_SIZE) + 2, g, Color.blue);
					g.drawOval((col * TILE_SIZE) + 1, (row * TILE_SIZE) + 1, TILE_SIZE - 4, TILE_SIZE - 4);
				}
				else if(gl.gameData[col][row] == GameLogic.Piece.BLUE_KING)
				{
					drawPiece((col * TILE_SIZE) + 2, (row * TILE_SIZE) + 2, g, Color.blue);
					g.drawImage(crownImage, (col * TILE_SIZE) + 6, (row * TILE_SIZE) + 6, TILE_SIZE - 12, TILE_SIZE - 12, null);
				}

				else if(gl.gameData[col][row] == GameLogic.Piece.RED)
				{
					drawPiece((col * TILE_SIZE) + 1, (row * TILE_SIZE) + 1, g, Color.red);
					g.drawOval((col * TILE_SIZE) + 2, (row * TILE_SIZE) + 2, TILE_SIZE - 4, TILE_SIZE - 4);
				}
				else if(gl.gameData[col][row] == GameLogic.Piece.RED_KING)
				{
					drawPiece((col * TILE_SIZE) + 1, (row * TILE_SIZE) + 1, g, Color.red);
					g.drawImage(crownImage, (col * TILE_SIZE) + 6, (row * TILE_SIZE) + 6, TILE_SIZE - 12, TILE_SIZE - 12, null);
				}
			}
		}

		if(gl.gameOver() == true)	//Warning:(118, 6) 'gl.gameOver() == true' can be simplified to 'gl.gameOver()'???
			new Thread(()->gameOverDisplay(gl)).start();	//this new thread separates the run of the board from JOptionPane
	}

	private void gameOverDisplay(GameLogic gl)	//once the game ends, displaying a message
	{
		JOptionPane.showMessageDialog(null,"You won :)","END OF THE GAME!", JOptionPane.INFORMATION_MESSAGE);
		gl.initializeBoard();
	}
}