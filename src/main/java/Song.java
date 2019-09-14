import java.util.List;
import java.util.Objects;

public class Song {
    private String name;
    private String id;
    private List<String> artists;
    private String playlistId;

    public Song(String name, String id, List<String> artists, String playlistId) {
        this.name = name;
        this.id = id;
        this.artists = artists;
        this.playlistId = playlistId;
    }

    public String toString() {
        return String.format("Song: %s, Id: %s Artists: %s PlayList ID: %s\n", name, id, artists, playlistId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public boolean isRepeat(Song song) {
        boolean sameArtist = false;
        boolean sameName = false;
        if(song.getArtists().equals(this.artists)) {
            sameArtist = true;
        }
        if(song.getName().equals(this.name)) {
            sameName = true;
        }
        return sameArtist && sameName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(name, song.name) &&
                Objects.equals(id, song.id) &&
                Objects.equals(artists, song.artists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, artists);
    }
}
