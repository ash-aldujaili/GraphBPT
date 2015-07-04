package ismmBpt2015.model;
/**
 * This class implements the features dealing with a bounding box
 * a boundinb box is defined with a 4 coordinates information that are
 * xMin, xMax, yMin, yMax
 * @author Abdullah Al-Dujaili
 *
 */
public class BoundingBox {
	
		final private int xMin, xMax, yMin, yMax;
	
		/**
		 * Constructs a bounding box from its min,max coordinates:
		 * @param xMin minimum x coordinate of the box
		 * @param xMax maximum x coordinate of the box
		 * @param yMin minimum y coordinate of the box
		 * @param yMax maximum y coordinate of the box
		 */
		public BoundingBox (int xMin, int xMax, int yMin, int yMax) {
				this.xMin = xMin;
				this.xMax = xMax;
				this.yMin = yMin;
				this.yMax = yMax;
		}
	
		/**
		 * Constructs a bounding box from its min,max coordinates:
		 * @param BB array representation of boudning box with the coordinates
		 * [xMin, xMax, yMin, yMax]
		 */
		public BoundingBox (int [] BB) {
				if ( BB[1] <= BB[0] || BB[3] <= BB[2])
						throw new IllegalArgumentException ("Inappropriate bounding box corrdinate");
				this.xMin = BB[0];
				this.xMax = BB[1];
				this.yMin = BB[2];
				this.yMax = BB[3];
		}
	
		/**
		 * Constructs a bounding box from a pixel:
		 * @param x x loc of the pixel
		 * @param y y loc of the pixel
		 */
		public BoundingBox (int x, int y) {
				this.xMax = x + 1;
				this.xMin = x;
				this.yMax = y + 1;
				this.yMin = y;
		}
	
		/**
		 * Constructs a bounding box from the union/intersection of 2 bounding boxes:
		 * @param a Bounding box a
		 * @param b Bounding box b
		 * @param isUnion specifies whether the new bounding box is union of the the 2 (true) or it's the intersection (false)
		 * if there is no intersection the bounding box will be of the parameters 0 0 0 0
		 */
		public BoundingBox (BoundingBox a, BoundingBox b, boolean isUnion) {
				if (isUnion) {
						this.xMax = Math.max (a.xMax, b.xMax);
						this.xMin = Math.min (a.xMin, b.xMin);
						this.yMax = Math.max (a.yMax, b.yMax);
						this.yMin = Math.min (a.yMin, b.yMin);
				} else {
						int xMaxTemp = Math.min (a.xMax, b.xMax);
						int xMinTemp = Math.max (a.xMin, b.xMin);
						int yMaxTemp = Math.min (a.yMax, b.yMax);
						int yMinTemp = Math.max (a.yMin, b.yMin);
			
						if (xMaxTemp > xMinTemp) {
								this.xMax = xMaxTemp;
								this.xMin = xMinTemp;
						} else {
								this.xMax = 0;
								this.xMin = 0;
						}
						if (yMaxTemp > yMinTemp) {
								this.yMax = yMaxTemp;
								this.yMin = yMinTemp;
						} else {
								this.yMax = 0;
								this.yMin = 0;
						}
				}
		}

	
		/**
		 * Get the area of the bounding box
		 * @return returns the area of the boudning box
		 */
		public int getArea () {
				return ((xMax - xMin) * (yMax - yMin));
		}
	
		/**
		 * Get the area of the union of two bounding box
		 * @param a Bounding box a
		 * @param b Bounding box b
		 * @return returns the area of the boudning box a union b
		 */
		public static int getAreaUnion (BoundingBox a, BoundingBox b) {
				BoundingBox c = new BoundingBox (a, b, true);
				return c.getArea ();
		}

		/**
		 * Get the area of the intersection of two bounding box
		 * @param a Bounding box a
		 * @param b Bounding box b
		 * @return returns the area of the boudning box a intersect b
		 */
		public static int getAreaIntersect (BoundingBox a, BoundingBox b) {
				BoundingBox c = new BoundingBox (a,b, false);
				return c.getArea ();
		}

		/**
		 * Get the xMin
		 * @returns the minimum x location of the bounding box
		 */
		public int xMin () {
				return this.xMin;
		}
	
		/**
		 * Get the xMax
		 * @returns the maximum x location of the bounding box
		 */
		public int xMax () {
				return this.xMax;
		}
	
		/**
		 * Get the yMin
		 * @returns the minimum y location of the bounding box
		 */
		public int yMin () {
				return this.yMin;
		}
	
		/**
		 * Get the yMax
		 * @returns the maximum y location of the bounding box
		 */
		public int yMax () {
				return this.yMax;
		}
	
	
		/**
		 * Get the height of the bounding box
		 * @return the height of the bounding box (yMax-yMin)
		 */
		public int getHeight () {
				return (this.yMax - this.yMin);
		}

		/**
		 * Get the width of the bounding box
		 * @return the width of the bounding box (yMax-yMin)
		 */
		public int getWidth () {
				return (this.xMax - this.xMin);
		}
	
		/**
		 * Retrun a textual description of the bounding box
		 * @return the width of the bounding box (yMax-yMin)
		 */
		public String toString () {
				return "xMin:"+xMin+"\nxMax:"+xMax+"\nyMin:"+yMin+"\nyMax:"+yMax+"\nArea:"+this.getArea ();
		}
	
		/**
		 * Get the coordinates of the bounding box
		 * @return an int array
		 * of length 4 representing minimum and maximum of x coordinate followed by y respectively.
		 * [xstart, xlast+1, ystart, ylast+1] for the bounding box
		 */
		public int[] getBBxy () {
				int [] BB = new int[4];
		
				BB[0]= xMin;
				BB[1]= xMax;
				BB[2]= yMin;
				BB[3]= yMax;
		
				return BB;
		}
}
