package reinforcement;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class RLTest extends AdvancedRobot implements LUTInterface {
	static File savetest = new File("C:/robocode/New folder/saveFile.txt");
	static File convergeCheck = new File("C:/robocode/New folder/convCheck.txt");
	static File rewardCheck = new File("C:/robocode/New folder/rewCheck.txt");
	static int battleCount = 1;
	public static double reward = 0;
	public static double finalReward = 0;
	public static final int numHeading = 4;
	public static final int numEnemyBearing = 4;
	public static final int numEnemyDistance = 2;
	public static final int numMyEnergy = 10;
	public static final int numEnemyEnergy = 10;
	public static int stateIndex[][][] = new int[numHeading][numEnemyBearing][numEnemyDistance];

	private static final int numState = numHeading * numEnemyBearing
			* numEnemyDistance;
	private static final int numAction = 6;
	static double[][] qtable = new double[numState][numAction];
	static int[] actionIndex = new int[6];
	public static final int goAhead = 0;
	public static final int goBack = 1;
	public static final int goAheadTurnLeft = 2;
	public static final int goAheadTurnRight = 3;
	public static final int goBackTurnLeft = 4;
	public static final int goBackTurnRight = 5;
	public static final int numActions = 6;
	private boolean first = true;
	public static final double LearningRate = 0.1;
	public static final double DiscountRate = 0.9;
	public static final double ExplorationRate = 1;
	public static final double ExploitationRate = 0.5;
	private int lastStateIndex;
	private int lastAction;
	public static final double RobotMoveDistance = 300.0;
	public static final double RobotTurnDegree = 45.0;
	private double firePower = 1;

	String nameEnemy;
	public double bearingEnemy;
	public double headingEnemy;
	public double spotTimeEnemy;
	public double speedEnemy;
	public double XPositionEnemy;
	public double YPositionEnemy;
	public double distanceEnemy = 10000;
	public double constantHeadingEnemy;
	public double energyEnemy;

	public void run() {
		initialiseLUT();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			move();
			firePower = 400 / distanceEnemy;
			radarMove();
			gunMove();
			if (getGunHeat() == 0) {
				setFire(firePower);
			}
			execute();
		}
	}

	private void gunMove() {
		long time;
		long nextTime;
		Point2D.Double p;
		p = new Point2D.Double(XPositionEnemy, YPositionEnemy);
		for (int i = 0; i < 20; i++) {
			nextTime = (int) Math
					.round((getrange(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
			time = getTime() + nextTime - 10;
			p = futurePosition(time);
		}
		double gunOffset = getGunHeadingRadians()
				- (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
		setTurnGunLeftRadians(nomalizeDegree(gunOffset));
	}

	public Point2D.Double futurePosition(long futureTime) {
		double duration = futureTime - spotTimeEnemy;
		double futureX;
		double futureY;
		if (Math.abs(constantHeadingEnemy) > 0.000001) {
			double radius = speedEnemy / constantHeadingEnemy;
			double tothead = duration * constantHeadingEnemy;
			futureY = YPositionEnemy
					+ (Math.sin(headingEnemy + tothead) * radius)
					- (Math.sin(headingEnemy) * radius);
			futureX = XPositionEnemy + (Math.cos(headingEnemy) * radius)
					- (Math.cos(headingEnemy + tothead) * radius);
		} else {
			futureY = YPositionEnemy + Math.cos(headingEnemy) * speedEnemy
					* duration;
			futureX = XPositionEnemy + Math.sin(headingEnemy) * speedEnemy
					* duration;
		}
		return new Point2D.Double(futureX, futureY);
	}

	private double getrange(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = Math.sqrt(xo * xo + yo * yo);
		return h;
	}

	private void radarMove() {
		double turnDegree;
		if (getTime() - spotTimeEnemy > 4) {
			turnDegree = Math.PI * 4;
		} else {
			turnDegree = getRadarHeadingRadians()
					- (Math.PI / 2 - Math.atan2(YPositionEnemy - getY(),
							XPositionEnemy - getX()));

			turnDegree = nomalizeDegree(turnDegree);
			if (turnDegree < 0)
				turnDegree -= Math.PI / 10;
			else
				turnDegree += Math.PI / 10;
		}

		setTurnRadarLeftRadians(turnDegree);
	}

	private double nomalizeDegree(double ang) {
		if (ang > Math.PI)
			ang -= 2 * Math.PI;
		if (ang < -Math.PI)
			ang += 2 * Math.PI;
		return ang;
	}

	private void move() {
		double tmpHeading = getHeading();
		tmpHeading += 45;
		if (tmpHeading > 360)
			tmpHeading -= 360;
		int heading = (int) (tmpHeading / 90);
		if (heading > numHeading - 1)
			heading = numHeading - 1;
		if (bearingEnemy < 0)
			bearingEnemy += 360;
		double tmpBearingEnemy = bearingEnemy + 45;
		if (tmpBearingEnemy > 360)
			tmpBearingEnemy -= 360;
		int enemyBearing = (int) (tmpBearingEnemy / 90);
		if (enemyBearing > numEnemyBearing - 1)
			enemyBearing = numEnemyBearing - 1;
		int enemyDistance = (int) (distanceEnemy / 200);
		if (enemyDistance > numEnemyDistance - 1)
			enemyDistance = numEnemyDistance - 1;
		int myEnegy = (int) (getEnergy() / 10);
		if (myEnegy > numMyEnergy - 1)
			myEnegy = numMyEnergy - 1;

		int enemyEnergy = (int) (energyEnemy / 10);
		if (enemyEnergy > numEnemyEnergy - 1)
			enemyEnergy = numEnemyEnergy - 1;

		double[] currentState = { heading, enemyBearing, enemyDistance };
		double crrentAction = train(currentState, reward);
		int intAction = (int) crrentAction;

		reward = 0;
		executeAction(intAction);
	}
	private void executeAction(int action) {
		switch (action) {
		case goAhead:
			setAhead(RobotMoveDistance);
			break;

		case goBack:
			setBack(RobotMoveDistance);
			break;

		case goAheadTurnLeft:
			setAhead(RobotMoveDistance);
			setTurnLeft(goAhead);
			break;

		case goAheadTurnRight:
			setAhead(RobotMoveDistance);
			setTurnRight(RobotTurnDegree);
			break;

		case goBackTurnLeft:
			setBack(RobotMoveDistance);
			setTurnRight(RobotTurnDegree);
			break;

		case goBackTurnRight:
			setBack(RobotMoveDistance);
			setTurnRight(RobotTurnDegree);
			break;
		}
	}

	@Override
	public double outputFor(double[] X) {
		return 0;
	}

	@Override
	public double train(double[] X, double reward) {
		int stateIndexNow = indexFor(X);
		double maxQ = maxQValueFor(stateIndexNow);
		int actionNow = selectAction(stateIndexNow);
		double diffQValue = 0;

		System.out.println("Reward: " + reward);
		if (first)
			first = false;
		else {


			// for Q-Learning
			double oldQValue = qtable[lastStateIndex][lastAction];
			double newQValue = (1 - LearningRate) * oldQValue + LearningRate
					* (reward + DiscountRate * maxQ);
			diffQValue = newQValue - oldQValue;
			qtable[lastStateIndex][lastAction] = newQValue;
		}
		lastStateIndex = stateIndexNow;
		lastAction = actionNow;
		if (stateIndexNow == 1199 && actionNow == 0)
			saveDiffQValue(stateIndexNow, actionNow, diffQValue, convergeCheck);


		return actionNow;
	}

	private int selectAction(int stateIndexNow) {
		int action = actionMaxQValue(stateIndexNow);
		if (Math.random() > ExplorationRate) {
			return action;
		} else {
			int tmpAction = (int)(Math.random() * 10) % 6;
			return tmpAction;
		}
	}

	private void saveDiffQValue(int stateIndexNow, double action,
			double diffQValue, File convergeCheck) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(convergeCheck, true));
			writer.write(battleCount + "\t" + stateIndexNow + "\t" + action
					+ "\t" + diffQValue + "\n");

		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void save(File argFile) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(argFile));
			for (int i = 0; i < numState; i++)
				for (int j = 0; j < numAction; j++) {					
					int saveDistance = i % 2;
					int saveHeading = i / 8;
					int saveBearing = (i - saveDistance * 2 - saveHeading * 4) / 4;					
					if (qtable[i][j] != 0)
						writer.write(/*i + "\t" +*/ saveHeading + "\t" + saveBearing + "\t" + saveDistance + "\t" + j + "\t" + qtable[i][j]
								+ "\n");
				}

		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}

	}

	@Override
	public void load(File inputFile) throws IOException {
		try {
			FileInputStream fileInputStream = new FileInputStream(inputFile);
			Scanner inputScanner = new Scanner(fileInputStream);

			for (int i = 0; i < numState; i++)
				for (int j = 0; j < numAction; j++) {
					double statee = inputScanner.nextDouble();
					double actionn = inputScanner.nextDouble();
					if ((int) statee == i && (int) actionn == j)
						qtable[i][j] = inputScanner.nextDouble();
				}

		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}

	@Override
	public void initialiseLUT() {
		int count = 0;
		for (int a = 0; a < numHeading; a++)
			for (int b = 0; b < numEnemyBearing; b++)
				for (int c = 0; c < numEnemyDistance; c++)
					stateIndex[a][b][c] = count++;

		for (int x = 0; x < numState; x++)
			for (int y = 0; y < numAction; y++)				
				qtable[x][y] = 0;
	}

	@Override
	public int indexFor(double[] X) {
		int index = ((int) X[0] + 1) * 4 + ((int) X[1] + 1) * 4 + ((int) X[2] + 1) * 2 -1;
		return index;
	}

	public double maxQValueFor(int index) {
		double max = 0;
		for (int i = 0; i < numAction; i++) {
			if (qtable[index][i] > max)
				max = qtable[index][i];
		}
		return max;
	}

	public int actionMaxQValue(int stateIndexNow) {
		double max = 0;
		int action = 0;
		MyNeuralNet nn = new MyNeuralNet();
		for (int i = 0; i < numAction; i++) {

			double saveDistance = stateIndexNow % 2;
			double saveHeading = stateIndexNow / 8;
			double saveBearing = (stateIndexNow - saveDistance * 2 - saveHeading * 4) / 4;
			double[] sVector = new double[]{saveHeading, saveBearing, saveDistance, i};
			double tmpMax = nn.getQValue(sVector);
			if (tmpMax > max) {
				max = tmpMax;
				action = i;
			}

		}
		return action;
	}
	public void onBulletHit(BulletHitEvent e) {
		double change = e.getBullet().getPower() * 9;
		reward += change;
		finalReward += change;
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {

	}

	public void onHitByBullet(HitByBulletEvent e) {

		double change = -5 * e.getBullet().getPower();
		reward += change;
		finalReward += change;
	}

	public void onBulletMissed(BulletMissedEvent e) {
		double change = -e.getBullet().getPower();
		reward += change;
		finalReward += change;
	}

	public void onHitRobot(HitRobotEvent e) {

		double change = -6.0;
		reward += change;
		finalReward += change;
	}

	public void onHitWall(HitWallEvent e) {

		double change = -(Math.abs(getVelocity()) * 0.5 - 1);
		reward += change;
		finalReward += change;
	}

}
