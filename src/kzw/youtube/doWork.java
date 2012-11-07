package kzw.youtube;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.UploadDialog;
import kzw.youtube.gui.YouTubeFrame;

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
    private final static Logger logger = YouTubeLogger.getIt(doWork.class.getName());
    private String titleSeed;
    private String description;
    private String keywords;

    public doWork(File t){
        tempObj = t;
    }

    public doWork setPlayListURL(URL plurl){
        playListURL=plurl;
        return this;
    }

    public doWork setSleep(Long s){
        sleepMin = s;
        return this;
    }

    public doWork setDir(String m){
        moveDir = m;
        return this;
    }

    public doWork setTitle(String t){
        titleSeed = t;
        return this;
    }

    public doWork setDescription(String d){
        description = d;
        return this;
    }

    @Override
    public void run(){
        logger.setLevel(DataPanel.selectedLogLevel);
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
            UploadDialog.reInit(fileCount,totalSize);
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
        int allFileCount=0;
        for(String path : fileList){
            try {
                if( 0 == currentCount && wasProcessed) {
                    UploadDialog.updateFileCountPb(-1,null,1);
                    // This should make google happy
                } else if(0<currentCount) {
                    String oldString = UploadDialog.updateRate("waiting for 4 sec between videos");
                    Thread.sleep(4000);
                    if(oldString!=null) UploadDialog.updateRate(oldString);
                }
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                return;
            }
            UploadDialog.resetFileCountPb(++allFileCount);            

            if(null!=doneFiles.get(path))continue;

            wasProcessed = false;
            Long size = (Long) fileSize.get(path);
            String fn = (String) fileName.get(path);
            String sizeToShow = UploadDialog.updateCurrentFile(size, fn);
            logger.log(Level.INFO, "Processing {0} with size {1}", new Object[]{fn, sizeToShow});

            // TODO: set the parameters from UI
            YouTube Yt=new YouTube(YouTubeFrame.privateSetting,"People",YTservice.getService());
            String titleString;
            if(titleSeed==null || titleSeed.isEmpty()) titleString = new File(path).getName();         
            else titleString = titleSeed + "-"+ allFileCount;
            String tempPath;
            try {
                logger.info("Copying file to working copy");
                tempPath = CopyToTemp.cp(path);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
                return;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                return;
            }
            //keywords implementation is wrong
            // total keywords length of 500 characters instead of 120, and each
            // individual keyword can now be up to 30 characters long instead of 25.//
            // The YouTube API validation code is being updated to reflect this, and
            // that change should go live sometime in March.
            // Solution:  need to put commas between keywords
            // a way to catch the youtube error
            Yt.setPlayList(playListURL)
                .setPath(tempPath)
                .videoTitle(titleString)
                .setKeywords(keywords)
                .setDescription(description);
            Yt.start();
            try {
                Yt.join();
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
                Yt.interrupt();
                return;
            }
            new File(tempPath).delete();
            if(!Yt.uploadSuccess) return;
            
            UploadDialog.updateFileCountPb(++currentCount, allFileCount, fileCount);
            Long delta = Yt.getDuration();
            if(delta>0 && Yt.completeJob){
                float rate=  size/delta.floatValue()/1024*1000;
                DecimalFormat myFormatter = new DecimalFormat("####.#");
                String rateString = myFormatter.format(rate);
                UploadDialog.updateRate(rateString + " kB/s");
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
                logger.log(Level.FINE, "Failed to move on attempt {0} Waiting for 0.5 sec", moveAttempt);
                System.gc();
                logger.log(Level.FINE, "Thread alive status is {0}", Yt.isAlive());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex1) {
                    logger.log(Level.SEVERE, null, ex1);
                    return;
                }
            }
            if(moveSuccess) {
                if(moveAttempt>1){ logger.log(Level.FINE, "Managed to move on attempt number {0}", moveAttempt); }
                continue;
            }
            logger.warning("Failed to move video to folder after 5 tries");
            JOptionPane.showMessageDialog(null,"Cannot move video to folder");
            return;
        }
        tempObj.delete();
        logger.info("all done");
    }

    public doWork setKeywords(String text) {
        keywords = text;
        return this;
    }

}
