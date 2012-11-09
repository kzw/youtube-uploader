package kzw.youtube.gui;

import com.google.gdata.util.ServiceException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import kzw.youtube.PlayList;
import kzw.youtube.YTservice;
import kzw.youtube.YouTubeLogger;
import kzw.youtube.doWork;

public class DataPanel extends JPanel implements ActionListener{
    
    //TODO allow user input on this
    private Preferences P=Preferences.userNodeForPackage(getClass());
    private static final String PLAYLIST_KEY = "playlist";
    private static final String LAST_DIR="last dir";
    private static final String MOVEFOLDER_KEY = "movefolder";
    private static final Logger logger = YouTubeLogger.getIt(DataPanel.class.getName());
    
    private final static String INFO_LEVEL = "info";
    private final static String VERBOSE_LEVEL = "verbose";
    private final static String OFF_LEVEL = "off";

    
    private final JRadioButton infoLevel = new JRadioButton("info");
    private final JRadioButton verboseLevel = new JRadioButton("verbose");
    private final JRadioButton offLevel = new JRadioButton("off");
    
    public static Level selectedLogLevel=Level.OFF;
    
    private final static Pattern pattern = Pattern.compile(".(mp4|dvr-ms|mts|wmv|avi|3gp|mov|mpg)$",Pattern.CASE_INSENSITIVE);
    private File temp = null;
    private final JTextField keywordsInput = new JTextField();
    private boolean badKeywords=false;

    
    public DataPanel() throws Exception{
        super(new GridLayout(0,2));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //<editor-fold defaultstate="collapsed" desc="log level UI">
        addLabel("Select log level");
        final ButtonGroup bg = new ButtonGroup();
        bg.add(offLevel);
        bg.add(infoLevel);
        bg.add(verboseLevel);
        offLevel.setSelected(true);
        
        // TODO: make these constants into numbers
        infoLevel.setActionCommand(INFO_LEVEL);
        verboseLevel.setActionCommand(VERBOSE_LEVEL);
        offLevel.setActionCommand(OFF_LEVEL);
        
        final JPanel buttonP = new JPanel(new GridLayout(0,3));
        buttonP.add(offLevel);
        buttonP.add(infoLevel);
        buttonP.add(verboseLevel);
        add(buttonP);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="title seed UI">
        addLabel("Title seed");
        final JTextField titleInput = new JTextField();
        add(titleInput);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Description UI">        
        addLabel("Description");
        final JTextField descriptionInput = new JTextField();
        add(descriptionInput);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="keywords UI">
        addLabel("Keywords");
        add(keywordsInput);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="playlist UIs">
        // TODO get this from drop down.. allow creation
        addLabel("Move videos to this playlist");
        final JTextField playlistInput = new JTextField();
        add(playlistInput);
        playlistInput.setText(P.get(PLAYLIST_KEY, ""));
        playlistInput.setToolTipText("Type playlist to move your uploaded videos");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="move to folder UI">
        addLabel("folder to move to after upload");
        final JTextField moveFolder = new JTextField();
        moveFolder.setEditable(false);
        moveFolder.addMouseListener(new MouseListener(){

            @Override
            public void mouseClicked(MouseEvent me) {
                JFileChooser fc1 = new JFileChooser(moveFolder.getText());
                fc1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if( fc1.showOpenDialog(DataPanel.this)!= JFileChooser.APPROVE_OPTION) {
                    moveFolder.setText(null);
                    return;
                }
                File file = fc1.getSelectedFile();
                moveFolder.setText(file.getAbsolutePath());
                P.put(MOVEFOLDER_KEY, file.getAbsolutePath());
            }

            //<editor-fold defaultstate="collapsed" desc="no ops">
            @Override
            public void mousePressed(MouseEvent me) { }

            @Override
            public void mouseReleased(MouseEvent me) { }

            @Override
            public void mouseEntered(MouseEvent me) { }

            @Override
            public void mouseExited(MouseEvent me) { }
            //</editor-fold>
        });
        add(moveFolder);
        moveFolder.setText(P.get(MOVEFOLDER_KEY, null));
        moveFolder.setToolTipText("Click here to set a folder on your computer to move uploaded files");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="choose video UI part 1">
        addLabel("choose videos");
        final JTextField videos = new JTextField();
        videos.setEditable(false);
        videos.setText(P.get(LAST_DIR, ""));
        //</editor-fold>
        
        // This variable is needed earlier
        final JButton uploadButton = new JButton("upload");
        
        //<editor-fold defaultstate="collapsed" desc="choose video UI part 2">
        videos.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent me) {
                logger.setLevel(DataPanel.selectedLogLevel);
                String dir=P.get(LAST_DIR, "");
                JFileChooser fc = new JFileChooser(dir);
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if( fc.showOpenDialog(DataPanel.this)!= JFileChooser.APPROVE_OPTION ) return;
                MainPanel.log.setText(null);
                try {
                    temp = File.createTempFile("test", ".videos");
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                temp.deleteOnExit();
                File[] file = fc.getSelectedFiles();
                Integer numberOfFiles = file.length;
                dir =file[0].getParent();
             
                P.put(LAST_DIR, dir);
                videos.setText(dir);
                String[] fList=file[0].list();
                BufferedWriter tempOut = null;
                try {
                    tempOut = new BufferedWriter(new FileWriter(temp));
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                Integer count = 0;
                for(Integer i=0;i<numberOfFiles;i++){
                    ArrayList<String> paths=new ArrayList<String>();
                    try {
                        if(file[i].isDirectory()){
                            paths=getVideosInDir(file[i].listFiles());
                        } else {
                            paths.add(file[i].getAbsolutePath());
                        }
                        for (String p: paths){
                            count++;
                            tempOut.write(p+"\n");
                            MainPanel.log.append(p+"\n");
                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    tempOut.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                if(count>0) {
                    UploadDialog.resetPB();
                    uploadButton.setEnabled(true);
                }
            }
            //<editor-fold defaultstate="collapsed" desc="no ops">
            @Override
            public void mousePressed(MouseEvent me) { }

            @Override
            public void mouseReleased(MouseEvent me) { }

            @Override
            public void mouseEntered(MouseEvent me) { }

            @Override
            public void mouseExited(MouseEvent me) { }
            //</editor-fold>
        });
        add(videos);
        videos.setToolTipText("Click here to select your videos");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="sleep UI">
        addLabel("sleep/minute");
        final JTextField sleepValue = new JTextField();
        add(sleepValue);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="upload button UI">         
        uploadButton.setEnabled(false);
        add(uploadButton);
        uploadButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                logger.setLevel(DataPanel.selectedLogLevel);
                if(!uploadButton.isEnabled()) return;
                String sleepString = sleepValue.getText();
                if(sleepString.equals("")) sleepString="0";
                Integer sleepMinute = Integer.parseInt(sleepString);
                              
                //<editor-fold defaultstate="collapsed" desc="get user and password and login">
                
                String userNameString=YouTubeFrame.UserNameString;
                if(userNameString.isEmpty()){
                    JOptionPane.showMessageDialog(null,"Username required.  Set it in the menu");
                    return;
                }
                String passwordString = YouTubeFrame.PasswordString;
                if(passwordString.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Password required.  Set it in the menu");
                    return;
                }

                if(!YTservice.login(userNameString, passwordString)){
                    JOptionPane.showMessageDialog(null,"Cannot login.  Check credentials");
                    return;
                }
                //</editor-fold>
                
                //<editor-fold defaultstate="collapsed" desc="get playlist url">
                URL playListFeedURL=null;
                String playListName=playlistInput.getText();
                if(playListName!=null && !playListName.equals("")){
                    try {
                        playListFeedURL=PlayList.getURL(playListName);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    } catch (ServiceException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    P.put(PLAYLIST_KEY, playListName);
                }
                //</editor-fold>
                
                MainPanel.log.setText(null);
                final doWork worker = new doWork(temp);
                worker.setDir(moveFolder.getText())
                        .setPlayListURL(playListFeedURL)
                        .setSleep(1L*sleepMinute)
                        .setTitle(titleInput.getText())
                        .setDescription(descriptionInput.getText());
                String keywords = getKeywords();
                if(keywords!=null) worker.setKeywords(keywordsInput.getText());
                else if(badKeywords){
                    JOptionPane.showMessageDialog(null, "Contains very long keywords and YouTube will reject. Use comma separated words");
                    return;
                }
                worker.start();
                new UploadDialog();
                if(worker.isAlive()){
                    worker.interrupt();
                    try {
                        worker.join();
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        //</editor-fold>
    }
    
    void addListeners(){
        infoLevel.addActionListener(this);
        verboseLevel.addActionListener(this);
        offLevel.addActionListener(this);        
    }
          
    private void addLabel(String labelText){
        JLabel label = new JLabel(labelText);
        add(label);
    }
    
    private ArrayList<String> getVideosInDir(File[] allFiles){
        ArrayList<String> videoPaths=new ArrayList();
        Matcher matcher;
         for (int i=0;i<allFiles.length;i++){
            matcher = pattern.matcher(allFiles[i].getAbsolutePath());
            if(!matcher.find()) continue;
            videoPaths.add(allFiles[i].getAbsolutePath());
         }
        return videoPaths;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        String ac = ae.getActionCommand();
        if(ac.equals(INFO_LEVEL)){
            selectedLogLevel=Level.INFO;
        } else if(ac.equals(VERBOSE_LEVEL)){
            selectedLogLevel=Level.FINE;
        } else{
            selectedLogLevel=Level.OFF;
        }
    }
    
    private String getKeywords(){
        String kw = keywordsInput.getText();
        if(kw==null) return null;
        String nicer = "";
        String[] words =kw.split(",");
        int num = words.length;
        num--;
        for(int i=0;i <= num;i++){
            String t = words[i].trim();
            if(t.length()>30){
                badKeywords = true;
                return null;
            }
            nicer += t;
            if(i<num) nicer += ',';
        }
        return nicer;
    }
}
