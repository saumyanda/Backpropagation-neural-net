package ece.cpen502;
import java.awt.geom.Point2D;

public class Target {

	String name;
	double bearing, head, speed, pos_x, pos_y, distance, changeHead, energy;
	long time;

	public Point2D.Double positionAtTime(long timeForPrediction) 
	{
		double predicted_Y, predicted_X;
		double delta_time = timeForPrediction - time;
		if (Math.abs(changeHead) > 0.00001) {
			double radius = speed / changeHead;
			double delta_head = delta_time * changeHead;
			predicted_Y = pos_y + (Math.sin(head + delta_head) * radius) - (Math.sin(head) * radius);
			predicted_X = pos_x + (Math.cos(head) * radius) - (Math.cos(head + delta_head) * radius);
		} else {
			predicted_Y = pos_y + Math.cos(head) * speed * delta_time;
			predicted_X = pos_x + Math.sin(head) * speed * delta_time;
		}
		return new Point2D.Double(predicted_X, predicted_Y);
	}

}
