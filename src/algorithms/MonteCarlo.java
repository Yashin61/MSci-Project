package algorithms;
import logic.GameLogic;
import logic.Move;
import ui.MainMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarlo extends Algorithm
{
	// the way it works is sorting best moves to worst and tgen randomly picks one 2000 times to the win position or 150 dept
	private static final double CHANCE = 0.5;
	private static int checks = MainMenu.diff; // it looks at 2000 simulations in depth 150
	private static final int DEPTH = 150;
	private static Random random = new Random();

	public MonteCarlo()
	{
		super("Monte Carlo");
	}

	@Override
	public Move getAIMove(GameLogic gl, int depth)
	{
		List<Move> moves = gl.getAllMoves();
		List<TreeNode> nodes = new ArrayList<>(moves.size());
		int i = 0;

		for(Move move : moves)
		{
			TreeNode node = new TreeNode(move,gl,i++);
			nodes.add(node);
		}

		nodes.sort(null);
		checks = 2000;
		for(i = 0; i < checks; i++)
		{
			double picked = Math.random();
			double currentChance = CHANCE;
			for(int j = 0; j < nodes.size(); j++)
			{
				if (picked >= currentChance || j == nodes.size()-1)
				{
					nodes.get(j).visit();
					break;
				}
				else
					currentChance *= CHANCE;
			}
			if(i%10 == 9)
				nodes.sort((o1,o2)-> Float.compare((o1.wins-o1.losses)/(float)o1.total, (o2.wins-o2.losses)/(float)o2.total));
		}
		float max = Float.NEGATIVE_INFINITY;
		TreeNode maxNode = null;

		for(TreeNode node : nodes)
		{
			if(node.total < 30)
				continue;
			float score = (node.wins-node.losses) / (float)node.total;
			if(score >= max)
			{
				maxNode = node;
				max = score;
			}
		}

		return maxNode.move;
	}

	private static GameLogic.Player playRandomGame(GameLogic gl)
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
			depth --;
		}

		if(moves.size() == 0)
			return gl.getCurrentPlayer().getOpposite();
		else
		{
			float score = gl.getCurrentPlayerScore();

			if(score > 2)
				return gl.getCurrentPlayer();
			if(score < -2)
				return gl.getCurrentPlayer().getOpposite();

			return null;
		}
	}

	private static float getScore(GameLogic gl, int depth, boolean maxing, float alpha, float beta)
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
				alpha = Math.max(alpha, score);

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
				beta = Math.min(beta, score);

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
		int total = 0;
		Move move;
		GameLogic gl;
		float score;
		int index;
		GameLogic.Player me;

		public TreeNode(Move move, GameLogic gl, int index)
		{
			this.move = move;
			me = gl.getCurrentPlayer();
			this.gl = new GameLogic(gl, move);
			this.score = getScore(gl, 5, gl.isKingMove, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
			this.index = index;
		}

		public void visit()
		{
			GameLogic.Player winner = playRandomGame(gl);

			if(winner == me)
				wins ++;
			else if(winner != null)
				losses ++;

			total ++;
		}

		public int compareTo(TreeNode other)
		{
			if(this == other)
				return 0;

			else
			{
				int compare = Float.compare(score, other.score);

				if(compare == 0)
					return other.index - index;

				else
					return compare;
			}
		}
	}
}