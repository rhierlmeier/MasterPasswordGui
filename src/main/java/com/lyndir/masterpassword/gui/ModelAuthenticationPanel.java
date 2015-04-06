package com.lyndir.masterpassword.gui;

import static com.lyndir.lhunath.opal.system.util.StringUtils.strf;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.masterpassword.model.MPUser;
import com.lyndir.masterpassword.model.MPUserFileManager;
import com.lyndir.masterpassword.gui.util.Components;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalComboBoxEditor;

import org.jetbrains.annotations.NotNull;


/**
 * @author lhunath, 2014-06-11
 */
public class ModelAuthenticationPanel extends AuthenticationPanel implements ItemListener, ActionListener, DocumentListener {

    private static final Logger logger = Logger.get( ModelAuthenticationPanel.class );

    private final JComboBox<ModelUser> userField;
    private final JLabel               masterPasswordLabel;
    private final JPasswordField       masterPasswordField;
    private final JPasswordField       repeatPasswordField;

    public ModelAuthenticationPanel(final UnlockFrame unlockFrame) {
        super( unlockFrame );
        add( Components.stud() );

        // Avatar
        avatarLabel.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                ModelUser selectedUser = getSelectedUser();
                if (selectedUser != null) {
                    selectedUser.setAvatar( selectedUser.getAvatar() + 1 );
                    updateUser( false );
                }
            }
        } );

        // User
        JLabel userLabel = Components.label( RB.msg("user") );
        add( userLabel );

        userField = Components.comboBox( readConfigUsers() );
        userField.setFont( Res.valueFont().deriveFont( 12f ) );
        userField.addItemListener( this );
        userField.addActionListener( this );
        userField.setEditor( new MetalComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField editorComponents = Components.textField();
                editorComponents.setForeground( Color.red );
                return editorComponents;
            }
        } );

        add( userField );
        add( Components.stud() );

        // Master Password
        masterPasswordLabel = Components.label( RB.msg("masterPassword" ));
        add( masterPasswordLabel );

        masterPasswordField = Components.passwordField();
        masterPasswordField.addActionListener( this );
        masterPasswordField.getDocument().addDocumentListener( this );
        add( masterPasswordField );
        
        repeatPasswordField = Components.passwordField();
        repeatPasswordField.addActionListener( this );
        repeatPasswordField.getDocument().addDocumentListener( this );
        add( repeatPasswordField );
    }

    @Override
    public Component getFocusComponent() {
        return masterPasswordField.isVisible()? masterPasswordField: null;
    }

    @Override
    protected void updateUser(boolean repack) {
        ModelUser selectedUser = getSelectedUser();
        if (selectedUser != null) {
            avatarLabel.setIcon( Res.avatar( selectedUser.getAvatar() ) );
            boolean showPasswordField = !selectedUser.keySaved();
            if (masterPasswordField.isVisible() != showPasswordField) {
                masterPasswordLabel.setVisible( showPasswordField );
                masterPasswordField.setVisible( showPasswordField );
                repack = true;
            }
        }

        super.updateUser( repack );
    }

    @Override
    protected ModelUser getSelectedUser() {
        int selectedIndex = userField.getSelectedIndex();
        if (selectedIndex < 0)
            return null;

        return userField.getModel().getElementAt( selectedIndex );
    }

    @NotNull
    @Override
    public char[] getMasterPassword() {
        return masterPasswordField.getPassword();
    }
    
    @Override
    public char[] getMasterPasswordRepeated() {
    	return repeatPasswordField.getPassword();
    }
    

    @Override
    public Iterable<? extends JButton> getButtons() {
        return ImmutableList.of( new JButton( Res.iconAdd() ) {
            {
                addActionListener( new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        String fullName = JOptionPane.showInputDialog( ModelAuthenticationPanel.this, //
                        		                                       RB.msg("newUser.msg"),
                                                                       RB.msg("newUser"), JOptionPane.QUESTION_MESSAGE );
                        MPUserFileManager.get().addUser( new MPUser( fullName ) );
                        userField.setModel( new DefaultComboBoxModel<>( readConfigUsers() ) );
                        updateUser( true );
                    }
                } );
                setToolTipText( "Add a new user to the list." );
            }
        }, new JButton( Res.iconDelete() ) {
            {
                addActionListener( new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        ModelUser deleteUser = getSelectedUser();
                        if (deleteUser == null)
                            return;

                        if (JOptionPane.showConfirmDialog( ModelAuthenticationPanel.this, //
                                                       RB.msg( "delUser.msg",
                                                             deleteUser.getFullName() ), //
                                                       RB.msg("delUser"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.CANCEL_OPTION)
                            return;

                        MPUserFileManager.get().deleteUser( deleteUser.getModel() );
                        userField.setModel( new DefaultComboBoxModel<>( readConfigUsers() ) );
                        updateUser( true );
                    }
                } );
                setToolTipText( RB.msg("delUser.tooltip") );
            }
        }, new JButton( Res.iconQuestion() ) {
            {
                addActionListener( new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        JOptionPane.showMessageDialog( ModelAuthenticationPanel.this, //
                                                       RB.msg( "help.msg",
                                                             MPUserFileManager.get().getPath().getAbsolutePath() ), //
                                                       RB.msg("help"), JOptionPane.INFORMATION_MESSAGE );
                    }
                } );
                setToolTipText(RB.msg("help.tooltip") );
            }
        } );
    }

    @Override
    public void reset() {
        masterPasswordField.setText( "" );
    }

    private ModelUser[] readConfigUsers() {
        return FluentIterable.from( MPUserFileManager.get().getUsers() ).transform( new Function<MPUser, ModelUser>() {
            @Nullable
            @Override
            public ModelUser apply(@Nullable final MPUser model) {
                return new ModelUser( Preconditions.checkNotNull( model ) );
            }
        } ).toArray( ModelUser.class );
    }

    @Override
    public void itemStateChanged(final ItemEvent e) {
        updateUser( false );
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        updateUser( false );
        unlockFrame.trySignIn( userField );
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
}
