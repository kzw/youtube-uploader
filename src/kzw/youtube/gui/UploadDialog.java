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
import kzw.youtube.doWork;

/**
 *
 * @author kz
 */
public class UploadDialog extends JDialog {

    
    private static final JProgressBar fileCountPb=new JProgressBar(SwingConstants.HORIZONTAL);
    private static final JProgressBar sizePb=new JProgressBar(SwingConstants.HORIZONTAL);
    private static final JProgressBar filePb = new JProgressBar(SwingConstants.HORIZONTAL);
    private static final JTextField rateText = new JTextField();
    private static final JLabel currentFile = new JLabel("Current file progress");
    private static Boolean first_time=true;
    private final static JTextArea log=new JTextArea(40,95);

    public static synchronized void updateFileCountPb(int currentCount, Integer allFileCount, int fileCount) {
        if(currentCount==1)fileCountPb.setIndeterminate(false);
        if(allFileCount==null){
            fileCountPb.setIndeterminate(true);
            return;
        }
        fileCountPb.setValue(allFileCount);
        fileCountPb.setString(allFileCount+"/"+fileCount);    
    }

    public static synchronized void resetPB(){
        filePb.setMaximum(100);         
        fileCountPb.setIndeterminate(true);
        sizePb.setIndeterminate(true); 
        filePb.setValue(0);
        fileCountPb.setValue(0);
        sizePb.setValue(0);
        YouTube.resetCurrentTotalSize();    
    }
    
    public static synchronized void reInit(int fileCount, int totalSize) {
        filePb.setStringPainted(true);
        sizePb.setStringPainted(true);
        fileCountPb.setMaximum(fileCount);
        sizePb.setMaximum(totalSize);
        sizePb.setIndeterminate(false);
    }

    public static synchronized String updateRate(String s) {
        String oldString = rateText.getText();
        rateText.setText(s);
        return oldString;
    }

    public static synchronized void resetFileCountPb(int allFileCount) {
        if(2==allFileCount){
            fileCountPb.setIndeterminate(false);
            fileCountPb.setStringPainted(true);
        } else if(1==allFileCount){
            fileCountPb.setStringPainted(false);
        }
    }

    public static synchronized void updateTotalSizePb(int totalSize) {
        sizePb.setValue(totalSize);
        int totalPercent=(int) (sizePb.getPercentComplete()*100);
        totalSize/=1024;
        int allFilesSize=sizePb.getMaximum()/1024;
        sizePb.setString(totalSize+" MB/"+allFilesSize+" MB ("+totalPercent+"%)");
    }

    public static synchronized void updateFilePb(int percent) {
        filePb.setValue(percent);
        filePb.setString(percent+"%");
    }

    public static synchronized String updateCurrentFile(Long size, String fn) {
        int sizeKB = size.intValue()/1024;
        int sizeMB = sizeKB/1024;
        String sizeToShow = sizeMB  < 1 ? sizeKB +"KB": sizeMB +"MB";
        if(fn.length()>18){
            currentFile.setToolTipText(fn);
            fn = fn.substring(0, 14)+"...";
        }
        currentFile.setText(fn +" ("+sizeToShow+")");
        return sizeToShow;
    }
    
    public UploadDialog(){
        super(Main.frame,true);
        if(first_time){
            filePb.setMaximum(100);         
            fileCountPb.setIndeterminate(true);
            fileCountPb.setSize(300,44);
            sizePb.setIndeterminate(true);           
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
