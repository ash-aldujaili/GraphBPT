package ismmBpt2015.model;
import java.io.IOException;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLNumericArray;

/**
 * This class represents a dictionary (bag) of the parameters needed to calculate the
 * kernel descriptors within the BPT. It is created based on the *.mat files that contains
 * either the parameters for descriptor of attributes and the descriptor of descriptors
 * @author Abdullah Al-Dujaili
 *
 */
public class KernelDescriptorDictionary {
	
		// kernel descriptor parameters (similar to the ones in generateDescriptor*Parameters.m)
		public final Matrix EIG_GD; // a matrix whose columns are the gradient eigen vectors
		public final Matrix EIG_CD; // a matrix whose columns are the color eigen vector
		public final Matrix EIG_SD; // a matrix whose columns are the shape eigen vector
	
		public final Matrix Z; // a matrix whose rows are the position samples
		public final Matrix C; // a matrix whose rows are the color samples
		public final Matrix O; // a matrix whose rows are the orientation samples
		public final Matrix B; // a matrix whose rows are the shape samples
	
		// the hyper parameters for the different kernels
		public final float POS_KER_GAMMA; //  
		public final float SHP_KER_GAMMA;
		public final float GRD_KER_GAMMA;
		public final float CLR_KER_GAMMA;

		/**
		 * Construct loads the parameters from the mat file whose name is the fileName
		 * @param fileName the matfile name
		 */
		@SuppressWarnings ("unchecked")
		public KernelDescriptorDictionary (String fileName)
				throws IOException {
				MatFileReader mfr = null;
				Map<String, MLArray> content;
				// read the file:
				mfr = new MatFileReader (fileName);
				// get the mat file contents:
				content = mfr.getContent ();
		
				// go over the contents:
				EIG_GD = new Matrix ((MLNumericArray<Float>)content.remove ("EIG_GD"));
				EIG_CD = new Matrix ((MLNumericArray<Float>)content.remove ("EIG_CD"));
				EIG_SD = new Matrix ((MLNumericArray<Float>)content.remove ("EIG_SD"));
				Z = new Matrix ((MLNumericArray<Float>)content.remove ("Z"));
				C = new Matrix ((MLNumericArray<Float>)content.remove ("C"));
				O = new Matrix ((MLNumericArray<Float>)content.remove ("O"));
				B = new Matrix ((MLNumericArray<Float>)content.remove ("B"));
				//System.out.println (((MLNumericArray<Float>)content.remove ("gGamma")).get (0));
				POS_KER_GAMMA = ((((MLNumericArray<Double>)content.remove ("pGamma")).get (0)).floatValue ());
				CLR_KER_GAMMA = ((((MLNumericArray<Double>)content.remove ("cGamma")).get (0)).floatValue ());
				SHP_KER_GAMMA = ((((MLNumericArray<Double>)content.remove ("sGamma")).get (0)).floatValue ());
				GRD_KER_GAMMA = ((((MLNumericArray<Double>)content.remove ("gGamma")).get (0)).floatValue ());
		}
}
