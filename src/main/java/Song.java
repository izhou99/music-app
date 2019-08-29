import java.util.List;

public class Song {
    private String name;
    private String id;
    private List<String> artists;

    public Song(String name, String id, List<String> artists) {
        this.name = name;
        this.id = id;
        this.artists = artists;
    }

    public String toString() {
        return String.format("Song: %s, Id: %s Artists: %s", name, id, artists);
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
}
