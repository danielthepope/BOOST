import java.util.ArrayList;


public class SonarArray {
	private ArrayList<SonarValue> array;
	private static final int ignoreDistancesOver = 70;
	
	public SonarArray()
	{
		array = new ArrayList<SonarValue>();
	}
	
	public void addValue(SonarValue value)
	{
		array.add(value);
	}
	
	public SonarValue findClosestAngle()
	{
		int minimumDistance = 255;
		ArrayList<SonarValue> closestValues = new ArrayList<SonarValue>();
		
		// Find the closest obstacle.
		for(SonarValue sv : array)
		{
			if (sv.getDistance() < minimumDistance && sv.getDistance() < ignoreDistancesOver)
			{
				minimumDistance = sv.getDistance();
			}
		}
		
		// If no obstacle has been found, return 0, 255
		if (minimumDistance == 255)
		{
			return new SonarValue(0, 255);
		}
		
		// Now the closest distance has been determined
		boolean found = false;
		for(SonarValue sv : array)
		{
			if (sv.getDistance() == minimumDistance)
			{
				found = true;
				closestValues.add(sv);
			}
			else if (found == true)
			{
				break;
			}
		}
		
		// Return the value in the middle
		int totalAngle = 0;
		for(SonarValue sv : closestValues)
		{
			totalAngle += sv.getAngle();
		}
		
		return new SonarValue(totalAngle / closestValues.size(), minimumDistance);
	}
}
