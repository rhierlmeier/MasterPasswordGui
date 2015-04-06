package com.lyndir.masterpassword.gui;

import com.lyndir.masterpassword.gui.util.Components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jetbrains.annotations.NotNull;


/**
 * @author lhunath, 2014-06-11
 */
public class IncognitoAuthenticationPanel extends AuthenticationPanel implements DocumentListener, ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField     fullNameField;
    private final JPasswordField masterPasswordField;
    
    private final JPasswordField repeateMasterPasswordField;

    public IncognitoAuthenticationPanel(final UnlockFrame unlockFrame) {

        // Full Name
        super( unlockFrame );
        add( Components.stud() );

        JLabel fullNameLabel = Components.label( RB.msg("fullName"));
        add( fullNameLabel );

        fullNameField = Components.textField();
        fullNameField.setFont( Res.valueFont().deriveFont( 12f ) );
        fullNameField.getDocument().addDocumentListener( this );
        fullNameField.addActionListener( this );
        add( fullNameField );
        add( Components.stud() );

        // Master Password
        JLabel masterPasswordLabel = Components.label( RB.msg("masterPassword") );
        add( masterPasswordLabel );

        masterPasswordField = Components.passwordField();
        masterPasswordField.addActionListener( this );
        masterPasswordField.getDocument().addDocumentListener( this );
        add( masterPasswordField );
        
        repeateMasterPasswordField = Components.passwordField();
        repeateMasterPasswordField.addActionListener( this );
        repeateMasterPasswordField.getDocument().addDocumentListener( this );
        add( repeateMasterPasswordField );
        
        
    }

    @Override
    public Component getFocusComponent() {
        return fullNameField;
    }

    @Override
    public void reset() {
        masterPasswordField.setText( "" );
    }

    @Override
    protected User getSelectedUser() {
        return new IncognitoUser( fullNameField.getText() );
    }

    @NotNull
    @Override
    public char[] getMasterPassword() {
        return masterPasswordField.getPassword();
    }
    
    @Override
    public char[] getMasterPasswordRepeated() {
    	return repeateMasterPasswordField.getPassword();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        updateUser( false );
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        updateUser( false );
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        updateUser( false );
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        updateUser( false );
    	unlockFrame.trySignIn( fullNameField, masterPasswordField, repeateMasterPasswordField );
    }
}
