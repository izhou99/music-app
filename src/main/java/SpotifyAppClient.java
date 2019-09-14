import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.RemoveTracksFromPlaylistRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.api.client.util.store.MemoryDataStoreFactory.getDefaultInstance;

public class SpotifyAppClient extends MusicAppClient {
    private SpotifyApi spotifyApi;
    /**
     * OAuth 2 scope.
     */
    private static final String SCOPE = "read";

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Global instance of the JSON factory.
     */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static final String SPOTIFY_ID = "SPOTIFY_ID";

    private SpotifyAppClient(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public static final String SPOTIFY_SECRET = "SPOTIFY_SECRET";
    private static final String TOKEN_SERVER = "https://accounts.spotify.com/api/token";
    private static final String AUTH_SERVER = "https://accounts.spotify.com/authorize";
    private static final List<String> scopes = ImmutableList.of("playlist-modify-public","user-read-private", "playlist-read-private", "playlist-read-collaborative", "user-top-read");
    private static final String LOCALHOST = "localhost";
    private static final int PORT = 8888;

    public static MusicAppClient getSpotifyAppClient() throws IOException {
        String clientId = System.getenv(SPOTIFY_ID);
        String clientSecret = System.getenv(SPOTIFY_SECRET);

        AuthorizationCodeFlow flow =
                new AuthorizationCodeFlow.Builder(
                        BearerToken.formEncodedBodyAccessMethod(),
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        new GenericUrl(TOKEN_SERVER),
                        new ClientParametersAuthentication(clientId, clientSecret),
                        clientId,
                        AUTH_SERVER)
                        .setDataStoreFactory(getDefaultInstance())
                        .setScopes(scopes)
                        .build();

        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder().setHost(LOCALHOST).setPort(PORT).build();
        Credential creds = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        creds.getAccessToken();
        SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(creds.getAccessToken()).build();

        return new SpotifyAppClient(spotifyApi);
    }

    @Override
    List<Song> getPlaylist(String playlist) throws IOException, SpotifyWebApiException {
        // Gets current user name
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile()
                .build();
        User user = getCurrentUsersProfileRequest.execute();

        System.out.println("Display name: " + user.getDisplayName() + user.getId());

        // Gets the users playlist names and playlist ids
        GetListOfUsersPlaylistsRequest getListOfUsersPlaylistsRequest = spotifyApi
                .getListOfUsersPlaylists(user.getId())
                .build();
        Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfUsersPlaylistsRequest.execute();
        PlaylistSimplified[] items = playlistSimplifiedPaging.getItems();
        Map<String, String> playlistNameToId = new HashMap<>();
        for (PlaylistSimplified item : items) {
            playlistNameToId.put(item.getName().toLowerCase(), item.getId());
        }
        System.out.println(playlistNameToId);
        String playlistId = playlistNameToId.get(playlist.toLowerCase());
        GetPlaylistsTracksRequest getPlaylistsTracksRequest = spotifyApi
                .getPlaylistsTracks(playlistId)
                .build();
        Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsTracksRequest.execute();
        PlaylistTrack[] tracks = playlistTrackPaging.getItems();
        List<Song> songs = new ArrayList<>();
        for (PlaylistTrack item : tracks) {
            songs.add(new Song(item.getTrack().getName(), item.getTrack().getId(), convertArtists(item.getTrack().getArtists()), playlistId));
        }
        return songs;
    }

    private List<String> convertArtists(ArtistSimplified[] original) {
        List<String> artists = new ArrayList<>();
        for (ArtistSimplified artist : original) {
            artists.add(artist.getName());
        }
        return artists;
    }

    @Override
    void removeTrackFromPlaylist(List<Song> songsToDelete) throws IOException, SpotifyWebApiException {
        // correctly removes the song from the playlist.
        // TODO: the same song with the same track ID can be added multiple times in the same playlist
        // this function will remove both tracks. next step is to remove duplicates but keep one song
        JsonParser parser = new JsonParser();
        JsonArray tracks = new JsonArray();
        for (Song song: songsToDelete) {
            String url = String.format("[{\"uri\":\"spotify:track:%s\"}]", song.getId());
            tracks.addAll(parser.parse(url).getAsJsonArray());
        }

        if (!tracks.isJsonNull() && !songsToDelete.get(0).getPlaylistId().isEmpty()) {
            RemoveTracksFromPlaylistRequest removeTracksFromPlaylistRequest = spotifyApi
                    .removeTracksFromPlaylist(songsToDelete.get(0).getPlaylistId(), tracks)
                    .build();
            SnapshotResult snapshotResult = removeTracksFromPlaylistRequest.execute();
            System.out.println("Snapshot ID: " + snapshotResult.getSnapshotId());
        }
    }
}
