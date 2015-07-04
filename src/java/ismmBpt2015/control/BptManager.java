package ismmBpt2015.control;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

import misc.ApplicationManager;
import misc.Bundle;
import misc.Config;
import misc.ImagePreview;
import misc.Util;

import ismmBpt2015.model.BinaryPartitionTree;
import ismmBpt2015.view.JBpt;

public class BptManager implements ApplicationManager, ActionListener {

		public static final String[] imageExtentions    = new String [] {"gif", "jpg", "jpeg", "png", "tif", "tiff", "bmp"};
		File imageFile = Config.getFile ("ImageFileName");
		String defaultValue = "data/images";

		// ========================================
		private JBpt jBpt;

		// ========================================
		public static final String actionOpen = "Open";

		// ========================================
		public static final List<String> fileActionsNames =
				Arrays.asList (actionOpen);

		// ========================================
		@SuppressWarnings ("unchecked")
		public static final Hashtable<String, Method> actionsMethod =
				Util.collectMethod (BptManager.class, fileActionsNames);

		public void  actionPerformed (ActionEvent e) {
				Util.actionPerformed (actionsMethod, e, this);
		}

		// ========================================
		BptController bptController;
		BinaryPartitionTree bpt;

		// ========================================
		public BptManager (BptController bptController, BinaryPartitionTree bpt, JBpt jBpt) {
				this.bptController = bptController;
				this.bpt = bpt;
				this.jBpt = jBpt;
		}

		// ========================================
		public void addMenuItem (JMenu... jMenu) {
				int idx = 0;
				Util.addMenuItem (fileActionsNames, this, jMenu[idx++]);
		}

		// ========================================
		public void addIconButtons (Container... containers) {
				Util.addIconButton (fileActionsNames, this, containers[0]);
		}

		// ========================================
		public void addActiveButtons (Hashtable<String, AbstractButton> buttons) {
		}

		// ========================================
		public void actionOpen () {
				File file = getChooseOpenFile ();
				if (file == null)
						return;
				(new Thread () { public void run () {
						bpt.load (file);
						bptController.reborn ();
				} }).start ();
		}

		// XXX put in VIEW package
		public File getChooseOpenFile () {
				JFileChooser jFileChooser = new JFileChooser (Config.getString ("ImageDirName", defaultValue));
				jFileChooser.setFileFilter (new FileNameExtensionFilter (Bundle.getLabel ("ImageFilter"), imageExtentions));
				JComponent preview = new ImagePreview (jFileChooser, 1024*1024);
				jFileChooser.setAccessory (preview);
				if (imageFile != null)
						jFileChooser.setSelectedFile (imageFile);
				if (jFileChooser.showOpenDialog (null) != JFileChooser.APPROVE_OPTION)
						return null;
				File file = jFileChooser.getSelectedFile ();
				Config.setFile ("ImageDirName", file.getParentFile ());
				Config.setFile ("ImageFileName", file);
				return file;
		}


		// ========================================
}
