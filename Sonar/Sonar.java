import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;


public class Sonar {

	private static UltrasonicSensor sensor;
	private static LightSensor ls;
	
	public static void main(String[] args) throws InterruptedException {
		int distance, on, off;
		LCD.drawString("Beep beep", 0, 0);
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(false);
		int previousDifference = 0;
		int difference = 0;
		int lightChange = 0;
		int threshold = 0;
		
		while(!Button.ESCAPE.isDown())
		{
			distance = sensor.getDistance();
			ls.setFloodlight(true);
			Thread.sleep(50);
			on = ls.readNormalizedValue();
			ls.setFloodlight(false);
			Thread.sleep(50);
			off = ls.readNormalizedValue();
			difference = on - off;
			lightChange = difference - previousDifference;
			previousDifference = difference;
			threshold = calculateThreshold(off);
			LCD.clear();
			LCD.drawString("Dist: " + distance, 0, 1);
			LCD.drawString("on:" + on + " off:" + off, 0, 2);
			LCD.drawString("diff: " + (on - off), 0, 3);
			LCD.drawString("change: " + lightChange, 0, 4);
			LCD.drawString("Threshold: " + threshold, 0, 5);
			if (difference > threshold)
			{
				LCD.drawString("Wall? Yes", 0, 6);
			} else {
				LCD.drawString("Wall? No", 0, 6);
			}
//			Sound.playTone(2000 - distance * 5, 20);
			Thread.sleep(200);
		}
	}
	
	private static int calculateThreshold(int v)
	{
		// y = 0.0011x2 - 0.8669x + 170.1
		// y = 0.00113159490529176x2 - 0.866937722472893x + 170.103701944699
		// y = 0.00105151582815248x2 - 0.81710134380644700x + 161.65010262509900000

		return (int) ((0.00105151582815248 * v * v) + (-0.817101343806447 * v) + 161.650102625099); // EXPERIMENT NORMAL
		
		//return 20; // EXPERIMENT 1
	}
}
