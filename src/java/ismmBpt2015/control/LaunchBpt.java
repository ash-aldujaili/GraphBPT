package ismmBpt2015.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import javax.swing.SwingUtilities;

import misc.Bundle;
import misc.Config;
import misc.Util;

import ismmBpt2015.model.BinaryPartitionTree;
import ismmBpt2015.model.Param;

public class LaunchBpt {

		public static void usage () {
				System.err.println ("Usage: java -jar IsmmBpt2015.jar [--help] [-D dataDir] [image]");
				System.exit (0);
		}
		public static final List<String> knownedOpt = Arrays.asList ("D");
		public static final List<String> knownedDoubleOpt = Arrays.asList ("help");

		public static void main (String[] args) {
				ArrayList<String> argsList = new ArrayList<String> ();  
				Hashtable<String, String> optsList = new Hashtable<String, String> ();
				ArrayList<String> doubleOptsList = new ArrayList<String> ();
				for (int i = 0; i < args.length; i++) {
						switch (args[i].charAt (0)) {
						case '-':
								if (args[i].charAt(1) == '-')
										doubleOptsList.add (args[i].substring (2));
								else {
										if (i == args.length-1)
												usage ();
										optsList.put (args[i++].substring (1), args[i]);
								}
								break;
						default:
								argsList.add(args[i]);
						}
				}
				for (String opt : optsList.keySet ())
						if (!knownedOpt.contains (opt))
								usage ();
				for (String opt : doubleOptsList)
						if (!knownedDoubleOpt.contains (opt))
								usage ();
				if (doubleOptsList.contains ("help") || argsList.size () > 1)
						usage ();

				String dataDirName = optsList.get ("D");
				if (dataDirName != null)
						Param.dataDirName = dataDirName;

				Util.setPWD (LaunchBpt.class);
				Config.load ("Bpt");
				final BinaryPartitionTree bpt = new BinaryPartitionTree ();
				if (argsList.size () != 0) {
						Bundle.setLocale (new Locale ("en", "US", ""));
						Bundle.load ("Help");
						Bundle.load ("ToolBar");
						Bundle.load ("Controller");
						Bundle.load ("Bpt");
						File file = new File (argsList.get (0));
						if (! file.exists ())
								usage ();
						bpt.createDirs ();
						bpt.load (file);
				} else {
						Bundle.load ("Help");
						Bundle.load ("ToolBar");
						Bundle.load ("Controller");
						Bundle.load ("Bpt");
						SwingUtilities.invokeLater (new Runnable () {
										public void run () {
												bpt.createDirs ();
												new BptController (bpt);
										}
								});
				}
		}
}
