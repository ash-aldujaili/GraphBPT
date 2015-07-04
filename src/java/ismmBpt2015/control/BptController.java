package ismmBpt2015.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import misc.Bundle;
import misc.Config;
import misc.Controller;
import misc.HelpManager;
import misc.MultiToolBarBorderLayout;
import misc.ToolBarManager;
import misc.Util;


import ismmBpt2015.model.BinaryPartitionTree;
import ismmBpt2015.view.JBpt;
import ismmBpt2015.view.JBptMenuBar;
import ismmBpt2015.view.JBptToolBar;

public class BptController extends Controller<BinaryPartitionTree> {

    // ========================================
    BinaryPartitionTree bpt;

    JBpt jBpt;

    JBptMenuBar jBptMenuBar;
    JBptToolBar jBptToolBar;

    BptManager bptManager;
    HelpManager helpManager;
    ToolBarManager toolBarManager;
    
    private Frame frame;

    // ========================================
    public BptController (BinaryPartitionTree bpt) {
				super (bpt);
    }

    // ========================================
    protected void createModel (BinaryPartitionTree bpt) {
				this.bpt = bpt;
				// frames.addUpdateObserver (this, "Name");
    }

    // ========================================
    protected Component createGUI () {
				frame = new Frame ();
				frame.setIconImage (getIcon ());
				JPanel contentPane = new JPanel (new MultiToolBarBorderLayout ());

				jBpt = new JBpt (bpt);

				toolBarManager = new ToolBarManager (getIcon (), contentPane);

				bptManager = new BptManager (this, bpt, jBpt);
				helpManager = new HelpManager (this, "Bpt");

				jBptToolBar = new JBptToolBar (this, bptManager, helpManager, toolBarManager);
				contentPane.add (jBpt, BorderLayout.CENTER);

				return contentPane;
    }

    protected void newJFrame () {
				// jBptDialog.setJFrame (jFrame);
    }

    // ========================================
    public String getTitle () {
				String name = bpt.getName ();
				return MessageFormat.format (Bundle.getTitle ("Bpt"), (name == null) ? Bundle.getAction ("Empty") : name);
    }

    public Image getIcon () {
				try {
						return Util.loadImageIcon (Config.getString ("BptIcon", "data/images/bpt.png")).getImage ();
				} catch (Exception e) {
						return null;
				}
    }

    public void updateName () {
				updateBundle ();
    }

    // ========================================
    protected JMenuBar createMenuBar () {
				return jBptMenuBar = new JBptMenuBar (this, bptManager, helpManager, toolBarManager);
    }

    // ========================================
    protected boolean tryClosingWindows () {
				Config.save ("Bpt");
				if (bpt.getModified ())
						switch (JOptionPane.showConfirmDialog (jFrame, Bundle.getMessage ("QuitJBpt"),
																									 Bundle.getTitle ("BptStillRunning"),
																									 JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
						case JOptionPane.YES_OPTION:
								return true;
						case JOptionPane.NO_OPTION:
						case JOptionPane.CANCEL_OPTION:
						case JOptionPane.CLOSED_OPTION:
								return false;
						}
				return true;
    }

    // ========================================
}
