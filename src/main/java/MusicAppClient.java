import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class MusicAppClient {
    List<String> removeDups(List<String> originalSongs, boolean includeRemix) {
        return null;
    }

    abstract Map<String, String> getPlaylist(String playlist) throws IOException, SpotifyWebApiException;
}
