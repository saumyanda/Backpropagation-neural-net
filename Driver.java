package ece.cpen502;

import java.io.File;
import java.io.IOException;

public class Driver {

	public static void main(String[] args) throws IOException {
		NeuralNet nn = new NeuralNet();
		nn.initializeTrainingSet();

		//Training the network for 100 trials
		int avgEpoch=0;
		for(int i = 0; i < 100; i++) {
			nn.initializeWeights();
			avgEpoch += nn.train();
		}
		avgEpoch = avgEpoch / 100;
		System.out.println("avg epoch " + avgEpoch);

		//Save error to a file
		nn.saveErrorToFile("C:\\Users\\saumy\\Desktop\\error.txt");

		//	Load and save weights to file
		nn.save(new File("C:\\Users\\saumy\\Desktop\\weights.txt"));
		try {
			nn.load("C:\\Users\\saumy\\Desktop\\weights.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
