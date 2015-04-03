package com.lyndir.masterpassword.gui;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;

import com.lyndir.masterpassword.MPIdenticon;
import com.lyndir.masterpassword.gui.util.Components;
import com.lyndir.masterpassword.model.IncorrectMasterPasswordException;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.annotation.Nullable;
import javax.swing.*;


/**
 * @author lhunath, 2014-06-08
 */
public class UnlockFrame extends JFrame {

    private final SignInCallback           signInCallback;
    private final Components.GradientPanel root;
    private final JLabel                   identiconLabel;
    private final JButton                  signInButton;
    private final JPanel                   authenticationContainer;
    private       AuthenticationPanel      authenticationPanel;
    public        User                     user;
    
    
    private final  Preferences prefs = Preferences.userNodeForPackage(UnlockFrame.class);


    public boolean getIncognito() {
    	return prefs.getBoolean("incognito", false);
    }
    
    public void setIncognito(boolean incognito) {
    	prefs.putBoolean("incognito", incognito);
    }
    public UnlockFrame(final SignInCallback signInCallback)
            throws HeadlessException {
        super( "Unlock Master Password" );
        this.signInCallback = signInCallback;

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        addWindowFocusListener( new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                root.setGradientColor( Res.colors().frameBg() );
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                root.setGradientColor( Color.RED );
            }
        } );

        // Sign In
        JPanel signInBox = Components.boxLayout( BoxLayout.LINE_AXIS, Box.createGlue(), signInButton = Components.button( "Sign In" ),
                                                 Box.createGlue() );
        signInBox.setBackground( null );

        setContentPane( root = Components.gradientPanel( new FlowLayout(), Res.colors().frameBg() ) );
        root.setLayout( new BoxLayout( root, BoxLayout.PAGE_AXIS ) );
        root.setBorder( BorderFactory.createEmptyBorder( 20, 20, 20, 20 ) );
        root.add( Components.borderPanel( authenticationContainer = Components.boxLayout( BoxLayout.PAGE_AXIS ),
                                          BorderFactory.createRaisedBevelBorder(), Res.colors().frameBg() ) );
        root.add( Box.createVerticalStrut( 8 ) );
        root.add( identiconLabel = Components.label( " ", SwingConstants.CENTER ) );
        root.add( Box.createVerticalStrut( 8 ) );
        root.add( signInBox );

        authenticationContainer.setOpaque( true );
        authenticationContainer.setBackground( Res.colors().controlBg() );
        authenticationContainer.setBorder( BorderFactory.createEmptyBorder( 20, 20, 20, 20 ) );
        identiconLabel.setFont( Res.emoticonsFont().deriveFont( 14.f ) );
        identiconLabel.setToolTipText(
                "A representation of your identity across all Master Password apps.\nIt should always be the same." );
        signInButton.addActionListener( new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                trySignIn();
            }
        } );

        createAuthenticationPanel();

        setLocationByPlatform( true );
        setLocationRelativeTo( null );
        
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("media/masterpassword-16.png"));
        setIconImage(icon.getImage());
        
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
    }

    protected void repack() {
        pack();
        setMinimumSize( new Dimension( Math.max( 300, getPreferredSize().width ), Math.max( 300, getPreferredSize().height ) ) );
        pack();
    }

    private void createAuthenticationPanel() {
        authenticationContainer.removeAll();

        if (getIncognito()) {
            authenticationPanel = new IncognitoAuthenticationPanel( this );
        } else {
            authenticationPanel = new ModelAuthenticationPanel( this );
        }
        authenticationPanel.updateUser( false );
        authenticationContainer.add( authenticationPanel );
        authenticationContainer.add( Components.stud() );

        final JCheckBox incognitoCheckBox = Components.checkBox( "Incognito" );
        incognitoCheckBox.setToolTipText( "Log in without saving any information." );
        incognitoCheckBox.setSelected( getIncognito() );
        incognitoCheckBox.addItemListener( new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                setIncognito(incognitoCheckBox.isSelected());
                
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        createAuthenticationPanel();
                    }
                } );
            }
        } );

        JComponent toolsPanel = Components.boxLayout( BoxLayout.LINE_AXIS, incognitoCheckBox, Box.createGlue() );
        authenticationContainer.add( toolsPanel );
        for (JButton button : authenticationPanel.getButtons()) {
            toolsPanel.add( button );
            button.setBorder( BorderFactory.createEmptyBorder() );
            button.setMargin( new Insets( 0, 0, 0, 0 ) );
            button.setAlignmentX( RIGHT_ALIGNMENT );
            button.setContentAreaFilled( false );
        }

        checkSignIn();
        validate();
        repack();

        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                ifNotNullElse( authenticationPanel.getFocusComponent(), signInButton ).requestFocusInWindow();
            }
        } );
    }

    void updateUser(@Nullable User user) {
        this.user = user;
        checkSignIn();
    }

    boolean checkSignIn() {
        String fullName = user == null? "": user.getFullName();
        char[] masterPassword = authenticationPanel.getMasterPassword();
        char[] repeated = authenticationPanel.getMasterPasswordRepeated();
        
        if(!Arrays.equals(masterPassword, repeated)) {
            identiconLabel.setText( "PW invalid" );
        	return false;
        }
        boolean enabled = !fullName.isEmpty() && masterPassword.length > 0;

        if (fullName.isEmpty() || masterPassword.length == 0)
            identiconLabel.setText( " " );
        else {
            MPIdenticon identicon = new MPIdenticon( fullName, masterPassword );
            identiconLabel.setText( identicon.getText() );
            identiconLabel.setForeground( identicon.getColor().getAWTColor( MPIdenticon.BackgroundMode.DARK ) );
        }

        signInButton.setEnabled( enabled );

        return enabled;
    }

    void trySignIn(final JComponent... signInComponents) {
        if (!checkSignIn())
            return;

        for (JComponent signInComponent : signInComponents)
            signInComponent.setEnabled( false );

        signInButton.setEnabled( false );
        signInButton.setText( "Signing In..." );

        Res.execute( this, new Runnable() {
            @Override
            public void run() {
                try {
                    user.authenticate( authenticationPanel.getMasterPassword() );

                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            signInCallback.signedIn( user );
                            dispose();
                        }
                    } );
                }
                catch (final IncorrectMasterPasswordException e) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog( null, e.getLocalizedMessage(), "Sign In Failed", JOptionPane.ERROR_MESSAGE );
                            authenticationPanel.reset();
                            signInButton.setText( "Sign In" );
                            for (JComponent signInComponent : signInComponents)
                                signInComponent.setEnabled( true );
                            checkSignIn();
                        }
                    } );
                }
            }
        } );
    }

    interface SignInCallback {

        void signedIn(User user);
    }
}
