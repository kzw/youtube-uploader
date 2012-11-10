/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube.gui;

import com.google.gdata.util.ServiceException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import kzw.youtube.Main;
import kzw.youtube.PlayList;

/**
 *
 * @author me
 */
class PlayListDialog extends JDialog implements WindowListener{
    
    String selectedList;
    private Set existingList;
    private final JComboBox toChoose=new JComboBox(new Object[]{"please wait. getting list"});
    
    PlayListDialog(String currentList)  {
        super(Main.frame,true);
        selectedList = currentList;
        getMenuTask mt = new getMenuTask();
        mt.start();
        JPanel P = new JPanel(new GridLayout(0,2,10,10));
        P.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(P, BorderLayout.PAGE_START);
        P.add(new JLabel("Select playlist"));
        toChoose.setEnabled(false);
        toChoose.getEditor().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                PlayListDialog.this.dispose();
            }
        });
        P.add(toChoose);
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
    }
    
    void showThis(){
        setVisible(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
    }
    
    private class getMenuTask extends Thread{
        @Override
        public void run(){
            try {
                existingList = PlayList.getList();
                toChoose.removeAllItems();
                for (Object o: existingList){
                    toChoose.addItem(o);
                }
                toChoose.setEnabled(true);
                toChoose.setEditable(true);
            } catch (IOException ex) {
                Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private synchronized void allowSelection(){
        toChoose.setEditable(true);
    }

    @Override
    public void windowOpened(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowClosing(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        Object selection=
                 toChoose.getSelectedItem();
        System.out.println("selected "+selection);
        if(existingList.contains(selection)) return;
        System.out.println("creating a new playlist "+selection);
        try {
            PlayList.create((String)selection);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
