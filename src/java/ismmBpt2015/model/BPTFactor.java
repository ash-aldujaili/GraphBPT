package ismmBpt2015.model;
/**
 * This class implements a factor (potential function) over a set of random variables
 * The current implementation computes singleton as well as pairwise factors over the nodes of the BPT.
 * A potential function is a mapping from the joint domain of a set of random variables
 * to a nonnegative real number. It encodes the likelihood of a given assignment to a given set 
 * of random variables
 * Currently there are methods that are calculting these factors inherently within the bptnode class
 * @author Abdullah Al-Dujaili
 *
 */
public class BPTFactor {
	
		private final int [] var; // variable labels
		private final int [] card;// variable cardinality
		private final float [] val; // potentials
	
		/**
		 * Constructs a singleton factor from a BPT node
		 * @param x
		 */
		public BPTFactor (BPTNode x) {
				// set var
				var = new int[1];
				var[0] = x.id ();
				// set cardinality
				card = new int[1];
				card[0] = Param.LABEL_CARD; //x.card ();
				// set value
				val = new float[card[0]];
				for (int i = 0; i < val.length; i++)
						val[i] = 2.f; //x.getPotential (i);
		}

		/**
		 * Construct a pairwise factor from two BPT nodes (ideally they should parent-child nodes)
		 * @param pparent node
		 * @param c child node
		 */
		public BPTFactor (BPTNode p, BPTNode c) {
				// set var
				var = new int[2];
				var[0] = p.id ();
				var[1] = c.id ();
				// set cardinaltiy
				card = new int[2];
				card[0] = Param.LABEL_CARD;// p.card ();
				card[1] = Param.LABEL_CARD;//c.card ();
				// set value:
				val = new float[card[0] * card[1]];
				int idx = 0;
				for (int i = 0; i < card[1]; i ++)
						for (int j = 0; j < card[0]; j++)
								val[idx++]= 0;
		}
	
		/**
		 * 
		 */
		public String toString () {
				// print the number of variables followed by variable indices
				String s = "\n" + String.valueOf (var.length) + "\n" + String.valueOf (var[0]);
				for (int i = 1; i < var.length; i++)
						s = s + " " + String.valueOf (var[i]);
				// print the variables cardinality
				s = s + "\n" + String.valueOf (card[0]);
				for (int i = 1; i < var.length; i++)
						s = s + " " + String.valueOf (card[i]);
				// print the value
				for (int i = 0; i < val.length; i++)
						s = s + "\n" + String.valueOf (i) + " " + String.valueOf (val[i]);
				return s;
		}
}
