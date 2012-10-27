/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import kzw.youtube.Main;
import kzw.youtube.YouTube;
import kzwdesktop.MainFrame;
import kzwencryption.Encryption;

/**
 *
 * @author kz
 */
public class YouTubeFrame extends MainFrame {
    
    static String UserNameString="";
    static String PasswordString="";
    public static final JCheckBoxMenuItem privateFlag = new JCheckBoxMenuItem("private video");
    public static byte[] ENCRYPTION_KEY={38,-16,16,-45,74,-3};
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String PRIVATE_FLAG = "private";
    private Preferences P=Preferences.userNodeForPackage(getClass());
    final static Logger logger = Logger.getLogger(YouTube.class.getName());
    public static Boolean privateSetting;

    public YouTubeFrame() throws Exception{
        //Create and set up the window.
        super("YouTube");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        add(new Gui());
        JMenuBar jm=new JMenuBar();
        JMenu menuPanel = new JMenu("Set up");
        final JMenuItem manageAccount = new JMenuItem("manage account");
        menuPanel.add(manageAccount);
        jm.add(menuPanel);
        
        final Encryption enc = new Encryption(ENCRYPTION_KEY);
        UserNameString = P.get(USERNAME_KEY,null);
        try {
            PasswordString = getPassword(enc);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        manageAccount.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                final JDialog accountDialog = new JDialog(Main.frame,true);
                JPanel accountPanel = new JPanel(new GridLayout(0,2,10,10));
                accountPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
                accountPanel.add(new JLabel("YouTube User"));
                final JTextField user=new JTextField();
                accountPanel.add(user);
                
                accountPanel.add(new JLabel("YouTube Password"));
                final JPasswordField pass = new JPasswordField();
                accountPanel.add(pass);
                
                accountPanel.add(new JLabel("Check to save credentials"));
                final JCheckBox saveCheckBox = new JCheckBox();
                accountPanel.add(saveCheckBox);
                
                JButton deleteButton = new JButton("Delete saved credentials");
                accountPanel.add(deleteButton);
                
                user.setText(UserNameString);
                pass.setText(PasswordString);
                
                deleteButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        P.remove(PASSWORD_KEY);
                        P.remove(USERNAME_KEY);
                        user.setText(null);
                        pass.setText(null);
                        UserNameString = null;
                        PasswordString = null;
                    }
                });
                
                JButton setButton = new JButton("Set credentials");
                accountPanel.add(setButton);
                setButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        PasswordString = new String(pass.getPassword());
                        UserNameString = user.getText();
                        if(saveCheckBox.isSelected()){
                            try {
                                setPassword(enc,PasswordString);
                            } catch (Exception ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                        P.put(USERNAME_KEY,UserNameString);
                        accountDialog.dispose();
                    }
                });
                
                
                accountDialog.add(accountPanel);
                accountDialog.pack();
                accountDialog.setLocationRelativeTo(Main.frame);
                accountDialog.setVisible(true);
            }
        });
        privateFlag.setEnabled(true);
        menuPanel.add(privateFlag);
        privateSetting = P.getBoolean(PRIVATE_FLAG, true);
        privateFlag.setState(privateSetting);
        privateFlag.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                privateSetting = !privateSetting;
                P.putBoolean(PRIVATE_FLAG, privateSetting );
            }
        });
        setJMenuBar(jm);
        
        //Display the window.
        pack();
        setVisible(true);
    }
    
    private String getPassword(Encryption e) throws Exception{
        String encPass = P.get(PASSWORD_KEY, null);
        if(encPass == null) return "";
        return e.decrypt(encPass);
    }
    
    private void setPassword(Encryption e, String p) throws Exception{
        if(p.equalsIgnoreCase("")) return;
        P.put(PASSWORD_KEY,e.encrypt(p));
    }
    
}
