package ismmBpt2015.view;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import misc.ApplicationManager;
import misc.Bundle;
import misc.ToolBarManager;
import misc.Util;

public class JBptToolBar {

    // ========================================
		public static String defaultCardinalPoint = "West";

    public JBptToolBar (ApplicationManager controllerManager, ApplicationManager bptManager,
												ApplicationManager helpManager, ToolBarManager toolBarManager) {
				JToolBar fileToolBar = toolBarManager.newJToolBar ("File", defaultCardinalPoint);
				JToolBar helpToolBar = toolBarManager.newJToolBar ("Help", defaultCardinalPoint);

				controllerManager.addIconButtons (fileToolBar);
				bptManager.addIconButtons (fileToolBar);
				helpManager.addIconButtons (helpToolBar);
				toolBarManager.addIconButtons (helpToolBar);

				Hashtable<String, AbstractButton> buttons = new Hashtable<String, AbstractButton> ();
				Util.collectButtons (buttons, fileToolBar);
				Util.collectButtons (buttons, helpToolBar);

				controllerManager.addActiveButtons (buttons);
				bptManager.addActiveButtons (buttons);
				helpManager.addActiveButtons (buttons);
				toolBarManager.addActiveButtons (buttons);
    }

    // ========================================
}
