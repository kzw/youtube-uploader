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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import kzw.youtube.Main;
import kzw.youtube.PlayList;

/**
 *
 * @author me
 */
class PlayListDialog extends JDialog{
    
    private Boolean alreadyConfirmed=false;
    private Set existingList;
    private byte count=0;
    private final JComboBox toChoose=new JComboBox(new Object[]{"please wait. getting list"});
    
    PlayListDialog(String currentList)  {
        super(Main.frame,true);
        JPanel P = new JPanel(new GridLayout(0,2,10,10));
        P.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(P, BorderLayout.PAGE_START);
        P.add(new JLabel("Select playlist"));
        toChoose.setEnabled(false);
//        toChoose.getEditor().addActionListener(new ActionListener(){
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                
//                PlayListDialog.this.dispose();
//            }
//        });
        toChoose.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                Object selection = toChoose.getSelectedItem();
                String selectedList = (String) selection;
                //System.out.println("selected "+selection);
                if(existingList.contains(selection)) {
                    if(count>1){
                        DataPanel.playlistInput.setText(selectedList);
                        PlayListDialog.this.dispose();
                    }
                    count++;
                    return;
                }
                //System.out.println("creating a new playlist "+selection);
                if(count>1){
                    if(alreadyConfirmed) return;
                    alreadyConfirmed=true;
                    int n = JOptionPane.showConfirmDialog(
                        Main.frame,
                        selectedList,
                        "Create this playlist?",
                        JOptionPane.YES_NO_OPTION);
                    if (n != JOptionPane.YES_OPTION) return;
                    try {
                        PlayList.create(selectedList);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ServiceException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }                   
                    DataPanel.playlistInput.setText(selectedList);
                    PlayListDialog.this.dispose();
                }
                count++;
            }
        });
        P.add(toChoose);
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
        getMenuTask mt = new getMenuTask();
        mt.start();
        setVisible(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

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

}
