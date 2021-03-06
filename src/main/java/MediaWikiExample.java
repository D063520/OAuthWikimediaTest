package com.github.scribejava.apis.examples;

import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParser;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.apis.MediaWikiApi;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Parameter;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MediaWikiExample {

    // To get your consumer key/secret, see https://meta.wikimedia.org/wiki/Special:OAuthConsumerRegistration/propose
    private static final String CONSUMER_KEY = "";
    private static final String CONSUMER_SECRET = "";

    private static final String API_USERINFO_URL
            = "https://meta.wikimedia.org/w/api.php?action=query&format=json&meta=userinfo";

    private MediaWikiExample() {
    }

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String... args) throws IOException, InterruptedException, ExecutionException, ParseException {
        final OAuth10aService service = new ServiceBuilder(CONSUMER_KEY)
                .apiSecret(CONSUMER_SECRET)
                .build(MediaWikiApi.instance());

        final Scanner in = new Scanner(System.in);

        System.out.println("=== MediaWiki's OAuth Workflow ===");
        System.out.println();

        // Obtain the Request Token
        System.out.println("Fetching the Request Token...");
        final OAuth1RequestToken requestToken = service.getRequestToken();
        System.out.println("Got the Request Token!");
        System.out.println();

        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(service.getAuthorizationUrl(requestToken));
        System.out.println("And paste the verifier here");
        System.out.print(">>");
        final String oauthVerifier = in.nextLine();
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);
        System.out.println("Got the Access Token!");
        System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
        System.out.println();

        // Now let's go and ask for a protected resource!
        System.out.println("Now we're going to access a protected resource...");
        OAuthRequest request = new OAuthRequest(Verb.GET, API_USERINFO_URL);
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            System.out.println("Got it! Lets see what we found...");
            System.out.println();
            System.out.println(response.getBody());
        }

        request = new OAuthRequest(Verb.POST, "https://www.wikidata.org/w/api.php" );
        request.addParameter("action", "query");
        request.addParameter("format", "json");
        request.addParameter("meta", "tokens");
        //OAuth1AccessToken newAccess = new OAuth1AccessToken(user.getAccessToken(),user.getAccessTokenSecret());
        service.signRequest(accessToken, request);
        Response response = service.execute(request);
        System.out.println(response.getBody());
        System.out.println(response.getHeaders().get("Set-Cookie"));
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(response.getBody());
        String csrftoken = ((JSONObject) ((JSONObject) jsonObject.get("query")).get("tokens")).get("csrftoken").toString();
        System.out.println(csrftoken);


        request = new OAuthRequest(Verb.POST, "https://www.wikidata.org/w/api.php" );
        //request.getHeaders().put("X-Wikimedia-Debug","backend=mwdebug1001.eqiad.wmnet; log");
        request.addParameter("action", "wbsetlabel");
        request.addParameter("id", "Q47545479");
        request.addParameter("language", "it");
        request.addParameter("value", "Sistema di Question Answering per dati in formato RDF");
        request.addParameter("format", "json");
        request.addParameter("token", csrftoken);
        System.out.println(request.getUrl());
        for (Parameter a : request.getBodyParams().getParams()){
            System.out.println(a.getValue());
        }
        service.signRequest(accessToken, request);
        response = service.execute(request);
        System.out.println(response.getBody());

        System.out.println();
        System.out.println("Thats it man! Go and build something awesome with MediaWiki and ScribeJava! :)");
    }

}
