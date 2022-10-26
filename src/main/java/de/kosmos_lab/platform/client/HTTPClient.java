package de.kosmos_lab.platform.client;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * common HTTP Client used for KosmoS
 */
public class HTTPClient extends HttpClient {
    public HTTPClient() throws Exception {
        super();
        this.start();
    }

    private static final Logger logger = LoggerFactory.getLogger("HTTPClient");

    /**
     * create a request to the given url with the given method
     *
     * @param url    the url to use
     * @param method the method to use [GET,POST,DELETE,PUT...]
     *
     * @return a Request object
     */
    @Nonnull
    public Request createRequest(@Nonnull String url, @Nonnull HttpMethod method) {

        logger.info("creating {} request to {}", method.name(), url);
        Request request = newRequest(url);
        request.method(method);
        request.agent("KosmoS HTTP Client");
        return request;
    }

    /**
     * create a request to the given url with the given method
     *
     * @param url    the url to use
     * @param method the method to use [GET,POST,DELETE,PUT...]
     * @param body   the string encoded body to send
     *
     * @return a Request object
     */
    @Nonnull
    public Request createRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull String body) {


        Request request = createRequest(url, method);
        if (body != null) {
            request.content(new StringContentProvider(body), "application/text");
        }
        return request;
    }

    /**
     * create a request to the given url with the given method
     *
     * @param url    the url to use
     * @param method the method to use [GET,POST,DELETE,PUT...]
     * @param body   the JSONObject to send as a body
     *
     * @return a Request object
     */
    public @Nonnull
    Request createRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull Object body) {


        Request request = createRequest(url, method);

        if (body != null) {
            if (body instanceof JSONObject || body instanceof JSONArray) {
                request.content(new StringContentProvider(body.toString()), "application/json");
            }
            if (body instanceof String) {
                request.content(new StringContentProvider((String) body), "application/text");
            } else {
                request.content(new StringContentProvider(body.toString()), "application/text");
            }
        }
        return request;
    }

    /**
     * create a request to the given url with the given method
     *
     * @param url    the url to use
     * @param method the method to use [GET,POST,DELETE,PUT...]
     * @param body   the JSONArray to send as a body
     *
     * @return a Request object
     */
    @Nonnull
    public Request createRequest(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull JSONArray body) {


        Request request = createRequest(url, method);
        if (body != null) {
            request.content(new StringContentProvider(body.toString()), "application/json");
        }
        return request;
    }

    /**
     * fetch a JSONArray from the given URL
     *
     * @param url    the URL to call
     * @param method the method to use [GET,POST,DELETE,PUT...]
     *
     * @return a JSONArray
     */
    @CheckForNull
    public JSONArray fetchJSONArray(@Nonnull String url, @Nonnull HttpMethod method) {
        ContentResponse r = getResponse(createRequest(url, method));
        if (r != null) {
            try {
                return new JSONArray(r.getContentAsString());
            } catch (JSONException ex) {
                logger.error("could not parse response to JSON!", ex);
            }
        }
        return null;
    }

    /**
     * fetch a JSONObject from the given URL
     *
     * @param url    the URL to call
     * @param method the method to use [GET,POST,DELETE,PUT...]
     *
     * @return a JSONObject
     */
    @CheckForNull
    public JSONObject fetchJSONObject(@Nonnull String url, @Nonnull HttpMethod method) {
        ContentResponse r = getResponse(createRequest(url, method));
        if (r != null) {
            try {
                return new JSONObject(r.getContentAsString());
            } catch (JSONException ex) {
                //logger.error("could not parse response to JSON!", ex);
            }
        }
        return null;
    }

    /**
     * get the result of calling a URL with the given method
     *
     * @param url    the URL to call
     * @param method the method to use [GET,POST,DELETE,PUT...]
     *
     * @return
     */
    @CheckForNull
    public ContentResponse getResponse(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull String body) {
        return this.getResponse(this.createRequest(url, method, body));

    }

    @CheckForNull
    public ContentResponse getResponse(@Nonnull String url, @Nonnull HttpMethod method, @CheckForNull JSONObject body) {
        return this.getResponse(this.createRequest(url, method, body));

    }

    @CheckForNull
    public ContentResponse getResponse(@Nonnull String url, @Nonnull HttpMethod method, @Nonnull JSONArray body) {
        return this.getResponse(this.createRequest(url, method, body));

    }

    @CheckForNull
    public ContentResponse getResponse(@Nonnull String url, @Nonnull HttpMethod method) {
        return this.getResponse(this.createRequest(url, method));

    }

    /**
     * post the given body to a specific url
     *
     * @param url  the url to post it to
     * @param body the body to post
     *
     * @return the JSONObject returned from
     */
    @CheckForNull
    public JSONObject postJSONObject(@Nonnull String url, @CheckForNull JSONObject body) {
        Request request = createRequest(url, HttpMethod.POST);
        if (body != null) {
            request.content(
                    new StringContentProvider(
                            body.toString()
                    ), "application/json");
        }
        ContentResponse r = getResponse(request);
        if (r != null) {
            return new JSONObject(r.getContentAsString());
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
    @CheckForNull
    public ContentResponse postJSONObject2(@Nonnull String url, @CheckForNull JSONObject body) {
        Request request = createRequest(url, HttpMethod.POST);
        if (body != null) {
            request.content(
                    new StringContentProvider(
                            body.toString()
                    ), "application/json");
        }
        return getResponse(request);
    }

    /**
     * get the response for a given result, if 401 is returned the jwt will be renewed and it will be tried again
     *
     * @param request the request to parse
     *
     * @return
     */
    @CheckForNull
    public ContentResponse getResponse(@Nonnull Request request) {
        ContentResponse response = null;
        try {
            response = request.send();


        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("could not get Response for Request {}",e.getMessage(), e);
        }
        return response;

    }
}
