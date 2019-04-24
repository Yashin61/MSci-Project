package algorithms;
import logic.GameLogic;
import logic.Move;
import ui.MainMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarlo extends Algorithm
{
	private static final double CHANCE = 0.5;	//the chance of picking the first move from the list
	private static int checks = MainMenu.diff;	//it looks at this many of simulations in depth 150
	private static final int DEPTH = 150;
	private static Random random = new Random();

	public MonteCarlo()
	{
		super("Monte Carlo");
	}

	@Override
	public Move getAIMove(GameLogic gl, int depth)
	{
		List<Move> moves = gl.getAllMoves();	//gets all the possible moves
		List<TreeNode> nodes = new ArrayList<>(moves.size());	//creates nodes for each of the moves
		int i = 0;

		for(Move move : moves)
		{
			TreeNode node = new TreeNode(move, gl, i++);
			nodes.add(node);
		}

		nodes.sort(null);	//sorting by score according to alpha-beta
		checks = 2000;
		for(i = 0; i < checks; i++)	//going through them with more chance of picking the highest score
		{
			double picked = Math.random();
			double currentChance = CHANCE;
			for(int j = 0; j < nodes.size(); j++)
			{
				if(picked >= currentChance || j == nodes.size()-1)
				{
					nodes.get(j).visit();
					break;
				}
				else
					currentChance *= CHANCE;
			}
			if(i%10 == 9)	//at every 10 times doing that, sort the list again to make sure that the best moves are always at the top
				nodes.sort((o1,o2) -> Float.compare((o1.wins-o1.losses)/(float)o1.total, (o2.wins-o2.losses)/(float)o2.total));
		}
		float max = Float.NEGATIVE_INFINITY;
		TreeNode maxNode = null;

		for(TreeNode node : nodes)	//going through all the nodes to find the one with greater wins/lost ratio
		{
			if(node.total < 30)	//if a node is visited less than 30 times, there is a chance to have bad move, so only consider the nodes visited more than 30 times
				continue;
			float score = (node.wins-node.losses) / (float)node.total;
			if(score >= max)
			{
				maxNode = node;
				max = score;
			}
		}

		return maxNode.move;	//pick the best move and return it
	}

	private static GameLogic.Player playRandomGame(GameLogic gl)	//picks random moves, and returns the player who won the game
	{
		List<Move> moves = gl.getAllMoves();

		moves.sort((a,b) -> {
			if(a.isJump() && !b.isJump())
				return -1;
			if(b.isJump() && !a.isJump())
				return 1;
			return 0;
		});

		int depth = DEPTH;

		while(moves.size() != 0 && depth != 0)
		{
			gl = new GameLogic(gl, moves.get(random.nextInt(moves.size())));
			moves = gl.getAllMoves();
			depth--;
		}

		//checks who won
		if(moves.size() == 0)	//if no moves i.e. no pieces or blocked, then current player loses
			return gl.getCurrentPlayer().getOpposite();	//return the opposite player
		else	//otherwise if the game not ended
		{
			float score = gl.getCurrentPlayerScore();

			if(score > 2)	//it wins
				return gl.getCurrentPlayer();
			if(score < -2)	//it loses
				return gl.getCurrentPlayer().getOpposite();

			return null;	//if it is in between, then the game is drawn
		}
	}

	private static float getScore(GameLogic gl, int depth, boolean maxing, float alpha, float beta)	//copy paste from AlphaBetaPruning.java
	{
		List<Move> moves = gl.getAllMoves();

		if(depth == 0)
			return maxing ? gl.getCurrentPlayerScore() : -gl.getCurrentPlayerScore();
		if(gl.isKingMove)
			depth++;
		if(moves.size() == 0)	//if no moves i.e. no pieces or blocked, then current player loses
			return maxing ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		if(maxing)	//maximising
		{
			float best = Float.NEGATIVE_INFINITY;

			for(Move move : moves)
			{
				float score = getScore(new GameLogic(gl, move), depth-1, gl.isKingMove, alpha, beta);
				best = Math.max(best, score);
				alpha = Math.max(alpha, score);	//update alpha with maximum value

				if(beta <= alpha)
					break;
			}

			return best;
		}
		else	//minimising
		{
			float best = Float.POSITIVE_INFINITY;

			for(Move move : moves)
			{
				float score = getScore(new GameLogic(gl, move), depth-1, !gl.isKingMove, alpha, beta);
				best = Math.min(best, score);
				beta = Math.min(beta, score);	//update beta with the minimum value

				if(beta <= alpha)
					break;
			}

			return best;
		}
	}

	private static class TreeNode implements Comparable<TreeNode>
	{
		int wins = 0;
		int losses = 0;
		int total = 0;	//total number of times it has been looked at
		Move move;	//the move that the TreeNode instance represents
		GameLogic gl;
		float score;
		int index;	//index of which node it is
		GameLogic.Player me;	//the player that this TreeNode represents

		public TreeNode(Move move, GameLogic gl, int index)	//every time a TreeNode is created, it gets the current score from alpha-beta for 5 levels deep
		{
			this.move = move;
			me = gl.getCurrentPlayer();
			this.gl = new GameLogic(gl, move);
			this.score = getScore(gl, 5, gl.isKingMove, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
			this.index = index;
		}

		public void visit()	//when visiting that node, it plays a random game
		{
			GameLogic.Player winner = playRandomGame(gl);	//gl is the current board

			if(winner == me)
				wins++;	//how many times this node wins
			else if(winner != null)
				losses++;

			total++;	//how many times looked at the node
		}

		public int compareTo(TreeNode other)
		{
			if(this == other)	//this TreeNod comparing to other
				return 0;

			else
			{
				int compare = Float.compare(score, other.score);

				if(compare == 0)	//if the scores are the same
					return other.index - index;	//still needed to sort. so, sort by index

				else
					return compare;	//sort by score
			}
		}
	}
}