package reinforcement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NeuralTest {
	static String numNeuronEpoch = new String ("C:/robocode/New folder/NeuronEpoch.txt");

	public static void main(String[] args) {
		MyNeuralNet NNTest = new MyNeuralNet();
		NNTest.main();
	}

	private static void saveConvergeEpoch(int n, int epoch) {
		BufferedWriter writer = null;
		try {
		writer = new BufferedWriter(new FileWriter(numNeuronEpoch, true));
			writer.write(n + "\t" + epoch + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
