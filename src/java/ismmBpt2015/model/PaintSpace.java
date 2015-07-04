package ismmBpt2015.model;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * The PaintSpace class provides a space for drawing 2d points, lines, rectangles, arcs,
 * ellipse and images over a 2d space. It is adapted from StdDraw.java with a concentraion on
 * the pain space itself with no GUI support
 * StdDraw.java is authored by:
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  
 *  See <a href="http://introcs.cs.princeton.edu/15inout">StdDraw</a> for more details
 * Each node represents a region of image with certain attributes
 * 
 * @author Abdullah Al-Dujaili
 * 
 */
public class PaintSpace {
		// pre-defined colors
    public  final static Color BLACK      = Color.BLACK;
    public  final static Color BLUE       = Color.BLUE;
    public  final static Color CYAN       = Color.CYAN;
    public  final static Color DARK_GRAY  = Color.DARK_GRAY;
    public  final static Color GRAY       = Color.GRAY;
    public  final static Color GREEN      = Color.GREEN;
    public  final static Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public  final static Color MAGENTA    = Color.MAGENTA;
    public  final static Color ORANGE     = Color.ORANGE;
    public  final static Color PINK       = Color.PINK;
    public  final static Color RED        = Color.RED;
    public  final static Color WHITE      = Color.WHITE;
    public  final static Color YELLOW     = Color.YELLOW;

    /**
     * Shade of blue used in Introduction to Programming in Java.
     * It is Pantone 300U. The RGB values are approximately (9, 90, 166).
     */
    public  final Color BOOK_BLUE       = new Color (  9,  90, 166);
    public  final Color BOOK_LIGHT_BLUE = new Color (103, 198, 243);

    /**
     * Shade of red used in Algorithms 4th edition.
     * It is Pantone 1805U. The RGB values are approximately (150, 35, 31).
     */
    public  final Color BOOK_RED = new Color (150, 35, 31);

    // default colors
    private static final Color DEFAULT_PEN_COLOR   = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    private static Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;

    // boundary of drawing canvas, 5% border
    private static final double BORDER = 0.05;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;
    
    // default font
    private static final Font DEFAULT_FONT = new Font ("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;

    // double buffered graphics
    private BufferedImage offscreenImage;
    private Graphics2D offscreen;
    
    private ImageIcon imIcon; 
    
    /**
     * Constructor
     */
    public PaintSpace () {
				init ();
    }
    /**
     * Set the window size to the default size 512-by-512 pixels.
     * This method must be called before any other commands.
     */
    public  void setCanvasSize () {
        setCanvasSize (DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Set the window size to w-by-h pixels.
     * This method must be called before any other commands.
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws a IllegalArgumentException if the width or height is 0 or negative
     */
    public void setCanvasSize (int w, int h) {
        if (w < 1 || h < 1)
						throw new IllegalArgumentException ("width and height must be positive");
        width = w;
        height = h;
        init ();
    }
    
    private void init () {
        offscreenImage = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
        //onscreenImage  = new BufferedImage (width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics ();
        //onscreen  = onscreenImage.createGraphics ();
        setXscale ();
        setYscale ();
        offscreen.setColor (DEFAULT_CLEAR_COLOR);
        offscreen.fillRect (0, 0, width, height);
        setPenColor ();
        setPenRadius ();
        setFont ();
        clear ();

        // add antialiasing
        RenderingHints hints = new RenderingHints (RenderingHints.KEY_ANTIALIASING,
																									 RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put (RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints (hints);
        
        imIcon = new ImageIcon (offscreenImage);
    }
	
	
	
    /*************************************************************************
     *  User and screen coordinate systems
     *************************************************************************/
		/**
		 * Set the x-scale to be the default (between 0.0 and 1.0).
		 */
		public  void setXscale () {
				setXscale (DEFAULT_XMIN, DEFAULT_XMAX);
		}

		/**
		 * Set the y-scale to be the default (between 0.0 and 1.0).
		 */
		public  void setYscale () {
				setYscale (DEFAULT_YMIN, DEFAULT_YMAX);
		}

		/**
		 * Set the x-scale (a 10% border is added to the values)
		 * @param min the minimum value of the x-scale
		 * @param max the maximum value of the x-scale
		 */
		public  void setXscale (double min, double max) {
				double size = max - min;
				xmin = min - BORDER * size;
				xmax = max + BORDER * size;
		}

		/**
		 * Set the y-scale (a 10% border is added to the values).
		 * @param min the minimum value of the y-scale
		 * @param max the maximum value of the y-scale
		 */
		public  void setYscale (double min, double max) {
				double size = max - min;
				ymin = min - BORDER * size;
				ymax = max + BORDER * size;
		}

		/**
		 * Set the x-scale and y-scale (a 10% border is added to the values)
		 * @param min the minimum value of the x- and y-scales
		 * @param max the maximum value of the x- and y-scales
		 */
		public  void setScale (double min, double max) {
				double size = max - min;
				xmin = min - BORDER * size;
				xmax = max + BORDER * size;
				ymin = min - BORDER * size;
				ymax = max + BORDER * size;
		}

		// helper functions that scale from user coordinates to screen coordinates and back
		private  double  scaleX (double x) { return width  * (x - xmin) / (xmax - xmin); }
		private  double  scaleY (double y) { return height * (ymax - y) / (ymax - ymin); }
		private  double factorX (double w) { return w * width  / Math.abs (xmax - xmin);  }
		private  double factorY (double h) { return h * height / Math.abs (ymax - ymin);  }
		public   double   userX (double x) { return xmin + x * (xmax - xmin) / width;    }
		public   double   userY (double y) { return ymax - y * (ymax - ymin) / height;   }


		/**
		 * Clear the screen to the default color (white).
		 */
		public void clear () {
				clear (DEFAULT_CLEAR_COLOR);
		}
		/**
		 * Clear the screen to the given color.
		 * @param color the Color to make the background
		 */
		public void clear (Color color) {
				offscreen.setColor (color);
				offscreen.fillRect (0, 0, width, height);
				offscreen.setColor (penColor);
		}

		/**
		 * Get the current pen radius.
		 */
		public  double getPenRadius () {
				return penRadius;
		}

		/**
		 * Set the pen size to the default (.002).
		 */
		public  void setPenRadius () {
				setPenRadius (DEFAULT_PEN_RADIUS);
		}

		/**
		 * Set the radius of the pen to the given size.
		 * @param r the radius of the pen
		 * @throws IllegalArgumentException if r is negative
		 */
		public  void setPenRadius (double r) {
				if (r < 0)
						throw new IllegalArgumentException ("pen radius must be nonnegative");
				penRadius = r;
				float scaledPenRadius = (float) (r * DEFAULT_SIZE);
				BasicStroke stroke = new BasicStroke (scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				// BasicStroke stroke = new BasicStroke (scaledPenRadius);
				offscreen.setStroke (stroke);
		}

		/**
		 * Get the current pen color.
		 */
		public  Color getPenColor () {
				return penColor;
		}

		/**
		 * Set the pen color to the default color (black).
		 */
		public  void setPenColor () {
				setPenColor (DEFAULT_PEN_COLOR);
		}

		/**
		 * Set the pen color to the given color. The available pen colors are
		 * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
		 * ORANGE, PINK, RED, WHITE, and YELLOW.
		 * @param color the Color to make the pen
		 */
		public  void setPenColor (Color color) {
				penColor = color;
				offscreen.setColor (penColor);
		}

		/**
		 * Set the pen color to the given RGB color.
		 * @param red the amount of red (between 0 and 255)
		 * @param green the amount of green (between 0 and 255)
		 * @param blue the amount of blue (between 0 and 255)
		 * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
		 */
		public  void setPenColor (int red, int green, int blue) {
				if (red   < 0 || red   >= 256)
						throw new IllegalArgumentException ("amount of red must be between 0 and 255");
				if (green < 0 || green >= 256)
						throw new IllegalArgumentException ("amount of red must be between 0 and 255");
				if (blue  < 0 || blue  >= 256)
						throw new IllegalArgumentException ("amount of red must be between 0 and 255");
				setPenColor (new Color (red, green, blue));
		}

		/**
		 * Get the current font.
		 */
		public  Font getFont () {
				return font;
		}

		/**
		 * Set the font to the default font (sans serif, 16 point).
		 */
		public  void setFont () {
				setFont (DEFAULT_FONT);
		}

		/**
		 * Set the font to the given value.
		 * @param f the font to make text
		 */
		public  void setFont (Font f) {
				font = f;
		}


    /*************************************************************************
     *  Drawing geometric shapes.
     *************************************************************************/

		/**
		 * Draw a line from (x0, y0) to (x1, y1).
		 * @param x0 the x-coordinate of the starting point
		 * @param y0 the y-coordinate of the starting point
		 * @param x1 the x-coordinate of the destination point
		 * @param y1 the y-coordinate of the destination point
		 */
		public  void line (double x0, double y0, double x1, double y1) {
				offscreen.draw (new Line2D.Double (scaleX (x0), scaleY (y0), scaleX (x1), scaleY (y1)));
         
		}

		/**
		 * Draw one pixel at (x, y).
		 * @param x the x-coordinate of the pixel
		 * @param y the y-coordinate of the pixel
		 */
		private void pixel (double x, double y) {
				offscreen.fillRect ((int) Math.round (scaleX (x)), (int) Math.round (scaleY (y)), 1, 1);
		}

		/**
		 * Draw a point at (x, y).
		 * @param x the x-coordinate of the point
		 * @param y the y-coordinate of the point
		 */
		public  void point (double x, double y) {
				double xs = scaleX (x);
				double ys = scaleY (y);
				double r = penRadius;
				float scaledPenRadius = (float) (r * DEFAULT_SIZE);

				if (scaledPenRadius <= 1)
						pixel (x, y);
				else
						offscreen.fill (new Ellipse2D.Double (xs - scaledPenRadius/2, ys - scaledPenRadius/2,
																									scaledPenRadius, scaledPenRadius));
		}

		/**
		 * Draw a circle of radius r, centered on (x, y).
		 * @param x the x-coordinate of the center of the circle
		 * @param y the y-coordinate of the center of the circle
		 * @param r the radius of the circle
		 * @throws IllegalArgumentException if the radius of the circle is negative
		 */
		public  void circle (double x, double y, double r) {
				if (r < 0)
						throw new IllegalArgumentException ("circle radius must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*r);
				double hs = factorY (2*r);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.draw (new Ellipse2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw filled circle of radius r, centered on (x, y).
		 * @param x the x-coordinate of the center of the circle
		 * @param y the y-coordinate of the center of the circle
		 * @param r the radius of the circle
		 * @throws IllegalArgumentException if the radius of the circle is negative
		 */
		public  void filledCircle (double x, double y, double r) {
				if (r < 0)
						throw new IllegalArgumentException ("circle radius must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*r);
				double hs = factorY (2*r);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.fill (new Ellipse2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}


		/**
		 * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
		 * @param x the x-coordinate of the center of the ellipse
		 * @param y the y-coordinate of the center of the ellipse
		 * @param semiMajorAxis is the semimajor axis of the ellipse
		 * @param semiMinorAxis is the semiminor axis of the ellipse
		 * @throws IllegalArgumentException if either of the axes are negative
		 */
		public void ellipse (double x, double y, double semiMajorAxis, double semiMinorAxis) {
				if (semiMajorAxis < 0)
						throw new IllegalArgumentException ("ellipse semimajor axis must be nonnegative");
				if (semiMinorAxis < 0)
						throw new IllegalArgumentException ("ellipse semiminor axis must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*semiMajorAxis);
				double hs = factorY (2*semiMinorAxis);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.draw (new Ellipse2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
		 * @param x the x-coordinate of the center of the ellipse
		 * @param y the y-coordinate of the center of the ellipse
		 * @param semiMajorAxis is the semimajor axis of the ellipse
		 * @param semiMinorAxis is the semiminor axis of the ellipse
		 * @throws IllegalArgumentException if either of the axes are negative
		 */
		public void filledEllipse (double x, double y, double semiMajorAxis, double semiMinorAxis) {
				if (semiMajorAxis < 0)
						throw new IllegalArgumentException ("ellipse semimajor axis must be nonnegative");
				if (semiMinorAxis < 0)
						throw new IllegalArgumentException ("ellipse semiminor axis must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*semiMajorAxis);
				double hs = factorY (2*semiMinorAxis);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.fill (new Ellipse2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw an arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
		 * @param x the x-coordinate of the center of the circle
		 * @param y the y-coordinate of the center of the circle
		 * @param r the radius of the circle
		 * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
		 * @param angle2 the angle at the end of the arc. For example, if
		 *        you want a 90 degree arc, then angle2 should be angle1 + 90.
		 * @throws IllegalArgumentException if the radius of the circle is negative
		 */
		public void arc (double x, double y, double r, double angle1, double angle2) {
				if (r < 0)
						throw new IllegalArgumentException ("arc radius must be nonnegative");
				while (angle2 < angle1)
						angle2 += 360;
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*r);
				double hs = factorY (2*r);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.draw (new Arc2D.Double (xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
		}

		/**
		 * Draw a square of side length 2r, centered on (x, y).
		 * @param x the x-coordinate of the center of the square
		 * @param y the y-coordinate of the center of the square
		 * @param r radius is half the length of any side of the square
		 * @throws IllegalArgumentException if r is negative
		 */
		public void square (double x, double y, double r) {
				if (r < 0)
						throw new IllegalArgumentException ("square side length must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*r);
				double hs = factorY (2*r);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.draw (new Rectangle2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw a filled square of side length 2r, centered on (x, y).
		 * @param x the x-coordinate of the center of the square
		 * @param y the y-coordinate of the center of the square
		 * @param r radius is half the length of any side of the square
		 * @throws IllegalArgumentException if r is negative
		 */
		public void filledSquare (double x, double y, double r) {
				if (r < 0)
						throw new IllegalArgumentException ("square side length must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*r);
				double hs = factorY (2*r);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.fill (new Rectangle2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}


		/**
		 * Draw a rectangle of given half width and half height, centered on (x, y).
		 * @param x the x-coordinate of the center of the rectangle
		 * @param y the y-coordinate of the center of the rectangle
		 * @param halfWidth is half the width of the rectangle
		 * @param halfHeight is half the height of the rectangle
		 * @throws IllegalArgumentException if halfWidth or halfHeight is negative
		 */
		public void rectangle (double x, double y, double halfWidth, double halfHeight) {
				if (halfWidth  < 0)
						throw new IllegalArgumentException ("half width must be nonnegative");
				if (halfHeight < 0)
						throw new IllegalArgumentException ("half height must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*halfWidth);
				double hs = factorY (2*halfHeight);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.draw (new Rectangle2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw a filled rectangle of given half width and half height, centered on (x, y).
		 * @param x the x-coordinate of the center of the rectangle
		 * @param y the y-coordinate of the center of the rectangle
		 * @param halfWidth is half the width of the rectangle
		 * @param halfHeight is half the height of the rectangle
		 * @throws IllegalArgumentException if halfWidth or halfHeight is negative
		 */
		public void filledRectangle (double x, double y, double halfWidth, double halfHeight) {
				if (halfWidth  < 0)
						throw new IllegalArgumentException ("half width must be nonnegative");
				if (halfHeight < 0)
						throw new IllegalArgumentException ("half height must be nonnegative");
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (2*halfWidth);
				double hs = factorY (2*halfHeight);
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.fill (new Rectangle2D.Double (xs - ws/2, ys - hs/2, ws, hs));
		}

		/**
		 * Draw a polygon with the given (x[i], y[i]) coordinates.
		 * @param x an array of all the x-coordindates of the polygon
		 * @param y an array of all the y-coordindates of the polygon
		 */
		public void polygon (double[] x, double[] y) {
				int N = x.length;
				GeneralPath path = new GeneralPath ();
				path.moveTo ((float) scaleX (x[0]), (float) scaleY (y[0]));
				for (int i = 0; i < N; i++)
						path.lineTo ((float) scaleX (x[i]), (float) scaleY (y[i]));
				path.closePath ();
				offscreen.draw (path);
		}

		/**
		 * Draw a filled polygon with the given (x[i], y[i]) coordinates.
		 * @param x an array of all the x-coordindates of the polygon
		 * @param y an array of all the y-coordindates of the polygon
		 */
		public void filledPolygon (double[] x, double[] y) {
				int N = x.length;
				GeneralPath path = new GeneralPath ();
				path.moveTo ((float) scaleX (x[0]), (float) scaleY (y[0]));
				for (int i = 0; i < N; i++)
						path.lineTo ((float) scaleX (x[i]), (float) scaleY (y[i]));
				path.closePath ();
				offscreen.fill (path);
		}

    /*************************************************************************
     *  Drawing images.
     *************************************************************************/

		// get an image from the given filename
		private Image getImage (String filename) {
				// to read from file
				ImageIcon icon = new ImageIcon (filename);

				// try to read from URL
				if ((icon == null) || (icon.getImageLoadStatus () != MediaTracker.COMPLETE)) {
						try {
								URL url = new URL (filename);
								icon = new ImageIcon (url);
						} catch (Exception e) {
								// not a url
						}
				}
				// in case file is inside a .jar
				if ((icon == null) || (icon.getImageLoadStatus () != MediaTracker.COMPLETE)) {
						URL url = PaintSpace.class.getResource (filename);
						if (url == null)
								throw new IllegalArgumentException ("image " + filename + " not found");
						icon = new ImageIcon (url);
				}
				return icon.getImage ();
		}

		/**
		 * Draw picture (gif, jpg, or png) centered on (x, y).
		 * @param x the center x-coordinate of the image
		 * @param y the center y-coordinate of the image
		 * @param s the name of the image/picture, e.g., "ball.gif"
		 * @throws IllegalArgumentException if the image is corrupt
		 */
		public void picture (double x, double y, String s) {
				Image image = getImage (s);
				double xs = scaleX (x);
				double ys = scaleY (y);
				int ws = image.getWidth (null);
				int hs = image.getHeight (null);
				if (ws < 0 || hs < 0)
						throw new IllegalArgumentException ("image " + s + " is corrupt");
				offscreen.drawImage (image, (int) Math.round (xs - ws/2.0), (int) Math.round (ys - hs/2.0), null);
		}

		/**
		 * Draw picture (gif, jpg, or png) centered on (x, y),
		 * rotated given number of degrees
		 * @param x the center x-coordinate of the image
		 * @param y the center y-coordinate of the image
		 * @param s the name of the image/picture, e.g., "ball.gif"
		 * @param degrees is the number of degrees to rotate counterclockwise
		 * @throws IllegalArgumentException if the image is corrupt
		 */
		public void picture (double x, double y, String s, double degrees) {
				Image image = getImage (s);
				double xs = scaleX (x);
				double ys = scaleY (y);
				int ws = image.getWidth (null);
				int hs = image.getHeight (null);
				if (ws < 0 || hs < 0)
						throw new IllegalArgumentException ("image " + s + " is corrupt");
				offscreen.rotate (Math.toRadians (-degrees), xs, ys);
				offscreen.drawImage (image, (int) Math.round (xs - ws/2.0), (int) Math.round (ys - hs/2.0), null);
				offscreen.rotate (Math.toRadians (+degrees), xs, ys);
		}

		/**
		 * Draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h.
		 * @param x the center x coordinate of the image
		 * @param y the center y coordinate of the image
		 * @param s the name of the image/picture, e.g., "ball.gif"
		 * @param w the width of the image
		 * @param h the height of the image
		 * @throws IllegalArgumentException if the width height are negative
		 * @throws IllegalArgumentException if the image is corrupt
		 */
		public void picture (double x, double y, String s, double w, double h) {
				Image image = getImage (s);
				double xs = scaleX (x);
				double ys = scaleY (y);
				if (w < 0)
						throw new IllegalArgumentException ("width is negative: " + w);
				if (h < 0)
						throw new IllegalArgumentException ("height is negative: " + h);
				double ws = factorX (w);
				double hs = factorY (h);
				if (ws < 0 || hs < 0)
						throw new IllegalArgumentException ("image " + s + " is corrupt");
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else
						offscreen.drawImage (image, (int) Math.round (xs - ws/2.0),
																 (int) Math.round (ys - hs/2.0),
																 (int) Math.round (ws),
																 (int) Math.round (hs), null);
		}


		/**
		 * Draw picture (gif, jpg, or png) centered on (x, y), rotated
		 * given number of degrees, rescaled to w-by-h.
		 * @param x the center x-coordinate of the image
		 * @param y the center y-coordinate of the image
		 * @param s the name of the image/picture, e.g., "ball.gif"
		 * @param w the width of the image
		 * @param h the height of the image
		 * @param degrees is the number of degrees to rotate counterclockwise
		 * @throws IllegalArgumentException if the image is corrupt
		 */
		public void picture (double x, double y, String s, double w, double h, double degrees) {
				Image image = getImage (s);
				double xs = scaleX (x);
				double ys = scaleY (y);
				double ws = factorX (w);
				double hs = factorY (h);
				if (ws < 0 || hs < 0)
						throw new IllegalArgumentException ("image " + s + " is corrupt");
				if (ws <= 1 && hs <= 1)
						pixel (x, y);
				else { // XXX felix
						offscreen.rotate (Math.toRadians (-degrees), xs, ys);
						offscreen.drawImage (image, (int) Math.round (xs - ws/2.0),
																 (int) Math.round (ys - hs/2.0),
																 (int) Math.round (ws),
																 (int) Math.round (hs), null);
						offscreen.rotate (Math.toRadians (+degrees), xs, ys);
				}
		}

    /*************************************************************************
     *  Drawing text.
     *************************************************************************/

		/**
		 * Write the given text string in the current font, centered on (x, y).
		 * @param x the center x-coordinate of the text
		 * @param y the center y-coordinate of the text
		 * @param s the text
		 */
		public void text (double x, double y, String s) {
				offscreen.setFont (font);
				FontMetrics metrics = offscreen.getFontMetrics ();
				double xs = scaleX (x);
				double ys = scaleY (y);
				int ws = metrics.stringWidth (s);
				int hs = metrics.getDescent ();
				offscreen.drawString (s, (float) (xs - ws/2.0), (float) (ys + hs));
		}

		/**
		 * Write the given text string in the current font, centered on (x, y) and
		 * rotated by the specified number of degrees  
		 * @param x the center x-coordinate of the text
		 * @param y the center y-coordinate of the text
		 * @param s the text
		 * @param degrees is the number of degrees to rotate counterclockwise
		 */
		public void text (double x, double y, String s, double degrees) {
				double xs = scaleX (x);
				double ys = scaleY (y);
				offscreen.rotate (Math.toRadians (-degrees), xs, ys);
				text (x, y, s);
				offscreen.rotate (Math.toRadians (+degrees), xs, ys);
		}

		/**
		 * Write the given text string in the current font, left-aligned at (x, y).
		 * @param x the x-coordinate of the text
		 * @param y the y-coordinate of the text
		 * @param s the text
		 */
		public void textLeft (double x, double y, String s) {
				offscreen.setFont (font);
				FontMetrics metrics = offscreen.getFontMetrics ();
				double xs = scaleX (x);
				double ys = scaleY (y);
				int hs = metrics.getDescent ();
				offscreen.drawString (s, (float) (xs), (float) (ys + hs));
		}

		/**
		 * Write the given text string in the current font, right-aligned at (x, y).
		 * @param x the x-coordinate of the text
		 * @param y the y-coordinate of the text
		 * @param s the text
		 */
		public  void textRight (double x, double y, String s) {
				offscreen.setFont (font);
				FontMetrics metrics = offscreen.getFontMetrics ();
				double xs = scaleX (x);
				double ys = scaleY (y);
				int ws = metrics.stringWidth (s);
				int hs = metrics.getDescent ();
				offscreen.drawString (s, (float) (xs - ws), (float) (ys + hs));
        
		}

    /*************************************************************************
     *  Save drawing to a file.
     *************************************************************************/

		/**
		 * Save onscreen image to file - suffix must be png, jpg, or gif.
		 * @param filename the name of the file with one of the required suffixes
		 */
		public void save (String filename) {
				File file = new File (filename);
				String suffix = filename.substring (filename.lastIndexOf ('.') + 1);

				// png files
				if (suffix.toLowerCase ().equals ("png")) {
						try {
								ImageIO.write (offscreenImage, suffix, file);
						} catch (IOException e) {
								e.printStackTrace ();
						}
				} else if (suffix.toLowerCase ().equals ("jpg")) {
						// need to change from ARGB to RGB for jpeg
						// reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
						WritableRaster raster = offscreenImage.getRaster ();
						WritableRaster newRaster;
						newRaster = raster.createWritableChild (0, 0, width, height, 0, 0, new int[] {0, 1, 2});
						DirectColorModel cm = (DirectColorModel) offscreenImage.getColorModel ();
						DirectColorModel newCM = new DirectColorModel (cm.getPixelSize (),
                                                           cm.getRedMask (),
                                                           cm.getGreenMask (),
                                                           cm.getBlueMask ());
						BufferedImage rgbBuffer = new BufferedImage (newCM, newRaster, false,  null);
						try {
								ImageIO.write (rgbBuffer, suffix, file);
						} catch (IOException e) {
								e.printStackTrace ();
						}
				} else
						System.out.println ("Invalid image file type: " + suffix);
		}
     
		/**
		 * Get the image icon for the drawing space
		 * @return image icon of the drawing space
		 */
		public ImageIcon getComponent () {
				return imIcon;
		}
}
