import java.io.File;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;


public class Boost {

	static UltrasonicSensor sensor;
	static LightSensor ls;
	public static final int MIN_DIST = 10; 
	
	public static void main(String[] args) throws Exception {
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		
		ls.setFloodlight(true);
		Motor.B.setSpeed(90);
		Motor.C.setSpeed(90);
		int distance = 255;
		int brightness = 0;
//		final File donk = new File("donk2.wav"); 
//		Sound.playSample(donk, 100);
		while(!Button.ESCAPE.isDown()) {
//			LCD.clear();
			LCD.drawString("I AM BOOST", 0, 0);
			distance = sensor.getDistance();
			brightness = ls.readValue();
			LCD.clear(1);
			LCD.drawString("distance: " + distance + "cm", 0, 1);
			LCD.clear(2);
			LCD.drawString("   light: " + brightness, 0, 2);
			isThereAWall();
			if (Button.ENTER.isDown()) {
				forward(1);
			}
			if (Button.RIGHT.isDown()) {
				turnLeft(90);
			}
			if (Button.LEFT.isDown()) {
				turnRight(90);
			}
			if (distance < MIN_DIST) {
//				Sound.playNote(Sound.PIANO, 880 - (distance * 20), 500);
				LCD.drawString("MOVE BITCH", 0, 3);
				LCD.drawString("GET OUT THE WAY!", 0, 4);
				// Motor.B.backward();
				// Motor.C.backward();
//				Sound.beep();
			} else {
				LCD.clear(3);
				LCD.clear(4);
				Motor.B.stop(true);
				Motor.C.stop(true);
			}
			Thread.sleep(250);
		}
		ls.setFloodlight(false);
	}
	
	public static void forward(double revolutions) throws Exception {
		Motor.B.forward();
		Motor.C.forward();
		Thread.sleep((long) (4000*revolutions));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}
	
	public static void backward(double revolutions) throws Exception {
		Motor.B.backward();
		Motor.C.backward();
		Thread.sleep((long) (4000*revolutions));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}
	
	public static void goRight(double revolutions) throws Exception {
		Motor.B.backward();
		Motor.C.forward();
		Thread.sleep((long) (4000*revolutions));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}
	
	public static void goLeft(double revolutions) throws Exception {
		Motor.B.forward();
		Motor.C.backward();
		Thread.sleep((long) (4000*revolutions));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}

	public static void turnRight(int degrees) {
		Motor.C.rotate(degrees * 2, true);
		Motor.B.rotate((0 - degrees) * 2, false);
	}

	public static void turnLeft(int degrees) {
		Motor.B.rotate(degrees * 2, true);
		Motor.C.rotate((0 - degrees) * 2, false);
	}
	
	public static boolean isThereAWall() throws Exception {
		int onValue, offValue, difference;
		ls.setFloodlight(true);
		Thread.sleep(250);
		onValue = ls.readValue();
		Thread.sleep(250);
		ls.setFloodlight(false);
		Thread.sleep(250);
		offValue = ls.readValue();
		difference = onValue - offValue;
//		Thread.sleep(250);
		LCD.clear(5);
		LCD.clear(6);
		LCD.drawString("diff=" + difference, 0, 6);
		if (difference > 4) {
			LCD.drawString("Hello wall!", 0, 5);
			return true;
		}
//		LCD.drawString("on:" + onValue + " off:" + offValue, 0, 5);
		return false;
	}
	
	public static void rotateHead(int degrees)
	{
		Motor.A.rotate(degrees);
	}
	
	public static ArrayList<Integer> calculateDistances(int rdeg)
	{
		ArrayList<Integer> distances = new ArrayList<Integer>();
		
		//distance straight ahead
		distances.add(sensor.getDistance());
		
		//rotate positive degree
		for(int i = 0; i <= 90; i+=rdeg)
		{
			rotateHead(rdeg);
			distances.add(sensor.getDistance());
		}
		
		//return to original position
		rotateHead(-90);
		
		//rotate negative degree
		for(int i = 0; i <= 90; i+=rdeg)
		{
			rotateHead(-rdeg);
			distances.add(sensor.getDistance());
		}
		
		//return to original position
		rotateHead(90);
		
		return distances;
	}
	
	public static boolean amIPerpendicular(ArrayList<Integer> distances, int rdeg)
	{
		//Find the distance with the least value
		//If there's a tie pick the first one
		int minimumDistance = 255;
		int minimumIndex = distances.size();
				
		for(int j = 0; j < distances.size(); j++)
		{
			if( distances.get(j) < minimumDistance )
			{
				minimumDistance = distances.get(j);
				minimumIndex = j;
			}
		}
		
		//now we analyse the data
		if(minimumIndex == 0 
				|| minimumIndex == distances.size()-1/2 
				|| minimumIndex == distances.size()-1)
		{
			return true;
		}
		
		return false;
	}
	
	public static void findPerpendicularWall(ArrayList<Integer> distances, int rdeg)
	{	
		//Find the distance with the least value
		//If there's a tie pick the first one
		int minimumDistance = 255;
		int minimumIndex = distances.size();
		
		for(int j = 0; j < distances.size(); j++)
		{
			if( distances.get(j) < minimumDistance )
			{
				minimumDistance = distances.get(j);
				minimumIndex = j;
			}
		}
		
		//now we analyse the data
		if(minimumDistance <= MIN_DIST) //we are very close to a wall
		{
			LCD.clear(3);
			LCD.clear(4);
			LCD.drawString("WALL TOO CLOSE", 0, 3);
			
			//Go backwards and repeat
		}
		else if(minimumDistance < 255)
		{
			LCD.clear(3);
			LCD.clear(4);
			LCD.drawString("DIST: " + minimumDistance, 0, 3);
			
			int angle = minimumIndex * rdeg;
			
			if(minimumIndex > distances.size()-1/2)
			{
				angle = angle - 90;
				LCD.drawString("TURNING LEFT: " + angle, 0, 3);
				turnLeft(angle);
			}
			else
			{
				LCD.drawString("TURNING RIGHT: " + angle, 0, 3);
				turnRight(angle);
			}	
		}
		
	}
}
