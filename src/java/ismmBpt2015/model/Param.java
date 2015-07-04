package ismmBpt2015.model;

import java.io.File;

/**
 * This class has the collection of all different parameters used to control and guide
 * the BPT data structure.
 * 
 * @author Abdullah Al-Dujaili
 *
 */

public class Param {

		public static final String FS = System.getProperty ("file.separator");

		/**
		 * Specifies how many bits are used for bitmap compression
		 */
		public static final short BIT_WIDTH = 6;
	
		/**
		 * Specifies the tolerance used when comparing two floats (used in BPTEdge)
		 */
		public static final double EPSILON = 1E-14;
		/**
		 * Specifies the default threshold parameter for the fraction of total number
		 * of nodes to be kept in the BPT (used in BPTNode)
		 */
		public static final float NODE_NUM_FRACTION_TH = 1.f;//0.0075f;//0.0075f;
	
		/**
		 * Specifies the default threshold parameters for the absoulute total number of nodes to be kept
		 * e.g if you set it to 255, it means the last 255 nodes of the tree will be kept.
		 * * this is not included yet in the code*
		 */
		public static final int NODE_NUM_TH = 127;
	
		/**
		 * Specifies the default threshold of edge sizes (connected nodes sizes) to be compared based on their weights or to be smaller/ greater
		 * This parameter is used in BPTEdge.compareTo method
		 */
		public static final float EDGE_DIFF = 2.f;
	
		/**
		 * Specifies the default threshold parameter for the total number of pixels
		 * in a BPT node as a fraction of the total number of pixels in the image (used in BPTNode)
		 */
		public static final float NODE_SIZE_FRACTION_TH = 0.f;
	
		/**
		 * Specifies the default threshold parameter for the total number of pixels
		 * in a BPT node as an absolute number of pixels (used in BPTNode)
		 */
		public static final int NODE_SIZE_TH = 0;

		/**
		 * Specifies the minimum dimension of the bounding box of the node, used as a pruning criterion in BPTNode
		 */
	
		/**
		 * Specifies the default depth of the binary partition tree (used after the complete construction of bpt
		 * Set this number to big values if you want other parameters to control the pruning other than this paramter
		 * @todo : Future versions should have a parameter to switch ON/OFF the level control so that it wont affect other parameters
		 * as it is a post processing step.
		 */
		public static final int BPT_LEVEL = 2000;
	
		/**
		 * Specifies if the nodes of BPT to be oriented towards the root or towards the leaves (affect its level attribute and the way,
		 * they are visualized)
		 */
		public static final boolean isGravityOn = false;
	
		/**
		 * Specifies if the DSIFT indices are considered and calculated along the tree
		 * construction. This has the effects of the following:
		 * 1. A dense sift parameters file must be proveided (./data/dsiftParam.mat) which is generated by 
		 * writeDSIFTspatialParamPerImg.m
		 * 2. Tree nodes which are not within the range of features will be pruned
		 */
		public static final boolean isDSIFTOn = false;
	
		/**
		 * Specifies whether image basic attributes such as gray, lbp, gradient should be computed.
		 * This is used in imageAttribute class
		 */
		public static final boolean isAttributeOn = false;
	
		/**
		 * Speciefies how the edges between nodes are compared, if it is set to true, the comparison will compare the difference on the levels first, else it will compare just on the weight (used in BPTEdge)
		 */
		public static final boolean isLevelCompareOn = false;

		/**
		 * Specifies the number of classes that we are interested in looking up
		 * This applies to all node
		 */
		public static final int LABEL_CARD = 2;
	
		/** 
		 * Specifies the combination of of two label variables assginment
		 */
		public static final int DUAL_LABEL_CARD = LABEL_CARD * LABEL_CARD;
	
		/**
		 * Specifies the color codings for the labels (classes)
		 */
		public static final int [] LABEL_COLOR_CODE = {
				0x5f0000ff,
				0x7fff0000,
				0x5fff0000, // class 1 : transparent red
				0x5f00ff00  // class 2 : trasparent green
		};
		/**
		 * Specifies the color for the region borders
		 */
		public static final int REGION_BORDER_COLOR = 0xff000000; // black
	
		public static enum nodeFeatureType { NO_FEATURE, ATTRB_FEATURE, DESCR_FEATURE}
		/**
		 * Specifies the window size for local image attributes:
		 * Currently LBP only is based on 3x3 window any modifications 
		 * here requires you to change the way LPB is computed in 
		 * ComputeImageAttributeTask under ImageAttribute class
		 */
		public static final byte WINDOW_SIZE = 3; // 
	
		/**
		 * Specifies smoothing gaussian spread (sigma)
		 */
		public static final float SIGMA = 0.5f;
	
		/**
		 * Specifies the normalization value for the above gausian function
		 */
		public static final float NORM_GAUSS = (float) (1 / (Math.sqrt (2 * Math.PI) * SIGMA));
	
		/**
		 * Specifies the normalization value for a window (used as a normlizer for statistics involved in a window)
		 */
		public static final float NORM_WINDOW = 1.f / (WINDOW_SIZE * WINDOW_SIZE);
		/**
		 * Specifies the number of threads used in parallel processing
		 */
		public static final byte NUM_THREADS = 8;
	
		/**
		 * Specifies the maximum displayable byte value
		 */
		public static final float MAX_RGB = 255.f;
	
		/**
		 * Specifies the maximum value of the gradient magnitude
		 */
		public static final float MAX_GM = (float) Math.sqrt (2 * 255 * 255);
	
		/**
		 * Specifies how much space the caption of a string describing the image should be shifted from the vertical and horizontal borders
		 */
		public static final int CAPTION_SPACE = 10;
	
		/**
		 * Specifies the binary bit pattern allocation mapping (clockwise)
		 */
		public static final byte [] LBP_MAP = { 0x04, 0x05, 0x06, 0x03, 0x00, 0x07, 0x02, 0x01, 0x00};
	
		/**
		 * Specifies the hyperparameter for normalizing the gradient magnitude (in BPTNodeDescriptor)
		 */
		public static final float GRD_EPSILON = 0.8f;
	
		/**
		 * Specifies the hyperparameter for normalizing the gradient magnitude (in BPTNodeDescriptor)
		 */
		public static final float STDEV_EPSILON = 0.2f;
	
		/**
		 * Specifies the normalization factor for spectral components
		 * 
		 */
		public static final float RGB_NORMALIZER  = 1.f / 255.f;
		/**
		 * Specifies whether a bitmap or a bounding box should be used for computing the region descriptors
		 * used in computing the kernel descriptors. It is also used in specifying what Dense sift features are included
		 */
		public static final boolean isBitMap = true;

		public static File dataDir;
		public static String dataDirName = "data";

		/**
		 * Specifies the files names for learning the optimum basis vectors for the kernel descriptor
		 */
		public static final String grdDescrFileName = "./data/grdDescr.samples";
		public static final String clrDescrFileName = "./data/clrDescr.samples";
		public static final String shpDescrFileName = "./data/shpDescr.samples";
		public static final String imgDescrFileName = "./data/imgDescr.samples";
	
		/**
		 * Specifies whether the name of .classes file which stores the node id and their inferred labels (classes)
		 */
		public static final String labelFileName = "bpt.classes";
	
		/**
		 * Specifies the name of the factor graph
		 */
		public static final String factorGraphFileName = "bpt.fg";
	
		/**
		 * Specified the directory where the annottated images are stored:
		 */
		public static File annotatedImagesDir;
		public static String annotatedImagesDirName =  "annotations";
	
		/**
		 * Specifies the names for the files that stores the node id and the indices of their features that are produced by vl_phow () from VLFEAT library
		 */
		public static final String leavesFileName = "bpt.leaves";
		public static final String parentsFileName = "bpt.parents";
	
		/**
		 * Specifies the names for the files that stores node id and their matlab-like linear pixel indices
		 */
		public static final String leavesPixelsFileName = "bpt.leavesPixels";
		public static final String parentsPixelsFileName= "bpt.parentsPixels";
		/**
		 * Specifies the name of the file that holds the spatial information of the DenseSIFT paramters
		 */
		public static final String DenseSIFTFileName = "./data/dsiftParam.mat";
}
