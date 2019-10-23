package ece.cpen502;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import Sarb.*;

public class NeuralNet implements NeuralNetInterface{

	//hyper-parameters
	private boolean binary=false;
	private double LearningRate=0.2;
	private double momentumTerm=0.9;
	private double errorThreshold = 0.05;
	private double a=0;
	private double b=1;
	private double max=0.5;
	private double min=-0.5;

	private int numInputs=2;
	private int numHidden=4;
	private int numOutputs=1;
	/*
	 * Layers of the network
	 */
	private double[] inputLayer=new double[numInputs+1];
	private double[] hiddenLayer=new double[numHidden+1];
	private double[] outputLayer=new double[numOutputs];

	/*
	 * Weights between the layers
	 */
	private double[][] weights_InputToHidden=new double[numInputs+1][numHidden];
	private double[][] weights_HiddenToOutput=new double[numHidden+1][numOutputs];

	/*
	 * Backpropagation update arrays
	 */
	private double[][] deltaWeights_InputToHidden=new double[numInputs+1][numHidden];
	private double[][] deltaWeights_HiddenToOutput=new double[numHidden+1][numOutputs];
	private double[] deltaOutput = new double[numOutputs];
	private double[] deltaHidden = new double[numHidden];

	/*
	 * Backpropagation error information
	 */
	private double[] totalError = new double[numOutputs];
	private double[] singleError = new double[numOutputs];

	/*
	 * To write error to a file
	 */
	private List<String> error = new LinkedList<>();

	/*
	 * Training set
	 */
	private double[][] trainingInputs;
	private double[][] trainingTargetOutputs;

	/**
	 * Constructor
	 * @param numInputs The number of inputs in your input vector
	 * @param numHidden The number of hidden neurons in your hidden layer. Only a single hidden layer is supported
	 * @param learningRate The learning rate coefficient
	 * @param momentum The momentum coefficient
	 * @param a Integer lower bound of sigmoid used by the output neuron only.
	 * @param n Integer upper bound of sigmoid used by the output neuron only.
	 */
	public NeuralNet(int numInputs, 
			int numHidden, 
			int numOutputs, 
			double learningRate, 
			double momentum, 
			double a, 
			double b) {
		this.numInputs = numInputs;
		this.numHidden = numHidden;
		this.numOutputs = numOutputs;
		this.LearningRate = learningRate;
		this.momentumTerm = momentum;
		this.a = a;
		this.b = b;
	}

	/*
	 * Default constructor
	 */
	public NeuralNet()
	{

	}

	/*
	 * Initialize training set
	 */
	public void initializeTrainingSet() {
		if (binary) {
			trainingInputs = new double[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}};
			trainingTargetOutputs = new double[][]{{0}, {1}, {1}, {0}};
		} else {
			trainingInputs = new double[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
			trainingTargetOutputs = new double[][]{{-1}, {1}, {1}, {-1}};
		}
	}

	/*
	 * Forward propagation
	 * Make a bottom up pass through the net computing weighted sums and activations for each neuron
	 */
	private void forwardPropagation(double[] X)
	{
		if(X.length != numInputs)
		{
			throw new ArrayIndexOutOfBoundsException();
		}

		//Adding bias as input to the weight vector
		inputLayer[0]=1;
		for(int i=1;i<inputLayer.length;i++)
		{
			inputLayer[i]=X[i-1];
		}

		//Calculate output of neurons in hidden layer
		for(int neuron=0; neuron<numHidden; neuron++)
		{
			//Calculate weighted sum
			hiddenLayer[neuron]=0;

			for(int prevNeuron=0; prevNeuron<numInputs+1; prevNeuron++)
			{
				hiddenLayer[neuron] += inputLayer[prevNeuron]*weights_InputToHidden[prevNeuron][neuron];
			}	
			//Apply activation function
			hiddenLayer[neuron]=customSigmoid(hiddenLayer[neuron]);	
		}
		//Calculate output of neurons in output layer
		for(int neuron=0; neuron<numOutputs; neuron++)
		{
			//Calculate weighted sum
			outputLayer[neuron]=0;

			for(int prevNeuron=0; prevNeuron<numHidden+1; prevNeuron++)
			{
				outputLayer[neuron] += hiddenLayer[prevNeuron]*weights_HiddenToOutput[prevNeuron][neuron];
			}					
			//Apply activation function
			outputLayer[neuron]=customSigmoid(outputLayer[neuron]);	
		}
	}


	/**
	 * @param X The input vector. An array of doubles.
	 * @return The value returned by th LUT or NN for this input vector
	 */
	@Override
	public double outputFor(double[] X) {
		forwardPropagation(X);
		return outputLayer[0];
	}

	/**
	 * This method will tell the NN or the LUT the output
	 * value that should be mapped to the given input vector. I.e.
	 * the desired correct output value for an input.
	 * @param X The input vector
	 * @param argValue The new value to learn
	 * @return The error in the output for that input vector
	 */
	@Override
	public double train(double[] X, double argValue) throws ArrayIndexOutOfBoundsException {
		//Feed the input forward
		//Output of each neuron is calculated
		forwardPropagation(X);
		return argValue - outputLayer[0];

	}

	/*
	 * Backpropagation method
	 */
	private void backPropagation()
	{
		//Computing deltaOutput
		//Output derivative: y(1-y)
		for(int neuron=0; neuron<numOutputs; neuron++)
		{
			deltaOutput[neuron]=0;
			if(binary)
			{
				deltaOutput[neuron]=singleError[neuron] * outputLayer[neuron] * (1-outputLayer[neuron]);
			}
			else
			{
				deltaOutput[neuron]=singleError[neuron] * (outputLayer[neuron] + 1) * 0.5 * (1-outputLayer[neuron]);
			}
		}

		//Update weights from hidden layer to output layer
		for(int neuron=0; neuron<numOutputs; neuron++)
		{
			for(int prevNeuron=0; prevNeuron<numHidden+1; prevNeuron++)
			{
				deltaWeights_HiddenToOutput[prevNeuron][neuron] = 
						momentumTerm * deltaWeights_HiddenToOutput[prevNeuron][neuron] + 
						LearningRate * deltaOutput[neuron] * hiddenLayer[prevNeuron];
				weights_HiddenToOutput[prevNeuron][neuron] += deltaWeights_HiddenToOutput[prevNeuron][neuron];
			}
		}

		//Computing deltaHidden
		//Output derivative: y(1-y)
		for(int neuron=0; neuron<numHidden; neuron++)
		{
			deltaHidden[neuron]=0;
			for(int nextNeuron=0; nextNeuron<numOutputs; nextNeuron++)
			{
				deltaHidden[neuron] += weights_HiddenToOutput[neuron][nextNeuron] * deltaOutput[nextNeuron];
			}

			if(binary)
			{
				deltaHidden[neuron] = deltaHidden[neuron] * hiddenLayer[neuron] * (1-hiddenLayer[neuron]);
			}
			else
			{
				deltaHidden[neuron] = deltaHidden[neuron] * (hiddenLayer[neuron] + 1) * 0.5 * (1-hiddenLayer[neuron]);
			}
		}
		//Update weights from input layer to hidden layer
		for(int neuron=0; neuron<numHidden; neuron++)
		{
			for(int prevNeuron=0; prevNeuron<numInputs+1; prevNeuron++)
			{
				deltaWeights_InputToHidden[prevNeuron][neuron] = 
						momentumTerm * deltaWeights_InputToHidden[prevNeuron][neuron] + 
						LearningRate * deltaHidden[neuron] * inputLayer[prevNeuron];
				weights_InputToHidden[prevNeuron][neuron] += deltaWeights_InputToHidden[prevNeuron][neuron];
			}
		}
	}

	public int train()
	{ 
		int epoch=0;
		error.clear();
		do
		{
			for(int neuron=0; neuron<numOutputs; neuron++)
			{
				totalError[neuron]=0;
			}
			int numberOfSamples=trainingInputs.length;
			for(int i=0; i<numberOfSamples; i++)
			{
				forwardPropagation(trainingInputs[i]);
				for(int j=0; j<numOutputs; j++)
				{
					singleError[j] = trainingTargetOutputs[i][j] - outputLayer[j];
					totalError[j] += Math.pow(singleError[j],2);
				}
				backPropagation();
			}
			for(int i=0; i<numOutputs; i++)
			{
				totalError[i] /= 2;
				System.out.println("The total error for output neuron is " + totalError[i]);
			}
			error.add(Double.toString(totalError[0]));
			epoch++;

		} while(totalError[0] > errorThreshold);

		System.out.println("Epoch " + epoch + "\n");
		return epoch;
	}

	/**
	 * A method to write either a LUT or weights of an neural net to a file.
	 * @param argFile of type File.
	 * @throws  
	 */
	@Override
	public void save(File argFile) {
		try
		{
			StringBuilder sb=new StringBuilder();
			for(int i=0; i<weights_InputToHidden.length; i++)
			{
				for(int j=0; j<weights_InputToHidden[0].length;j++)
				{
					sb.append(weights_InputToHidden[i][j]+"\t");
				}
				sb.append("\n");
			}
			sb.append("\n");
			for(int i=0; i<weights_HiddenToOutput.length; i++)
			{
				for(int j=0; j<weights_HiddenToOutput[0].length;j++)
				{
					sb.append(weights_HiddenToOutput[i][j]+"\t");
				}
				sb.append("\n");
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(argFile));
			writer.write(sb.toString());
			writer.close();
		}
		catch(IOException e)
		{
			e.getMessage();
		}		
	}

	/*
	 * Save error to file
	 */
	public void saveErrorToFile(String argFileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(argFileName));
			for(int i=0; i<error.size(); i++)
			{
				writer.write(error.get(i));
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			e.getMessage();
		}
	}

	/**
	 * Loads the LUT or neural net weights from file. The load must of course
	 * have knowledge of how the data was written out by the save method.
	 * You should raise an error in the case that an attempt is being
	 * made to load data into an LUT or neural net whose structure does not match
	 * the data in the file. (e.g. wrong number of hidden neurons).
	 * @throws IOException
	 */
	@Override
	public void load(String argFileName) throws IOException {
		File file = new File(argFileName); 
		Scanner sc = new Scanner(file);
		double[][] weights1 = new double[numInputs + 1][numHidden];
		double[][] weights2 = new double[numHidden + 1][numOutputs];
		boolean reading_weights1 = true;
		int lineIndex = 0;
		while (sc.hasNextLine())
		{
			if (reading_weights1) 
			{
				String[] line = sc.nextLine().trim().split("\t");
				if (line[0].length() == 0) {
					reading_weights1 = false;
					lineIndex = 0;
					continue;
				}

				for (int i = 0; i < line.length; i++) {
					weights1[lineIndex][i] = Double.parseDouble(line[i]);
				}
				lineIndex++;
			}
			else
			{
				String[] line = sc.nextLine().trim().split("\t");
				if (line[0].length() == 0) {
					break;
				}

				for (int i = 0; i < line.length; i++) {
					weights2[lineIndex][i] = Double.parseDouble(line[i]);
				}
				lineIndex++;
			}
		}
		sc.close();
	}

	/**
	 * Return a bipolar sigmoid of the input X
	 * @param x The input
	 * @return f(x) = -1 + (2/(1+e^-x))
	 */
	@Override
	public double sigmoid(double x) {
		return -1+(2/(1+(Math.exp(-x))));
	}

	/**
	 * This method implements a general sigmoid with asymptotes bounded by (a,b)
	 * @param x The input
	 * @return f(x) = a + ((b-a)/(1+e^-x))
	 */
	@Override
	public double customSigmoid(double x) {
		if(!binary)
		{
			a=-1;
			b=1;
		}
		return a + ((b-a)/(1 + (Math.exp(-x))));
	}

	/**
	 * Initialize the weights to random values.
	 * For say 2 inputs, the input vector is [0] & [1]. We add [2] for the bias.
	 * Like wise for hidden units. For say 2 hidden units which are stored in an array.
	 * [0] & [1] are the hidden & [2] the bias.
	 * We also initialise the last weight change arrays. This is to implement the alpha term.
	 */
	@Override
	public void initializeWeights() {
		//Initialize weights and change in weights between input layer and hidden layer
		for(int i=0; i<numInputs+1; i++)
		{
			for(int j=0; j<numHidden; j++)
			{
				weights_InputToHidden[i][j] = min + (Math.random()*(max-min));
				deltaWeights_InputToHidden[i][j]=0.0;
			}
		}
		//Initialize weights and change in weights between hidden layer and output layer
		for(int i=0; i<numHidden+1; i++)
		{
			for(int j=0; j<numOutputs; j++)
			{
				//Random weights between min: -0.5 and max: 0.5
				weights_HiddenToOutput[i][j] = min + (Math.random()*(max-min));
				deltaWeights_HiddenToOutput[i][j]=0.0;
			}
		}	
	}

	/**
	 * Initialize the weights to 0.
	 */
	@Override
	public void zeroWeights() {
		//Initialize weights and change in weights between input layer and hidden layer
		for(int i=0; i<numInputs+1; i++)
		{
			for(int j=0; j<numHidden; j++)
			{
				weights_InputToHidden[i][j] = 0.0;
				deltaWeights_InputToHidden[i][j]=0.0;
			}
		}
		//Initialize weights and change in weights between hidden layer and output layer
		for(int i=0; i<numHidden+1; i++)
		{
			for(int j=0; j<numOutputs; j++)
			{
				weights_HiddenToOutput[i][j] = 0.0;
				deltaWeights_HiddenToOutput[i][j]=0.0;
			}
		}

	}

}
