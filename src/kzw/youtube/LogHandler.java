package kzw.youtube;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import kzw.youtube.gui.DataPanel;
import kzw.youtube.gui.UploadDialog;

/**
 *
 * @author kz
 */
class LogHandler extends Handler{

    @Override
    public void publish(LogRecord lr) { 
        if(DataPanel.selectedLogLevel.intValue()<Level.INFO.intValue()){
            UploadDialog.writeLog(getFormatter().format(lr));
        } else {
            UploadDialog.writeLog(lr.getThreadID()+" "+lr.getSourceClassName()+" "+lr.getSourceMethodName()+" "+getFormatter().formatMessage(lr) +"\n");
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="no ops">
    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    //</editor-fold>
}
