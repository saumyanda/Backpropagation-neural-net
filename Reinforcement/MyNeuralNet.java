package reinforcement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import robocode.RobocodeFileOutputStream;

public class MyNeuralNet implements NeuralNetInterface {


	public static void main() {
		System.out.println("numHidden: " + numHidden);
		MyNeuralNet bp = new MyNeuralNet();
		try {
			bp.load(inputFile);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		bp.initWeights();

		do {
			totalError = 0;
			epoch++;
			for (int i = 0; i < numVectors; i++) {
				bp.train(inputValues[i], targetValues[i]);
				totalError += getOneVectorError(tmpOut, targetValues[i]);

			}


			System.out.println("epoch: " + epoch + ", total error: " + totalError);
			if (epoch > 1000)
				break;
		} while (totalError > acceptableError /*xxx-->0*/);

	}

	private static double getOneVectorError(double tmpOut, double target) {
		double onceError = 0.5 * Math.pow((tmpOut - target), 2);
		return onceError;

	}

	@Override
	public double outputFor(double[] X) {
		return 0;
	}

	@Override
	public double train(double[] X, double target) {
		calculateHidVal(X);
		calculateOutVal();

		calculateOutErr(target);
		calculateHidErr();


		updateWeightInpToHid(X);
		updateWeightHidToOut();
		return tmpOut;
	}

	private void calculateHidVal(double[] X) {
		for (int i = 0; i < numHidden; i++) {
			hiddenValues[i] = 0;
			for (int j = 0; j < numInputs; j++) {
				hiddenValues[i] += X[j] * weightInputToHidden[i][j];
			}
			hiddenValues[i] += weightInputToHidden[i][numInputs] * 1; 

			hiddenValues[i] = sigmoid(hiddenValues[i]);
		}
	}

	private void calculateOutVal() {
		tmpOut = 0;
		for (int i = 0; i < numHidden; i++) {
			tmpOut += hiddenValues[i] * weightHiddenToOutput[i];
		}
		tmpOut += weightHiddenToOutput[numHidden] * 1; 
		tmpOut = sigmoid(tmpOut); 
	}

	private void calculateOutErr(double target) {

		outputError = 0.5 * (1 +tmpOut) * (1 - tmpOut) * (target - tmpOut);


	}

	private void calculateHidErr() {

		for (int i = 0; i < numHidden; i++) {
			hiddenError[i] = 0.5 * (1 +hiddenValues[i]) * (1 - hiddenValues[i])
					* outputError * weightHiddenToOutput[i];

		}
	}

	private void updateWeightInpToHid(double[] X) {

		for (int i = 0; i < numHidden; i++) {
			System.arraycopy(weightInputToHidden[i], 0, tempWeightInputToHidden[i], 0,
					weightInputToHidden[i].length);
		}


		for (int i = 0; i < numHidden; i++) {
			for (int j = 0; j < numInputs; j++) {
				weightInputToHidden[i][j] += learningRate
						* hiddenError[i] * X[j] + momentumTerm
						*  getPrevToHidDeltaWt(i, j);
			}
			weightInputToHidden[i][numInputs] += learningRate
					* hiddenError[i] + momentumTerm
					*  getPrevToHidDeltaWt(i, numInputs);
		}

		for (int i = 0; i < numHidden; i++) {
			System.arraycopy(tempWeightInputToHidden[i], 0, previousWeightInputToHidden[i], 0,
					weightInputToHidden[i].length);
		}

	}

	private void updateWeightHidToOut() {
		System.arraycopy(weightHiddenToOutput, 0, tempWeightHiddenToOutput, 0,
				weightHiddenToOutput.length);

		for (int i = 0; i < numHidden; i++) {
			weightHiddenToOutput[i] += learningRate * outputError
					* hiddenValues[i] + momentumTerm
					* getPrevToOutDeltaWt(i);
		}		
		weightHiddenToOutput[numHidden] += learningRate * outputError * 1
				+ momentumTerm * getPrevToOutDeltaWt(numHidden);
		System.arraycopy(tempWeightHiddenToOutput, 0,
				previousWeightHiddenToOutput, 0,
				tempWeightHiddenToOutput.length);
	}

	private double getPrevToOutDeltaWt(int i) {
		if (previousWeightHiddenToOutput[i] != 0)
			return weightHiddenToOutput[i] - previousWeightHiddenToOutput[i];
		else
			return 0;
	}

	private double getPrevToHidDeltaWt(int i, int j) {
		if (previousWeightInputToHidden[i][j] != 0)
			return weightInputToHidden[i][j]
					- previousWeightInputToHidden[i][j];
		else
			return 0;
	}

	@Override
	public double sigmoid(double x) {

		return 2 / (1 + Math.pow(Math.E, -x)) - 1;
	}

	@Override
	public double customSigmoid(double x) {
		return (B - A) / (1 + Math.pow(Math.E, -x)) - A;
	}

	@Override
	public void initWeights() {
		for (int i = 0; i < numHidden; i++) {
			for (int j = 0; j < numInputs + 1; j++) {
				weightInputToHidden[i][j] = Math.random() - 0.5;

			}
		}

		for (int j = 0; j < numHidden + 1; j++) { 
			weightHiddenToOutput[j] = Math.random() - 0.5;
		}
	}

	@Override
	public void zeroWeights() {
		for (int i = 0; i < numHidden; i++) {
			for (int j = 0; j < numInputs + 1; j++) {
				weightInputToHidden[i][j] = 0;
			}
		}

		for (int j = 0; j < numHidden + 1; j++) { 
			weightHiddenToOutput[j] = 0;
		}
	}

	public void saveErrEpoch(File argFile, int epoch, double totalError) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(argFile, true));
			writer.write(epoch + "\t" + totalError + "\n");

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
		PrintStream saveFile = null;

		try {
			saveFile = new PrintStream(new RobocodeFileOutputStream(argFile));
		} catch (IOException e) {
			System.out
			.println("*** Could not create output stream for NN save file.");
		}

		saveFile.println(numInputs);
		saveFile.println(numHidden);
		for (int i = 0; i < numHidden; i++) {
			for (int j = 0; j < numInputs; j++) {
				saveFile.println(weightInputToHidden[i][j]);
			}
			saveFile.println(weightInputToHidden[i][numInputs]);
		}

		for (int i = 0; i < numHidden; i++) {
			saveFile.println(weightHiddenToOutput[i]);
		}
		saveFile.println(weightHiddenToOutput[numHidden]); 
		saveFile.close();		
	}


	public void load(String inputFile) throws IOException {
		try {
			FileInputStream fileInputStream = new FileInputStream(inputFile);
			Scanner inputScanner = new Scanner(fileInputStream);

			for (int i = 0; i < numVectors; i++) {
				for (int j = 0; j < numInputs; j++) {
					inputValues[i][j] = inputScanner.nextDouble();
				}
				inputValues[i][numInputs] = bias;
				targetValues[i] = inputScanner.nextDouble();
			}
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}

	static int epoch = 1;
	static int numInputs = 4; 
	static public int numHidden = 18; 
	static int numOutputs = 1; 
	static int numVectors = 42;
	static double learningRate = 0.4;
	static double momentumTerm = 0.9;
	static double bias = 1.0;
	double A;
	double B;
	static double totalError; 
	static double acceptableError = 0.05;

	static double[][] weightInputToHidden = new double[numHidden][numInputs + 1];
	static double[][] previousWeightInputToHidden = new double[numHidden][numInputs + 1];


	static double[] weightHiddenToOutput = new double[numHidden + 1];
	static double[] previousWeightHiddenToOutput = new double[numHidden + 1];

	static double[][] tempWeightInputToHidden = new double[numHidden][numInputs + 1];
	static double[] tempWeightHiddenToOutput = new double[numHidden + 1];
	static double[] hiddenValues = new double[numHidden];
	static double[][] inputValues = new double[numVectors][numInputs + 1];
	static double[] targetValues = new double[numVectors];
	static double tmpOut = 0;
	static double outputError = 0;
	static double[] hiddenError = new double[numHidden];

	File paraFile = new File("C:/robocode/New folder/param.txt");
	static File ErrorEpochFile = new File("C:/robocode/New folder/Finalepoch.txt");
	static String inputFile = new String("C:/robocode/New folder/XOR.txt");
	static String LUT = new String ("C:/robocode/New folder/myLUT.txt");
	static String savetest = new String ("C:/robocode/New folder/saveFile.txt");
	@Override
	public void load(File argFileName) throws IOException {

	}

	public double getQValue(double[] sVector) {
		calculateHidVal(sVector);
		calculateOutVal();

		return tmpOut;
	}
}
