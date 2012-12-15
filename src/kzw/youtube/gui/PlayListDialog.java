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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
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
    
    private Set existingList;
    private byte count=0;
    private String selectedList;
    private final JComboBox toChoose=new JComboBox(new Object[]{"please wait. getting list"});
    
    PlayListDialog(String currentList)  {
        super(Main.frame,true);
        final String originalSelectedList = currentList;
        JPanel P = new JPanel(new GridLayout(0,1,10,10));
        P.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(P, BorderLayout.PAGE_START);
        P.add(new JLabel("Select or create playlist"));
        toChoose.setToolTipText("Select existing or type new playlist here and press ENTER");
        toChoose.setEnabled(false);
        toChoose.setSelectedItem(currentList);
        toChoose.getEditor().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectedList = (String) toChoose.getSelectedItem();
                int n = JOptionPane.showConfirmDialog(
                        Main.frame,
                        selectedList,
                        "Create this playlist?",
                        JOptionPane.YES_NO_OPTION);
                    if (n != JOptionPane.YES_OPTION) {
                        PlayListDialog.this.dispose();
                        selectedList = originalSelectedList;
                        return;
                    }
                    try {
                        PlayList.create(selectedList);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ServiceException ex) {
                        Logger.getLogger(PlayListDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        });
        toChoose.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                Object selection = toChoose.getSelectedItem();
                if(null==selection) count++;
                if(existingList.contains(selection)) {
                    selectedList = (String) selection;
                    if(count>1){
                        PlayListDialog.this.dispose();
                    }
                    count++;
                }
            }
        });
        P.add(toChoose);
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
        GetPlayListDataTask mt = new GetPlayListDataTask();
        mt.start();
        this.addWindowListener(new WindowAdapter(){
        
            @Override
            public void windowClosing(WindowEvent e) {
                selectedList=null;            
            }
        });
        setVisible(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    
    String getSelection(){ return selectedList; }
    
    private class GetPlayListDataTask extends Thread{
        @Override
        public void run(){
            try {
                existingList = PlayList.getList();
                toChoose.removeAllItems();
                Object[] playListArray = existingList.toArray(new String[0]);
                Arrays.sort(playListArray);
                for (Object o: playListArray){
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
