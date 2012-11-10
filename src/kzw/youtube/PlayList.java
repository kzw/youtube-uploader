package kzw.youtube;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author me
 */
public class PlayList {
    
    
   // TODO create playlist 
   private static HashMap playListMAP =new HashMap();
   private final static Pattern playListURLPattern = Pattern.compile("tag:youtube.+:user:.+:playlist:(.+)$");

   public static Set getList() throws IOException, ServiceException{
       if(playListMAP.isEmpty()) getMap();
       return playListMAP.keySet();
   }
   
   private static void getMap() throws IOException, ServiceException{
        String url = "http://gdata.youtube.com/feeds/api/users/default/playlists";
        VideoFeed feedList = YTservice.getService().getFeed(new URL(url), VideoFeed.class);
        Matcher m;
        if(feedList.getEntries().isEmpty()){
            System.out.println("No entries in play list");
            return;
        }
        for (VideoEntry v: feedList.getEntries()){
            m=playListURLPattern.matcher(v.getId());
            if(!m.find()){
                System.err.println("Cannot parse playlist ID");
                return;
            }
            url="http://gdata.youtube.com/feeds/api/playlists/"+m.group(1);
            playListMAP.put(v.getTitle().getPlainText(),new URL(url));
        }   
   }
  
   public static URL getURL(String name) throws IOException, ServiceException {
        if(!playListMAP.isEmpty()){
            if(playListMAP.containsKey(name)) return (URL) playListMAP.get(name);
        }
        getMap();
        if(playListMAP !=null && playListMAP.containsKey(name)) return (URL) playListMAP.get(name);
        return null;
    }
    
}
