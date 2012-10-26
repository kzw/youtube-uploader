/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kzw.youtube;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author me
 */
public class PlayList {
    
   private static HashMap playListMAP =new HashMap();
   private final static Pattern playListURLPattern = Pattern.compile("tag:youtube.+:user:.+:playlist:(.+)$");

   public static URL getURL(String name) throws IOException, ServiceException{
        String url = "http://gdata.youtube.com/feeds/api/users/"+YTservice.getUser()+"/playlists";
        VideoFeed feedList = YTservice.getService().getFeed(new URL(url), VideoFeed.class);
        if(!playListMAP.isEmpty()){
            if(playListMAP.containsKey(name)) return (URL) playListMAP.get(name);
            return null;
        }
        Matcher m;
        if(feedList.getEntries().isEmpty()){
            System.out.println("No entries in play list");
            return null;
        }
        for (VideoEntry v: feedList.getEntries()){
            m=playListURLPattern.matcher(v.getId());
            if(!m.find()){
                System.out.println("Cannot parse playlist ID");
                return null;
            }
            url="http://gdata.youtube.com/feeds/api/playlists/"+m.group(1);
            playListMAP.put(v.getTitle().getPlainText(),new URL(url));
        }
        if(playListMAP !=null && playListMAP.containsKey(name)) return (URL) playListMAP.get(name);
        return null;
    }
    
}
