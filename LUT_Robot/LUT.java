package ece.cpen502;
import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;

public class LUT_Robot extends AdvancedRobot {

	private static final double PI = Math.PI;
	private Target t;
	private LUT lut;
	private Learning agent;
	private double reward = 0.0;
	private double fire_power;
	private int isHitByWall = 0;
	private int isHitByBullet = 0;

	private static int numberOfLosses = 0;
	private static int numberOfWins = 0;

	public void run() {
		lut = new LUT();
		loadData();
		agent = new Learning(lut);
		t = new Target();
		t.distance = 100000;

		setColors(Color.CYAN, Color.PINK, Color.CYAN);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI);

		while (true) {
			moveRobot();
			fire_power = 400 / t.distance;
			if (fire_power > 3)
				fire_power = 3;
			radarRotate();
			gunRotate();
			if (getGunHeat() == 0)
				setFire(fire_power);
			execute();
		}

	}

	/*
	 * Robot events
	 */

	 //onBulletHit: What to do when you hit other robots
	 public void onBulletHit(BulletHitEvent event) {
		 if (t.name.equals(event.getName())) {
			 double change = event.getBullet().getPower() * 9;
			 out.println("Bullet Hit: " + change);
			 reward += change;
		 }
	 }

	 //onBulletMissed: What to do when you miss other robots
	 public void onBulletMissed(BulletMissedEvent event) {
		 double change = -event.getBullet().getPower();
		 out.println("Bullet Missed: " + change);
		 reward += change;
	 }

	 //onHitByBullet: What to do when you are hit by a bullet
	 public void onHitByBullet(HitByBulletEvent event) {
		 if (t.name.equals(event.getName())) {
			 double power = event.getBullet().getPower();
			 double change = -(4 * power + 2 * (power - 1));
			 out.println("Hit By Bullet: " + change);
			 reward += change;
		 }
		 isHitByBullet = 1;
	 }

	 //onHitRobot: What is to be done when you are hit by another robot
	 public void onHitRobot(HitRobotEvent event) {
		 if (t.name.equals(event.getName())) {
			 double change = -6.0;
			 out.println("Hit Robot: " + change);
			 reward += change;
		 }
	 }

	 //onHitWall: What is to be done when you are hit by a wall
	 public void onHitWall(HitWallEvent event) {
		 double change = -10.0;
		 out.println("Hit Wall: " + change);
		 reward += change;
		 isHitByWall = 1;
	 }

	 //onScannedRobot: What is to be done when you see another robot
	 public void onScannedRobot(ScannedRobotEvent event) {
		 if ((event.getDistance() < t.distance) || (t.name.equals(event.getName()))) {
			 double bearing = (getHeadingRadians() + event.getBearingRadians()) % (2 * PI);
			 double h = normalizeBearing(event.getHeadingRadians() - t.head);
			 t.name = event.getName();
			 t.changeHead = h / (getTime() - t.time);
			 t.pos_x = getX()+Math.sin(bearing)*event.getDistance();
			 t.pos_y = getY()+Math.cos(bearing)*event.getDistance();
			 t.bearing = event.getBearingRadians();
			 t.head = event.getHeadingRadians();
			 t.time = getTime();
			 t.speed = event.getVelocity();
			 t.distance = event.getDistance();
			 t.energy = event.getEnergy();
		 }
	 }

	 //onRobotDead: What to do when other robot dead
	 public void onRobotDeath(RobotDeathEvent event) {
		 if (t.name.equals(event.getName()))
			 t.distance = 10000;
	 }

	 //onWin: Robot wins the game
	 public void onWin(WinEvent event) {
		 //numberOfWins++;
		 //System.out.println("Number of wins: "+numberOfWins);
		 saveData();
	 }

	 //onDeath: Robot loses the game
	 public void onDeath(DeathEvent event) {
		 //numberOfLosses++;
		 //System.out.println("Number of losses: "+numberOfLosses);
		 saveData();
	 }

	 /*
	  * Helper methods
	  */

	 private void moveRobot() {
		 int state = getState();
		 int action = agent.selectAction(state, getTime());
		 out.println("Action selected: " + action);
		 agent.offPolicyLearning(state, action, reward);
		 reward = 0.0;
		 isHitByWall = 0;
		 isHitByBullet = 0;

		 switch (action) {
		 case Action.RobotAhead:
			 setAhead(Action.RobotMoveDistance);
			 break;
		 case Action.RobotBack:
			 setBack(Action.RobotMoveDistance);
			 break;
		 case Action.RobotAheadTurnLeft:
			 setAhead(Action.RobotMoveDistance);
			 setTurnLeft(Action.RobotTurningDegree);
			 break;
		 case Action.RobotAheadTurnRight:
			 setAhead(Action.RobotMoveDistance);
			 setTurnRight(Action.RobotTurningDegree);
			 break;
		 case Action.RobotBackTurnLeft:
			 setAhead(Action.RobotMoveDistance);
			 setTurnRight(Action.RobotTurningDegree);
			 break;
		 case Action.RobotBackTurnRight:
			 setAhead(t.bearing);
			 setTurnLeft(Action.RobotTurningDegree);
			 break;
		 }
	 }

	 private int getState() {
		 int heading = State.getHeading(getHeading());
		 int distance = State.getTargetDistance(t.distance);
		 int bearing = State.getTargetBearing(t.bearing);
		 out.println("State(" + heading + ", " + distance + ", " + bearing + ", " + isHitByWall + ", " + isHitByBullet + ")");
		 return State.Mapping[heading][distance][bearing][isHitByWall][isHitByBullet];
	 }

	 private void radarRotate() {
		 double radarOffset;
		 if (getTime() - t.time > 4) { 
			 radarOffset = 4 * PI;
		 } else {
			 radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(t.pos_y - getY(),t.pos_x - getX()));
			 radarOffset = normalizeBearing(radarOffset);
			 if (radarOffset < 0)
				 radarOffset -= PI / 10;
			 else
				 radarOffset += PI / 10;
		 }
		 setTurnRadarLeftRadians(radarOffset);
	 }

	 private void gunRotate() {
		 long time;
		 long nextTime;
		 Point2D.Double p;
		 p = new Point2D.Double(t.pos_x, t.pos_y);
		 for (int i = 0; i < 20; i++) {
			 nextTime = (int) Math.round((getRange(getX(), getY(), p.x, p.y) / (20 - (3 * fire_power))));
			 time = getTime() + nextTime - 10;
			 p = t.positionAtTime(time);
		 }
		 double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
		 setTurnGunLeftRadians(normalizeBearing(gunOffset));
	 }

	 private double normalizeBearing(double bearing) {
		 if (bearing < -PI)
			 bearing += 2 * PI;
		 if (bearing > PI)
			 bearing -= 2 * PI;
		 return bearing;
	 }

	 private double getRange(double x1, double y1, double x2, double y2) {
		 double deltaX = x2 - x1;
		 double deltaY = y2 - y1;
		 return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	 }

	 /*
	  * Load and save the lookup table
	  */
	 private void loadData() {
		 try {
			 lut.load("C:\\robocode\\robots\\ece\\cpen502\\LUT_Robot.dat");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }

	 private void saveData() {
		 try {
			 lut.save(getDataFile("LUT_Robot.dat"));
		 } catch (Exception e) {
			 out.println("Exception trying to write: " + e);
		 }
	 }

}
