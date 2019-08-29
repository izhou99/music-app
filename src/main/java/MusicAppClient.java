import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.*;

public abstract class MusicAppClient {
    /**
     * A list of songs to remove from the playlist
     * @param originalSongs
     * @param includeRemix
     * @return
     */
    List<Song> removeDups(List<Song> originalSongs, boolean includeRemix) {
        List<Song> songsToDelete = new ArrayList<>();
        List<Song> processedSongs = new ArrayList<>();

        while (!originalSongs.isEmpty()) {
            Song song = originalSongs.remove(0);
            List<Song> repeats = new ArrayList<>();
            for (Song s : originalSongs) {
                if (song.isRepeat(s)) {
                    repeats.add(s);
                }
            }
            originalSongs.removeAll(repeats);
            songsToDelete.addAll(repeats);
            processedSongs.add(song);
        }


        return songsToDelete;
    }

    abstract List<Song> getPlaylist(String playlist) throws IOException, SpotifyWebApiException;
}
