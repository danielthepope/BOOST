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


public class Boost
{
	private static DifferentialPilot pilot;
	private static UltrasonicSensor sensor;
	private static LightSensor ls;
	private static final int MIN_DIST = 11;
	private static final int LED_THRESHOLD = 15;
	private static final int DIFF_THRESHOLD = 40;
	private static int headAngle = 0;
	private static int previousLightDifference = 255;
	private static LightHistory lightHistory;
//	private static final File music = new File("imperial.wav");
	private static final File doh = new File("doh.wav");
	private static final File aaah = new File("aaah.wav");
	private static final File woohoo = new File("woohoo.wav");
//	private static final File flintstones = new File("flintstones.wav");
	
	public static void main(String[] args) throws Exception
	{
		setup();
		
//		algorithmOne();
		algorithmOnePointOne();
//		testMethod();
	}
	
	private static void setup()
	{
		pilot = new DifferentialPilot(55, 164, Motor.B, Motor.C); //56 164
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(false);
		lightHistory = new LightHistory(5);
		
		Motor.A.setSpeed(720);
		
		pilot.setTravelSpeed(100);
		pilot.setRotateSpeed(45);
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
		// -2: I know there's nothing around me. Let's go forward to the limit
		// -1: Rotating head, trying to find a perpendicular wall
		// 0 : Boost is looking perpendicular at a wall
		// 1 : There is a wall in front of me. I need to turn right
		// 2 : I know there is a wall to my left. Go forward, checking both sensors
		// 3 : There is no wall on the left. I now need to turn left
		// 4 : I have just turned left. I need to go forward until I find a wall again
		// 5 : I'm slowly converging with the wall to the left
		// 6 : I'm slowly going away from the wall on the left
		int state = -1 ;
		LCD.drawString("I AM BOOST 1.1", 0, 0);
		while (!Button.ESCAPE.isDown())
		{
			LCD.clear(1);
			LCD.clear(2);
			LCD.drawString("State " + state, 0, 1);
			if (state == -1)
			{
				//TODO State -2 not implemented yet.
				dansFindPerpendicularWall(-90, 90, 2);
				state = 0;
			}
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
						continue;
					}
				}
				else if (checkFrontWall()) // If there is a wall in front, turn right.
				{
					state = 1;
					continue;
				}
				else
				{
					int trend = lightHistory.checkForATrend();
					if (trend == 1)
					{
						// We're about to collide with the wall
						state = 5;
					}
					else if (trend == -1)
					{
						// We're losing the wall
						state = 6;
					}
					// else we're following the walll perfectly
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
				lightHistory.clear();
				state = 2;
			}
			if (state == 3) // The wall to my left has gone!
			{
				Sound.playSample(aaah);
				go(35);
				LCD.clear(2);
				LCD.drawString("Oh dear oh dear oh dear", 0, 2);
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
					Sound.playSample(woohoo);
					lightHistory.clear();
				}
				// If there is not a side wall yet, keep going forward. No change.
				// state = 4;
			}
			if (state == 5) // We're slowly converging with the wall
			{
				stop();
				dansFindPerpendicularWall(45, 90, 2);
				state = 0;
			}
			if (state == 6) // We're slowly going away from the wall
			{
				stop();
				turnLeft(45);
				dansFindPerpendicularWall(45, 90, 2);
				state = 0;
			}
			Thread.sleep(50);
		}
	}
	
	private static void testMethod() throws Exception
	{
//		findPerpendicularWall(calculateDistances(5), 5);
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
		onValue = ls.getNormalizedLightValue();
		ls.setFloodlight(false);
		Thread.sleep(50);
		offValue = ls.getNormalizedLightValue();
		difference = onValue - offValue;
		lightHistory.add(difference);
		
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
	
	private static void dansFindPerpendicularWall(int min, int max, int radiusStep)
	{
		boolean foundWall = false;
		SonarArray array;
		SonarValue closestValue;
		do
		{
			array = new SonarArray();
//			int radiusStep = 2;
			int distance = 255;
			for (int r = min; r <= max; r += radiusStep)
			{
				rotateHeadToAngle(r);
				distance = sensor.getDistance();
				array.addValue(new SonarValue(r, distance));
			}
			closestValue = array.findClosestAngle();
			if (closestValue.getDistance() == 255)
			{
				// TODO This bit is wrong. Get rid of it. (i.e. move it to another state)
				// No obstacles nearby! Go forward a bit
				go(500);
			}
			else
			{
				foundWall = true;
			}
		} while (foundWall == false);
		int angle = closestValue.getAngle(); 
		rotateHeadToAngle(angle);
		turnLeft(angle);
		rotateHeadToAngle(0);
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
		rotateHeadToAngle(0);
		
		//rotate negative degree
		for(int i = 0; i <= 90; i+=rdeg)
		{
			rotateHead(-rdeg);
			distances.add(sensor.getDistance());
		}
		
		//return to original position
		rotateHeadToAngle(0);
		
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
