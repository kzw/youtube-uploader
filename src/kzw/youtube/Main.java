package kzw.youtube;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import kzw.youtube.gui.YouTubeFrame;

public class Main {
    
    public static YouTubeFrame frame;
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    frame = new YouTubeFrame();        
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }
}
