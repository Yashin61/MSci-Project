package algorithms;
import logic.GameLogic;
import logic.Move;

//it is just an abstraction of algorithms, so then easy to switch between algorithms without more codes
public abstract class Algorithm
{
	private String name;
	public abstract Move getAIMove(GameLogic gl, int depth);

	public Algorithm(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.name.equals(obj.toString());
	}
}