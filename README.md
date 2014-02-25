BOOST
=====

CM30229 Coursework 1

Robot thoughts
--------------

With the side led, keep flashing it. If there is a significant difference between the reading when the light is on and the reading when the light is off, there is a wall there. 

Step 1: Go forward slowly, sweeping the ultrasound head around a 180 degree angle. The head will eventually get to a point where at a particular angle, a wall will be the correct distance away. 

Step 2: This angle can be calculated. Rotate robot so that it is parallel to the wall. 

Step 3: Flash the led, taking a light measurement with light on and another with the light off. There should be a difference. Keep taking this measurement every half second. 

Step 4: move forwards with the led flashing add and the ultrasound pointing directly ahead. At some point, Either the ultrasound will detect a wall or the difference between light on and light off will be minimal. 

Corner turning

If the wall detector no longer detects a wall, it could be because it has veered away from the wall slowly. It should turn left 45 degrees, then sweep to look for a wall within 20cm. If there is, align up to the wall and carry on. Otherwise it should turn the full 90 degrees (45 more) and carry on.

NOTES FOR REPORT

Error with light detector on back and ultrasound check on front.  Light registers end of wall (but not true) and BOOST too forward (i.e. ultrasound also registers end of wall), so BOOST wrongly turns.  SOLUTION: Move light detector closer to ultrasound, move forward X mm before rotation.




DAVE'S AWESOME IDEA
- Can we make a robot using finite state machine that can navigate around a course, starting parallel to the wall, just using the LED?
	- Assuming we are only turning left
- Then test in different light conditions
	- Expected fail
- Added the variable threshold to improve the test
	- Test should now pass :)
- Move on to Brooks, we want it to be real world - add ultrasound to make it more robust and start it from anywhere.

This might take longer than 2 pages to write, but write it then cut it down.
	
	Test contents
- Number of turns done correctly - pass
- Fail when robot doesn't turn when it should
- Fail when robot turns when it shouldn't
- Try walls of different materials. If it goes straight past a material change, that's also a pass :)
