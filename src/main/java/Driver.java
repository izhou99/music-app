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
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.ImmutableList;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.google.api.client.json.JsonFactory;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Driver {
  /** OAuth 2 scope. */
  private static final String SCOPE = "read";

  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  public static final String SPOTIFY_ID = "SPOTIFY_ID";
  public static final String SPOTIFY_SECRET = "SPOTIFY_SECRET";
  private static final String TOKEN_SERVER = "https://accounts.spotify.com/api/token";
  private static final String AUTH_SERVER = "https://accounts.spotify.com/authorize";
  private static final List<String> scopes = ImmutableList.of("user-top-read");
  private static final String LOCALHOST = "localhost";
  private static final int PORT = 8888;

  public static void main(String[] args) throws IOException, SpotifyWebApiException {
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
            .setDataStoreFactory(new FileDataStoreFactory(new File(System.getProperty("user.dir"))))
            .setScopes(scopes)
            .build();

    LocalServerReceiver receiver =
        new LocalServerReceiver.Builder().setHost(LOCALHOST).setPort(PORT).build();
    Credential creds = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    creds.getAccessToken();
    SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(creds.getAccessToken()).build();

    GetUsersTopTracksRequest request = spotifyApi.getUsersTopTracks().build();
    Paging<Track> response = request.execute();
    Track[] items = response.getItems();
    System.out.println("Ivan's top tracks: ");
    for (Track track : items) {
      System.out.println(track.getName());
    }
  }
}
