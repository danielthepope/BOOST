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
		if (values.size() < historySize) return 0;
		
		int numberOfIncreases = 0;
		for (int i = 0; i < historySize - 1; i++)
		{
			if (values.get(i) < values.get(i + 1)) numberOfIncreases++;
			else if (values.get(i) > values.get(i + 1)) numberOfIncreases--;
		}
		if (numberOfIncreases >= historySize - 1) return 1; // Robot is coming towards a wall
		else if (numberOfIncreases <= -historySize + 1) return -1; // Robot is moving away from wall
		else return 0;
	}
	
	public void clear()
	{
		values.clear();
	}
}
