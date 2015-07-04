package ismmBpt2015.model;
/*************************************************************************
 *  Compilation:  javac BPTEdge.java
 *  Execution:    java BPTEdge
 *
 *  Immutable weighted edge for BPT.
 *
 *************************************************************************/

/**
 * The BPTEdge represents an edge in the binary partition tree of an image.
 * Each edge connects two neighboring nodes in the tree. It is undirected
 * and naturally ordered by its weight.
 * 
 * @author Abdullah Al-Dujaili
 * 
 */
public class BPTEdge implements Comparable<BPTEdge> {

		private final BPTNode u,v;	// the nodes connected by the edge
		private final float w;		// the weight of the edge
		private final float s; // the size of the two nodes altogether

		/**
		 * Constructs a BPT edge with a between two nodes with. The node itself can access
		 * @param u node u
		 * @param v node v
		 * @param N total number of pixels within the image (useful for computing weight)
		 */
		public BPTEdge (BPTNode u, BPTNode v, float invN) {
				this.u = u;
				this.v = v;

				// calculate the weight of the edge based on the attribute of the two nodes:
				/**TODO to make it more flexible about computing the region criterion */
				// local variables for computing the edge weight:
		
		
				// Geometrical information:
				float size = (float) (u.size () + v.size ()); // merged node size
				// get the formed boudning box
				float invSize = 1.f /size;
				// Spectral information
				float [] uSpctrl = u.getSpectral (); // spectral mean of  u
				float [] vSpctrl = v.getSpectral (); // spectral mean of  v
				int D = uSpctrl.length;
				float [] uvSpctrl = new float[D]; // spectral mean of u+v
		
				// Unnormalized Euclidean norm of of ||u- (u+v)|| and ||v- (u+v)||
				float diffU = 0.f;
				float diffV = 0.f;
		
				for (int i = 0; i < D; i++) {
						uvSpctrl[i] = vSpctrl[i] + ((uSpctrl[i] - vSpctrl[i]) * u.size ()) * invSize;// / (size);
						diffU = (diffU + (uSpctrl[i] - uvSpctrl[i]) * (uSpctrl[i] - uvSpctrl[i]));
						diffV = (diffV + (vSpctrl[i] - uvSpctrl[i]) * (vSpctrl[i] - uvSpctrl[i]));
				}
				// make sure regions of closer spectrals get together
				float spectral = (float) ((u.size () * Math.pow ((double) diffU, 0.5)) + (v.size () * Math.pow ((double) diffV, 0.5))); //* invN;
	    
				// Spatial information:
				// give regions of more squarish shape a higher priority
				this.w = spectral;
				this.s = u.level () + v.level ();
		}
	
		/**
		 * Constructs an edge with null nodes but a specific weight
		 * This construction is useful when selecting the smallest edge in linear creation
		 * the level of this edge wont be of an importance in comparing as the comparison would only depend on the weight if any of the nodes are null
		 * @param w : the weight for the edge to have
		 */
		public BPTEdge (float w) {
				this.w = w;
				u = null;
				v = null;
				s = 0;
		}

    /**
     * Compares this edge to that edge by comparing the node weight
     * @param that the other edge
     * @return { a negative integer, zero, a positive integer } if this edge is
     *    { less than, equal to, greater than } that edge with respect to its weight
     */
    @SuppressWarnings ("unused")
				public int compareTo (BPTEdge that) {
				// checking if either of the edges has links to a null node
				if (this.u == null || this.v == null || that.u == null || that.v == null || !Param.isLevelCompareOn) {
						// compare on weights only
						if (this.w < that.w)
								return -1;
						if (this.w > that.w) 
								return +1;
						return 0;
				} else {
						// compare on edges and levels
						// level difference + EDGE_LEVEL_DIFF
						if (this.s > that.s + Param.EDGE_DIFF)
								return +1;
						// level diference - EDGE_LEVEL_DIFF
						if (this.s < that.s - Param.EDGE_DIFF)
								return -1;
						// otherwise consider weight:
						if (this.w < that.w)
								return -1;
						if (this.w > that.w) 
								return +1;
						return 0;
        }
    }

    /**
     * Returns the u node of the edge
     * @return the u node of the edge
     */
    public BPTNode u () {
				return u;
    }
    
    /**
     * Returns the u node of the edge
     * @return the u node of the edge
     */
    public BPTNode v () {
				return v;
    }
 
    /**
     * Returns the weight of the edge
     * @return the weight of the edge
     */
    public float w () {
				return w;
    }
}
