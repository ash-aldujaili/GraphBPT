package ismmBpt2015.view;

import java.util.Hashtable;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import misc.ApplicationManager;
import misc.ToolBarManager;
import misc.Util;

@SuppressWarnings ("serial") 
public class JBptMenuBar extends JMenuBar {

    // ========================================
		public JBptMenuBar (ApplicationManager controllerManager, ApplicationManager bptManager, ApplicationManager helpManager, ToolBarManager toolBarManager) {
				setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
				JMenu fileMenu = Util.addJMenu (this, "File");
				add (Box.createHorizontalGlue ());
				JMenu helpMenu = Util.addJMenu (this, "Help");

				bptManager.addMenuItem (fileMenu);
				controllerManager.addMenuItem (fileMenu);
				helpManager.addMenuItem (helpMenu);
				toolBarManager.addMenuItem (helpMenu);

				Hashtable<String, AbstractButton> buttons = new Hashtable<String, AbstractButton> ();
				Util.collectButtons (buttons, fileMenu);
				Util.collectButtons (buttons, helpMenu);

				controllerManager.addActiveButtons (buttons);
				bptManager.addActiveButtons (buttons);
				helpManager.addActiveButtons (buttons);
				toolBarManager.addActiveButtons (buttons);
    }

    // ========================================
}

