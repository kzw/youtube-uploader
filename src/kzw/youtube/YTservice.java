/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.util.AuthenticationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class YTservice {
    //TODO: make it settable via GUI
    private static final String developerKey =
            "AI39si4_Bl1ztDgAcuMHqTu7fZUAmBPHLHwn3LJrsidlTcjlzmotqQrilekYlasesX028ig6rAqd1_4i4hGscghc4wcNm5Zg9A";
    
    private final static YouTubeService service = 
            new YouTubeService("yt uploader", developerKey);
    
    private static String pass="";
    private static String user="";
    private static Boolean signedIn=false;

    static String getUser(){
        return user;
    }
    
    static YouTubeService getService(){
        return service;
    }
        
    public static Boolean login(String u, String p){
        if(pass.equals(p) && user.equals(u) && signedIn) return true;
        pass=p;
        user=u;
        try {
            service.setUserCredentials(user, pass);
        } catch (AuthenticationException ex) {
            Logger.getLogger(YTservice.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
}
