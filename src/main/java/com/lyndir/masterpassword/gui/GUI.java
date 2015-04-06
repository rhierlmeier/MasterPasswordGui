/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


package com.lyndir.masterpassword.gui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.lyndir.lhunath.opal.system.logging.Logger;


/**
 * <p> <i>Jun 10, 2008</i> </p>
 *
 * @author mbillemo
 */
public class GUI implements UnlockFrame.SignInCallback {

    private static final Logger logger = Logger.get( GUI.class );

    private final UnlockFrame unlockFrame = new UnlockFrame( this );
    private PasswordFrame passwordFrame;

    public static void main(final String[] args)
            throws IOException {

//        if (Config.get().checkForUpdates())
//            checkUpdate();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
        }

       new GUI().open();
    }

    private static void checkUpdate() {
        try {
            Enumeration<URL> manifestURLs = Thread.currentThread().getContextClassLoader().getResources( JarFile.MANIFEST_NAME );
            while (manifestURLs.hasMoreElements()) {
                InputStream manifestStream = manifestURLs.nextElement().openStream();
                Attributes attributes = new Manifest( manifestStream ).getMainAttributes();
                if (!GUI.class.getCanonicalName().equals( attributes.getValue( Attributes.Name.MAIN_CLASS ) ))
                    continue;

                String manifestRevision = attributes.getValue( Attributes.Name.IMPLEMENTATION_VERSION );
                String upstreamRevisionURL = "http://masterpasswordapp.com/masterpassword-gui.jar.rev";
                CharSource upstream = Resources.asCharSource( URI.create( upstreamRevisionURL ).toURL(), Charsets.UTF_8 );
                String upstreamRevision = upstream.readFirstLine();
                logger.inf( "Local Revision:    <%s>", manifestRevision );
                logger.inf( "Upstream Revision: <%s>", upstreamRevision );
                if (manifestRevision != null && !manifestRevision.equalsIgnoreCase( upstreamRevision )) {
                    logger.wrn( "You are not running the current official version.  Please update from:\n"
                                + "http://masterpasswordapp.com/masterpassword-gui.jar" );
                    JOptionPane.showMessageDialog( null, "A new version of Master Password is available.\n"
                                                         + "Please download the latest version from http://masterpasswordapp.com",
                                                   "Update Available", JOptionPane.WARNING_MESSAGE );
                }
            }
        }
        catch (IOException e) {
            logger.wrn( e, "Couldn't check for version update." );
        }
    }

    protected void open() {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                if (passwordFrame == null)
                    unlockFrame.setVisible( true );
                else
                    passwordFrame.setVisible( true );
                
                installInTray();
                
                
            }
        } );
    }
    
    private void installInTray() {
    	
    	if (!SystemTray.isSupported()) {
            return;
        }
    	
    	final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(createImage("/media/masterpassword_32.png", "tray icon"));
        trayIcon.setImageAutoSize(true);
        
        final SystemTray tray = SystemTray.getSystemTray();
        
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem openItem = new MenuItem("Open");
        //Add components to pop-up menu
        popup.add(exitItem);
        popup.add(openItem);
        
        ActionListener openListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                if (passwordFrame == null)
                    unlockFrame.setVisible( true );
                else
                    passwordFrame.setVisible( true );
			}
		};
        
        openItem.addActionListener(openListener);
        
        exitItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
        
        trayIcon.addActionListener(openListener);
        trayIcon.setPopupMenu(popup);
       
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
        	logger.wrn(e, "TrayIcon could not be added.");
        }    	
    	
    }
    
    protected static Image createImage(String path, String description) {
        URL imageURL = GUI.class.getResource(path);
         
        if (imageURL == null) {
        	logger.wrn("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }    

    @Override
    public void signedIn(final User user) {
        passwordFrame = newPasswordFrame( user );
        open();
    }

    protected PasswordFrame newPasswordFrame(final User user) {
    	PasswordFrame ret = new PasswordFrame( user );
        ret.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
        return ret;
        
    }
}
