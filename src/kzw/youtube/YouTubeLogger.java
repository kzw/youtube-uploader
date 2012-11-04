/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author kz
 */
public class YouTubeLogger {
    public static Handler lh = new LogHandler();
    private static Boolean first_time = true;

    public static Logger getIt(String className){
        final Logger logger = Logger.getLogger(className);
        if(first_time){
            lh.setFormatter(new SimpleFormatter());
            lh.setLevel(Level.FINE);
            first_time=false;
        }
        logger.addHandler(lh);
        logger.setUseParentHandlers(false);
        return logger;
    }    
}
