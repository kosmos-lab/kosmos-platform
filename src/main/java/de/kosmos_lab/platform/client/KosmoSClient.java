package de.kosmos_lab.platform.client;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 * test client, primarly used for testing
 */
public class KosmoSClient extends HttpClient {
    private static final Logger logger = LoggerFactory.getLogger("KosmoSClient");

    private final String base;
    private final String user;
    private String pass;
    private String token = null;

    /**
     * create a new Client
     *
     * @param baseurl the base url
     * @param user    the password
     * @param pass    the password
     *
     * @throws Exception some catastrophic failure
     */
    public KosmoSClient(@Nonnull String baseurl, @Nonnull String user, @Nonnull String pass) throws Exception {
        this.base = baseurl;
        this.user = user;
        this.pass = pass;
        this.start();
    }

    public KosmoSClient(@Nonnull String baseurl, @Nullable String token) throws Exception {
        this.base = baseurl;
        this.token = token;
        this.user = null;
        this.pass = null;

        this.start();
    }

    @CheckForNull
    public Request createAuthedDeleteRequest(@Nonnull String url, @CheckForNull JSONObject body) {
        Request request = createAuthedRequest(url, HttpMethod.DELETE);
        if (request != null ) {
            if (body != null) {
                request.content(new StringContentProvider(body.toString()), "application/json");
            }
        }
        return request;

    }

    @CheckForNull
    public Request createAuthedPostRequest(@Nonnull String url, @CheckForNull JSONObject body) {
        Request request = createAuthedRequest(url, HttpMethod.POST);
        if (request != null) {
            if (body != null) {
                request.content(new StringContentProvider(body.toString()), "application/json");
            }
        }
        return request;

    }

    /**
     * create a request with Authorization header
     *
     * @param url    the url to connect to
     * @param method the method to use
     *
     * @return
     */
    @CheckForNull
    public Request createAuthedRequest(@Nonnull String url, @Nonnull HttpMethod method) {
        if (token == null) {
            if (!refreshToken()) {
                return null;
            }
        }
        Request request = createRequest(url, method);
        if ( request != null ) {
            request.header("Authorization", "Bearer " + this.token);
        }
        return request;
    }

    @CheckForNull
    public Request createAuthedRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull JSONObject body) {
        Request request = createAuthedRequest(url, method);
        if (request != null) {
            if (body != null) {
                request.content(new StringContentProvider(body.toString()), "application/json");
            }
        }
        return request;
    }

    @CheckForNull
    public Request createAuthedRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull JSONArray body) {

        Request request = createAuthedRequest(url, method);
        if (request != null) {
            if (body != null) {
                request.content(new StringContentProvider(body.toString()), "application/json");
            }
        }
        return request;
    }

    @CheckForNull
    public Request createAuthedRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull String body) {
        Request request = createAuthedRequest(url, method);
        if (request != null) {
            if (body != null) {
                request.content(new StringContentProvider(body), "application/text");
            }
        }
        return request;

    }

    /**
     * create a requesst to the given url with the given method
     *
     * @param url    the url to use
     * @param method the method to use
     *
     * @return a Request object
     */
    @CheckForNull
    private Request createRequest(@Nonnull String url, @Nonnull HttpMethod method) {
        if (!url.startsWith("http")) {
            url = base + url;
        }
        logger.info("{}: creating {} request for to {}", this.getUserName(), method.name(), url);
        Request request = newRequest(url);
        request.method(method);
        request.agent("KosmoS Client");
        return request;
    }

    /**
     * get the result of calling an URL with the given method
     *
     * @param url    the URL to call
     * @param method the method to use [GET,POST,DELETE,PUT...]
     *
     * @return
     */

    @CheckForNull
    public JSONArray fetchJSONArray(@Nonnull String url, @Nonnull HttpMethod method) {
        Request request = createAuthedRequest(url, method);
        if (request != null) {
            ContentResponse response = getResponse(request);
            if (response != null) {
                return new JSONArray(response.getContentAsString());
            }

        }
        return null;

    }

    @CheckForNull
    public JSONObject fetchJSONObject(@Nonnull String url, @Nonnull HttpMethod method) {
        Request request = createAuthedRequest(url, method);
        if (request != null) {
            ContentResponse response = getResponse(request);
            if (response != null) {
                return new JSONObject(response.getContentAsString());
            }

        }
        return null;
    }

    /**
     * get the result of calling an URL with GET
     *
     * @param url the URL to call
     *
     * @return
     */
    @CheckForNull
    public JSONArray getJSONArray(@Nonnull String url) {
        return fetchJSONArray(url, HttpMethod.GET);
    }

    @CheckForNull
    public JSONObject getJSONObject(String url) {
        return fetchJSONObject(url, HttpMethod.GET);
    }

    @CheckForNull
    public String getPassword() {
        return this.pass;
    }

    @CheckForNull
    public ContentResponse getResponse(@Nonnull String url,@Nonnull HttpMethod method,@CheckForNull String body) {
        Request request = this.createAuthedRequest(url, method, body);
        if (request != null) {
            return this.getResponse(request);
        }
        return null;
    }

    @CheckForNull public ContentResponse getResponse(@Nonnull String url,@Nonnull HttpMethod method,@CheckForNull JSONObject body) {
        Request request = this.createAuthedRequest(url, method, body);
        if (request != null) {
            return this.getResponse(request);
        }
        return null;

    }

    @CheckForNull public ContentResponse getResponse(@Nonnull String url,@Nonnull HttpMethod method,@CheckForNull JSONArray body) {
        Request request = this.createAuthedRequest(url, method, body);
        if (request != null) {
            return this.getResponse(request);
        }
        return null;

    }

    @CheckForNull  public ContentResponse getResponse(@Nonnull String url,@Nonnull HttpMethod method) {

        Request request = this.createAuthedRequest(url, method);
        if (request != null) {
            return this.getResponse(request);
        }
        return null;

    }

    /**
     * get the response for a given result, if 401 is returned the jwt will be renewed and it will be tried again
     *
     * @param request the request to parse
     *
     * @return
     */
    @CheckForNull
    public ContentResponse getResponse(@CheckForNull Request request) {
        if (request == null ) {
            return null;
        }
        ContentResponse response = null;
        try {
            response = request.send();
            if (response.getStatus() == 401) {
                refreshToken();
                request.header("Authorization", "Bearer " + this.token);
                response = request.send();
            }

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("could not get Response for Request", e);
        }
        return response;

    }

    @CheckForNull public String getUserName() {
        return this.user;
    }

    @CheckForNull public String login() {
        Request request = this.createRequest("/user/login", HttpMethod.POST);
        if (request != null ) {
            try {
                request.param("user", user);
                request.param("pass", pass);
                ContentResponse response = request.send();
                logger.info("login status: {}", response.getStatus());
                if (response.getStatus() == 200) {
                    return response.getContentAsString();

                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("could not get Response for Request", e);
            }
        }
        return null;
    }

    /**
     * post the given body to a specific url
     *
     * @param url  the url to post it to
     * @param body the body to post
     *
     * @return the JSONObject returned from
     */
   @CheckForNull public JSONObject postJSONObject(@Nonnull String url,@CheckForNull JSONObject body) {
       ContentResponse response = getResponse(url, HttpMethod.POST, body);
        if (response != null )  {
            return new JSONObject(response.getContentAsString());
        }
        return null;
    }

    /**
     * post the given body to a specific url
     *
     * @param url  the url to post it to
     * @param body the body to post
     *
     * @return the JSONObject returned from
     */
    @CheckForNull public ContentResponse postJSONObject2(@Nonnull String url,@CheckForNull JSONObject body) {
        return getResponse(url, HttpMethod.POST, body);
    }

    /**
     * get a new JWT token (try to login)
     *
     * @return true if login was successful
     */
    public boolean refreshToken() {
        if (user != null && pass != null) {
            String t = login();
            if (t != null) {
                this.token = t;
                return true;
            }
        } else {
            this.token = "";
            return true;
        }
        return false;

    }

    public void setPassword(@Nonnull  String pass) {
        this.pass = pass;
    }

}
