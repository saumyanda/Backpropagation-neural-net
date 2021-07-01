package reinforcement;
import java.io.File;
import java.io.IOException;

public interface CommonInterface {

	public double outputFor(double [] X);

	/**
	 * This method will tell the NN or the LUT the output
	 * value that should be mapped to the given input vector. I.e.
	 * the desired correct output value for an input.
	 * @param X The input vector
	 * @param argValue The new value to learn
	 * @return The error in the output for that input vector
	 */
	public double train(double [] X, double argValue);

	/**
	 * A method to write either a LUT or weights of an neural net to a file.
	 * @param argFile of type File.
	 */
	public void save(File argFile);

	/**
	 * Loads the LUT or neural net weights from file. The load must of course
	 * have knowledge of how the data was written out by the save method.
	 * You should raise an error in the case that an attempt is being
	 * made to load data into an LUT or neural net whose structure does not match
	 * the data in the file. (e.g. wrong number of hidden neurons).
	 * @throws IOException 
	 */
	public void load(File argFileName) throws IOException;


}
LUTInterface.java
package reinforcement;

public interface LUTInterface extends CommonInterface {

	/**
	 * Constructor. (You will need to define one in your implementation)
	 * @param argNumInputs The number of inputs in your input vector
	 * @param argVariableFloor An array specifying the lowest value of each variable in the input 
	ctor.
	 * @param argVariableCeiling An array specifying the highest value of each of the variables in 
	e input vector.
	 * The order must match the order as referred to in argVariableFloor. 
	 * 
	 public LUT (
	 int argNumInputs,
	 int [] argVariableFloor,
	 int [] argVariableCeiling );
	 */ 

	/**
	 * Initialise the look up table to all zeros.
	 */
	public void initialiseLUT();

	/**
	 * A helper method that translates a vector being used to index the look up table
	 * into an ordinal that can then be used to access the associated look up table element.
	 * @param X The state action vector used to index the LUT
	 * @return The index where this vector maps to
	 */
	public int indexFor(double [] X);


} // End of public interface LUT
