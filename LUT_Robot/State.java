package ece.cpen502;
public class State {

	public static final int NUM_TARGET_DISTANCE = 20;
	public static final int NUM_TARGET_BEARING = 4;
	public static final int NUM_HEADING = 4;
	public static final int NUM_HIT_BY_BULLET = 2;
	public static final int NUM_HIT_WALL = 2;
	public static final int NUM_STATES;
	public static final int[][][][][] Mapping;

	static {
		Mapping = new int[NUM_HEADING][NUM_TARGET_DISTANCE][NUM_TARGET_BEARING][NUM_HIT_WALL][NUM_HIT_BY_BULLET];
		int index = 0;
		for (int i = 0; i < NUM_HEADING; i++) {
			for (int j = 0; j < NUM_TARGET_DISTANCE; j++) {
				for (int k = 0; k < NUM_TARGET_BEARING; k++) {
					for (int l = 0; l < NUM_HIT_WALL; l++) {
						for (int m = 0; m < NUM_HIT_BY_BULLET; m++)
							Mapping[i][j][k][l][m] = index++;
					}
				}
			}
		}
		NUM_STATES = index;
	}

	public static int getHeading(double head) {
		double angle = 360.0 / NUM_HEADING;
		double new_head = head + angle / 2;
		if (new_head > 360.0)
			new_head -= 360.0;
		int target_heading = (int) (new_head / angle);
		return target_heading;
	}

	public static int getTargetDistance(double dist) {
		int target_distance = (int) (dist / 30.0);
		if (target_distance > NUM_TARGET_DISTANCE - 1)
			target_distance = NUM_TARGET_DISTANCE - 1;
		return target_distance;
	}

	public static int getTargetBearing(double bearing) {
		if (bearing < 0)
			bearing = Math.PI * 2 + bearing;
		double angle = (Math.PI * 2) / NUM_TARGET_BEARING;
		double new_bearing = bearing + angle / 2;
		if (new_bearing > (Math.PI * 2))
			new_bearing -= Math.PI * 2;
		int target_bearing = (int) (new_bearing / angle);
		return target_bearing;
	}

}
