package de.kosmos_lab.platform.web;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.json.JSONObject;
import org.keycloak.OAuthErrorException;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.ServerRequest.HttpFailure;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class KeyCloakAdapter {
    private final JSONObject config;
    private String authUrl;
    private String redirectUrl;
    private KeyCloakAdapter.Pkce pkce;
    private Status status;

    public KeyCloakAdapter(String server, String realm, String clientId, String clientSecret){
    this (String.format("{\n" +
            "  \"realm\" : \"%s\",\n" +
            "  \"resource\" : \"%s\",\n" +
            "  \"auth-server-url\" : \"%s\",\n" +
            "  \"ssl-required\" : \"external\",\n" +
            "  \"use-resource-role-mappings\" : false,\n" +
            "  \"enable-cors\" : true,\n" +
            "  \"cors-max-age\" : 1000,\n" +
            "  \"cors-allowed-methods\" : \"POST, PUT, DELETE, GET\",\n" +
            "  \"cors-exposed-headers\" : \"WWW-Authenticate, My-custom-exposed-Header\",\n" +
            "  \"bearer-only\" : false,\n" +
            "  \"enable-basic-auth\" : false,\n" +
            "  \"expose-token\" : true,\n" +
            "  \"verify-token-audience\" : true,\n" +
            "  \"credentials\" : {\n" +
            "    \"secret\" : \"%s\"\n" +
            "  },\n" +
            "\n" +
            "  \"connection-pool-size\" : 20,\n" +
            "  \"socket-timeout-millis\" : 5000,\n" +
            "  \"connection-timeout-millis\" : 6000,\n" +
            "  \"connection-ttl-millis\" : 500,\n" +
            "  \"disable-trust-manager\" : false,\n" +
            "  \"allow-any-hostname\" : false,\n" +
            "  \"token-minimum-time-to-live\" : 10,\n" +
            "  \"min-time-between-jwks-requests\" : 10,\n" +
            "  \"public-key-cache-ttl\" : 86400,\n" +
            "  \"redirect-rewrite-rules\" : {\n" +
            "    \"^/wsmaster/api/(.*)$\" : \"/api/$1\"\n" +
            "  }\n" +
            "}",realm, clientId,server,clientSecret));
}
    public KeyCloakAdapter(String config) {
        this(new JSONObject(config));
    }

    public KeyCloakAdapter(JSONObject config) {
        this.config = config;
        this.deployment = KosmosOIDDeploymentBuilder.build(config);


    }



    public String getAuthUrl(String redirectUrl) {
        this.pkce = this.generatePkce();
        this.redirectUrl = redirectUrl;
        this.authUrl = this.createAuthUrl(redirectUrl, (String) null, pkce);
        return authUrl;
    }
    public String getAuthUrl(String redirectUrl,String state) {
        this.pkce = this.generatePkce();
        this.redirectUrl = redirectUrl;
        this.authUrl = this.createAuthUrl(redirectUrl, state, pkce);
        return authUrl;
    }
    


    private static final String KEYCLOAK_JSON = "META-INF/keycloak.json";
    private KeycloakDeployment deployment;
    private int listenPort = 0;
    private String listenHostname = "localhost";
    private AccessTokenResponse tokenResponse;
    private String tokenString;
    private String idTokenString;
    private IDToken idToken;
    private AccessToken token;
    private String refreshToken;
    private Locale locale;
    private ResteasyClient resteasyClient;
    Pattern callbackPattern = Pattern.compile("callback\\s*=\\s*\"([^\"]+)\"");
    Pattern paramPattern = Pattern.compile("param=\"([^\"]+)\"\\s+label=\"([^\"]+)\"\\s+mask=(\\S+)");
    Pattern codePattern = Pattern.compile("code=([^&]+)");
    private KeyCloakAdapter.DesktopProvider desktopProvider = new KeyCloakAdapter.DesktopProvider();







    public void setResteasyClient(ResteasyClient resteasyClient) {
        this.resteasyClient = resteasyClient;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public int getListenPort() {
        return this.listenPort;
    }

    public void setListenPort(int listenPort) {
        if (listenPort >= 0 && listenPort <= 65535) {
            this.listenPort = listenPort;
        } else {
            throw new IllegalArgumentException("localPort");
        }
    }

    public String getListenHostname() {
        return this.listenHostname;
    }

    public void setListenHostname(String listenHostname) {
        this.listenHostname = listenHostname;
    }

    

    public void logout() throws IOException, InterruptedException, URISyntaxException {
        
        this.tokenString = null;
        this.token = null;
        this.idTokenString = null;
        this.idToken = null;
        this.refreshToken = null;
        this.status = null;
    }



    public void close() {
        

    }

    protected String createAuthUrl(String redirectUri, String state, KeyCloakAdapter.Pkce pkce) {
        KeycloakUriBuilder builder = this.deployment.getAuthUrl().clone().queryParam("response_type", new Object[]{"code"}).queryParam("client_id", new Object[]{this.deployment.getResourceName()}).queryParam("redirect_uri", new Object[]{redirectUri}).queryParam("scope", new Object[]{"openid"});
        if (state != null) {
            builder.queryParam("state", new Object[]{state});
        }

        if (this.locale != null) {
            builder.queryParam("ui_locales", new Object[]{this.locale.getLanguage()});
        }

        if (pkce != null) {
            builder.queryParam("code_challenge", new Object[]{pkce.getCodeChallenge()});
            builder.queryParam("code_challenge_method", new Object[]{"S256"});
        }

        return builder.build(new Object[0]).toString();
    }

    protected KeyCloakAdapter.Pkce generatePkce() {
        return KeyCloakAdapter.Pkce.generatePkce();
    }

    private void logoutDesktop() throws IOException, URISyntaxException, InterruptedException {
        KeyCloakAdapter.CallbackListener callback = new KeyCloakAdapter.CallbackListener();
        callback.start();
        String redirectUri = this.getRedirectUri(callback);
        String logoutUrl = this.deployment.getLogoutUrl().clone().queryParam("post_logout_redirect_uri", new Object[]{redirectUri}).queryParam("id_token_hint", new Object[]{this.idTokenString}).build(new Object[0]).toString();
        this.desktopProvider.browse(new URI(logoutUrl));

        try {
            callback.await();
        } catch (InterruptedException var5) {
            callback.stop();
            throw var5;
        }
    }

    private String getRedirectUri(KeyCloakAdapter.CallbackListener callback) {
        return String.format("http://%s:%s", this.getListenHostname(), callback.getLocalPort());
    }

    public void loginManual() throws IOException, ServerRequest.HttpFailure, VerificationException {
        this.loginManual(System.out, new InputStreamReader(System.in));
    }

    public void loginManual(PrintStream printer, Reader reader) throws IOException, ServerRequest.HttpFailure, VerificationException {
        String redirectUri = "urn:ietf:wg:oauth:2.0:oob";
        KeyCloakAdapter.Pkce pkce = this.generatePkce();
        String authUrl = this.createAuthUrl(redirectUri, (String)null, pkce);
        printer.println("Open the following URL in a browser. After login copy/paste the code back and press <enter>");
        printer.println(authUrl);
        printer.println();
        printer.print("Code: ");
        String code = this.readCode(reader);
        this.processCode(code, redirectUri, pkce);
        this.status = KeyCloakAdapter.Status.LOGGED_MANUAL;
    }

    public String getTokenString() {
        return this.tokenString;
    }

    public String getTokenString(long minValidity, TimeUnit unit) throws VerificationException, IOException, ServerRequest.HttpFailure {
        long expires = (long)this.token.getExpiration() * 1000L - unit.toMillis(minValidity);
        if (expires < System.currentTimeMillis()) {
            this.refreshToken();
        }

        return this.tokenString;
    }

    public void refreshToken() throws IOException, ServerRequest.HttpFailure, VerificationException {
        AccessTokenResponse tokenResponse = ServerRequest.invokeRefresh(this.deployment, this.refreshToken);
        this.parseAccessToken(tokenResponse);
    }

    public void refreshToken(String refreshToken) throws IOException, ServerRequest.HttpFailure, VerificationException {
        AccessTokenResponse tokenResponse = ServerRequest.invokeRefresh(this.deployment, refreshToken);
        this.parseAccessToken(tokenResponse);
    }

    private void parseAccessToken(AccessTokenResponse tokenResponse) throws VerificationException {
        this.tokenResponse = tokenResponse;
        this.tokenString = tokenResponse.getToken();
        this.refreshToken = tokenResponse.getRefreshToken();
        this.idTokenString = tokenResponse.getIdToken();
        AdapterTokenVerifier.VerifiedTokens tokens = AdapterTokenVerifier.verifyTokens(this.tokenString, this.idTokenString, this.deployment);
        this.token = tokens.getAccessToken();
        this.idToken = tokens.getIdToken();
    }

    public AccessToken getToken() {
        return this.token;
    }

    public IDToken getIdToken() {
        return this.idToken;
    }

    public String getIdTokenString() {
        return this.idTokenString;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public AccessTokenResponse getTokenResponse() {
        return this.tokenResponse;
    }

    public void setDesktopProvider(KeyCloakAdapter.DesktopProvider desktopProvider) {
        this.desktopProvider = desktopProvider;
    }

    public boolean isDesktopSupported() {
        return this.desktopProvider.isDesktopSupported();
    }

    public KeycloakDeployment getDeployment() {
        return this.deployment;
    }

    private void processCode(String code, String redirectUri, KeyCloakAdapter.Pkce pkce) throws IOException, ServerRequest.HttpFailure, VerificationException {
        AccessTokenResponse tokenResponse = ServerRequest.invokeAccessCodeToToken(this.deployment, code, redirectUri, (String)null, pkce == null ? null : pkce.getCodeVerifier());
        this.parseAccessToken(tokenResponse);
    }

    private String readCode(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] cb = new char[1];

        while(reader.read(cb) != -1) {
            char c = cb[0];
            if (c == ' ' || c == '\n' || c == '\r') {
                break;
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public void process(String code) throws HttpFailure, VerificationException, IOException {
        this.processCode(code,redirectUrl,pkce);
    }

    public static class DesktopProvider {
        public DesktopProvider() {
        }

        public boolean isDesktopSupported() {
            return Desktop.isDesktopSupported();
        }

        public void browse(URI uri) throws IOException {
            Desktop.getDesktop().browse(uri);
        }
    }

    public static class Pkce {
        public static final int PKCE_CODE_VERIFIER_MAX_LENGTH = 128;
        private final String codeChallenge;
        private final String codeVerifier;

        public Pkce(String codeVerifier, String codeChallenge) {
            this.codeChallenge = codeChallenge;
            this.codeVerifier = codeVerifier;
        }

        public String getCodeChallenge() {
            return this.codeChallenge;
        }

        public String getCodeVerifier() {
            return this.codeVerifier;
        }

        public static KeyCloakAdapter.Pkce generatePkce() {
            try {
                String codeVerifier = SecretGenerator.getInstance().randomString(128);
                String codeChallenge = generateS256CodeChallenge(codeVerifier);
                return new KeyCloakAdapter.Pkce(codeVerifier, codeChallenge);
            } catch (Exception var2) {
                throw new RuntimeException("Could not generate PKCE", var2);
            }
        }

        private static String generateS256CodeChallenge(String codeVerifier) throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(codeVerifier.getBytes(StandardCharsets.ISO_8859_1));
            return Base64Url.encode(md.digest());
        }
    }

    class CallbackListener implements HttpHandler {
        private final CountDownLatch shutdownSignal = new CountDownLatch(1);
        private String code;
        private String error;
        private String errorDescription;
        private String state;
        private Undertow server;
        private GracefulShutdownHandler gracefulShutdownHandler;

        CallbackListener() {
        }

        public void start() {
            PathHandler pathHandler = Handlers.path().addExactPath("/", this);
            AllowedMethodsHandler allowedMethodsHandler = new AllowedMethodsHandler(pathHandler, new HttpString[]{Methods.GET});
            this.gracefulShutdownHandler = Handlers.gracefulShutdown(allowedMethodsHandler);
            this.server = Undertow.builder().setIoThreads(1).setWorkerThreads(1).addHttpListener(KeyCloakAdapter.this.getListenPort(), KeyCloakAdapter.this.getListenHostname()).setHandler(this.gracefulShutdownHandler).build();
            this.server.start();
        }

        public void stop() {
            try {
                this.server.stop();
            } catch (Exception var2) {
            }

            this.shutdownSignal.countDown();
        }

        public int getLocalPort() {
            return ((InetSocketAddress)((Undertow.ListenerInfo)this.server.getListenerInfo().get(0)).getAddress()).getPort();
        }

        public void await() throws InterruptedException {
            this.shutdownSignal.await();
        }

        public void handleRequest(HttpServerExchange exchange) throws Exception {
            this.gracefulShutdownHandler.shutdown();
            if (!exchange.getQueryParameters().isEmpty()) {
                this.readQueryParameters(exchange);
            }

            exchange.setStatusCode(302);
            exchange.getResponseHeaders().add(Headers.LOCATION, this.getRedirectUrl());
            exchange.endExchange();
            this.shutdownSignal.countDown();
            ForkJoinPool.commonPool().execute(this::stop);
        }

        private void readQueryParameters(HttpServerExchange exchange) {
            this.code = this.getQueryParameterIfPresent(exchange, "code");
            this.error = this.getQueryParameterIfPresent(exchange, "error");
            this.errorDescription = this.getQueryParameterIfPresent(exchange, "error_description");
            this.state = this.getQueryParameterIfPresent(exchange, "state");
        }

        private String getQueryParameterIfPresent(HttpServerExchange exchange, String name) {
            Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
            return queryParameters.containsKey(name) ? (String)((Deque)queryParameters.get(name)).getFirst() : null;
        }

        private String getRedirectUrl() {
            String redirectUrl = KeyCloakAdapter.this.deployment.getTokenUrl().replace("/token", "/delegated");
            if (this.error != null) {
                redirectUrl = redirectUrl + "?error=true";
            }

            return redirectUrl;
        }
    }

    private static enum Status {
        LOGGED_MANUAL,
        LOGGED_DESKTOP;

        private Status() {
        }
    }
}
