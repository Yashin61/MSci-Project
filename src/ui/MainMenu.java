package ui;
import algorithms.Algorithm;
import algorithms.Minimax;
import algorithms.AlphaBetaPruning;
import algorithms.MonteCarlo;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import javax.swing.*;

public class MainMenu
{
	private static boolean RIGHT_TO_LEFT = false;
	private static boolean playFirst = true;
	private static boolean bothAI = false;
	public static int diff;
	private static JFrame mainMenu;
	private static Algorithm algorithm = new Minimax();
	private static boolean forceJump = true;

	private static void addComponentsToPane(Container pane)
	{
		if(!(pane.getLayout() instanceof BorderLayout))	//not really necessary to have
		{
			pane.add(new JLabel("Container doesn't use BorderLayout!"));
			return;
		}
		if(RIGHT_TO_LEFT)	//right to left orientation(English writing)
			pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JButton instructions = new JButton("Instructions");
		pane.add(instructions, BorderLayout.PAGE_START);
		instructions.addActionListener(e -> showInstructions());
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

	private static void showInstructions()
	{
		JTextPane tp = new JTextPane();

		try
		{
			String path = "Instructions.txt";	//for the purpose of working in .jar file, it has been put in this directory
			File file = new File(path);
			FileReader fr = new FileReader(file);

			while(fr.read() != -1)
				tp.read(fr,null);

			fr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		JFrame frame=new JFrame("Instructions");
		Font font = new Font("", Font.BOLD,11);
		tp.setFont(font);
		frame.pack();
		frame.setSize(488,488);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		JScrollPane jp = new JScrollPane(tp);
		frame.add(jp, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setEnabled(false);
	}

	private static void settingsMenu()
	{
		JPanel panel = new JPanel(new GridLayout(9,1));
		JLabel label = new JLabel("Computer Algorithm");
		panel.add(label);
		JComboBox<Algorithm> algorithms = new JComboBox();
		algorithms.addItem(new Minimax());
		algorithms.addItem(new AlphaBetaPruning());
		algorithms.addItem(new MonteCarlo());

		for(int i = 0; i < algorithms.getItemCount(); i++)	//for default selection
			if(algorithms.getItemAt(i).equals(algorithm))
				algorithms.setSelectedIndex(i);

		JLabel label2 = new JLabel("Difficulty Level");
		JComboBox difficulties = new JComboBox();
		difficulties.addItem("Easy");
		difficulties.addItem("Normal");
		difficulties.addItem("Hard");
		difficulties.addItem("Formidable");
		difficulties.setSelectedIndex(0);

		difficulties.addActionListener(e -> {
			Object choice = ((JComboBox)e.getSource()).getSelectedItem();

			if(choice instanceof MonteCarlo)
			{
				switch((String)choice)
				{
					case "Easy": diff = 1000;
						break;
					case "Normal": diff = 2000;
						break;
					case "Hard": diff = 3000;
						break;
					case "Formidable": diff = 4000;
						break;
				}
			}
			else
			{
				switch((String)choice)
				{
					case "Easy": diff = 0;
						break;
					case "Normal": diff = 3;
						break;
					case "Hard": diff = 6;
						break;
					case "Formidable": diff = 9;
						break;
				}
			}
		});

		//setting the buttons
		JCheckBox forceJump = new JCheckBox("Force Jump");
		forceJump.setSelected(MainMenu.forceJump);
		JRadioButton humanFirst = new JRadioButton("Play First");
		JRadioButton aiFirst = new JRadioButton("Play Second");
		ButtonGroup buttonGroup = new ButtonGroup();	//radio buttons
		buttonGroup.add(humanFirst);
		buttonGroup.add(aiFirst);
		humanFirst.setSelected(playFirst);
		humanFirst.setEnabled(!bothAI);
		aiFirst.setSelected(!playFirst);
		aiFirst.setEnabled(!bothAI);
		JRadioButton compVsComp = new JRadioButton("Computer vs Computer");

		compVsComp.addActionListener((event) -> {
			humanFirst.setSelected(true);
			humanFirst.setEnabled(false);
			aiFirst.setSelected(false);
			aiFirst.setEnabled(false);
		});

		JRadioButton humanVsComp = new JRadioButton("Human vs Computer");

		humanVsComp.addActionListener((event) -> {
			aiFirst.setEnabled(true);
			humanFirst.setEnabled(true);
		});

		ButtonGroup vsAI = new ButtonGroup();
		vsAI.add(compVsComp);
		vsAI.add(humanVsComp);
		compVsComp.setSelected(bothAI);
		humanVsComp.setSelected(!bothAI);
		panel.add(algorithms);
		panel.add(label2);
		panel.add(difficulties);
		panel.add(forceJump);
		panel.add(humanFirst);
		panel.add(aiFirst);
		panel.add(compVsComp);
		panel.add(humanVsComp);
		int result = JOptionPane.showConfirmDialog(null, panel, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if(result == JOptionPane.OK_OPTION)	//sets the changes in the Settings menu by clicking the OK button
		{
			algorithm = (Algorithm)algorithms.getSelectedItem();
			playFirst = humanFirst.isSelected();
			bothAI = compVsComp.isSelected();
			MainMenu.forceJump = forceJump.isSelected();
		}
	}

	private static void newGame()
	{
		javax.swing.SwingUtilities.invokeLater(() -> {
			BoardUI b = BoardUI.getBoardUI(playFirst, bothAI, algorithm, diff, forceJump);	//creating a new board in lambda expression
			JFrame frame = new JFrame();
			frame.setLayout(new BorderLayout());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addMouseListener(b);
			frame.requestFocus();
			frame.setVisible(true);
			frame.setResizable(false);
			JPanel controls = new JPanel();
			JButton undo = new JButton("Undo");

			undo.addActionListener((event) -> b.undo());

			controls.add(undo);
			frame.add(b, BorderLayout.PAGE_START);
			frame.add(controls, BorderLayout.PAGE_END);
			frame.pack();
			frame.setLocationRelativeTo(null);
		});

		mainMenu.setVisible(false);	//once the game starts, the main menu disappears
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
		javax.swing.SwingUtilities.invokeLater(() -> showMainMenu());
	}
}