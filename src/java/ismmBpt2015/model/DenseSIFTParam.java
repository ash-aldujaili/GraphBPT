package ismmBpt2015.model;

import java.io.IOException;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLNumericArray;
/**
 * This class stores the parameter values that used to create the dense sift feautres in vl_phow () function 
 * from VLFEAT library. This is helpful in knowing which features to be indexed for a specific node in the bpt
 * according to its bitmap or bounding box.
 * The reason this is important is that the features are calculated with specific set of parameters such as size (scale) , step
 * which makes the process of knowing the exact feature index for a specific pixel location tricky. This class abstracts this difficult
 * task through some mathematical transformations without the need to a search table.
 * Given a pixel of location x, y:
 * The corresponding features in [kpts, descrs] = vl_phow (image, opts); can be indexed as the following:
 * 
 * 	x = min (max (x,xf),xl (1)); % smallest size
 * 	y = min (max (y,yf),yl (1)); %
 * 	check if it is lying on the grid
 * 	idxX = ceil ((x-xf)/step);
 * 	idxY = ceil ((y-yf)/step);
 * 	get all the descriptors at that point
 * 	idx = ones (lSize,1);
 * 	for i = 1 : lSize
 * 		idx (i) = (idxX * numY (i)) + idxY + sum (offset (1:i)) + 1;
 * 	end
 * @author Abdullah Al-Dujaili
 *
 */
public class DenseSIFTParam {
	
		// 
		// Some parameter about the starting and ending points of feature locations
		public final MLNumericArray<Integer> xl; // a 1-D vector represent the last x location for the current scale (size) of the feature
		public final MLNumericArray<Integer> yl; // a 1-D vector represent the last y location for the current scale (size) of the feature
		public final int xf; // the first x location of the features for all sizes
		public final int yf; // the first y location of the features for all sizes.
		public final int step; // the step
		public final double invStep; // the inverse of the step
		public final MLNumericArray<Integer> cumOffset; // the cummlative offsets of features caclulated at scales (sizes) before the current one
		public final MLNumericArray<Integer> numY; // the height (y axis) of the grid in number of points for each of the scale (size)

		/**
		 * Construct loads the parameters from the mat file whose name is the fileName
		 * @param fileName the matfile name
		 */
		@SuppressWarnings ("unchecked")
		public DenseSIFTParam (String fileName)
				throws IOException {
				MatFileReader mfr = null;
				Map<String, MLArray> content;
				// read the file:
				mfr = new MatFileReader (fileName);
				// get the mat file contents:
				content = mfr.getContent ();
				// go over the contents:
				xf =  ((MLNumericArray<Integer>) content.remove ("xf")).get (0);
				yf =  ((MLNumericArray<Integer>) content.remove ("yf")).get (0);
				step =  ((MLNumericArray<Integer>) content.remove ("step")).get (0);
				invStep = ((MLNumericArray<Double>) content.remove ("invStep")).get (0);
				cumOffset =  ((MLNumericArray<Integer>) content.remove ("cumOffset"));
				numY = ((MLNumericArray<Integer>) content.remove ("numY"));
		
				xl = ((MLNumericArray<Integer>) content.remove ("xl"));
				yl = ((MLNumericArray<Integer>) content.remove ("yl"));
		}
}
