import java.io.File;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.DifferentialPilot;


public class Boost
{
	private static DifferentialPilot pilot;
	private static UltrasonicSensor sensor;
	private static LightSensor ls;
	private static final int EXPERIMENT_NUMBER = 0; // 0 = normal operation.
	private static final int MIN_DIST = 15; // 11
	private static int LED_THRESHOLD = 20;
	private static final int LIGHT_HISTORY_SIZE = 7;
	private static final int SONAR_DISTANCE_LIMIT = 70;
	private static int headAngle = 0;
	private static int previousLightDifference = 255;
	private static LightHistory lightHistory;
	private static final File doh = new File("doh.wav");
	private static final File aaah = new File("aaah.wav");
	private static final File woohoo = new File("woohoo.wav");
	private static final File timidDoh = new File("timidDoh.wav");
	private static final File oopsy = new File("oopsy.wav");
	
	public static void main(String[] args) throws Exception
	{
		setup();
		
		algorithmOnePointOne();
	}
	
	private static void setup()
	{
		pilot = new DifferentialPilot(55, 164, Motor.B, Motor.C);
		sensor = new UltrasonicSensor(SensorPort.S4);
		ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(false);
		lightHistory = new LightHistory(LIGHT_HISTORY_SIZE);
		
		Motor.A.setSpeed(720);
		
		pilot.setTravelSpeed(130);
		pilot.setRotateSpeed(90);
	}

	private static void algorithmOnePointOne() throws Exception
	{
		// Set state
		// 0 : Rotating head, trying to find a perpendicular wall
		// 1 : I know there's nothing around me. Let's go forward to the limit
		// 2 : Boost is looking perpendicular at a wall
		// 3 : There is a wall in front of me. I need to turn right
		// 4 : I know there is a wall to my left. Go forward, checking both sensors
		// 5 : There is no wall on the left. I now need to turn left
		// 6 : I have just turned left. I need to go forward until I find a wall again
		// 7 : I'm slowly converging with the wall to the left
		// 8 : I'm slowly going away from the wall on the left
		int state = 0;
		if (EXPERIMENT_NUMBER > 0) state = 4;
		LCD.drawString("I AM BOOST (ex " + EXPERIMENT_NUMBER + ")", 0, 0);
		while (!Button.ESCAPE.isDown())
		{
			LCD.clear(1);
			LCD.clear(2);
			LCD.drawString("State " + state, 0, 1);
			if (state == 0)
			{
				if( findPerpendicularWall(-90, 90, 2) )
				{
					state = 2;
				}
				else //no perp wall so we go to state -2
				{
					state = 1;
				}
			}
			else if (state == 1)
			{
				go(500);
				state = 0;
			}
			else if (state == 2)
			{
				if(checkSideWall())
				{
					LCD.drawString("I found a side wall", 0, 2);
					lightHistory.clear();
					state = 4;
					continue;
				}
				else if(!checkFrontWall())
				{
					LCD.drawString("I haven't found a wall yet", 0, 2);
					go();
				}
				else
				{
					state = 3;
					continue;
				}				
			}
			else if (state == 3) // There is a wall in front of me!
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
				else if (reading <= MIN_DIST - 5)
				{
					go(((MIN_DIST - 3) - sensor.getDistance()) * -10); // Reverse a bit
				}
				turnRight(90);
				lightHistory.clear();
				state = 4;
			}
			else if (state == 4)
			{
				LCD.drawString("I'm following the side wall", 0, 2);
				go();
				if (checkFrontWall()) // If there is a wall in front, turn right.
				{
					state = 3;
					continue;
				}
				else if (!checkSideWall()) // If there is no side wall we need to turn left
				{
					if (EXPERIMENT_NUMBER == 1 || EXPERIMENT_NUMBER == 2)
					{
						stop();
						state = 5;
					}
					else
					{
						// EXPERIMENT NORMAL:
						stop();
						int sideWall = reallyNoSideWall();
						if (sideWall == 1)
						{
							state = 5; // No wall = turn left
							continue;
						}
						else if (sideWall == -2 && EXPERIMENT_NUMBER == 0)
						{
							state = 9; // i.e. go left 90 degrees.
							continue;
						}
						else if (sideWall == -1 && EXPERIMENT_NUMBER == 0)
						{
							state = 9;
							continue;
						}
						else
						{
							go(90);
						}
					}
				} 
				else
				{
					int trend = lightHistory.checkForATrend();
					if (trend == 1) // We're about to collide with the wall
					{
						state = 7;
					}
					else if (trend == -1) // We're losing the wall
					{
						state = 8;
					}
					// else we're following the wall perfectly. Keep calm and carry on.
				}
			}
			else if (state == 5) // The wall to my left has gone!
			{
				Sound.playSample(aaah);
				go(35);
				LCD.clear(2);
				LCD.drawString("Oh dear oh dear oh dear", 0, 2);
				arcLeft(90);
				state = 6;
			}
			else if (state == 6)
			{
				go();
				if (checkFrontWall())
				{
					state = 3;
				}
				else if (checkSideWall())
				{
					lightHistory.clear();
					state = 4;
					Sound.playSample(woohoo);
				}
				// If there is not a side wall yet, keep going forward. No change.
				// state = 6;
			}
			else if (state == 7) // We're slowly converging with the wall
			{
				Sound.playSample(timidDoh);
				stop();
				arcRight(5); // Turn right a little bit
				go();
				lightHistory.clear();
				state = 4;
			}
			else if (state == 8) // We're slowly going away from the wall
			{
				Sound.playSample(oopsy);
				stop();
				arcLeft(5); // Turn left a little bit
				go();
				lightHistory.clear();
				state = 4;
			}
			else if (state == 9) // We're either too close to the wall on the left or a bit too far away
			{
				stop();
				turnLeft(90);
				state = 2;
			}
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
	
	/*
	 * Return
	 * 1 if there is NO wall (i.e. wall is MIN_DIST + 10 away
	 * 0 if there is a wall (i.e. wall is between MIN_DIST and MIN_DIST+4
	 * -1 if there is a wall - but it's close (i.e wall is less than MIN_DIST)
	 * -2 if there is a wall - but we should turn a little closer (i.e. wall is between MIN_DIST+5 and MIN_DIST + 10)
	 */
	private static int reallyNoSideWall()
	{
		int sideWall = -7;
		
		rotateHeadToAngle(90);
		if(!checkFrontWall(MIN_DIST + 10))
		{
			sideWall = 1;
		}
		else if(checkFrontWall(MIN_DIST + 5) && !checkFrontWall(MIN_DIST))
		{
			sideWall = 0;
		}
		else if(checkFrontWall(MIN_DIST))
		{
			sideWall = -1;
		}
		else if (checkFrontWall(MIN_DIST+10) && !checkFrontWall(MIN_DIST + 4))
		{
			sideWall = -2;
		}
		
		rotateHeadToAngle(0);
		
		return sideWall;
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
		LED_THRESHOLD = calculateThreshold(offValue);
		difference = onValue - offValue;
		
		if(previousLightDifference == 255)
			previousLightDifference = difference;
		
		differenceChange = previousLightDifference - difference;
		previousLightDifference = difference;
		
		lightHistory.add(differenceChange);
		
		LCD.clear(4);
		LCD.clear(5);
		LCD.clear(6);
		LCD.drawString("      diff=" + difference, 0, 4);
		LCD.drawString("diffchange=" + differenceChange, 0, 5);
		LCD.drawString(" threshold=" + LED_THRESHOLD, 0, 6);
		if (difference > LED_THRESHOLD) return true;
		else return false;
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
	
	private static int calculateThreshold(int v)
	{
		// Threshold calculated using Excel.
		// y = 0.00105151582815248x2 - 0.81710134380644700x + 161.65010262509900000
		if (EXPERIMENT_NUMBER == 1) return 20;
		else return (int) ((0.00105151582815248 * v * v) + (-0.817101343806447 * v) + 161.650102625099);
	}
}
