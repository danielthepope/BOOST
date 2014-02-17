
public class SonarValue {
	private int angle;    // Typically from -90 to 90
	private int distance; // In centimetres.
	
	public SonarValue(int angle, int distance)
	{
		this.angle = angle;
		this.distance = distance;
	}
	
	public int getAngle()
	{
		return angle;
	}
	
	public int getDistance()
	{
		return distance;
	}
}
