/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package kzw.youtube;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.SwingUtilities;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.YouTubeFrame;

/**
 * Demonstrates YouTube Data API operation to upload large media files.
 *
 * 
 */
public class Main {
    /**
    * YouTubeUploadClient is a sample command line application that
    * demonstrates how to upload large media files to youtube.  This sample
    * uses resumable upload feature to upload large media.
    *
    * @param args Used to pass the username and password of a test account.
    */
    
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
