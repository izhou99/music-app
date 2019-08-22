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
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.ImmutableList;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.api.client.util.store.MemoryDataStoreFactory.getDefaultInstance;

public class SpotifyAppClient extends MusicAppClient {
    private SpotifyApi spotifyApi;
    /** OAuth 2 scope. */
    private static final String SCOPE = "read";

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static final String SPOTIFY_ID = "SPOTIFY_ID";

    private SpotifyAppClient(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public static final String SPOTIFY_SECRET = "SPOTIFY_SECRET";
    private static final String TOKEN_SERVER = "https://accounts.spotify.com/api/token";
    private static final String AUTH_SERVER = "https://accounts.spotify.com/authorize";
    private static final List<String> scopes = ImmutableList.of("user-read-private", "playlist-read-private","playlist-read-collaborative","user-top-read");
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
    Map<String, String> getPlaylist(String playlist) throws IOException, SpotifyWebApiException {
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
        for (PlaylistSimplified item: items) {
            playlistNameToId.put(item.getName().toLowerCase(),item.getId());
        }
        System.out.println(playlistNameToId);
        String playlistId = playlistNameToId.get(playlist.toLowerCase());
        GetPlaylistsTracksRequest getPlaylistsTracksRequest = spotifyApi
                .getPlaylistsTracks(playlistId)
                .build();
        Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsTracksRequest.execute();
        PlaylistTrack[] tracks = playlistTrackPaging.getItems();
        Map<String, String> trackNameToId = new HashMap<>();
        for (PlaylistTrack item: tracks) {
            trackNameToId.put(item.getTrack().getName(),item.getTrack().getId());
        }
        return trackNameToId;
    }
}
