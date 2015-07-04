package ismmBpt2015.model;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import edu.princeton.cs.algs4.ResizingArrayQueue;

/**
 * This class implements a bit map that encodes the pixels represented 
 * by a specific node in BPT. The bit map is encoded using RLE.
 * The bit map can be constructed from a single pixel, or from two bitmaps
 * 
 * @author Abdullah Al-Dujaili
 *
 */
public class BitMap {
	
		// for computing the average compression ratio (see the constructors function)
	
		// for debungging purposes: (if set to true, it prints out the compression ratio for any BitMap constructed)
		static final boolean DEBUG = false;
		/**
		 * a Run-length code representing the bitmap
		 */
		private final boolean[] bitStream;
		private final boolean first; // flag to tell what value does the top-left corner have.
	
		// parameters for RLE:
		/**
		 * The bounding box coordinates of the bit map with respect to 
		 * the image
		 */
		private final BoundingBox BB;
	
		/** 
		 * constructor from a pixel
		 * @param x : the pixel x location
		 * @param y : the pixel y location
		 */
		public BitMap (int x, int y) {
				this.first = true;
				this.BB = new BoundingBox (x,y);
				this.bitStream = compress (1);
		
				// view the compression ratio:
				if (DEBUG)
						System.out.println (this.getCompressionRatio ());
		}
	
		/** 
		 * constructor from a BB
		 * @param BB : the bounding box corrdinate [xMin, xMax, yMin, yMax]
		 * 
		 */
		public BitMap (int [] BB) {
				this.first = true;
				this.BB = new BoundingBox (BB);
				this.bitStream = compress (this.BB.getArea ());
		
				// view the compression ratio:
				if (DEBUG)
						System.out.println (this.getCompressionRatio ());
		}
	
		/**
		 * Constructs from two bit maps
		 * @param a : bitmap 1
		 * @param b : bitmap 2
		 */
		public BitMap (BitMap a, BitMap b) {
				// location attributes
				this.BB = new BoundingBox (a.BB, b.BB, true);
				// uncompressed bitmap
				boolean[] tempStream = new boolean[BB.getArea ()];
				boolean[] streamA = a.getMap ();
				boolean[] streamB = b.getMap ();
		
				// interset !
				for (int y = BB.yMin (); y < BB.yMax (); y++)
						for (int x = BB.xMin (); x < BB.xMax (); x++) {
								//int idx = ((x - this.BB.xMin ())* this.BB.getHeight ()) + (y - this.BB.yMin ());
								int idx = ((y - this.BB.yMin ())* this.BB.getWidth ()) + (x - this.BB.xMin ());
								tempStream[idx] = false;
								// if the bitmap is within A
								if ((a.BB.xMax () > x) && (a.BB.xMin () <= x) && (a.BB.yMax () > y) && (a.BB.yMin () <= y))
										//tempStream[idx] = tempStream[idx] | streamA[ ((x-a.BB.xMin ()) * (a.BB.getHeight ())) + (y- a.BB.yMin ())];
										tempStream[idx] = tempStream[idx] | streamA[ ((y-a.BB.yMin ()) * (a.BB.getWidth ())) + (x- a.BB.xMin ())];

								// if the bitmap is within A
								if ((b.BB.xMax () > x) && (b.BB.xMin () <= x) && (b.BB.yMax () > y) && (b.BB.yMin () <= y))
										//tempStream[idx] = tempStream[idx] | streamB[ ((x-b.BB.xMin ()) * (b.BB.getHeight ())) + (y- b.BB.yMin ())];
										tempStream[idx] = tempStream[idx] | streamB[ ((y-b.BB.yMin ()) * (b.BB.getWidth ())) + (x- b.BB.xMin ())];
						}
		
				first = tempStream[0];
        // compress !
				this.bitStream = compress (tempStream);
		
				// view the compression ratio:
				if (DEBUG)
						System.out.println (this.getCompressionRatio ());
				// calculate the mean of the compression ration:
		}
	
	
		/**
		 * Compress a constant bit stream of length L
		 * @param a the bitstream
		 */
		static boolean[] compress (int L) {
				// variables for RLE:
				final char maxCount = ((1 << Param.BIT_WIDTH) -1);
				final int numChunks = (L + maxCount)/ (maxCount);
				final char lastCount = (char) (L - (numChunks-1) * maxCount);
		
				boolean [] out = new boolean[ (numChunks*2 -1) * Param.BIT_WIDTH];
		
				for (int i = 0; i < (numChunks*2 -2); i= i + 2) // iterate over the bits
						for (short w = 0; w < Param.BIT_WIDTH; w++) {
								out[i*Param.BIT_WIDTH+w] = true;
								out[ (i+1)*Param.BIT_WIDTH+w] = false;
						}
				// write the last code
				for (short w = 0; w < Param.BIT_WIDTH; w++)
						out[ (numChunks * 2-2)*Param.BIT_WIDTH+w]= ((((lastCount >> Param.BIT_WIDTH-1-w) & 0x01) == 1));
		
				// return it
				return out;
		}
	
		/**
		 * Compress the bit map for efficient memory use:
		 * @param a the bitstream
		 */
		static boolean[] compress (boolean [] in) {
		
				// variables for RLE:
				int L = in.length;
				char cnt = 1; // cnt of bits
				boolean currBit= in[0]; // current bit under ins 
				ResizingArrayQueue<Boolean> bq = new ResizingArrayQueue<Boolean> ();
		
				for (int i = 1; i < L; i++)
						// iterate over the bits
						if ((currBit != in[i]) || (cnt == ((char) ((1 << Param.BIT_WIDTH) - 1)))) {
								// encode the current run
								for (short w = Param.BIT_WIDTH-1; w >= 0; w--)
										bq.enqueue ((((cnt >> w) & 0x01) == 1));
								// get ready for the next run
								currBit= ! currBit;
								if (cnt == ((char) ((1 << Param.BIT_WIDTH) - 1))) {
										cnt = 0;
										i--;
								} else
										cnt = 1;
						} else
								cnt++;
				// write the last code
				for (short w = Param.BIT_WIDTH-1; w >= 0; w--)
						bq.enqueue ((((cnt >> w) & 0x01) == 1));
		
				// Copy the compressed bit stream
				boolean [] out = new boolean[bq.size ()];
		
				int i = 0;
				for ( boolean x : bq)
						out[i++] = x;
				// return it
				return out;
		}
	
		/**
		 * Get the compression ratio for the bit map
		 * @return the compression ratio
		 */
		public float getCompressionRatio () {
				return ((float) this.bitStream.length) / this.BB.getArea ();
		}

		/**
		 * Decompress the bit map for viewing purposes:
		 * @param a  BitMap
		 * @throws an illegal argument exception if the bitstream length is not of multiple Param.BIT_WIDTH,the count bitwidth
		 * @return returns the decompressed bitstream
		 */
		static boolean[] decompress (BitMap a) {
				// initialize:
				boolean [] out = new boolean[a.BB.getArea ()];
				boolean [] in  = a.getBitStream ();
		
				// the core of the decompress algorithm
				boolean currBit = a.getFirstBit (); // current bit status
				int currPos = 0; // current position in the bit map
				char cnt; // to hold the current value

				// read the compressed bit stream:
				final int L = in.length;
				
				if (L % Param.BIT_WIDTH != 0)
						throw new IllegalArgumentException ("The bistream a should have a length of multiple Ws");

				for (int i = 0; i < L; i= i + Param.BIT_WIDTH) {
						// advance by 4 bits
						cnt = 0;
						for (short w = 0; w < Param.BIT_WIDTH; w++)
								cnt = (char) (cnt | (char) (((in[i+w])? 1 : 0) << (Param.BIT_WIDTH-1-w)));
						for (char c = 0; c < cnt; c++)
								out[currPos + c] = currBit;
					
						// reverse and update the information for the next run of bits
						currPos = currPos + cnt;
						currBit = !currBit;
				}
				
				// return the map
				return out;
		}
	
		/**
		 * Decompress the bit map for viewing purposes:
		 * @param in  the compressed bitstream
		 * @pram firsBit the status of the first bit in the uncompressed stream
		 * @param size size of the orignal bit stream
		 * @throws an illegal argument exception if the bitstream length is not of multiple Param.BIT_WIDTH,the count bitwidth
		 * @return returns the decompressed bitstream
		 */
		static boolean[] decompress (boolean [] in, boolean firstBit, int size) {
				// initialize:
				boolean [] out = new boolean[size];

				// the core of the decompress algorithm
				boolean currBit = firstBit; // current bit status
				int currPos = 0; // current position in the bit map
				char cnt; // to hold the current value
		
		
				// read the compressed bit stream:
				final int L = in.length;
		
				if (L % Param.BIT_WIDTH != 0)
						throw new IllegalArgumentException ("The bistream a should have a length of multiple Ws");

				System.out.println ();
				for (int i = 0; i < L; i= i + Param.BIT_WIDTH) {
						// advance by 4 bits
						cnt = 0;
						for (short w = 0; w < Param.BIT_WIDTH; w++)
								cnt = (char) (cnt | (char) (((in[i+w])? 1 : 0) << (Param.BIT_WIDTH-1-w)));
						for (char c = 0; c < cnt; c++)
								out[currPos + c] = currBit;
			
						// reverse and update the information for the next run of bits
						currPos = currPos + cnt;
						currBit = !currBit;
				}
		
				// return the map
				return out;
		}
	
		/**
		 * Returns the uncompressed map of the bit map as a bit stream
		 *@return returns the uncompressed bitmap
		 */
		public boolean[] getMap () {
				return decompress (this);
		}
	
		/**
		 * Returns the bounding box of the bit map
		 * @return the bitmap's bounding box
		 */
		public BoundingBox getBB () {
				return this.BB;
		}
	
		/**
		 * returns the compressed bit stream of the bit map
		 * @return the compressed bit stream of the bitmap
		 */
		public boolean [] getBitStream ()
		{
				return this.bitStream;
		}
	
		/**
		 * returns the value of the first bit in the uncompressed bit map
		 * useful for decompressing the bit map
		 * @return the value of the first bit in the uncompressed bit map
		 */
		public boolean getFirstBit () {
				return this.first;
		}
	
		public BufferedImage toImage () {
				int width = (this.BB.getWidth ());
				int height = (this.BB.getHeight ());
		
				BufferedImage img = new BufferedImage (width,height, BufferedImage.TYPE_INT_RGB);
		
				boolean [] bm = this.getMap ();
				for (int y = 0; y < height; y++)
						for (int x = 0; x < width; x++)
								img.setRGB (x, y, (bm[y * width + x])? 0xffffffff : 0x00000000);
		
				return img;	
		}
}
