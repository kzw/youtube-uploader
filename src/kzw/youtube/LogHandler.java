/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.uploadFrame;

/**
 *
 * @author kz
 */
class LogHandler extends Handler{

    @Override
    public void publish(LogRecord lr) { 
        if(DataPanel.selectedLogLevel.intValue()<Level.INFO.intValue()){
            uploadFrame.writeLog(getFormatter().format(lr));
        } else {
            uploadFrame.writeLog(lr.getThreadID()+" "+lr.getSourceClassName()+" "+lr.getSourceMethodName()+" "+lr.getMessage()+"\n");
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
}
