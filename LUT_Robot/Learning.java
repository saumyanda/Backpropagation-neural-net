package ece.cpen502;

public class Learning {
	//Learning Rate
	public static final double learningRate = 0.1;
	//Discount Factor or Gamma
	public static final double discountFactor = 0.9;

	public static final double exploitationRate = 1;
	private int current_state;
	private int current_action;
	private boolean is_first_round = true;
	private LUT lut;

	public Learning(LUT lut) {
		this.lut = lut;
	}

	/*
	 * Off Policy Learning
	 */
	public void offPolicyLearning(int next_state, int next_action, double reward) {
		if (is_first_round)
			is_first_round = false;
		else {
			double old_QValue = lut.getQValue(current_state, current_action);
			double new_QValue = (1 - learningRate) * old_QValue + learningRate * (reward + discountFactor * lut.getMaxQValue(next_state));
			lut.setQValue(current_state, current_action, new_QValue);
		}
		current_state = next_state;
		current_action = next_action;
	}

	/*
	 * On Policy Learning
	 */
	public void onPolicyLearning(int next_state, int next_action, double reward) {
		if (is_first_round)
			is_first_round = false;
		else {
			double old_QValue = lut.getQValue(current_state, current_action);
			double new_QValue = (1 - learningRate) * old_QValue + learningRate * (reward + discountFactor * lut.getQValue(next_state, next_action));
			lut.setQValue(current_state, current_action, new_QValue);
		}
		current_state = next_state;
		current_action = next_action;
	}


	public int selectAction(int state, long time) {
		double q_value;
		double sum = 0.0;
		double[] value_array = new double[Action.NumberOfRobotActions];
		for (int i = 0; i < value_array.length; i++) {
			q_value = lut.getQValue(state, i);
			value_array[i] = Math.exp(exploitationRate * q_value);
			sum += value_array[i];
			//System.out.println("Q-value: " + qValue);
		}

		if (sum != 0)
			for (int i = 0; i < value_array.length; i++) {
				value_array[i] /= sum;
				//System.out.println("P(a|s): " + value[i]);
			}
		else
			return lut.get_MaxQAction(state);

		int action = 0;
		double cumulative_probability = 0.0;
		double random_number = Math.random();
		System.out.println("Random Number: " + random_number);
		while (random_number > cumulative_probability && action < value_array.length) {
			cumulative_probability += value_array[action];
			action++;
		}
		return action - 1;
	}
}
