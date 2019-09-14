import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;

public class Driver {

    public static void main(String[] args) throws IOException, SpotifyWebApiException {
        MusicAppClient musicAppClient = SpotifyAppClient.getSpotifyAppClient();
        List<Song> songs = musicAppClient.getPlaylist("Test");
        List<Song> toRemoveSongs = musicAppClient.removeDups(songs, true);
        System.out.println("Original Songs " + songs.toString());
        System.out.println("Songs to Remove: " + toRemoveSongs);
        musicAppClient.removeTrackFromPlaylist(toRemoveSongs);
        songs = musicAppClient.getPlaylist("Test");
        System.out.println("Deduped Playlist: " + songs);
    }
}
