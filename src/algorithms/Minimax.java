package algorithms;
import logic.GameLogic;
import logic.Move;
import java.util.List;

public class Minimax extends Algorithm
{
	public Minimax()
	{
		super("Minimax");
	}

    public Move getAIMove(GameLogic gl, int depth)	//picking and returning a move from all possible moves based on current state of board
    {
    	List<Move> moves = gl.getAllMoves();
		float[] scores = new float[moves.size()];
		int maxScoreIndex = 0;
		int i = 0;

		for(Move move : moves)
		{
			GameLogic moved = new GameLogic(gl, move);	//getting a copy of the board and making the current move in the copy
			scores[i] = getScore(moved, depth, gl.isKingMove);

			if(scores[i] > scores[maxScoreIndex])
				maxScoreIndex = i;	//keeping track of the best move

			i++;
		}

		return moves.get(maxScoreIndex);
    }

	private float getScore(GameLogic gl, int depth, boolean maxing)
	{
		List<Move> moves = gl.getAllMoves();

		if(depth == 0)
			return maxing ? gl.getCurrentPlayerScore() : -gl.getCurrentPlayerScore();
		if(gl.isKingMove)
			depth++;
		if(moves.size() == 0)	//if no moves i.e. no pieces or blocked, then current player loses
			return maxing ? -13 : 13;
		if(maxing)	//maximising
		{
			float best = -12;

			for(Move move : moves)
			{
				float score = getScore(new GameLogic(gl, move), depth-1, gl.isKingMove);
				best = Math.max(best, score);
			}

			return best;
		}
		else	//minimising
		{
			float best = 12;

			for(Move move : moves)
			{
				float score = getScore(new GameLogic(gl, move), depth-1, !gl.isKingMove);
				best = Math.min(best, score);
			}

			return best;
		}
	}
}