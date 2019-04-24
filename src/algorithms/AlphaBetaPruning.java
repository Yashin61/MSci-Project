package algorithms;
import logic.GameLogic;
import logic.Move;
import java.util.List;

public class AlphaBetaPruning extends Algorithm
{
	public AlphaBetaPruning()
	{
		super("Alpha-Beta Pruning");
	}

	public Move getAIMove(GameLogic gl, int depth)  //picking and returning a move from all possible moves based on the current state of the board
	{
		List<Move> moves = gl.getAllMoves();

		if(moves.size() == 0)
			throw new IllegalArgumentException();

		float[] scores = new float[moves.size()];
		int maxScoreIndex = 0;
		int i = 0;
		float alpha = Float.NEGATIVE_INFINITY;	//alpha keeps track of max score
		float beta = Float.POSITIVE_INFINITY;	//beta keeps track of mini score

		for(Move move : moves)
		{
			GameLogic moved = new GameLogic(gl, move);	//getting a copy of the board and making the current move in the copy
			scores[i] = getScore(moved, depth, gl.isKingMove, alpha, beta);

			if(scores[i] > scores[maxScoreIndex])
				maxScoreIndex = i;	//keeping track of the best move

			alpha = Math.max(alpha, scores[i]);
			i++;
		}

		return moves.get(maxScoreIndex);
	}

	private float getScore(GameLogic gl, int depth, boolean maxing, float alpha, float beta)
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
}