import java.io.File;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.PilotProps;


public class Boost {

	private static DifferentialPilot pilot;
	private static UltrasonicSensor sensor;
	private static LightSensor ls;
	private static final int MIN_DIST = 11;
	private static final int LED_THRESHOLD = 2;
	private static final int DIFF_THRESHOLD = 4;
	private static final double ROTATION_COEFFICIENT = 3;
	private static int headAngle = 0;
	private static int previousLightDifference = 255;
//	private static final File music = new File("imperial.wav");
	private static final File doh = new File("doh.wav");
	private static final File aaah = new File("aaah.wav");
//	private static final File woohoo = new File("woohoo.wav");
//	private static final File flintstones = new File("flintstones.wav");
	
	public static void main(String[] args) throws Exception
	{
		setup();
//		algorithmOne();
		algorithmOnePointOne();
//		pilotTest();
	}
	
	private static void setup()
	{
		pilot = new DifferentialPilot(55, 164, Motor.B, Motor.C); //56 164
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(false);
		
		Motor.A.setSpeed(180);
		
		pilot.setTravelSpeed(100);
		pilot.setRotateSpeed(45);
	}
	
	private static void original() throws Exception
	{
		int distance = 255;
		int brightness = 0;
//		final File music = new File("imperial.wav"); 
//		Sound.playSample(music, 100);
		while(!Button.ESCAPE.isDown())
		{
			LCD.drawString("I AM BOOST", 0, 0);
			distance = sensor.getDistance();
			brightness = ls.readValue();
			LCD.clear(1);
			LCD.drawString("distance: " + distance + "cm", 0, 1);
			LCD.clear(2);
			LCD.drawString("   light: " + brightness, 0, 2);
			checkSideWall();
			if (Button.ENTER.isDown())
			{
				forward(1);
			}
			if (Button.RIGHT.isDown())
			{
				turnLeft(90);
			}
			if (Button.LEFT.isDown())
			{
				turnRight(90);
			}
			if (distance < MIN_DIST)
			{
				LCD.drawString("MOVE BITCH", 0, 3);
				LCD.drawString("GET OUT THE WAY!", 0, 4);
//				backward(1.0);
//				Sound.beep();
			} else {
				LCD.clear(3);
				LCD.clear(4);
				Motor.B.stop(true);
				Motor.C.stop(true);
			}
			Thread.sleep(50);
		}
	}
	
	private static void algorithmOne() throws Exception
	{
		LCD.drawString("I AM BOOST 1.0", 0, 0);
		//Set states
		boolean frontWall = false;
		boolean sideWall = false;
		boolean oldSideWall = false;
		
		//Start off perpendicular to a wall, some distance away
		while(!Button.ESCAPE.isDown())
		{
			frontWall = checkFrontWall();
			oldSideWall = sideWall;
			sideWall = checkSideWall();
			
			LCD.clear();
			
			if (frontWall) LCD.drawString("FRONT", 0, 1);
			if (sideWall) LCD.drawString("SIDE", 0, 2);
			/*if (!oldSideWall) LCD.drawString("OLDSIDE", 0, 3);
			if (!sideWall) LCD.drawString("SIDE2", 0, 2);
			if (oldSideWall) LCD.drawString("OLDSIDE2", 0, 3);
			
			if (!sideWall && oldSideWall) LCD.drawString("WOAH", 0, 3);*/
			
			if (!sideWall && oldSideWall && reallyNoSideWall()) //We've changed from a state of wall to non wall
			{
				stop();
				turnLeft(90);
				go(); //go to make sure we don't get caught in a perpetual spin
			}
			else if (!frontWall)
			{
				go();
			}
			else if (frontWall)
			{
				stop();
				turnRight(90);
				go(); //go to make sure we don't get caught in a perpetual spin
			}
			else
			{
				stop();
				Thread.sleep(3000);
				return;
			}
		}
	}
	
	private static void algorithmOnePointOne() throws Exception
	{
		// Set state
		// 0 : Initial state. Check the location of Boost
		// 1 : There is a wall in front of me. I need to turn right
		// 2 : I know there is a wall to my left. Go forward, checking both sensors
		// 3 : There is no wall on the left. I now need to turn left
		// 4 : I have just turned left. I need to go forward until I find a wall again
		int state = 0;
		LCD.drawString("I AM BOOST 1.1", 0, 0);
		while (!Button.ESCAPE.isDown())
		{
			LCD.clear(1);
			LCD.clear(2);
			LCD.drawString("State " + state, 0, 1);
			if (state == 0)
			{
				if(checkSideWall())
				{
					LCD.drawString("I found a side wall", 0, 2);
					state = 2;
					continue;
				}
				else if(!checkFrontWall())
				{
					LCD.drawString("I haven't found a wall yet", 0, 2);
					go();
				}
				else
				{
					state = 1;
					continue;
				}				
			}
			if (state == 2)
			{
				LCD.drawString("I'm following the side wall", 0, 2);
				go();
				if (!checkSideWall()) // If there is no side wall we need to turn left
				{
					stop();
					if (reallyNoSideWall())
					{
						state = 3;
					}
				}
				else if (checkFrontWall()) // If there is a wall in front, turn right.
				{
					state = 1;
				}
			}
			if (state == 1) // There is a wall in front of me!
			{
				stop();
				LCD.clear(2);
				LCD.drawString("AAAH! Hello wall.", 0, 2);
				Sound.playSample(doh);
				int reading = sensor.getDistance();
				if (reading == 255)
				{
					go(MIN_DIST * -10); // Head is touching the wall
				}
				else if (reading <= MIN_DIST)
				{
					go((MIN_DIST - sensor.getDistance()) * -10); // Reverse a bit
				}
				turnRight(90);
				state = 2;
			}
			if (state == 3) // The wall to my left has gone!
			{
				go(35);
				LCD.clear(2);
				LCD.drawString("Oh dear oh dear oh dear", 0, 2);
				Sound.playSample(aaah);
				arcLeft(90);
				state = 4;
			}
			if (state == 4)
			{
				go();
				if (checkFrontWall())
				{
					state = 1;
				}
				else if (checkSideWall())
				{
					state = 2;
				}
				// If there is not a side wall yet, keep going forward. No change.
				// state = 4;
			}
			Thread.sleep(50);
		}
	}
	
	private static void pilotTest() throws Exception
	{
		dansFindPerpendicularWall();
	}
	
	private static void forward(double revolutions) throws Exception
	{
		int angle = 360 * (int) revolutions;
		Motor.B.rotate(angle, true);
		Motor.C.rotate(angle, false);
	}
	
	private static void go()
	{
		pilot.forward();
	}
	
	private static void go(int mm)
	{
		pilot.travel(mm);
		stop();
	}
	
	private static void stop()
	{
		pilot.stop();
	}

	private static void turnRight(int degrees)
	{
		pilot.rotate(-degrees);
	}

	private static void turnLeft(int degrees)
	{
		pilot.rotate(degrees);
	}
	
	private static void arcLeft(int degrees)
	{
		pilot.arc(82, degrees);
	}
	
	private static boolean reallyNoSideWall()
	{
		boolean isThereReallyNoSideWall;
		
		rotateHead(90);
		isThereReallyNoSideWall = !checkFrontWall(MIN_DIST + 6);
		rotateHead(-90);
		
		return isThereReallyNoSideWall;
	}
	
	private static boolean checkSideWall() throws Exception
	{
		int onValue, offValue, difference, differenceChange;
		ls.setFloodlight(true);
		Thread.sleep(50);
		onValue = ls.readValue();
		ls.setFloodlight(false);
		Thread.sleep(50);
		offValue = ls.readValue();
		difference = onValue - offValue;
		
		if(previousLightDifference == 255)
			previousLightDifference = difference;
		
		differenceChange = previousLightDifference - difference;
		previousLightDifference = difference;
		
		LCD.clear(4);
		LCD.clear(5);
		LCD.drawString("      diff=" + difference, 0, 4);
		LCD.drawString("diffchange=" + differenceChange, 0, 5);
		if (difference > LED_THRESHOLD)
		{
			if (differenceChange < DIFF_THRESHOLD)
			{
				return true;
			}
			else
			{
				Sound.playTone(300, 20);
			}
		}
		return false;
	}
	
	private static boolean checkFrontWall()
	{
		int distance = sensor.getDistance();
		return distance < MIN_DIST;
	}
	
	private static boolean checkFrontWall(int dist)
	{
		int distance = sensor.getDistance();
		return distance < dist;
	}
	
	private static void rotateHead(int degrees)
	{
		headAngle += degrees;
		Motor.A.rotate(degrees);
	}
	
	private static void rotateHeadToAngle(int degrees)
	{
		Motor.A.rotate(degrees - headAngle);
		headAngle = degrees;
	}
	
	private static void dansFindPerpendicularWall()
	{
		//pilot.forward();
		int[] distances = new int[181];
		int i = 0;
		int closestAngle = 0;
		for (int r = 0; r <= 180; r++)
		{
			//LCD.clear();
			LCD.drawInt(r - 90, 4, 0, 0);
			rotateHeadToAngle(r - 90);
			distances[i] = sensor.getDistance();
			if (distances[i] < 255)
			{
				Sound.playTone(2000 - distances[i] * 5, 20);
				LCD.drawInt(distances[i], 4, 0, 1);
			}
			if (distances[i] < distances[closestAngle] && distances[i] < 50)
			{
				closestAngle = i;
				LCD.drawInt(closestAngle - 90, 4, 0, 2);
				LCD.drawInt(distances[closestAngle], 4, 6, 2);
			}
			i++;
		}
		rotateHeadToAngle(0);
		turnLeft(closestAngle - 90);
		
	}
	
	private static ArrayList<Integer> calculateDistances(int rdeg)
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
	
	private static boolean amIPerpendicular(ArrayList<Integer> distances, int rdeg)
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
	
	private static void findPerpendicularWall(ArrayList<Integer> distances, int rdeg)
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
