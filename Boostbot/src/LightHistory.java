import java.util.ArrayList;


public class LightHistory {
	private ArrayList<Integer> values;
	private int historySize;
	
	public LightHistory(int historySize)
	{
		values = new ArrayList<Integer>();
		this.historySize = historySize;
	}
	
	public void add(int lightValue)
	{
		values.add(lightValue);
		if (values.size() > historySize)
		{
			values.remove(0);
			// At this point, the size of the history is equal to historySize
		}
	}
	
	public int checkForATrend()
	{
		// 0 1 1 0 0 = go right, return 1
		// 0 0 -1 0 0 = go left, return -1
		// 0 1 -1 1 0 = go straight on, return 0
		// 0 0 0 0 0 = go straight on, return 0
		if (values.size() < historySize) return 0;
		
		boolean goLeft = true;
		boolean goRight = true;
		for (int i = 0; i < historySize; i++)
		{
			if (values.get(i) > 0) goRight = false;
			if (values.get(i) < 0) goLeft = false;
		}
		if (goLeft && !goRight) return -1;
		else if (!goLeft && goRight) return 1;
		else return 0;
	}
	
	public void clear()
	{
		values.clear();
	}
}
