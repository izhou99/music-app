import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;

public abstract class MusicAppClient {
    List<Song> removeDups(List<String> originalSongs, boolean includeRemix) {
        return null;
    }

    abstract List<Song> getPlaylist(String playlist) throws IOException, SpotifyWebApiException;
}
