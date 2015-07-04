package ismmBpt2015.view;
/*************************************************************************
 *  Compilation:  javac GUI.java
 *  Execution:    java GUI
 *
 *  GUI for displaying BPT.
 *
 *************************************************************************/
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;

import misc.ProgressStatePanel;
import misc.Util;

import ismmBpt2015.model.BinaryPartitionTree;
import ismmBpt2015.model.Param;

/**
 * This GUI shows the image as we traverse through the tree according to a specified pruning 
 * value
 * 
 * @author Abdullah Al-Dujaili
 * 
 */
@SuppressWarnings ("serial")
public class JBpt extends JPanel implements ChangeListener {
		// GUI related variables	
		private JLabel imageLabel = new JLabel (); 
		// BPT related variables
		private BinaryPartitionTree bpt;
		private int level;
		private final int LEVEL_MIN = 0;
		private int LEVEL_MAX;// = Integer.MAX_VALUE;
		private JSlider bptLevel;
    ProgressStatePanel progressStatePanel;

		public JBpt (BinaryPartitionTree bpt) {
				super (new BorderLayout ());
				this.bpt = bpt;
				progressStatePanel = new ProgressStatePanel (bpt.progressState);
				bptLevel = new JSlider (JSlider.HORIZONTAL);//, LEVEL_MIN, LEVEL_MAX, LEVEL_INIT);
				imageLabel.setSize (100, 100);
				add (Util.getJScrollPane (imageLabel), BorderLayout.CENTER);
				add (progressStatePanel, BorderLayout.PAGE_START);
				add (bptLevel, BorderLayout.PAGE_END);
				bptLevel.addChangeListener (this);
				bpt.addUpdateObserver (this, "Progress", "Empty", "Image");
				setMinimumSize (new Dimension (400, 400));
		}
	
		public void updateEmpty () {
				imageLabel.setIcon (null);
				//imageLabel.setSize (new Dimension (400, 400));
		}

		public void updateImage () {
				LEVEL_MAX = Param.isGravityOn ? Param.BPT_LEVEL : bpt.getHeight ();
				ImageIcon icon = bpt.getImageIcon (LEVEL_MIN);
				imageLabel.setIcon (icon);
				imageLabel.invalidate ();
				imageLabel.validate ();
				imageLabel.repaint ();

				bptLevel.setMinimum (LEVEL_MIN);
				bptLevel.setMaximum (LEVEL_MAX);
				bptLevel.setValue (LEVEL_MIN);
		}
	
    public void updateProgress () {
				progressStatePanel.updateProgress ();
    }

		public void stateChanged (ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				level = source.getValue ();
				if (bpt != null)
						imageLabel.setIcon (bpt.getImageIcon (level));
				else
						System.out.println ("bpt is null");
		}
}
