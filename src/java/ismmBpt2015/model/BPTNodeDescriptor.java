package ismmBpt2015.model;

import edu.princeton.cs.stdlib.In;

/*************************************************************************
 *  Compilation:  javac BPTNodeDescriptor.java
 *  Execution:    java BPTNodeDescriptor
 *
 *  Immutable descriptor for BPT node.
 *
 *************************************************************************/

/**
 * The BPTNodeDescriptors describes a set of features for a binary partition tree node
 * The set of features used include geometric as well as statistics.
 * The features are computed from the features of the children nodes
 * All the features are made final as the nodes are not going to change
 * Some of the features are made public for covenient access.
 * @author Abdullah Al-Dujaili
 * @TODO put all the features in array format ?
 */
class BPTNodeDescriptor {
 
		//=============================================
		// statistic descriptors:
		private final  float [] spctrlMean; // spectral normalized means of the node (the actual color in case of a leaf node (single pixel))
		public final int size;  // the node size (area)
	
		//  geometric descriptors
		public final float xMean, yMean;   // node unormalized spatial mean ( the actual location in case of a leaf node (pixel))
		//=============================================
      
		/**
		 * constructs a BPT node descriptors
		 * this constructor is used for non-leaf nodes.
		 * Check the constructor below for leaf BPT nodes.
		 * @param ld the left child descriptors 
		 * @param rd the right child descriptors
		 */ 
		public BPTNodeDescriptor (BPTNodeDescriptor ld, BPTNodeDescriptor rd) {
				this.size  = ld.size + rd.size;
		
				// spectral attribute:	
				int N = ld.spctrlMean.length;
				this.spctrlMean = new float[N]; // normalized means
				float [] spctrlDelta = new float[N]; // temp delta variable for efficient computation of mean and other features
				for (int i = 0; i < N; i++)
						spctrlDelta[i] = (rd.spctrlMean[i] - ld.spctrlMean[i]);
		
				double invSize = 1.0 / this.size;
				for (int i = 0; i < N; i++)
						// calculate the spectral mean
						this.spctrlMean[i] = (float) (ld.spctrlMean[i] + (spctrlDelta[i] * rd.size) * invSize); // calculate the spectral means

				// spatial attribute
				float xDelta, yDelta; // temp delta for efficint computation of mean and other spatial features
		
				xDelta = (rd.xMean - ld.xMean);
				yDelta = (rd.yMean - ld.yMean);
		
				this.xMean = (float) (ld.xMean + (xDelta * rd.size) * invSize); 
				this.yMean = (float) (ld.yMean + (yDelta * rd.size) * invSize); 
		}
	 
		/**
		 * constructs a BPT node descriptors
		 * this constructor is used for leaf nodes (pixel)
		 * @param spctrlVal the pixel spectral value
		 * @param xLoc the pixel xlocation
		 * @param yLoc the pixel ylocation
		 */ 
		public BPTNodeDescriptor (float[] spctrlVal, int xLoc, int yLoc) {
				this.size  = 1;
		
				// spectral attribute
				int N = spctrlVal.length;
		
				this.spctrlMean = new float[N];

				for (int i = 0; i < N; i++)
						this.spctrlMean[i] = spctrlVal[i];	
		
				// spatial attribute
				this.xMean = (float) xLoc;
				this.yMean = (float) yLoc;
		} 
	 
		/**
		 * Gets the node's spectral mean of all band
		 *@return the node's spectral bands normalized means
		 */
		public float[] getSpctrlMean () { 
				return (this.spctrlMean); 
		}
	 
		/**
		 * Gets the node ith spectral band mean
		 *@param i the index of the spectral band of interest (in case of RGB images it is within [0,1,2])
		 *@throws an exception when an index out of bound is entered
		 *@return the node's the ith spectral band normalized mean
		 */
		public float getSpctrlMean (int i) { 
				int N = this.spctrlMean.length;
				if (i < 0 || i > N-1) 
						throw new IllegalArgumentException ("The index should be within the number of the spectral bands");
				return (this.spctrlMean[i]); 
		}
	
		/**
		 * Get the node size (number of pixels)
		 *@return the node total number of pixels
		 */
		public int getSize () {
				return this.size;
		}
}
