package ui;
import algorithms.Algorithm;
import algorithms.Minimax;
import algorithms.AlphaBetaPruning;
import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;
import static ui.BoardUI.imageReader;

public class MainMenu
{
	private static boolean RIGHT_TO_LEFT = false;	//???
	private static boolean playFirst = true;
	private static boolean bothAI = false;
	private static int diff;
	private static JFrame mainMenu;
	private static Algorithm algorithm = new Minimax();

	private static void addComponentsToPane(Container pane)
	{
		if(!(pane.getLayout() instanceof BorderLayout))
		{
			pane.add(new JLabel("Container doesn't use BorderLayout!"));
			return;
		}
		if(RIGHT_TO_LEFT)
			pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JButton instructions = new JButton("Instructions");
		pane.add(instructions, BorderLayout.PAGE_START);
		JButton newGame = new JButton("New Game");
		newGame.addActionListener((event)->newGame());
		newGame.setPreferredSize(new Dimension(200, 100));	//making the center component bigger
		pane.add(newGame, BorderLayout.CENTER);
		JButton settings = new JButton("Settings");
		pane.add(settings, BorderLayout.LINE_START);
		settings.addActionListener(e -> settingsMenu());
		JButton loadGame = new JButton("Load Game");
		pane.add(loadGame, BorderLayout.PAGE_END);
		JButton statistics = new JButton("Statistics");
		pane.add(statistics, BorderLayout.LINE_END);
		mainMenu.setResizable(false);
	}

	private static void settingsMenu()
	{
		JPanel panel = new JPanel(new GridLayout(7,1));
		JLabel label = new JLabel("Computer Algorithm");
		panel.add(label);
		JComboBox<Algorithm> algorithms = new JComboBox();
		algorithms.addItem(new Minimax());
		algorithms.addItem(new AlphaBetaPruning());

		for(int i = 0; i < algorithms.getItemCount(); i++)
			if(algorithms.getItemAt(i).equals(algorithm))
				algorithms.setSelectedIndex(i);

		JCheckBox forceJump = new JCheckBox("Force Jump");
		JRadioButton humanFirst = new JRadioButton("Play First");
		JRadioButton aiFirst = new JRadioButton("Play Second");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(humanFirst);
		buttonGroup.add(aiFirst);
		humanFirst.setSelected(playFirst);
		humanFirst.setEnabled(!bothAI);
		aiFirst.setSelected(!playFirst);
		aiFirst.setEnabled(!bothAI);
		JRadioButton compVsComp = new JRadioButton("Computer vs Computer");
		compVsComp.addActionListener((event)->{
			humanFirst.setSelected(true);
			humanFirst.setEnabled(false);
			aiFirst.setSelected(false);
			aiFirst.setEnabled(false);
		});
		JRadioButton humanVsComp = new JRadioButton("Human vs Computer");
		humanVsComp.addActionListener((event)->{
			aiFirst.setEnabled(true);
			humanFirst.setEnabled(true);
		});
		ButtonGroup vsAI = new ButtonGroup();
		vsAI.add(compVsComp);
		vsAI.add(humanVsComp);
		compVsComp.setSelected(bothAI);
		humanVsComp.setSelected(!bothAI);
		panel.add(algorithms);
		panel.add(forceJump);
		panel.add(humanFirst);
		panel.add(aiFirst);
		panel.add(compVsComp);
		panel.add(humanVsComp);
		int result = JOptionPane.showConfirmDialog(null, panel, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			algorithm = (Algorithm)algorithms.getSelectedItem();
			playFirst = humanFirst.isSelected();
			bothAI = compVsComp.isSelected();
			Hashtable<Integer, JLabel> labels = new Hashtable<>();
			labels.put(0, new JLabel("Easy"));
			labels.put(2, new JLabel("Normal"));
			labels.put(4, new JLabel("Hard"));
			labels.put(6, new JLabel("Very Hard"));
			labels.put(8, new JLabel("Formidable"));
			JPanel diffSelection = new JPanel();
			JSlider difficulty = new JSlider();
			difficulty.setMinimum(0);
			difficulty.setMaximum(8);
			difficulty.setSnapToTicks(true);
			difficulty.setMajorTickSpacing(2);
			difficulty.setLabelTable(labels);
			difficulty.setValue(diff);
			difficulty.setPaintLabels(true);
			difficulty.setPaintTicks(true);
			difficulty.setPreferredSize(new Dimension(400,50));
			diffSelection.add(difficulty);
			result = JOptionPane.showConfirmDialog(null, diffSelection, "Difficulty Levels", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(result == JOptionPane.OK_OPTION)
				diff = difficulty.getValue();

		}
	}

	private static void newGame()
	{
		javax.swing.SwingUtilities.invokeLater(()->{
			BoardUI b = new BoardUI(playFirst, bothAI, algorithm, diff);
			BoardUI.instance = b;
			JFrame frame = new JFrame();
			frame.setIconImage(imageReader());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addMouseListener(b);
			frame.requestFocus();
			frame.setVisible(true);
			frame.add(b);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setResizable(false);
		});
		mainMenu.setVisible(false);
	}

	private static void showMainMenu()	//for thread safety, this method should be invoked from the event-dispatching thread
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainMenu = new JFrame("Main Menu");
		mainMenu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentsToPane(mainMenu.getContentPane());
		mainMenu.pack();
		mainMenu.setLocationRelativeTo(null);
		mainMenu.setVisible(true);
	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(()->showMainMenu());
	}
}