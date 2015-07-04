package ismmBpt2015.model;
import com.jmatio.types.MLNumericArray;

/**
 * This class stores a parameterized MxN numeric matrix and implements related operations
 * the class also implements a set of 1D numeric array operations like dot product, minus, plus
 * @author Abdullah Al-Dujaili
 * @TODO : Due to the limited features of generics with Number, the class here is only done with float,
 * next versions hopefully extend that.
 */
public class Matrix {

		public final int M,N;
		private final float[] mat;
	
		/**
		 * Constructs an empty matrix
		 * @param m number of rows
		 * @param n number of cols
		 * @SuppressWarnings ("unchecked")
		 */
		//@SuppressWarnings ("unchecked")
		public Matrix (int M, int N) {
				this.M = M;
				this.N = N;
				mat = new float[M*N];
		}
	
		/**
		 * Constructs a matrix from MLNumericArray<Float> class of (JMATIO)
		 * @param args
		 */
		//@SuppressWarnings ("unchecked")
		public Matrix (MLNumericArray<Float> mlArray) {
				M = mlArray.getM ();
				N = mlArray.getN ();
				mat = new float[M * N];
		
				for (int m = 0; m < M; m++)
						for (int n = 0; n < N; n++) {
								Number temp = mlArray.get (m, n);
								mat[m * N + n] = temp.floatValue ();
						}
		}
	
		/**
		 * Check the matrix Number instance usefu
		 */
		/**
		 * Get the mth row as an array:
		 * @param args
		 */
		//@SuppressWarnings ("unchecked")
		public float[] getRowAsArray (int m) {
				if (m < 0 || m > (M-1))
						throw new IllegalArgumentException ("Invalid row number!");

				float [] row = new float[N];
		
				for (int n = 0; n < N; n++)
						row[n] = mat[m * N + n];
				return row;
		}
	
		/**
		 * Get the nth column as an array:
		 * @param args
		 */
		//@SuppressWarnings ("unchecked")
		public float[] getColAsArray (int n) {
				if (n < 0 || n > (N-1))
						throw new IllegalArgumentException ("Invalid column number!");
				float [] col = new float[M];
				for (int m = 0; m < M; m++)
						col[m] = mat[m * N + n];
				return col;
		}

		/**
		 * Get the (m,n) element within the matrix
		 * @param m the row index
		 * @param n the column index
		 */
		public float getElement (int m, int n) {
				if (m < 0 || m > (M-1))
						throw new IllegalArgumentException ("Invalid row number!");
				if (n < 0 || n > (N-1))
						throw new IllegalArgumentException ("Invalid column number!");
				return mat[m * N + n];
		}
	
		/**
		 * Get the (idx) element within the matrix
		 * @param idx the 1d index
		 */
		public float getElement (int idx) {
				//if (idx < 0 || idx > (N * M-1)) throw new IllegalArgumentException ("Invalid number!");
				return mat[idx];
		}
	
		/**
		 * A string representation of the matrix object
		 */
		public String toString () {
				String st = "";
				for (int m = 0; m < M; m++) {
						for (int n = 0; n < N; n++)
								st += (String.valueOf (mat[m * N + n]))+"\t";
						st += "\n";
				}
				return st;
		}
	
		//************************************************************************************************
		//  @TODO : create a better overloaded operation
		//************************************************************************************************
		/**
		 * Implements a dot product between two numeric arrays of the same length for different numeric types
		 * @param a array a
		 * @param b array b
		 * @return dot (a,b)
		 */
		public static float dot (float[] a, float[] b) {
				if (a.length != b.length)
						throw new IllegalArgumentException ("Vector length mismatch! ");
				float val =0.f;
				for (int i = 0; i < a.length; i++)
						val += a[i] * b[i];
				return val;  
		}
	
		///////////////////////PLUS///////////////////////////////
		/**
		 * Implements a plus between two numeric arrays of the same length for different numeric types
		 * @param a array a
		 * @param b array b
		 * @return a+b
		 */
		public static float[] plus (float[] a, float[] b) {
				if (a.length != b.length)
						throw new IllegalArgumentException ("Vector length mismatch! ");
				float[] val = new float[a.length];
				for (int i = 0; i < a.length; i++)
						val[i] = a[i] + b[i];
				return val;  
		}

		//////////////////////////////////////minus/////////////////////////////////
		/**
		 * Implements a plus between two numeric arrays of the same length for different numeric types
		 * @param a array a
		 * @param b array b
		 * @return a+b
		 */
		public static float[] minus (float[] a, float[] b) {
				if (a.length != b.length)
						throw new IllegalArgumentException ("Vector length mismatch! ");
				float[] val = new float[a.length];
				for (int i = 0; i < a.length; i++)
						val[i] = a[i] - b[i];
				return val;  
		}

		/**
		 * Print an array of floats:
		 */
		public static void printArray (float [] a) {
				for (int i = 0; i < a.length; i++)
						System.out.println (a[i]);
		}
}
