package kzw.youtube;

import com.google.gdata.client.media.ResumableGDataFileUploader;
import com.google.gdata.client.uploader.ProgressListener;
import com.google.gdata.client.uploader.ResumableHttpFileUploader;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.swing.JOptionPane;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.UploadDialog;


public class YouTube extends Thread{

    private Long startEpoch;
    private Long endEpoch;
    private URL afterPlayListURL=null;
    private Boolean privateVideo;
    private MediaCategory mediaCategory;
    private String currentTitle = null;
    private String path;
    private YouTubeService service = null;
    Boolean completeJob = true;
    public static final String RESUMABLE_UPLOAD_URL =
      "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";

    /** Time interval at which upload task will notify about the progress */
    private static final int PROGRESS_UPDATE_INTERVAL = 1000;
    Boolean uploadSuccess=false;
    private static ResumableGDataFileUploader interruptedUploader=null;
    private static String interruptedPath;

    /** Max size for each upload chunk */
    private static final int DEFAULT_CHUNK_SIZE = 700000;
    private static int currentSizeKB=0;
    private Long currentFileSize=0L;
    private String description;
    
    // refactor ala glacier project
    private final static Logger logger = YouTubeLogger.getIt(YouTube.class.getName());
    private String keywords;
    
    Long getDuration(){
        return (endEpoch - startEpoch);
    }
    
    YouTube setPlayList(URL u) {
        afterPlayListURL=u;
        return this;
    }
    
    YouTube setKeywords(String s){
        keywords = s;
        return this;
    }
    
    public YouTube(Boolean PrivateVideo,String categoryString,YouTubeService service) {
        this.service = service;
        this.privateVideo = PrivateVideo;
        // set category via UI
        this.mediaCategory = new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, categoryString);
    }

    YouTube videoTitle(String title){
        currentTitle = title;
        return this;
    }
    
    YouTube setPath(String p){
        path = p;
        return this;
    }
    
    YouTube setDescription(String d){
        description = d;
        return this;
    }
    
    @Override
    public void run(){
        logger.setLevel(DataPanel.selectedLogLevel);
        File videoFile = new File(path);
        if (!videoFile.exists()) {
            logger.log(Level.WARNING, "Sorry, that video doesn''t exist at :{0}", path);
            return;
        }
        currentFileSize=videoFile.length()/1024;
        MediaFileSource ms = new MediaFileSource(videoFile, new MimetypesFileTypeMap().getContentType(videoFile));

        String videoTitle;
        if(currentTitle == null){
            videoTitle = path;
        } else {
           videoTitle = currentTitle;
        }

        VideoEntry newEntry = new VideoEntry();
        YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
        // TODO: allow this location set via GUI
        //newEntry.setLocation("");
        mg.addCategory(mediaCategory);
        mg.setTitle(new MediaTitle());
        mg.getTitle().setPlainTextContent(videoTitle);
        mg.setKeywords(new MediaKeywords());
        // TODO: allow this set via GUI
        mg.getKeywords().addKeyword(keywords);
        mg.setDescription(new MediaDescription());
        if(description==null) description = videoTitle;
        mg.getDescription().setPlainTextContent(description);
        
        //TODO: allow this set via GUI
        mg.setPrivate(privateVideo);
        newEntry.setMediaSource(ms);

        FileUploadProgressListener listener = new FileUploadProgressListener();
        ResumableGDataFileUploader uploader = null;        
        if(interruptedUploader!=null && path.equals(interruptedPath)){
            uploader=interruptedUploader;
            completeJob = false;
            uploader.resume();
        } else {
            try {
                uploader = new ResumableGDataFileUploader.Builder(
                               service, new URL(RESUMABLE_UPLOAD_URL), ms, newEntry)
                               .title(videoTitle)
                               .trackProgress(listener, PROGRESS_UPDATE_INTERVAL)
                               .chunkSize(DEFAULT_CHUNK_SIZE)        
                               .build();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null,"System error occured.  If this recurs, turn on logging.");
                System.exit(1);
            } catch (ServiceException ex) {
                logger.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null,"YouTube returns an error; it did not like something about the request. If this recurs, turn on logging");
                System.exit(2);
            }
            startEpoch = new Date().getTime();
            uploader.start();
        }
        while (!uploader.isDone()) {
              try {
                  Thread.sleep(PROGRESS_UPDATE_INTERVAL);
              } catch (InterruptedException ex) {
                  logger.log(Level.SEVERE, null, ex);
                  uploader.pause();
                  interruptedUploader=uploader;
                  interruptedPath=path;
                  return;
              }
        }
        interruptedUploader=null;

        switch(uploader.getUploadState()) {
          case COMPLETE:
            uploadSuccess=true;
            logger.info("One video uploaded successfully");
            endEpoch = new Date().getTime();
            break;
          case CLIENT_ERROR:
            uploadSuccess=false;
            logger.warning("Upload Failed");
            break;
          default:
            logger.warning("Unexpected upload status");
            break;
        }
        VideoEntry createdEntry = null;
        try {
            createdEntry = uploader.getResponse(VideoEntry.class);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            uploadSuccess=false;
        } catch (ServiceException ex) {
            logger.log(Level.SEVERE, null, ex);
            uploadSuccess=false;
        }
        uploader.pause();
        System.gc();
        try {
            ms.getInputStream().close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        logger.log(Level.FINE, "video id is {0}", createdEntry.getMediaGroup().getVideoId());
        if(afterPlayListURL!=null){
            System.err.println("moving to playlist");
            logger.fine("Moving to playlist");
            PlaylistEntry ple = new PlaylistEntry(createdEntry);
            try {
                service.insert(afterPlayListURL, ple);
                logger.info("successfully moved to playlist");
                System.err.append("successfully moved to playlist");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                uploadSuccess=false;
            } catch (ServiceException ex) {
                logger.log(Level.SEVERE, null, ex);
                uploadSuccess=false;
            }
        }
    }
    
    private class FileUploadProgressListener implements ProgressListener {
        public synchronized void progressChanged(ResumableHttpFileUploader upl)
        {
            int totalSize=0;
            switch(upl.getUploadState()) {
                case COMPLETE:
                    logger.fine("Upload Completed");
                    GetSetCurrentTotalSize(GetSetCurrentTotalSize(0)+currentFileSize.intValue());
                    break;
                case CLIENT_ERROR:
                    logger.warning("Upload Failed");
                    break;
                case IN_PROGRESS:
                    int percent = (int) (upl.getProgress()*100);
                    UploadDialog.updateFilePb(percent);
                    totalSize=GetSetCurrentTotalSize(0);
                    totalSize += (int)(upl.getProgress()*currentFileSize);
                    UploadDialog.updateTotalSizePb(totalSize);
                    break;
                case NOT_STARTED:
                    logger.warning("Upload Not Started");
                    break;
            }
        }
    }
    
    private synchronized int GetSetCurrentTotalSize (int current){
            if(current==0) return currentSizeKB;
            currentSizeKB = current;
            return currentSizeKB;
    }
    
    public static synchronized void resetCurrentTotalSize(){ currentSizeKB=0; }
}
