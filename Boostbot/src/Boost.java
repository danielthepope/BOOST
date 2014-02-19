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
	private static final int LED_THRESHOLD = 20;
	private static final int DIFF_THRESHOLD = 40;
	private static final int LIGHT_HISTORY_SIZE = 7; //TODO we can probably reduce this.
	private static final int SONAR_DISTANCE_LIMIT = 70;
	private static int headAngle = 0;
	private static int previousLightDifference = 255;
	private static LightHistory lightHistory;
//	private static final File music = new File("imperial.wav");
	private static final File doh = new File("doh.wav");
	private static final File aaah = new File("aaah.wav");
	private static final File woohoo = new File("woohoo.wav");
//	private static final File hehehe = new File("hehehe.wav");
	private static final File timidDoh = new File("timidDoh.wav");
	private static final File oopsy = new File("oopsy.wav");
//	private static final File flintstones = new File("flintstones.wav");
	
	public static void main(String[] args) throws Exception
	{
		setup();
		
		algorithmOnePointOne();
//		testMethod();
	}
	
	private static void setup()
	{
		pilot = new DifferentialPilot(55, 164, Motor.B, Motor.C); //56 164
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(false);
		lightHistory = new LightHistory(LIGHT_HISTORY_SIZE);
		
		Motor.A.setSpeed(720);
		
		pilot.setTravelSpeed(100);
		pilot.setRotateSpeed(45);
	}
	
	private static void testMethod() throws Exception
	{
		go(100);
		arcRight(5);
		go(100);
		arcLeft(5);
		stop();
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
				if( findPerpendicularWall(-90, 90, 2) )
				{
					state = 0;
				}
				else //no perp wall so we go to state -2
				{
					state = -2;
				}
			}
			else if (state == -2)
			{
				go(500);
				state = -1;
			}
			else if (state == 0)
			{
				if(checkSideWall())
				{
					LCD.drawString("I found a side wall", 0, 2);
					lightHistory.clear();
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
			else if (state == 2)
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
					else
					{
						go(90);
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
					if (trend == 1) // We're about to collide with the wall
					{
						state = 5;
					}
					else if (trend == -1) // We're losing the wall
					{
						state = 6;
					}
					// else we're following the wall perfectly. Keep calm and carry on.
				}
			}
			else if (state == 1) // There is a wall in front of me!
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
			else if (state == 3) // The wall to my left has gone!
			{
				Sound.playSample(aaah);
				go(35);
				LCD.clear(2);
				LCD.drawString("Oh dear oh dear oh dear", 0, 2);
				arcLeft(90);
				state = 4;
			}
			else if (state == 4)
			{
				go();
				if (checkFrontWall())
				{
					state = 1;
				}
				else if (checkSideWall())
				{
					lightHistory.clear();
					state = 2;
					Sound.playSample(woohoo);
				}
				// If there is not a side wall yet, keep going forward. No change.
				// state = 4;
			}
			else if (state == 5) // We're slowly converging with the wall
			{
				Sound.playSample(timidDoh);
				stop();
				arcRight(5); // Turn right a little bit
				go();
				lightHistory.clear();
				state = 2;
			}
			else if (state == 6) // We're slowly going away from the wall
			{
				Sound.playSample(oopsy);
				stop();
				arcLeft(5); // Turn left a little bit
				go();
				lightHistory.clear();
				state = 2;
			}
			//Thread.sleep(50);
		}
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
	
	private static void arcRight(int degrees)
	{
		pilot.arc(-82, -degrees);
	}
	
	private static boolean reallyNoSideWall()
	{
		boolean isThereReallyNoSideWall;
		
		rotateHeadToAngle(90);
		isThereReallyNoSideWall = !checkFrontWall(MIN_DIST + 10);
		rotateHeadToAngle(0);
		
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
		
		if(previousLightDifference == 255)
			previousLightDifference = difference;
		
		differenceChange = previousLightDifference - difference;
		previousLightDifference = difference;
		
		lightHistory.add(differenceChange);
		
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
	
	private static boolean findPerpendicularWall(int min, int max, int radiusStep)
	{
		SonarArray array;
		SonarValue closestValue;
		array = new SonarArray(SONAR_DISTANCE_LIMIT);
		int distance;
		for (int r = min; r <= max; r += radiusStep)
		{
			//LCD.clear(3);
			rotateHeadToAngle(r);
			distance = sensor.getDistance();
			LCD.drawString(r + "*, " + distance + "cm   ", 0, 3);
			array.addValue(new SonarValue(r, distance));
		}
		closestValue = array.findClosestAngle();
		LCD.clear(3);
		if (closestValue.getDistance() > SONAR_DISTANCE_LIMIT)
		{
			rotateHeadToAngle(0);
			return false;
		}
		else
		{
			int angle = closestValue.getAngle(); 
			rotateHeadToAngle(angle);
			turnLeft(angle);
			rotateHeadToAngle(0);
			return true;
		}
	}
}
