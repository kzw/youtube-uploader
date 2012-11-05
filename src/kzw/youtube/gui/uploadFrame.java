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
import kzw.youtube.YouTube;

/**
 *
 * @author kz
 */
public class uploadFrame extends JDialog {

    
    public static final JProgressBar fileCountPb=new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JProgressBar sizePb=new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JProgressBar filePb = new JProgressBar(SwingConstants.HORIZONTAL);
    public static final JTextField rateText = new JTextField();
    public static final JLabel currentFile = new JLabel("Current file progress");
    private static Boolean first_time=true;
    final static JTextArea log=new JTextArea(40,95);

    public static void updateFileCountPb(Integer currentCount, int allFileCount, Integer fileCount) {
        if(currentCount==1)fileCountPb.setIndeterminate(false);
        fileCountPb.setValue(allFileCount);
        fileCountPb.setString(allFileCount+"/"+fileCount);    
    }

    public static void resetPB(){
        filePb.setMaximum(100);         
        fileCountPb.setIndeterminate(true);
        sizePb.setIndeterminate(true); 
        filePb.setValue(0);
        fileCountPb.setValue(0);
        sizePb.setValue(0);
        YouTube.resetCurrentTotalSize();    
    }
    
    public static void reInit(Integer fileCount, int totalSize) {
        filePb.setStringPainted(true);
        sizePb.setStringPainted(true);
        fileCountPb.setMaximum(fileCount);
        sizePb.setMaximum(totalSize);
        sizePb.setIndeterminate(false);
    }

    private void init(){
        uploadFrame.filePb.setMaximum(100);         
        uploadFrame.fileCountPb.setIndeterminate(true);
        uploadFrame.fileCountPb.setSize(300,44);
        uploadFrame.sizePb.setIndeterminate(true);
    }
    
    public uploadFrame(){
        super(Main.frame,true);
        if(first_time){
            init();
            first_time=false;
        }
        log.setText(null);
        JPanel P = new JPanel(new GridLayout(0,2,10,10));
        P.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(P,BorderLayout.PAGE_START);
        P.add(new JLabel("File count progress"));
        P.add(fileCountPb);
        
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
