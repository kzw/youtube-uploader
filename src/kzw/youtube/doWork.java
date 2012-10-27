/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.YouTubeFrame;
import kzw.youtube.gui.uploadFrame;

public class doWork extends Thread{
        private String moveDir;
        private Long sleepMin;
        private File tempObj;
        private static HashMap doneFiles=new HashMap();
        private HashMap fileSize = new HashMap();
        private HashMap fileName = new HashMap();
        private URL playListURL;
        private ArrayList<String> fileList = new ArrayList<String>();
        private static int totalSize=0;
        final static Logger logger = Logger.getLogger(doWork.class.getName());
                
        public void setPlayListURL(URL plurl){
            playListURL=plurl;
        }
        public void setTemp(File f){
            tempObj = f;
        }
        
        public void setSleep(Long s){
            sleepMin = s;
        }
        
        public void setDir(String m){
            moveDir = m;
        }
        @Override
        public void run(){
            logger.setLevel(DataPanel.selectedLogLevel);
            // TODO create a frame here and log to the new text area
            if(sleepMin!=0) logger.fine("waiting for "+sleepMin+" minutes");
            try {
                sleep(sleepMin*1000L*60);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                return;
            }
            
            BufferedReader br=null;
            try {
                br = new BufferedReader(new FileReader(tempObj));
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            String vp;
            Integer fileCount=0;
            totalSize=0;
            try {
                while((vp = br.readLine()) !=null){
                    fileList.add(vp);
                    fileCount++;
                    File f = new File(vp);
                    Long fs=f.length();
                    totalSize += fs/1024;
                    fileName.put(vp, f.getName());
                    fileSize.put(vp, f.length());
                }
                uploadFrame.filePb.setStringPainted(true);
                uploadFrame.sizePb.setStringPainted(true);
                uploadFrame.pb.setMaximum(fileCount);
                uploadFrame.sizePb.setMaximum(totalSize);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            try {
                br.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }

            Boolean wasProcessed=true;
            Integer currentCount=0;
            uploadFrame.sizePb.setIndeterminate(false);
            int allFileCount=0;
            for(String path : fileList){
                try {
                    if( 0 == currentCount && wasProcessed) {
                        uploadFrame.pb.setIndeterminate(true);
                        // This should make google happy
                       
                    } else if(0<currentCount) {
                        String s=uploadFrame.rateText.getText();
                        uploadFrame.rateText.setText("waiting for 4 sec between videos");
//                        logger.warning("Waiting for 4 sec to make google happy");
                        Thread.sleep(4000);
                        if(s!=null) uploadFrame.rateText.setText(s);
                    }
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    return;
                }
                allFileCount++;
                if(2==allFileCount){
                    uploadFrame.pb.setIndeterminate(false);
                    uploadFrame.pb.setStringPainted(true);
                }
                if(null!=doneFiles.get(path))continue;

                wasProcessed = false;
                Long size = (Long) fileSize.get(path);
                Long sizeMB = size/1024/1024;
                Long startEpoch = new Date().getTime();
                logger.info("Processing "+fileName.get(path)+" with size "+ sizeMB + "MB");
                float rate;
                
                // TODO: set the parameters from UI
                YouTube Yt=new YouTube(YouTubeFrame.privateSetting,"People",YTservice.getService());
                Yt.setPlayList(playListURL);
                Yt.setPath(path);
                Yt.start();
                try {
                    Yt.join();
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    Yt.interrupt();
                    return;
                }
                wasProcessed = Yt.uploadSuccess;
                if(!wasProcessed) return;
                currentCount++;
                if(currentCount==1)uploadFrame.pb.setIndeterminate(false);
                uploadFrame.pb.setValue(allFileCount);
                uploadFrame.pb.setString(allFileCount+"/"+fileCount);
                Long finishEpoch = new Date().getTime();
                Long delta = finishEpoch - startEpoch;
                if(delta>0){
                    rate= (float) (size.floatValue()/delta.floatValue()/1024*1000);
                    DecimalFormat myFormatter = new DecimalFormat("####.##");
                    String rateString = myFormatter.format(rate);
                    uploadFrame.rateText.setText(rateString);
                }
                doneFiles.put(path, 1);
                if(moveDir==null || moveDir.equals("")) continue;
                File oldFile = new File(path);
                File newFile = new File(moveDir,oldFile.getName());
                Boolean moveSuccess=false;
                int moveAttempt;
                for(moveAttempt=1;moveAttempt<21;moveAttempt++){
                    if(oldFile.renameTo(newFile)){
                        moveSuccess=true;
                        break;
                    }
                    logger.fine("Failed to move on attempt "+moveAttempt+" Waiting for 0.5 sec");
                    System.gc();
                    logger.fine("Thread alive status is "+Yt.isAlive());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        logger.log(Level.SEVERE, null, ex1);
                        return;
                    }
                }
                if(moveSuccess) {
                    if(moveAttempt>1){ logger.fine("Managed to move on attempt number "+moveAttempt); }
                    continue;
                }
                logger.warning("Failed to move video to folder after 20 tries");
                JOptionPane.showMessageDialog(null,"Cannot move video to folder");
                return;
            }
            tempObj.delete();
            logger.info("all done");
        }
        static synchronized int getTotalSize(){
            return totalSize;
        }

}
