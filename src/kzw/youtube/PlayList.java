package kzw.youtube;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
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
    private final static String listOfListURL = "http://gdata.youtube.com/feeds/api/users/default/playlists";
    private final static String BASE_URL = "http://gdata.youtube.com/feeds/api/playlists/";

   public static Set getList() throws IOException, ServiceException{
       if(playListMAP.isEmpty()) getMap();
       return playListMAP.keySet();
   }
   
   private static void getMap() throws IOException, ServiceException{
        VideoFeed feedList = YTservice.getService().getFeed(new URL(listOfListURL), VideoFeed.class);
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
            playListMAP.put(v.getTitle().getPlainText(),new URL(BASE_URL + m.group(1)));
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
   
   public static void create(String name) throws MalformedURLException, IOException, ServiceException{

        PlaylistLinkEntry newEntry = new PlaylistLinkEntry();
        newEntry.setTitle(new PlainTextConstruct(name));
        newEntry.setSummary(new PlainTextConstruct(name));
        newEntry.setPrivate(true);
        PlaylistLinkEntry createdEntry = YTservice.getService().insert(new URL(listOfListURL), newEntry);
        playListMAP.put(name, new URL(BASE_URL + createdEntry.getPlaylistId()));
   }
}
