package ismmBpt2015.model;
import java.util.TreeSet;

/*************************************************************************
 *  Compilation:  javac BPTNode.java
 *  Execution:    java BPTNode
 *
 *  node for the binary partition tree.
 *
 *************************************************************************/

/**
 * The BPTNode represents a node in the binary partition tree of an image.
 * Each node represents a region of image with certain attributes
 * 
 * @author Abdullah Al-Dujaili
 * 
 */

public class BPTNode implements Comparable<BPTNode> {
	
		// instance variables related to the tree topology:
		private int id; 		// the node id
		private int level;			// the node level
		private int w;					// the node weight (the total number the node children and grandchildren / useful in displaying the tree)
		private BPTNode lChild,rChild;	// the node left and right children
		private TreeSet<BPTNode> st;			// the node neighbors 
		private boolean hasParent;				// check whether the node has a parent (this is useful in ignoring edges that used to connect the children nodes)
	
		// temporary necessary descriptors==========================
		private final  float [] spctrlMean; // spectral normalized means of the node (the actual color in case of a leaf node (single pixel))
		public final int size;  // the node size (area)
	
		//  geometric descriptors
		public final float xMean, yMean;   // node unormalized spatial mean ( the actual location in case of a leaf node (pixel))

		//============================================================

		private final BitMap BM;
		/**
		 * constructs a BPT parent node
		 * this constructor is used usually for non-leaf nodes.
		 * Check the constructor below for leaf BPT nodes.
		 * @param id the node id
		 * @param l the node level (aggregation)
		 * @param e the edge connecting the two nodes to be merged
		 * @param T the threshold, children nodes with a specific attribute value less than the threshold are pruned
		 * currently, the attribute is based on the node level
		 * @param N the number of image pixels, useful for various computation within the node (pruning and attribute computation)

		 */
		public BPTNode (int id, BPTEdge e, float numTH, float sizeTH, final int N) {	//, ImageAttribute imgAttrb)//, KernelDescriptorDictionary dict1)
				// local variables
				BPTNode leftNode = e.u ();
				BPTNode rightNode = e.v ();
				// initialize and set topology-related instance variables:
				this.id = id;
				this.level = Math.max (leftNode.level,rightNode.level) + 1;
				this.hasParent = false;
				// update the neighbors of the node:
				st = leftNode.neighbours ();
				for ( BPTNode x : rightNode.neighbours ())
						st.add (x);
				st.remove (leftNode);
				st.remove (rightNode);
				// update the neighbors of the neighbors
				for (BPTNode x : st) { 
						x.addNeighbour (this);
						x.removeNeighbour (leftNode);
						x.removeNeighbour (rightNode);
				}
				// Mark the merged nodes as children (to neglect their edges in the priority queue)
				leftNode.hasParent = true;
				rightNode.hasParent = true;
				// children nodes neighbors are of no importance to the current BPT implementation (remove them off the heap)
				leftNode.st = null;
				rightNode.st = null;

				// initialize and set attribute-related instance variables:
				this.BM = new BitMap (leftNode.BM, rightNode.BM);
		
				//======================temporary change:===================
				// computing necessary features:
				this.size  = leftNode.size + rightNode.size;
					
				// spectral attribute:	
				this.spctrlMean = new float[3]; // normalized means
					
				float [] spctrlDelta = new float[3]; // temp delta variable for efficient computation of mean and other features
				for (int i = 0; i < 3; i++)
						spctrlDelta[i] = (rightNode.spctrlMean[i] - leftNode.spctrlMean[i]);
				double invSize = 1.0 / this.size;
				for (int i = 0; i < spctrlMean.length; i++)
						// calculate the spectral mean
						this.spctrlMean[i] = (float) (leftNode.spctrlMean[i] + (spctrlDelta[i] * rightNode.size) * invSize); // calculate the spectral means
				// spatial attribute
				float xDelta, yDelta; // temp delta for efficint computation of mean and other spatial features
					
				xDelta = (rightNode.xMean - leftNode.xMean);
				yDelta = (rightNode.yMean - leftNode.yMean);
					
				this.xMean = (float) (leftNode.xMean + (xDelta * rightNode.size) * invSize); 
				this.yMean = (float) (leftNode.yMean + (yDelta * rightNode.size) * invSize);		
				//===================================================
				// Pruning criteria :
				// Currently it is based on the relative size of the region with respect to the image
				// by default it prunes nodes with < (0.05 * N pixels) or if it s not in the top 0.2 * N nodes
				boolean pruneL = (leftNode.size ()  < sizeTH * N)  || (leftNode.id ()  > (int) ((numTH) * (2 * N -1))) || (leftNode.size () < Param.NODE_SIZE_TH)  ;// || (( ((float) leftNode.size)  /  leftNode.getAreaBB ()) < Param.NODE_SIZE_RATIO_TH); // flag for pruning U node
				boolean pruneR = (rightNode.size () < sizeTH * N)  || (rightNode.id () > (int) ((numTH) * (2 * N -1))) || (rightNode.size () < Param.NODE_SIZE_TH) ;// || (( ((float) rightNode.size) / rightNode.getAreaBB ()) < Param.NODE_SIZE_RATIO_TH); // flag for pruning V node
		
				// Ensure pruning is consistent along the tree (a node is pruned if both of its children are)
				// since we cant guarantee the shape of the node after formation consistency has to be disconsidered here
				if ((pruneL || pruneR) || (this.id () > Param.NODE_NUM_TH)) { // &&  (leftNode.w () == 0) && (rightNode.w () == 0)){
						this.lChild = null;
						this.rChild = null;
						this.w = 0;
				}	else {
						this.lChild = leftNode;
						this.rChild = rightNode;
						this.w = leftNode.w + rightNode.w +1;
				}	
		}

		/**
		 * constructs a leaf BPT node with a level L=0 and null children nodes
		 * this constructor is used usually for non-leaf nodes.
		 * Check the constructor below for leaf BPT nodes.
		 * @param id the node id
		 * @param pSpctrl the pixel spectral info
		 * @param x the pixel x location
		 * @param y the pixel y location
		 */
		public BPTNode (int id, float[] pSpctrl, int x, int y) {
				// initialize and set toplogy-related instance variables:
				this.id = id;
				this.st = new TreeSet<BPTNode> ();
				///==================================================================
				this.size  = 1;
		
				// spectral attribute
				this.spctrlMean = new float[3];

				for (int i = 0; i < spctrlMean.length; i++) {
						this.spctrlMean[i] = pSpctrl[i];	
				}
				// spatial attribute
				this.xMean = (float) x;
				this.yMean = (float) y;
				///==================================================================
		
				// Set bounding box attributes:
				this.BM = new BitMap (x,y);
		}
	
		/**
		 * Reset the id of the node :
		 * This function is used after the construction of the BPT. To relabel all the nodes
		 * of the tree to the interval [0 : number of nodes -1]
		 * @param id : the new id to be assigned to the node
		 * @param isLevelChanged : specifies whether the level of the node is change, set to true when we want to drag the nodes
		 * towards the lowest level (gravity effect), this function is used with 
		 */
		public void pruneChildren (boolean isLevelChanged) {
				this.lChild = null;
				this.rChild = null;
				this.w = 0;
		
				if (isLevelChanged)
						this.level = Param.BPT_LEVEL;
		}
	
		/**
		 * Reset the id of the node :
		 * This function is used after the construction of the BPT. To relabel all the nodes
		 * of the tree to the interval [0 : number of nodes -1]
		 * @param id : the new id to be assigned to the node
		 */
		public void resetId (int id) {
				this.id = id;
		}
	
		/**
		 * Reset the w of the node :
		 * This function is used after the construction of the BPT. To reweight all the nodes
		 * depending on their subnodes
		 * @param w : the new weight to be assigned to the node
		 */
		public void resetW (int w) {
				this.w = w;
		}
	
		/**
		 * a zero if it is a leaf a non-zero otherwisze
		 * @return the weight of the node
		 */
		public int w () {
				return this.w;
		}

		/**
		 * Reset the level of the node :
		 * This function is used after the construction of the BPT. To reassign all the nodes
		 * of the tree to the interval [0 : height -1]
		 * @param l : the new level to be assigned to the node
		 */
		public void resetLevel (int l) {
				this.level = l;
		}

		/**
		 * Updates the neighbors set of the node by adding one node
		 * @param u a neighboring node of the node.
		 */
		public void addNeighbour (BPTNode u) {
				st.add (u);
		}
	
		/**
		 * Updates the neighbors set of the node by removing one node
		 * @param u a neighboring node of the node.
		 */
		public void removeNeighbour (BPTNode u) {
				st.remove (u);
		}
	
		/**
		 * Returns the node id
		 * @return the node id
		 */
		public int id () {
				return id;
		}
	
		/**
		 * Returns the node weight
		 * @return the node weight
		 */
		public int weight () {
				return w;
		}
	
		/**
		 * Returns the node level
		 * @return the node level
		 */
		public int level () {
				return level;
		}


		/**
		 * Returns the left child of the node
		 * @return the left child
		 */
		public BPTNode leftChild () {
				return lChild;
		}
	
		/**
		 * Returns the right child of the node
		 * @return the right child
		 */
		public BPTNode rightChild ()  {
				return rChild;
		}
	
		/**
		 * Returns the set of the node neighbors
		 * @return the set of node neighbors
		 */
		public TreeSet<BPTNode> neighbours () {
				return st;
		}

	
    /**
     * Compares this node to that node by comparing the node id
     * @param that the other node
     * @return { a negative integer, zero, a positive integer } if this node is
     *    { less than, equal to, greater than } that node with respect to its id
     */
    public int compareTo (BPTNode that) {
        if (this.id < that.id)
						return -1;
        if (this.id > that.id)
						return +1;
        return 0;
    }

    /**
     * Tells whether the node has a parent or not
     * @return true if the node has a parent, false otherwise
     */
    public boolean hasParent () {
				return hasParent;
    }

    /**
     * Returns the node first 3 spectral values as an array 
     * helpful for visualizing the node image
     *@return rgbVal the first 3 spectral band mean values for the node as int
     *consisting of the red component in bits 16-23, 
     *the green component in bits 8-15, and the blue component in bits 0-7
     */
    public int getRGB () {
				//int rVal = (int) (this.descr.getSpctrlMean (0));
				int rVal = (int) (this.spctrlMean[0]);
				//int gVal = (int) (this.descr.getSpctrlMean (1));
				int gVal = (int) (this.spctrlMean[1]);
				//int bVal = (int) (this.descr.getSpctrlMean (2));
				int bVal = (int) (this.spctrlMean[2]);
				int rgbVal =
						((rVal << 16) & 0x00ff0000) |
						((gVal << 8) & 0x0000ff00) |
						((bVal) & 0x000000ff) |
						0xff000000 ;
				return rgbVal;
    }
 
    /**
     * Returns the node region size 
     * @return the node region size
     */
    public int size () {
				//return this.descr.getSize ();
				return this.size;
    }
   
    /** 
     * Returns the node spectral features
     *@return the mean spectral values of the node region
     *
     */
		public float [] getSpectral () {
				//return this.descr.getSpctrlMean ();
				return this.spctrlMean;
		}
	
		/**
		 * get the features of the node
		 * @return the node features
	 
		 public float[] getFeatures () {
		 return features;
		 }
		*/

		/**
		 * Get the bounding box of the node's region
		 * @return an approximation of the node's region as a bounding box which is an int array
		 * of length 4 representing minimum and maximum of x coordinate followed by y respectively.
		 * [xstart, xlast+1, ystart, ylast+1]
		 */
		public int[] getBBxy () {
				return this.BM.getBB ().getBBxy ();
		
		}
	
		/**
		 * Return the bit map of this region
		 * @return the bit map of this node
		 */
		public BitMap getBitMap () {
				return this.BM;
		}
	
		/**
		 * Get the area of the bounding box for that node
		 * @return the area of the bounding box of the node
		 */
		public int getAreaBB () {
				return this.BM.getBB ().getArea ();
		}
	
	
		/**
		 * get BBArea from the union of two nodes
		 * @param u one node
		 * @param v the other node
		 * @return the Bounding box area from merging two nodes
		 */
		static public int getAreaBB (BPTNode u, BPTNode v) {
				return BoundingBox.getAreaUnion (u.BM.getBB (),v.BM.getBB ());
		}
	
		/**
		 * Computes the singleton factor of the node and return it as a string to be printed out
		 * The decision of returning the factor as a string is because this is needed to be written to 
		 * factor file which will be read up by an inference solver.
		 * @return the string representation of the singleton factor in accordance with the format of *.fg files in libDAI library
		 */
		public String singletonFactorAsString () {
				// Get the string representation of the singleton factor
				// print the number of variables followed by variable indices
				String s = "\n\n1\n" + String.valueOf (this.id)+" ";
				// print the variables cardinality
				s = s + "\n" + String.valueOf (Param.LABEL_CARD)+" ";
				// the values of the potenial function for all the classes:
				float[] val = new float[Param.LABEL_CARD];	
				// print the length of the val vector
				s = s + "\n" + String.valueOf (val.length);
				// print the value
				for (int i = 0; i < val.length; i++) {
						// compute the potential (currently it's the distance from the green)
						val[i] = computeSingletonPotential (i); // use a proper function
						s = s + "\n" + String.valueOf (i) + " " + String.format ("%.2f",val[i]);
				}
				// return the factor
				return s;
		}

		/**
		 * Computes the pairwise factors of the node and its children, it returns them as a string to be printed out
		 * The decision of returning the factor as a string is because this is needed to be written to 
		 * factor file which will be read up by an inference solver.
		 * @return the string representation of the singleton factor in accordance with the format of *.fg files in libDAI library
		 */
		public String pairwiseFactorAsString () {
				// Computing the pairwise with the left child if it exists:
				// print the number of variables followed by variable indices
				String s = "";
				if (this.lChild != null) {
						// print the number of variables and their indices
						s= s + "\n\n2\n" + String.valueOf (this.id) + " " + String.valueOf (this.lChild.id)+" ";
						// print the variables cardinality
						s = s + "\n" + String.valueOf (Param.LABEL_CARD) + " " + String.valueOf (Param.LABEL_CARD) +" ";
						// the values of the potential function for all the classes:
						float [] val = new float[Param.DUAL_LABEL_CARD];
						// print the length of the val vector
						s = s + "\n" + String.valueOf (val.length);
						// print the value
						int idx = 0;
						for (int i = 0; i < Param.LABEL_CARD; i++) // for the child labels
								for (int j = 0; j < Param.LABEL_CARD; j++) { // for the parent class
										// compute the potentials
										val[idx] = computePairwisePontential (i,j); // use a proper function
										s = s + "\n" + String.valueOf (idx) + " " + String.format ("%.2f",val[idx++]);
								}
				
				}
				// Computing the pairwise with the right child if it exists:
				if (this.rChild != null) {
						// print the number of variables and their indices
						s= s + "\n\n2\n" + String.valueOf (this.id) + " " + String.valueOf (this.rChild.id)+" ";
						// print the variables cardinality
						s = s + "\n" + String.valueOf (Param.LABEL_CARD) + " " + String.valueOf (Param.LABEL_CARD)+" ";
						// the values of the potential function for all the classes:
						float [] val = new float[Param.DUAL_LABEL_CARD];
						// print the length of the val vector
						s = s + "\n" + String.valueOf (val.length);
						// print the value
						int idx = 0;
						for (int i = 0; i < Param.LABEL_CARD; i++) // for the child labels
								for (int j = 0; j < Param.LABEL_CARD; j++) { // for the parent class
										// compute the potentials
										val[idx] = computePairwisePontential (i,j); // use a proper function
										s = s + "\n" + String.valueOf (idx) + " " + String.format ("%.2f",val[idx++]);
								}
				}
				// return both of the string representation of the factors if any:
				return s;
		}
	
		/**
		 * This method is used to compute the value of the pairwise potenial function
		 * for the given labels of the parent and its child
		 * @param pLabel : parent label
		 * @param cLabel : child label
		 * @return the value of the pairwise potential function between the node and its parent
		 */
		private float computePairwisePontential (int cLabel, int pLabel) {
				return ((pLabel == cLabel)? 50.f : ((cLabel > pLabel)? 10.f : 100.f));
		}
	
		/**
		 * This method is used to compute the value of the singleton potenial function
		 * for the given label of a node (variable)
		 * @param l : the node label
		 * @return the value of the singleton potential function of the node given its label is l.
		 */
		private float computeSingletonPotential (int l) {
				// simple function based on color
				float [] uSpctrl = this.getSpectral (); // spectral mean of  u
				// leaf pic
				float [] tSpctrl = {22.f,46.f, 72.f}; // target reference;
				float [] tSpctrl1 = {62.f, 60.f, 200.f};
				float [] tSpctrl2 = {36.f, 90.f, 50.f};
				float [] bSpctrl = { 222.f,200.f, 60.f}; // background reference
				float diffT = 0.f;
				float diffT1 = 0.f;
				float diffT2 = 0.f;
				float diffB = 0.f;
				for (int i = 0; i < uSpctrl.length; i++) {
						diffT = (float) (diffT + Math.pow ((double) (uSpctrl[i] - tSpctrl[i]), 2.0));
						diffT1 = (float) (diffT1 + Math.pow ((double) (uSpctrl[i] - tSpctrl1[i]), 2.0));
						diffT2 = (float) (diffT2 + Math.pow ((double) (uSpctrl[i] - tSpctrl2[i]), 2.0));
						diffB = (float) (diffB + Math.pow ((double) (uSpctrl[i] - bSpctrl[i]), 2.0));
				}
				diffT = (float) Math.pow (diffT, 0.5);
				diffT1 = (float) Math.pow (diffT1, 0.5);
				diffB = (float) Math.pow (diffB, 0.5);
				if (l == 0)
						diffT = Math.min (Math.min (diffT,diffT1),diffT2);
				else
						diffT = diffB;	
				return diffT;
		}
}
