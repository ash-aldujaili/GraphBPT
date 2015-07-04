package ismmBpt2015.model;
import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * This class calculates in parallel a set of pixel-level attributes
 * (gradient magnitude, gradient orientation, Local binary pattern, red channel,
 *  green channel , blue channel.
 * This class is meant to be used with .....
 * to construct the image kernel descriptor
 * @author Abdullah Al-Dujaili
 * 
 * inspired by http://math.hws.edu/javanotes/source/chapter12/MultiprocessingDemo2.java
 *
 */
public class ImageAttribute {
	
		//***********************************************
		// Image Attributes- related instance variables:
		//***********************************************
		// color attributes:
		private final byte [] abgr;
	
		private byte [] gTemp;
		byte [] g;
		// image dimensions and channels
		public final short NUM_CHANNELS;
		public final int IMG_HEIGHT;
		public final int IMG_WIDTH;
		private final byte rIdx, gIdx, bIdx;
		// shape attributes:
		byte [] lbp;
		float [] stDev;
		// gradient attributes:
		float [] gM;
		float [] gOx;
		float [] gOy;
	
		// instance variables to help in visualizing and clearing memory
		private float maxStd;
		private int pixelCnt = 0;
		//***********************************************
	
		//***********************************************
		// Parallel processing-related classes:
		//***********************************************
	
		// a queue of tasks to be processed:
		private ConcurrentLinkedQueue<Runnable> taskQ;
		/**
		 * An object of type ComputeImageAttributeTask represent the task of computing the attributes
		 * of one row of an image. The task has a run () method which does the actual computation.
		 * @author Abdullah Al-Dujaili
		 *
		 */
		private class ComputeImageAttributeTask implements Runnable {

				// some instance variables
				private final int rowNumber;
				/**
				 * Construct an object of type ComputeImageAttributeTask
				 * The standard deviation is calculated based on the algorithm in :
				 * http://www.cs.berkeley.edu/~mhoemmen/cs194/Tutorials/variance.pdf
				 * @param rowNumber
				 */
				public ComputeImageAttributeTask (int rowNumber) {
						this.rowNumber = rowNumber;
				}
		
				//@Override
				public void run () {
			
						// 
						// Go over all the pixels in the row and generate their attributes:
						/**
						 * @TODO : currenlty the operations here only support 3x3 because of LBP
						 */
						int windowIdx = Param.WINDOW_SIZE >> 1;
						int pixelIdx = rowNumber * IMG_WIDTH;
						float locMaxStd = Float.MIN_VALUE;
			
						for (int i = pixelIdx; i < (IMG_WIDTH + pixelIdx); i++) {
								// std related
								double M = 0.0;
								double Q = 0.0;
								short pixelCnt = 8;
								lbp[i] = 0;
								// perform window operations:
								for (int y = rowNumber - windowIdx; y < rowNumber + windowIdx + 1; y++) {
										int r = y;
										if ( y < 0) r = 0;
										if ( y >= IMG_HEIGHT) r = IMG_HEIGHT-1;
					
										for (int x = i - windowIdx ; x < i + windowIdx + 1; x ++) {
												// compute the current pixel index:
												int c = (x - pixelIdx);
												if (x < pixelIdx) c = 0;
												if (x >= (IMG_WIDTH + pixelIdx)) c = IMG_WIDTH-1;
												int idx = c + r * IMG_WIDTH;
												// compute std-related
						
												if (pixelCnt == 8) {
														M = (double) (0xff &g[idx]);
														Q = 0.0;
												} else {
														double invk = 1.0 / (9 - pixelCnt);
														double deltaM = ((double) (0xff &g[idx]) - M);
														Q = Q + (8 - pixelCnt) * deltaM * deltaM * invk;
														M = M + deltaM * invk;	
												}
												// compute lbp
												if (pixelCnt != (windowIdx * (Param.WINDOW_SIZE + 1)))
														if ((0xff & g[i]) > (0xff & g[idx]))
																lbp[i] |= (0x01 << Param.LBP_MAP[pixelCnt]);
												pixelCnt--;
										}
								}
								// compute std:
								stDev[i] = (float) Math.sqrt (Q * Param.NORM_WINDOW);
								locMaxStd = Math.max (locMaxStd, stDev[i]);
								// compute gX , gM
								float gX = (float) (0xff & g[Math.min (i+1, (IMG_WIDTH + pixelIdx)-1)]) - (float) (0xff & g[Math.max (pixelIdx,i-1)]);
								int y1Idx = (i - IMG_WIDTH < 0)? i : (i - IMG_WIDTH);
								int y2Idx = ((i + IMG_WIDTH) >= (IMG_WIDTH * IMG_HEIGHT))? i : i + IMG_WIDTH;
								float gY = (float) (0xff &g[y2Idx]) - (float) (0xff &g[y1Idx]);
								// compute gM
								gM[i] = (float) Math.pow ((gX * gX) + (gY * gY), 0.5);
								// compute g0x
								gOx[i] = (gY == 0.f)? 0.f : (gY / gM[i]);
								gOy[i] = (gX == 0.f)? 0.f : (gX / gM[i]);
								//System.out.print (" "+gM[i]);
						}
						// sync for the max value of standard deviation
						synchronized (taskQ) {
								maxStd = Math.max (maxStd, locMaxStd);
						}
				}
		}
	
		/**
		 * An object of type SmoothGrayImageTask represent the task of smoothing gray component of an
		 * image with a gaussian mask, this is done using 2 separable filters:
		 * @author Abdullah Al-Dujaili
		 *
		 */
		private class SmoothGrayImageTask implements Runnable {

				// some instance variables
				private final int rowNumber;
				private final boolean isFrstPass; // offset for accessing the auxiliary array
				private final float invSigma;
				/**
				 * Construct an object of type ComputeImageAttributeTask
				 * @param rowNumber
				 */
				public SmoothGrayImageTask (int rowNumber, boolean isFrstPass) {
						this.rowNumber = rowNumber;
						this.isFrstPass = isFrstPass;
						this.invSigma = 1.f / Param.SIGMA;
				}
		
				//@Override
				public void run () {
						// 
						// Go over all the pixels in the row and generate their attributes:
						int l1;
						int l2;
						byte [] g1;
						byte [] g2;
					
						if (isFrstPass) {
								l1 = IMG_WIDTH;
								l2 = IMG_HEIGHT;
								g1 = g;
								g2 = gTemp;
						} else {
								l1 = IMG_HEIGHT;
								l2 = IMG_WIDTH;
								g1 = gTemp;
								g2 = g;
						}
						int pixelIdx = rowNumber * l1;
						int windowIdx = Param.WINDOW_SIZE >> 1;
								
						for (int i = pixelIdx; i < (l1 + pixelIdx); i++) {
								double gray = 0.f;
								double w = 0.f;
								double sumW = 0.f;
								// compute the gray component:
								for (int j = i - windowIdx; j < i + windowIdx +1; j++) {
										int idx = (j < pixelIdx)? pixelIdx: ((j > (l1 + pixelIdx -1))? (l1 + pixelIdx -1):j);
										w = (Math.exp (-0.5 * invSigma * invSigma * Math.abs (j-i) * Math.abs (j-i)));
										gray += (w* (0xff & g1[idx]));
										sumW += w;
								}	
								g2[rowNumber + (i - pixelIdx) * l2] = (byte) ((gray / sumW));
						}	
				}
		}
	
		/**
		 * An object of type ComputeGrayImageTask represent the task of computing gray component of an
		 * image
		 * @author Abdullah Al-Dujaili
		 *
		 */
		private class ComputeGrayImageTask implements Runnable {

				// some instance variables
				private final int rowNumber;
				/**
				 * Construct an object of type ComputeImageAttributeTask
				 * @param rowNumber
				 */
				public ComputeGrayImageTask (int rowNumber) {
						this.rowNumber = rowNumber;
				}
		
				//@Override
				public void run () {
						//System.out.println ("Thread "+Thread.currentThread ().getId ()+"is running to compute gray!");
						// Go over all the pixels in the row and generate their attributes:
						int pixelIdx = rowNumber * IMG_WIDTH;
			
						for (int i = pixelIdx; i < (IMG_WIDTH + pixelIdx); i++)
								if (NUM_CHANNELS == 1)
										g[i] = abgr[NUM_CHANNELS * i + rIdx];
								else {
										// compute the gray component:
										int pix = NUM_CHANNELS * i;
										float R = (float) (0xff & abgr[pix + rIdx]);
										float G = (float) (0xff & abgr[pix + gIdx]);
										float B = (float) (0xff & abgr[pix + bIdx]);
										float gray = (0.2989f * R + 0.5870f * G + 0.1140f * B);
										g[i] = (byte) gray;
								}
						//System.out.println ("unique id "+i+" Computing row :"+rowNumber+" pixel:"+ (i-rowNumber)+" Computed:"+gray+" Stored:"+ (float) (0x0ff & g[i]));
						//System.out.println ("Thread "+Thread.currentThread ().getId ()+"is done to compute gray!");
				}
		}
	
		private class Worker extends Thread {

				public void run () {
						try {
								while (true) {
										Runnable task = taskQ.poll ();
										if (task == null)
												break;
										task.run ();
								}
						} finally {
						}
				}
		}

		//***********************************************
	
		/**
		 * Constructor get the image and process its attributes:
		 */
		public ImageAttribute (File imageFile) {

				//**************************************************************
				// read the image:
				BufferedImage img;
				try {
						if (!imageFile.isFile ())
								throw new RuntimeException (imageFile + " is not a file !");
						img = ImageIO.read (imageFile);
				}	catch (IOException e)	{
						throw new RuntimeException ("Could not open file : "+imageFile);
				}

				//**************************************************************
				// set its spectral information:
				//**************************************************************
				abgr = ((DataBufferByte) img.getRaster ().getDataBuffer ()).getData ();
				// set its dimensional information:
				IMG_HEIGHT = img.getHeight ();
				IMG_WIDTH  = img.getWidth ();
				NUM_CHANNELS = (short) ((abgr.length) / (IMG_WIDTH * IMG_HEIGHT));
				// r g b indices within the abgr index
				rIdx = (byte) ((NUM_CHANNELS == 1) ?  0 : ((NUM_CHANNELS == 3)? 2 : 3));
				gIdx = (byte) ((NUM_CHANNELS == 1) ?  0 : ((NUM_CHANNELS == 3)? 1 : 2));
				bIdx = (byte) ((NUM_CHANNELS == 1) ?  0 : ((NUM_CHANNELS == 3)? 0 : 1));

				// destroy img
				img = null;
				// enable the next piece of code when using kernel descriptor
		
				if (Param.isAttributeOn) {
						g = new byte[IMG_HEIGHT * IMG_WIDTH];
						gTemp = new byte[IMG_HEIGHT * IMG_WIDTH];
		
						// gradient info:
						gM = new float[IMG_HEIGHT * IMG_WIDTH];
						gOx = new float[IMG_HEIGHT * IMG_WIDTH];
						gOy = new float[IMG_HEIGHT * IMG_WIDTH];
						// shape info:
						lbp= new byte [IMG_HEIGHT * IMG_WIDTH];
						// standard deviation:
						stDev = new float[IMG_HEIGHT * IMG_WIDTH];

						//************************************
						// start computing other information:
						//************************************
						// Parallel processing related variables:
						// set the queue
						taskQ = new ConcurrentLinkedQueue<Runnable> ();
						// set the workers:
						Worker [] workers = new Worker[Param.NUM_THREADS];
						// create tasks and put them in the queue
						for (int i = 0; i < IMG_HEIGHT; i++)
								taskQ.add (new ComputeGrayImageTask (i));
						// fire !
						fireThreads (workers);
						// wait for the threads
						checkThreads (workers);
		
						// gaussian smmothing:
						for (int i = 0; i < IMG_HEIGHT; i++)
								taskQ.add (new SmoothGrayImageTask (i, true));
						// fire !
						fireThreads (workers);
						// wait for the threads
						checkThreads (workers);
						for (int i = 0; i < IMG_WIDTH; i++)
								taskQ.add (new SmoothGrayImageTask (i, false));
						// fire !
						fireThreads (workers);
						// wait for the threads
						checkThreads (workers);
		
						// other attributes:
						for (int i = 0; i < IMG_HEIGHT; i++)
								taskQ.add (new ComputeImageAttributeTask (i));
						// fire !
						fireThreads (workers);
						// wait for the threads
						checkThreads (workers);
		
						// deloitering:
						taskQ = null;
						workers = null;
						gTemp = null;
				}
		}
	
		/**
		 * Returns the gray image 
		 * @return an image which represent the gray image 
		 */
		public BufferedImage getGray () {
				BufferedImage img = new BufferedImage (IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
				int id = 0;
				for (int y = 0; y < IMG_HEIGHT; y++){
						for (int x = 0; x < IMG_WIDTH; x++) {
								byte val = g[id];
                int rgb = (0x00ff0000 & (val << 16)) | (0x0000ff00 & (val<<8)) | (0x000000ff & val);
                
                img.setRGB (x, y, rgb);
								id++;
						}
				}
				return img;
		}

		/**
		 * Clear the attributes to save memory if no more access to them is needed;
		 * @param pixelCnt the number of pixels processed at the time of call
		 */
		public void clearAttributes (int currPixelCntProc) {
				pixelCnt += currPixelCntProc;
				if (pixelCnt == g.length) {
						System.out.println ("Clearing attributes..");
						g = null;
						gM = null;
						gOx = null;
						gOy = null;
						lbp = null;
						stDev = null;
				}
		}

		/**
		 * @TODO find a good alternative to convert a byte array into an image
		 * @param val the byte
		 * @return the rgb value of the byte (its a replica of the same byte concatenated)
		 */
		int byte2rgb (byte val) {
				return ((0x00ff0000 & (val << 16)) | (0x0000ff00 & (val<<8)) | (0x000000ff & val));
		}
	
	
		int gm2rgb (float gm) {
				float val = (gm * Param.MAX_RGB)/ (Param.MAX_GM);
				return byte2rgb ((byte) val);
		}
	
		/**
		 * Normalized the std values into a value suitable for visualization
		 * @param std the value (it is always between 0 , maxStd)
		 * @return an integer representing the value in rgb
		 */
		int std2rgb (float std) {
				if (this.maxStd == 0.f)
						return byte2rgb ((byte) (Param.MAX_RGB /2));
		
				float val = (std * Param.MAX_RGB) / (this.maxStd);
				return byte2rgb ((byte) val);
		}
	
		/**
		 * Normalized the orientation values into a value suitable for visualization
		 * @param orientation the value (it is always between -1 , +1)
		 * @return an integer representing the value in rgb
		 */
		int orient2rgb (float orientation) {
				float val = (orientation + 1.f)/ 2.f;
		
				assert (val <= 1.0f);
		
				byte normVal = (byte) ( val * Param.MAX_RGB);
				return byte2rgb (normVal);
		}
	
	
		/**
		 * Get the standard deviation for the pixel with x y index
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the std of that pixel
		 */
		public float getStDev (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return stDev[idx];
		}
	
		/**
		 * Get the standard deviation for the pixel with 1d index
		 * @param idx the pixel 1d index
		 * @return the std of that pixel
		 */
		public float getStDev (int idx) {
				return stDev[idx];
		}

		/**
		 * Get the local binary pattern for the pixel with x y index
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the lbp of that pixel
		 */
		public byte getLBP (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return lbp[idx];
		}
	
		/**
		 * Get the local binary pattern for the pixel with 1d index
		 * @param idx the pixel 1d index
		 * @return the lbp of that pixel
		 */
		public byte getLBP (int idx) {
				return lbp[idx];
		}
	
		/**
		 * Get the gradient magnitude for the pixel with x y index
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradMag (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return gM[idx];
		}
	
		/**
		 * Get the gradient magnitude for the pixel with 1d index
		 * @param idx the pixel 1d index
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradMag (int idx) {
				return gM[idx];
		}
	
	
		/**
		 * Get the gradient orientation sin0 for the pixel with x y index
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradOriX (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return gOx[idx];
		}

		/**
		 * Get the gradient sin0 for the pixel with 1d index
		 * @param idx the pixel 1d index
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradOriX (int idx)
		{
				return gOx[idx];
		}
	
		/**
		 * Get the gradient orientation sin0 for the pixel with x y index
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradOriY (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return gOy[idx];
		}

		/**
		 * Get the gradient sin0 for the pixel with 1d index
		 * @param idx the pixel 1d index
		 * @return the gradient magnitude of that pixel
		 */
		public float getGradOriY (int idx) {
				return gOy[idx];
		}
	
		/**
		 * Get the RGB color from the abgr array
		 * @param x the pixel x location
		 * @param y the pixel y location
		 * @return the rgb of that pixel
		 */
		public int getRGB (int x, int y) {
				int idx = y * this.IMG_WIDTH + x;
				return
						((0x00ff0000 & (this.getR (idx) << 16)) |
						 (0x0000ff00 & (this.getG (idx) <<8)) |
						 (0x000000ff & this.getB (idx)) |
						 0xff000000) ;
		}
	
		/**
		 * Get the RGB color from the abgr array
		 * @param idx the pixel 1D index
		 * @return the rgb of that pixel
		 */
		public int getRGB (int idx) {
				return
						((0x00ff0000 & (this.getR (idx) << 16)) |
						 (0x0000ff00 & (this.getG (idx) <<8)) |
						 (0x000000ff & this.getB (idx)) |
						 0xff000000) ;
		}

		/**
		 * Get the Red component byte from the abgr array
		 * @param idx the pixel index
		 * @return the red compoent of that pixel
		 */
		public byte getR (int idx) {
				return abgr[NUM_CHANNELS * idx + rIdx];
		}
	
		/**
		 * Get the Green component byte from the abgr array
		 * @param idx the pixel index
		 * @return the green compoent of that pixel
		 */
		public byte getG (int idx) {
				return abgr[NUM_CHANNELS * idx + gIdx];
		}
	
		/**
		 * Get the Blue component byte from the abgr array
		 * @param idx the pixel index
		 * @return the blue compoent of that pixel
		 */
		public byte getB (int idx) {
				return abgr[NUM_CHANNELS * idx + bIdx];
		}
		/**
		 * launch the set of threads
		 * @param workers
		 */
		private void fireThreads (Worker [] workers) {
				// put the threads on fire
				for (int i = 0; i < Param.NUM_THREADS; i++) {
						workers[i] = new Worker ();
						try {
								workers[i].setPriority (Thread.currentThread ().getPriority () + 1);	
						} catch (Exception e)	{
						}
						workers[i].start ();
				}
		}

		/**
		 * Check if the threads have finished their work to continue:
		 * @param workers the set of threads
		 */
		private void checkThreads (Worker [] workers) {
				for (int i = 0; i < Param.NUM_THREADS; i++) {
						while (workers[i].isAlive ()) {
								try {
										workers[i].join ();
								}	catch (InterruptedException e) {
								}
						}
				}
		}
}
