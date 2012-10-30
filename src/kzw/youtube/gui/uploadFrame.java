/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import kzw.youtube.Main;

/**
 *
 * @author kz
 */
public class uploadFrame extends JDialog {

    
    public static final JProgressBar pb=new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JProgressBar sizePb=new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JProgressBar filePb = new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JTextField rateText = new JTextField();
    public static final JLabel currentFile = new JLabel("Current file progress");
    
    final static JTextArea log=new JTextArea(40,95);

    public uploadFrame(){
        super(Main.frame,true);
        log.setText(null);
        JPanel P = new JPanel(new GridLayout(0,2,10,10));
        P.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(P,BorderLayout.PAGE_START);
        P.add(new JLabel("File count progress"));
        P.add(pb);
        
        P.add(new JLabel("Total size progress"));
        P.add(sizePb);
        
        P.add(currentFile);
        P.add(filePb);
        P.add(new JLabel("Recent file upload rate"));
        P.add(rateText);
        rateText.setEditable(false);
        
        if(!Level.OFF.equals(DataPanel.selectedLogLevel)){
            mainP.add(log,BorderLayout.SOUTH);
        }
        add(mainP);

        pack();
        setLocationRelativeTo(Main.frame);
        setVisible(true);
    }
    public synchronized static void writeLog(String lm){
        log.append(lm);
    }
}
