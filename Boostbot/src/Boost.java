import java.io.File;

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
			if (distance < 10) {
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
}
