import java.io.File;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;


public class Boost {

	public static void main(String[] args) throws Exception {
		UltrasonicSensor sensor = new UltrasonicSensor(SensorPort.S4);
		LightSensor ls = new LightSensor(SensorPort.S1);
		ls.setFloodlight(true);
		Motor.B.setSpeed(90);
		Motor.C.setSpeed(90);
		int distance = 255;
		int brightness = 0;
		final File donk = new File("donk2.wav"); 
		Sound.playSample(donk, 100);
		while(!Button.ESCAPE.isDown()) {
			LCD.clear();
			LCD.drawString("I AM BOOST", 0, 0);
			distance = sensor.getDistance();
			brightness = ls.readValue();
			LCD.drawString("distance: " + distance + "cm", 0, 1);
			LCD.drawString("   light: " + brightness, 0, 2);
			if (Button.ENTER.isDown()) {
				forward(1);
			}
			if (Button.RIGHT.isDown()) {
				turnLeft(1);
			}
			if (Button.LEFT.isDown()) {
				turnRight(1);
			}
			if (distance < 10) {
//				Sound.playNote(Sound.PIANO, 880 - (distance * 20), 500);
				LCD.drawString("MOVE BITCH", 0, 3);
				LCD.drawString("GET OUT THE WAY!", 0, 4);
				Motor.B.backward();
				Motor.C.backward();
//				Sound.beep();
			} else {
				Motor.B.stop(true);
				Motor.C.stop(true);
			}
			Thread.sleep(250);
		}
		ls.setFloodlight(false);
	}
	
	public static void forward(float seconds) throws Exception {
		Motor.B.forward();
		Motor.C.forward();
		Thread.sleep((long) (1000*seconds));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}
	
	public static void turnRight(float seconds) throws Exception {
		Motor.B.backward();
		Motor.C.forward();
		Thread.sleep((long) (1000*seconds));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}
	
	public static void turnLeft(float seconds) throws Exception {
		Motor.B.forward();
		Motor.C.backward();
		Thread.sleep((long) (1000*seconds));
		Motor.B.stop(true);
		Motor.C.stop(true);
	}

}
