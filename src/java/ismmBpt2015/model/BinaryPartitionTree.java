package ismmBpt2015.model;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import edu.princeton.cs.algs4.MinPQ;

import misc.Bundle;
import misc.ProgressState;
import misc.StateNotifier;

/**
 * The BPT class represents the binary partition tree of an image where
 * the root represents the whole image and the leaf nodes represent the pixels
 * Some optimization in terms of memory and computation time are done by pruning the tree 
 * as well as approximating the regions with a boundary box
 * @author Abdullah Al-Dujaili
 * 
 */

public class BinaryPartitionTree extends StateNotifier {
		/*
		 * The BPT root 
		 */
		private BPTNode root;		  
		/*
		 * Variables related to the image features and their descriptor dictionaries:
		 */
		private ImageAttribute imgAttrb ; // the
	
		// tree properties:
		private int height = 0; // bpt height (including the root and the leaves)
		private int size = 0; // number of nodes
		/* @TODO this is not essential in the final product*/ 
		private static final boolean DEBUG = false; // for debugging purposes
	
    public ProgressState progressState = new ProgressState (this, "Progress");
		private boolean modified;
		private String name;
	
		public BinaryPartitionTree () {
				Param.dataDir = new File (Param.dataDirName);
				Param.annotatedImagesDir = new File (Param.dataDir, Param.annotatedImagesDirName);
		}

		public void createDirs () {
				// creating needed folders:
				File file = Param.dataDir;
				if (!file.exists ()) {
						if (file.mkdir ())
								System.out.println ("Directory data is created!");		
						else
								System.out.println ("Failed to create directory!");
				}
				file = Param.annotatedImagesDir;
				if (!file.exists ()) {
						if (file.mkdir ())
								System.out.println ("Directory annotations is created!");		
						else
								System.out.println ("Failed to create directory!");
				}
		}

		//=========================================================================================
		/**
		 * Construct the partition tree from an image
		 * @param imgStr the image file
		 */
		public void load (File imageFile) {
				load (imageFile, Param.NODE_NUM_FRACTION_TH, Param.NODE_SIZE_FRACTION_TH);
		}

		/**
		 * Construct the partition tree from an image with specified values for the parameter
		 * @param imgStr the image file
		 * @param nodeNumTH specifies how many nodes the tree should have as a fraction of the whole number of nodes (2*N-1)
		 * @param nodeSizeTH specifies how many pixels each region should have as a fraction of the whole image size
		 */
		public void load (File imageFile, float nodeNumTH, float nodeSizeTH) {
				broadcastUpdate ("Empty");
				name = imageFile.getName ();
				try {
						// image attribute:
						imgAttrb = new ImageAttribute (imageFile);
						// Some local variables to manage building the tree
						final int W = imgAttrb.IMG_WIDTH;
						final int H = imgAttrb.IMG_HEIGHT;
						final int NUM_PIX = H * W;
						final float invN = 1.f / NUM_PIX;
						int N 	= 2 * NUM_PIX - 2; // a variable for the current node id
						MinPQ<BPTEdge> pq = new MinPQ<BPTEdge> ();
						BPTNode [] tempNodes = new BPTNode[N];	  // the leaf nodes of the BPT
						BPTEdge e;	 // reference to the edge popped out from the queue
						progressState.init (Bundle.getMessage ("LoadImage"), NUM_PIX*3);

						// Build the leaf nodes and insert their edges into the priority queue:

						// Build all leaf nodes:
						int id 	= 0; // the index variable tracks the number of the nodes constructed so far
						float [] val = new float[3];
						for (int y = 0; y < H; y++)
								for (int x = 0; x < W; x++) {	
										// @TODO currently the image spectral values are encoded in int 
										val[0]= (float) (0xff & imgAttrb.getR (id));
										val[1]= (float) (0xff & imgAttrb.getG (id));
										val[2]= (float) (0xff & imgAttrb.getB (id));	
										tempNodes[id] = new BPTNode (N--, val, x, y);
										id++;
										if (!progressState.addValue (1))
												return;
								}
						val = null;
						// update the number of remaining nodes:
						//N = N - NUM_PIX;
						// now compute all the edges and put the smallest only in the pq:
						// create an edge with max weight:
						final BPTEdge MAX_EDGE = new BPTEdge (Float.POSITIVE_INFINITY);
						BPTEdge minEdge;
						int i = 0;
						for (int y = 0; y < H; y++) {
								for (int x = 0; x < W; x++) {
										minEdge = MAX_EDGE;
										// left hand neighbour
										if (x > 0) {
												// add the edge
												tempNodes[i].addNeighbour (tempNodes[i-1]);
												// create it
												e = new BPTEdge (tempNodes[i-1], tempNodes[i], invN);
												// add it if its the smallest
												if (minEdge.compareTo (e) > 0)
														minEdge = e;
										}
										// right hand neighbour
										if (x < (W -1)) {
												// create
												// add the edge
												tempNodes[i].addNeighbour (tempNodes[i+1]);
												// create it
												e = new BPTEdge (tempNodes[i+1], tempNodes[i], invN);
												// add it if it is the smallest
												if  (minEdge.compareTo (e) > 0)
														minEdge = e;
										}
										// top neighbor
										if(y > 0) {
												// create
												// add the edge
												tempNodes[i].addNeighbour (tempNodes[i-W]);
												// create it
												e = new BPTEdge (tempNodes[i-W], tempNodes[i], invN);
												// replcae it if it is the samllest
												if  (minEdge.compareTo (e) > 0)
														minEdge = e;
										}
										// bottom neighbour
										if(y < (H-1)) {
												// create
												// add the edge
												tempNodes[i].addNeighbour (tempNodes[i+W]);
												// create it
												e = new BPTEdge (tempNodes[i+W], tempNodes[i], invN);
												// replace if it is the smallest
												if  (minEdge.compareTo (e) > 0)
														minEdge = e;
										}
										// insert the minimum to the queue:
										//pq.add(minEdge);	
										pq.insert (minEdge);
										i++;
										if (!progressState.addValue (1))
												return;
								}
						}
						// check the number of nodes and edges
						// free tempNodes
						tempNodes = null;
						// Start merging !
						while (N > -1) {
								// we need N-1 merges to build the BPT
								int numPop = 0;
								// get the edge with the lowest weight whose nodes have no parents:
								do {
										e = pq.delMin ();
										if (DEBUG)
												numPop++;
								} while (e.u ().hasParent () || e.v ().hasParent ());
								if (DEBUG)
										System.out.println(numPop);

								// merge ! 
								root = new BPTNode (N--, e, nodeNumTH, nodeSizeTH, NUM_PIX);//, imgAttrb);//, attrbDict);

								// update the edge information
								minEdge = MAX_EDGE;
								for (BPTNode x : root.neighbours ()) {
										e = new BPTEdge (root, x, invN);
										if  (minEdge.compareTo (e) > 0)
												minEdge = e;	
								}
								pq.insert(minEdge);
								if (!progressState.addValue (1))
										return;
						}
						// end of unnecessary part:

						// deloiter:
						minEdge = null;

						// choose what type of operation depending on the operation mode
						byte opMode = Param.isGravityOn ? 0x01 : 0x00;
						opMode = (byte) (Param.isDSIFTOn? (0x02 | opMode) : opMode);

						switch(opMode) {
						case 0 : // no gravity, no dsift
								height = calcParamNTidyUp (root, 0);
								exportPixelIndices ();
								break;
						case 1 : // gravity, no dsift
								height = calcParamNTidyDown (root, 0);
								exportPixelIndices ();
								break;
						case 2 : // no gravity, dsift 
								try {
										DenseSIFTParam dsiftParam = new DenseSIFTParam (Param.DenseSIFTFileName);
										height = calcParamNTidyUp (root, 0, new BoundingBox (dsiftParam.xf, dsiftParam.xl.get (0), dsiftParam.yf, dsiftParam.yl.get (0)));
										exportDSIFTFeatureIndices (dsiftParam);
								} catch (IOException e1) {
										e1.printStackTrace ();
								}
								break;
						default : // gravity, dsift
								try {
										DenseSIFTParam dsiftParam = new DenseSIFTParam(Param.DenseSIFTFileName);
										height = calcParamNTidyDown (root, 0, new BoundingBox (dsiftParam.xf, dsiftParam.xl.get (0), dsiftParam.yf, dsiftParam.yl.get (0)));
										exportDSIFTFeatureIndices (dsiftParam);
								} catch (IOException e2) {
										e2.printStackTrace ();
								}
								break;
						}
				} finally {
						progressState.end ();
				}
				broadcastUpdate ("Image");
		}

		/**
		 * returns the binary partition tree root
		 * @return the BPT root node.
		 */
		public BPTNode root () {
				return root;
		}

		/**
		 * returns the the number of nodes in the tree
		 * @return the total count of nodes in BPT.
		 */
		public int getSize () {
				return this.size;
		}

		/**
		 * Get the number of links/edges/pairwise interactions in the tree. Useful for graphical model
		 * @return the total count of edges (pairwise interactions)
		 */
		public int getEdgeCount () {
				return (this.size-1);
		}


		/**
		 * returns the binary partition tree height
		 * @return the height of BPT.
		 */
		public int getHeight () {
				return this.height;
		}

		/*
		 * a function to traverse the tree and calculate its height, size ,and the number of links(edges), and max level
		 * This function is called only once after the generation of the tree by either
		 * getHeight() or getEdgeCount or any other function that requires such parameters,
		 * It reset the id of the nodes, their levels as well it calculate the height
		 * (reorganize the structure of the tree)
		 * This function tidies the trees with all the nodes dragged towards the root
		 * @param x the current node
		 * @param l an auxiliary variable to keep track of the node levels
		 */
		private int calcParamNTidyUp (BPTNode x, int l) {
				if (x == null)
						return -1;
				x.resetId (size++);
				x.resetLevel (l);
				if (x.level () < Param.BPT_LEVEL) {
						int h = Math.max (calcParamNTidyUp (x.rightChild (), l + 1), calcParamNTidyUp (x.leftChild (), l + 1)) + 1;
						if (x.w () != 0) // if it is not a leaf node already
								x.resetW (x.rightChild ().w () + x.leftChild ().w () + 1);
						return h;
				} else {
						x.pruneChildren (false);
						return 0;
				}
		}

		/*
		 * a function to traverse the tree and calculate its height, size ,and the number of links(edges), and max level
		 * This function is called only once after the generation of the tree by either
		 * getHeight() or getEdgeCount or any other function that requires such parameters,
		 * It reset the id of the nodes, their levels as well it calculate the height
		 * (reorganize the structure of the tree)
		 * This function tidies the trees with all the nodes dragged towards the root
		 * @param x the current node
		 * @param l an auxiliary variable to keep track of the node levels
		 * @param refBB prune the children of the node if their BBs dont interest with the reference BB.
		 */
		private int calcParamNTidyUp(BPTNode x, int l, BoundingBox refBB) {
				if (x == null)
						return -1;
				// reset the related parameters
				x.resetId (size++);
				x.resetLevel (l);

				// specify the conditions upon which children should be pruned
				boolean pruneChildren = (x.level() >= Param.BPT_LEVEL);
				if (! pruneChildren) {
						int h = Math.max (calcParamNTidyUp (x.rightChild (), l + 1, refBB), calcParamNTidyUp (x.leftChild (), l + 1, refBB)) + 1;
						if (x.w () != 0) // if it is not a leaf node already
								x.resetW (x.rightChild ().w () + x.leftChild ().w () + 1);
						return h;
				} else {
						x.pruneChildren (false);
						return 0;
				}
		}

		/*
		 * This function is used to check whether a node contains at least on dsift feature
		 * if the the dsift feature mode is enabled:
		 */
		/*
		 * a function to traverse the tree and calculate its height, size ,and the number of links(edges), and max level
		 * This function is called only once after the generation of the tree by either
		 * getHeight() or getEdgeCount or any other function that requires such parameters,
		 * It reset the id of the nodes, their levels as well it calculate the height
		 * (reorganize the structure of the tree)
		 * This function tidy the tree with all the nodes dragged towards its leaves
		 * @param x the current node
		 * @param l an auxiliary variable to keep track of the node levels
		 */
		private int calcParamNTidyDown(BPTNode x, int l) {
				if (x == null)
						return -1;
				x.resetId (size++);
				x.resetLevel (l);
				if (x.level () < Param.BPT_LEVEL) {
						int h = Math.max (calcParamNTidyDown (x.rightChild (), l + 1), calcParamNTidyDown (x.leftChild (), l + 1)) + 1;
						if (x.w () != 0) {
								// if it is not a leaf node already
								x.resetW (x.rightChild ().w () + x.leftChild ().w () + 1);
								x.resetLevel (Math.min (x.rightChild ().level (), x.leftChild ().level ()) - 1); 
						} else
								x.resetLevel (Param.BPT_LEVEL);
						return h;
				} else {
						x.pruneChildren (true);
						return 0;
				}
		}

		/*
		 * a function to traverse the tree and calculate its height, size ,and the number of links(edges), and max level
		 * This function is called only once after the generation of the tree by either
		 * getHeight() or getEdgeCount or any other function that requires such parameters,
		 * It reset the id of the nodes, their levels as well it calculate the height
		 * (reorganize the structure of the tree)
		 * This function tidy the tree with all the nodes dragged towards its leaves
		 * @param x the current node
		 * @param l an auxiliary variable to keep track of the node levels
		 * @param refBB prune the children of the node if their BBs dont interest with the reference BB.
		 */
		private int calcParamNTidyDown(BPTNode x, int l, BoundingBox refBB) {
				if (x == null)
						return -1;
				x.resetId (size++);
				x.resetLevel (l);
				// specify the conditions upon which children should be pruned
				boolean pruneChildren = (x.level() >= Param.BPT_LEVEL);
				if (!pruneChildren) {
						int h = Math.max (calcParamNTidyDown (x.rightChild (), l + 1, refBB), calcParamNTidyDown (x.leftChild (), l + 1, refBB)) + 1;
						if (x.w () != 0) {
								// if it is not a leaf node already
								x.resetW (x.rightChild ().w () + x.leftChild ().w () + 1);
								x.resetLevel (Math.min (x.rightChild ().level (), x.leftChild ().level ()) - 1); 
						} else
								x.resetLevel (Param.BPT_LEVEL);
						return h;
				} else {
						x.pruneChildren(true);
						return 0;
				}
		}

		/**
		 * This function is used if it the sift features are used to describe the regions of the BPT
		 * It generates two files one for leave nodes of the form:
		 * leaves.nodes of a columns of integers arranged as the following for  each leaf node (line per node)
		 * [ node ID | node level | 1-D indices of all the dense sift features extracted by vl_phow whose centres are within the node region ]
		 * parents.nodes : columns of integers arranged as the following for each parent node (line per node)
		 * [ node ID | node level | ID of the immediate childrent in descending order | the Max ID of the subnodes beneath that node]
		 */
		private void exportDSIFTFeatureIndices(DenseSIFTParam dsiftParam) {
				class NodeFileWriter {
						// file writers
						private PrintWriter outLeaves = null, outParents = null;
						private DenseSIFTParam dsiftParam;// = new DenseSIFTParam(Param.DenseSIFTFileName);
						// an array that assign for each node the exclusive upperbound of the nodes beneath that node
						private int [] exclusiveChildId;

						public NodeFileWriter (DenseSIFTParam dsiftParam) {
								try {
										// set dsift
										this.dsiftParam = dsiftParam;
										// open the files
										outLeaves = new PrintWriter (new BufferedWriter (new FileWriter (new File (Param.dataDir, Param.leavesFileName))));
										outParents = new PrintWriter (new BufferedWriter (new FileWriter (new File (Param.dataDir, Param.parentsFileName))));
										// declare and initilize the index
										exclusiveChildId = new int[size];
										exclusiveChildId[0] = size;
										// traverse the bpt 
										populateNodeFiles (root);
										// print the information for the root node ( this assumes that the bpt has at least 3 nodes)
										BPTNode rNode = root.rightChild ();
										BPTNode lNode = root.leftChild ();

										int minId = Math.min (rNode.id (), lNode.id ());
										int maxId = (rNode.id () == minId)? lNode.id () : rNode.id ();

										outParents.println (root.id () + " " + root.level () + " " +root.level () + " " + maxId + " " + minId + " " + (size-1));
										// close the files:
										outLeaves.close ();
										outParents.close ();
								} catch (IOException e) {
										e.printStackTrace();
								}
						}

						/*
						 * This function populates the information about the tree toplogy with respect to the features
						 * evaluated. for a row of a leaf node it returns all the 1d indices of the corresponding features
						 * in descrs computed by vl_phow()
						 * for a row of a parent node it returns all the nodes below in the form of [min subnode ID - max subnode ID] inclusive
						 */
						private void populateNodeFiles(BPTNode node) {
								if (node == null)
										return;
								if (node.w () == 0) {
										// if leave nodes
										// print the node id
										outLeaves.print (node.id ()+ " " + node.level () + " ");
										// the region boundaries
										int xMinMap = node.getBitMap ().getBB ().xMin ();
										int xMaxMap = node.getBitMap ().getBB ().xMax ();
										int yMinMap = node.getBitMap ().getBB ().yMin ();
										int yMaxMap = node.getBitMap ().getBB ().yMax ();
										int width = xMaxMap - xMinMap;
										boolean [] bp = node.getBitMap ().getMap ();
										// how many size(scale) are taken
										int M = dsiftParam.xl.getM ();

										// align the boundaries to be within the feature boundaries
										int xMin = Math.min (Math.max (xMinMap, dsiftParam.xf), dsiftParam.xl.get (0));
										int xMax = Math.min (Math.max (xMaxMap - 1, dsiftParam.xf), dsiftParam.xl.get (0));
										int yMin = Math.min (Math.max (yMinMap, dsiftParam.yf), dsiftParam.yl.get (0));
										int yMax = Math.min (Math.max (yMaxMap - 1, dsiftParam.yf), dsiftParam.yl.get (0));
										// align them on the feature grid
										xMin = (int) Math.ceil ((xMin - dsiftParam.xf) * dsiftParam.invStep) * dsiftParam.step + dsiftParam.xf ;
										xMax = (int) Math.floor((xMax - dsiftParam.xf) * dsiftParam.invStep) * dsiftParam.step + dsiftParam.xf + 1;
										yMin = (int) Math.ceil ((yMin - dsiftParam.yf) * dsiftParam.invStep) * dsiftParam.step + dsiftParam.yf ;
										yMax = (int) Math.floor((yMax - dsiftParam.yf) * dsiftParam.invStep) * dsiftParam.step + dsiftParam.yf + 1;

										for (int y = yMin; y < yMax; y += dsiftParam.step)
												for (int x = xMin; x < xMax; x += dsiftParam.step) {
														if (bp[(y - yMinMap) * width + (x - xMinMap)] || !Param.isBitMap) {
																// check if it is lying on the grid
																int idxX = (int) ((x- dsiftParam.xf) * dsiftParam.invStep);
																int idxY = (int) ((y- dsiftParam.yf) * dsiftParam.invStep);
																// get all the descriptors at that point
																int idx;

																for (int i = 0; i < M; i++)
																		if (x <= dsiftParam.xl.get (i) && y <= dsiftParam.yl.get (i)) {
																				idx = (idxX * dsiftParam.numY.get (i)) + idxY + dsiftParam.cumOffset.get (i) + 1;
																				outLeaves.print (idx + " ");
																		}
														}
												}
										outLeaves.println();
								} else {
										// set the exclusive child id for this node:

										// two cases
										BPTNode rNode = node.rightChild ();
										BPTNode lNode = node.leftChild ();

										int minId = Math.min (rNode.id (), lNode.id ());
										int maxId = (rNode.id () == minId)? lNode.id () : rNode.id ();
										exclusiveChildId[minId] = maxId;
										exclusiveChildId[maxId] = exclusiveChildId[node.id ()];

										if (rNode.w ()!= 0) {
												// if it is not a leaf node
												int otherChildNodeId = (rNode.leftChild ().id () != (rNode.id () + 1)) ? rNode.leftChild ().id () : rNode.rightChild ().id ();
												outParents.println (rNode.id () + " " + rNode.level () + " " + otherChildNodeId + " "  + (rNode.id () + 1) + " " + (exclusiveChildId[rNode.id ()] - 1));
										}
										if (lNode.w()!= 0) {
												// if its not a leaf node
												int otherChildNodeId = (lNode.leftChild ().id () != (lNode.id () + 1)) ? lNode.leftChild ().id () : lNode.rightChild ().id ();
												outParents.println (lNode.id () + " " + lNode.level () + " " + otherChildNodeId + " " + (lNode.id () + 1) + " " + (exclusiveChildId[lNode.id ()] - 1));
										}	
										populateNodeFiles (node.rightChild ());
										populateNodeFiles (node.leftChild ());
								}
								return;
						}
				}
				NodeFileWriter nfrter = new NodeFileWriter (dsiftParam);
		}

		/**
		 * This function is used if the pixels indices are to be exported of each node
		 * It generates two files one for leave nodes of the form:
		 * leavesPixels.nodes of a columns of integers arranged as the following for  each leaf node (line per node)
		 * [ node ID | node level | 1-D indices of all pixels within the node region ]
		 * parentsPixels.nodes : columns of integers arranged as the following for each parent node (line per node)
		 * [ node ID | node level | ID of the immediate children in descending order | the Max ID of the subnodes beneath that node]
		 */
		private void exportPixelIndices () {
				class NodeFileWriter {
						// file writers
						private PrintWriter outLeaves = null, outParents = null;
						private int [] exclusiveChildId;

						public NodeFileWriter () {
								try {
										// set dsift
										// open the files
										outLeaves = new PrintWriter (new BufferedWriter (new FileWriter (new File (Param.dataDir, Param.leavesPixelsFileName))));
										outParents = new PrintWriter (new BufferedWriter (new FileWriter (new File (Param.dataDir, Param.parentsPixelsFileName))));
										// declare and initilize the index
										exclusiveChildId = new int[size];
										exclusiveChildId[0] = size;
										// traverse the bpt
										populateNodeFiles (root, -1);
										// print the information for the root node ( this assumes that the bpt has at least 3 nodes)
										BPTNode rNode = root.rightChild ();
										BPTNode lNode = root.leftChild ();

										int minId = Math.min (rNode.id (), lNode.id ());
										int maxId = (rNode.id () == minId)? lNode.id () : rNode.id ();

										outParents.println (root.id () + " " + root.w () + " " +root.level () + " -1 " + maxId + " " + minId + " " + (size-1));
										// close the files:
										outLeaves.close ();
										outParents.close ();
								} catch (IOException e) {
										e.printStackTrace();
								}
						}

						/*
						 * This function populates the information about the tree toplogy with respect to the features
						 * evaluated. for a row of a leaf node it returns all the 1d indices of the corresponding features
						 * in descrs computed by vl_phow()
						 * for a row of a parent node it returns all the nodes below in the form of [min subnode ID - max subnode ID] inclusive
						 */
						private void populateNodeFiles(BPTNode node, int parentId) {
								if (node == null) return;

								if (node.w () == 0) {
										// if leave nodes
										// print the node id
										outLeaves.print (node.id ()+ " " + node.w ()+ " " + node.level () + " " + parentId + " ");
										// the region boundaries
										int xMin = node.getBitMap ().getBB ().xMin ();
										int xMax = node.getBitMap ().getBB ().xMax ();
										int yMin = node.getBitMap ().getBB ().yMin ();
										int yMax = node.getBitMap ().getBB ().yMax ();
										int width = xMax - xMin;
										boolean [] bp = node.getBitMap ().getMap ();
										// how many size(scale) are taken
										//System.out.println("xMin, xMax, yMin, yMax "+xMin+" "+xMax+" "+yMin+" "+yMax);
										//System.out.println("xMino, xMaxo, yMino, yMaxo"+xMinMap+" "+xMaxMap+" "+yMinMap+" "+yMaxMap);
										int idx;
										for (int y = yMin; y < yMax; y++)
												for (int x = xMin; x < xMax; x++)
														if (bp[(y - yMin) * width + (x - xMin)] || !Param.isBitMap) {
																idx = x * imgAttrb.IMG_HEIGHT + y +1;
																outLeaves.print (idx + " ");
														}
										outLeaves.println ();
								} else {
										// set the exclusive child id for this node:

										// two cases
										//outParents.println(node.id() + " " + node.rightChild().id() + " " + node.leftChild().id());
										BPTNode rNode = node.rightChild ();
										BPTNode lNode = node.leftChild ();

										int minId = Math.min (rNode.id (), lNode.id ());
										int maxId = (rNode.id () == minId)? lNode.id () : rNode.id ();
										exclusiveChildId[minId] = maxId;
										exclusiveChildId[maxId] = exclusiveChildId[node.id ()];

										if (rNode.w()!= 0) {
												// if it is not a leaf node
												int otherChildNodeId = (rNode.leftChild ().id () != (rNode.id () + 1)) ? rNode.leftChild ().id () : rNode.rightChild ().id ();
												outParents.println (rNode.id () + " " + rNode.w () + " " + rNode.level () + " " + node.id () + " "+ otherChildNodeId + " "  + (rNode.id () + 1) + " " + (exclusiveChildId[rNode.id ()] - 1));
										}
										if (lNode.w ()!= 0) {
												// if its not a leaf node
												int otherChildNodeId = (lNode.leftChild ().id () != (lNode.id () + 1)) ? lNode.leftChild ().id () : lNode.rightChild ().id ();
												outParents.println (lNode.id () + " " + lNode.w () + " " + lNode.level () + " " + node.id () + " " + otherChildNodeId + " " + (lNode.id () + 1) + " " + (exclusiveChildId[lNode.id ()] - 1));
										}	
										populateNodeFiles (node.rightChild (),node.id ());
										populateNodeFiles (node.leftChild (),node.id ());
								}
								return;
						}
				}
				NodeFileWriter nfrter = new NodeFileWriter ();
		}

		/**
		 * Get the image represented by the BPT
		 * @return the image corresponding to the BPT
		 */
		public BufferedImage getImage () {
				BufferedImage img = new BufferedImage (imgAttrb.IMG_WIDTH, imgAttrb.IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
				for (int y = 0; y < imgAttrb.IMG_HEIGHT; y++)
						for (int x = 0; x < imgAttrb.IMG_WIDTH; x++)
								img.setRGB (x, y, imgAttrb.getRGB (x, y));
				return img;
		}

		/**
		 * Useful function for GUI applications 
		 * @param l the level at which we would like to get the bpt image
		 * @return Returns a ImageIcon containing a bpt picture at a certain level, for embedding in a Jlabel JPanel,
		 * JFrame or other GUI widget.
		 */
		public ImageIcon getImageIcon (int l) {
				if (l < 0)
						throw new IllegalArgumentException ("Level should be nonnegative");
				BufferedImage image = new BufferedImage (imgAttrb.IMG_WIDTH, imgAttrb.IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
				//int currentLevel = (int) ( ( (float) l / Integer.MAX_VALUE) * height);
				//System.out.println (" Current level:"+l+" MaxLevel:"+ height);
				drawNodes (image, root, l);
				ImageIcon icon = new ImageIcon (image);
				return icon;
		}

		/**
		 * Produces images in the directory specified, that correspond to different levels of the BPT with each node region
		 * being overlaid (annotated) with its class.
		 * @param labelsFileName a file with .classes that lists the nodes by their ids and their labels <node-id><space><nodelabel> per coloiumn
		 * @param imgsPath the directory where to store the set of these images
		 */
		//public void annotateNodes (String labelsFileName, String imgsPath) 
		public void annotateNodes (){
				try {
						// read the node labels from the labelsFileName
						Scanner in = null;
						in = new Scanner (new File (Param.dataDir, Param.labelFileName));
						int [] nodeLabels = new int[in.nextInt ()]; // an array of class labels for each node indexed by the node id
						for (int i = 0; i < nodeLabels.length; i++) {
								// each line is aligned as the followning <node-id><space><nodelabel>:
								nodeLabels[in.nextInt ()] = in.nextInt (); 
						}
						in.close ();
						// define the input image:
						BufferedImage img = getImage ();
						// define an labels image:
						BufferedImage labelImg = new BufferedImage (imgAttrb.IMG_WIDTH, imgAttrb.IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
						// the final overlaid image:
						BufferedImage image = new BufferedImage (imgAttrb.IMG_WIDTH, imgAttrb.IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
						Graphics g = image.getGraphics ();
						// Go over all the levels of the tree
						int levels = Param.isGravityOn ? Param.BPT_LEVEL : this.height;
						for (int l= 0; l <= levels; l++) {
								// find and tag:
								drawLabels (labelImg, root, l, nodeLabels);

								// overlay with transparent colors of the class labels:
								g.drawImage (img, 0, 0, null);
								g.drawImage (labelImg, 0, 0, null);

								//write the produced image 
								ImageIO.write (image, "png", new File (Param.annotatedImagesDir, "l"+l+".png"));
								// currently it saves the images with .png extension
						}
				} catch (IOException e) {
						e.printStackTrace ();
				}
		}


		/**
		 * Returns a JLabel containing this picture, for embedding in a JPanel,
		 * JFrame or other GUI widget.
		 * @param l the level at which we would like to get the bpt image. This is an integer value between 0 and Integer.MAX_VALUE
		 * @return the JLabel for the bpt picture at level l
		 */
		JLabel getJLabel (int l) {
				if (l < 0)
						throw new IllegalArgumentException ("Level should be nonnegative");
				ImageIcon icon = getImageIcon (l);
				return new JLabel (icon);
		}

		/**
		 * Returns a JLabel for an image displaying the pixels of the node of interest while
		 * other pixels are blank.
		 *  @param id the node id of interest
		 *  @param isBitMap specifies whether we want to display the bounding box or the actual region (bit map)
		 *  @returns JLabel object to be used in JFrame object for displaying the image
		 *  @author Robert Sedgewick
		 *  @author Kevin Wayne
		 *  @author Abdullah Al-Dujaili 
		 */
		public JLabel getNodeJLabel (int id, boolean isBitMap) {
				BufferedImage image = new BufferedImage (imgAttrb.IMG_WIDTH, imgAttrb.IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
				search4NodeNDraw (image, root, id, isBitMap);
				return new JLabel (new ImageIcon (image));
		}

		/*
		 * recursive function used to look through the bpt for a node with a specific id 
		 * and then set the input image pixels corresponding to that node to the original 
		 * image pixels value.
		 * @param inImg the image whose a set of pixels are to be altered 
		 * @pram x the current node under inspection
		 * @param id the node id of interest
		 * @param features array to set to the node features
		 * @param isBitMap a flag specifices whether we want the bounding box pixels or the region's ones.
		 */
		private void search4NodeNDraw (BufferedImage inImg, BPTNode x, int id, boolean isBitMap) {
				if (x == null)
						return; // return from null node
				if (x.id () == id) {
						if (isBitMap)
								setBBPixels (inImg,x.getBitMap ());  // Node found set the pixles of that region to their original colours
						else
								setBBPixels (inImg,x.getBBxy ());  // Node found set the pixles of that region to their original colours	
				} else {
						// keep searching
						search4NodeNDraw (inImg, x.rightChild (), id, isBitMap);
						search4NodeNDraw (inImg, x.leftChild (), id, isBitMap);
				}
		}

		/**
		 * Returns the bpt highest aggregated level
		 * 
		 * @return the highest aggregated level
		 */
		public int level () {
				return root.level ();
		}

		/*
		 * Recursive function to identify nodes with this level value
		 * @param inImg the image whose a set of pixels are to be altered 
		 * @param l the level above which nodes are of interest
		 * @pram x the current node under inspection
		 */
		private void drawNodes (BufferedImage inImg, BPTNode x,int level) {
				if (x == null)
						return;
				if (level < 0)
						throw new IllegalArgumentException ("level value should be nonnegative");
				if (x.level () < level) {
						if (x.w () == 0)
								setBBPixels (inImg, x.getBitMap (), x.getRGB (), false);
						else {
								drawNodes (inImg, x.leftChild (), level);
								drawNodes (inImg, x.rightChild (), level);
						}
				}	else {	
						setBBPixels (inImg, x.getBitMap (), x.getRGB (), false);
						return;
				}
		}

		/*
		 * Recursive function to identify nodes (regions) with this level value and draw its label over its
		 * region 
		 * @param inImg the image whose a set of pixels are to be altered 
		 * @pram x the current node under inspection
		 * @param level the level above which nodes are of interest
		 * @param nodeLabels an array that has all the assignments of the nodes
		 */
		private void drawLabels (BufferedImage inImg, BPTNode x,int level, int [] nodeLabels) {
				if (x == null)
						return;
				if (level < 0)
						throw new IllegalArgumentException ("level value should be nonnegative");
				if (x.level () < level) {
						if (x.leftChild () == null  || x.rightChild () == null) {
								//setBBPixels (inImg, x.getBBxy (), x.getRGB ());
								setBBPixels (inImg, x.getBitMap (), Param.LABEL_COLOR_CODE[nodeLabels[x.id ()]], true);
						}
						if (x.leftChild () == null)
								;
						else if (x.leftChild ().level () < level)
								drawLabels (inImg, x.leftChild (), level, nodeLabels);
						else
								setBBPixels (inImg, x.leftChild ().getBitMap (), Param.LABEL_COLOR_CODE[nodeLabels[x.leftChild ().id ()]], true);

						if (x.rightChild () == null)
								;
						else if (x.rightChild ().level () < level)
								drawLabels (inImg, x.rightChild (), level, nodeLabels);
						else
								setBBPixels (inImg, x.rightChild ().getBitMap (), Param.LABEL_COLOR_CODE[nodeLabels[x.rightChild ().id ()]], true);
				} else {
						setBBPixels (inImg, x.getBitMap (), Param.LABEL_COLOR_CODE[nodeLabels[x.id ()]], true);
						return;
				}
		}

		/**
		 * Set the pixels in the input image contained in the input bounding box by actual pixels color of the bpt original image
		 * @param inImg the input image
		 * @param BB the bounding box [xstart, xlast+1, ystart, ylast+1]
		 */
		private void setBBPixels (BufferedImage inImg, int [] BB){
				for (int y = BB[2]; y < BB[3]; y++)
						for (int x = BB[0]; x < BB[1]; x++)
								inImg.setRGB (x, y, imgAttrb.getRGB (x, y));	
		}

		/**
		 * Set the pixels in the input image contained in the region specified by the bitmap by the region mean color
		 * or the actual pixels color
		 * @param inImg the input image
		 * @param BM the bitmap to be coloured
		 */
		private void setBBPixels (BufferedImage inImg, BitMap BM){
				int xMin = BM.getBB ().xMin ();
				int xMax = BM.getBB ().xMax ();
				int yMin = BM.getBB ().yMin ();
				int yMax = BM.getBB ().yMax ();
				boolean [] bp = BM.getMap ();

				//System.out.println ("actual stream length:"+bp.length);
				//System.out.println ("BB area:"+BM.getBB ().getArea ());
				int id =0;
				for (int y = yMin; y < yMax; y++)
						for (int x = xMin; x < xMax; x++)
								if (bp[id++])
										inImg.setRGB (x, y, imgAttrb.getRGB (x, y));
		}

		/**
		 * Set the pixels in the input image contained in the region specified by the bitmap by the specified colour
		 * @param inImg the input image
		 * @param BM the bitmap to be coloured
		 * @param rgb the desired color for the pixels 
		 * @param isBordered specifies whether to outline the bitmap region or not
		 */
		private void setBBPixels (BufferedImage inImg, BitMap BM, int rgb, boolean isBordered){
				int xMin = BM.getBB ().xMin ();
				int xMax = BM.getBB ().xMax ();
				int yMin = BM.getBB ().yMin ();
				int yMax = BM.getBB ().yMax ();
				//int xIdx;
				//int yIdx;
				int width = BM.getBB ().getWidth ();
				boolean [] bp = BM.getMap ();
				int id=-1;

				for (int y = yMin; y < yMax; y++)
						for (int x = xMin; x < xMax; x++)
								if (bp[++id]) {
										// check for border
										if (!isBordered)
												inImg.setRGB (x, y, rgb);
										else {
												//draw outline if its along the boudning box
												if ( (x == xMin) || (x == (xMax-1)) || (y == yMin) || (y == (yMax-1)))
														inImg.setRGB (x, y, Param.REGION_BORDER_COLOR);
												//draw if it is a neighbour of the bitmap
												else if (! (bp[id + 1] && bp[id - 1] && bp[id + width] && bp[id - width]))
														inImg.setRGB (x, y, Param.REGION_BORDER_COLOR);
												else
														inImg.setRGB (x, y, rgb);
										}
								}
		}


		/**
		 * Creates a factor graph out of the BPT and stores the factors (potentials)
		 * in the file fileName whose extension is fg
		 * @param fileName : 
		 */
		public void createFactorGraph () { // (String fileName)
				try {
						// checking its extension:
						// file writer:
						PrintWriter out = null;
						out = new PrintWriter (new BufferedWriter (new FileWriter (new File (Param.dataDir, Param.factorGraphFileName))));
						// write the total number of factors:
						out.print (2 * this.size-1);
						// go over the factors and write them out to the *.fg file recursively:
						writeFactors (out,root); // this traverse the 
						// close the writer:
						//out.println ();
						out.close ();
				} catch (IOException e) {
						e.printStackTrace ();
				}
		}

		/**
		 * Private function for writing factors of the tree to the *.fg file handled by out object
		 * 
		 * @param out PrintWriter object that handles the factor graph file *.fg
		 * @param x the current node
		 */
		private void writeFactors (PrintWriter out, BPTNode x) {
				// Get the singleton of the current node and its pairwise factors with its children if any
				out.print (x.singletonFactorAsString ());
				out.print (x.pairwiseFactorAsString ());
				// if it has children go recursively:
				if (x.leftChild () != null)
						writeFactors (out, x.leftChild ());
				if (x.rightChild () != null)
						writeFactors (out, x.rightChild ());
		}

		/**
		 *  Perform inference on the BPT using the *.fg file name. If the file does not exist,
		 *  it computes it.
		 *  @param fileName : the file name of the fg graph, 
		 * 
		 *  
		 */
		public void doInference () { // (String fileName) throws InterruptedException
				// check if the *.fg file is created
				// Now that the fg file is there, run the inference engine ! 
				ProcessBuilder pb = new ProcessBuilder ("./infer", Param.dataDir+Param.FS+Param.factorGraphFileName, "pif","jt");
				pb.redirectOutput (new File (Param.dataDir, Param.labelFileName)); // currently it saves as .classes file in the same directory as the factor graph file
				//pb.redirectOutput ();
				try {
						Process p = pb.start ();
						try {
								p.waitFor ();
						} catch (InterruptedException e) {
						}
						System.out.println ("Phew ! done with inference");
				} catch (IOException e) {
						e.printStackTrace ();
				}
		}

		public boolean getModified () {
				return this.modified;
		}

		public String getName () {
				return name;
		}
		// ========================================
}
