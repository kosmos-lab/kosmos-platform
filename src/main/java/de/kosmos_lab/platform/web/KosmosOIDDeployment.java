package de.kosmos_lab.platform.web;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.authentication.ClientCredentialsProvider;
import org.keycloak.adapters.authorization.PolicyEnforcer;
import org.keycloak.adapters.rotation.PublicKeyLocator;
import org.keycloak.common.enums.RelativeUrlsUsed;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.enums.TokenStore;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.util.JsonSerialization;

public class KosmosOIDDeployment  extends KeycloakDeployment {
    private static final Logger log = Logger.getLogger(KosmosOIDDeployment.class);
    public JSONObject config;
    protected RelativeUrlsUsed relativeUrls;
    protected String realm;
    protected PublicKeyLocator publicKeyLocator;
    protected String authServerBaseUrl;
    protected String realmInfoUrl;
    protected KeycloakUriBuilder authUrl;
    protected String tokenUrl;
    protected KeycloakUriBuilder logoutUrl;
    protected String accountUrl;
    protected String registerNodeUrl;
    protected String unregisterNodeUrl;
    protected String jwksUrl;
    protected String principalAttribute = "sub";
    protected String resourceName;
    protected boolean bearerOnly;
    protected boolean autodetectBearerOnly;
    protected boolean enableBasicAuth;
    protected boolean publicClient;
    protected Map<String, Object> resourceCredentials = new HashMap();
    protected ClientCredentialsProvider clientAuthenticator;
    protected Callable<HttpClient> client;
    protected String scope;
    protected SslRequired sslRequired;
    protected int confidentialPort;
    protected TokenStore tokenStore;
    protected String adapterStateCookiePath;
    protected String stateCookieName;
    protected boolean useResourceRoleMappings;
    protected boolean cors;
    protected int corsMaxAge;
    protected String corsAllowedHeaders;
    protected String corsAllowedMethods;
    protected String corsExposedHeaders;
    protected boolean exposeToken;
    protected boolean alwaysRefreshToken;
    protected boolean registerNodeAtStartup;
    protected int registerNodePeriod;
    protected boolean turnOffChangeSessionIdOnLogin;
    protected volatile int notBefore;
    protected int tokenMinimumTimeToLive;
    protected int minTimeBetweenJwksRequests;
    protected int publicKeyCacheTtl;
    protected Callable<PolicyEnforcer> policyEnforcer;
    protected boolean pkce;
    protected boolean ignoreOAuthQueryParameter;
    protected Map<String, String> redirectRewriteRules;
    protected boolean delegateBearerErrorResponseSending;
    protected boolean verifyTokenAudience;

    public KosmosOIDDeployment() {
        this.sslRequired = SslRequired.ALL;
        this.confidentialPort = -1;
        this.tokenStore = TokenStore.SESSION;
        this.adapterStateCookiePath = "";
        this.stateCookieName = "OAuth_Token_Request_State";
        this.corsMaxAge = -1;
        this.pkce = false;
        this.delegateBearerErrorResponseSending = false;
        this.verifyTokenAudience = false;
    }

    public boolean isConfigured() {
        return this.getRealm() != null && this.getPublicKeyLocator() != null && (this.isBearerOnly() || this.getAuthServerBaseUrl() != null);
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public PublicKeyLocator getPublicKeyLocator() {
        return this.publicKeyLocator;
    }

    public void setPublicKeyLocator(PublicKeyLocator publicKeyLocator) {
        this.publicKeyLocator = publicKeyLocator;
    }

    public String getAuthServerBaseUrl() {
        return this.authServerBaseUrl;
    }

    public void setAuthServerBaseUrl(AdapterConfig config) {
        this.authServerBaseUrl = config.getAuthServerUrl();
        if (this.authServerBaseUrl != null) {
            this.authServerBaseUrl = KeycloakUriBuilder.fromUri(this.authServerBaseUrl).build(new Object[0]).toString();

            this.authUrl = null;
            this.realmInfoUrl = null;
            this.tokenUrl = null;
            this.logoutUrl = null;
            this.accountUrl = null;
            this.registerNodeUrl = null;
            this.unregisterNodeUrl = null;
            this.jwksUrl = null;
            URI authServerUri = URI.create(this.authServerBaseUrl);
            if (authServerUri.getHost() == null) {
                this.relativeUrls = RelativeUrlsUsed.ALWAYS;
            } else {
                this.relativeUrls = RelativeUrlsUsed.NEVER;
            }

        }
    }

    protected void resolveUrls() {
        if (this.realmInfoUrl == null) {
            synchronized(this) {
                if (this.realmInfoUrl == null) {
                    OIDCConfigurationRepresentation config = null;
                    String content ="";
                    if ( this.config != null ) {
                        log.infov("have config {0}",this.config.toString());
                        JSONObject c = this.config.optJSONObject("config");
                        if ( c != null ) {
                            try {
                                log.infov("loading from config");

                                config = (OIDCConfigurationRepresentation)JsonSerialization.readValue(content, OIDCConfigurationRepresentation.class);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    KeycloakUriBuilder authUrlBuilder = KeycloakUriBuilder.fromUri(this.authServerBaseUrl);
                    if ( config == null ) {

                        String discoveryUrl = authUrlBuilder.clone().path("/realms/{realm-name}/.well-known/openid-configuration").build(new Object[]{this.getRealm()}).toString();
                        try {
                            log.infov("Resolving URLs from {0}", discoveryUrl);

                            config = this.getOidcConfiguration(discoveryUrl);
                            log.infov("Loaded URLs from {0}", discoveryUrl);
                        } catch (Exception var6) {
                            log.warnv(var6, "Failed to load URLs from {0}", discoveryUrl);
                        }
                    }
                    try {
                        //(OIDCConfigurationRepresentation)JsonSerialization.readValue(response.getEntity().getContent(), OIDCConfigurationRepresentation.class);


                        this.authUrl = KeycloakUriBuilder.fromUri(config.getAuthorizationEndpoint());
                        this.realmInfoUrl = config.getIssuer();
                        this.tokenUrl = config.getTokenEndpoint();
                        this.logoutUrl = KeycloakUriBuilder.fromUri(config.getLogoutEndpoint());
                        this.accountUrl = KeycloakUriBuilder.fromUri(config.getIssuer()).path("/account").build(new Object[0]).toString();
                        this.registerNodeUrl = authUrlBuilder.clone().path("/realms/{realm-name}/clients-managements/register-node").build(new Object[]{this.getRealm()}).toString();
                        this.unregisterNodeUrl = authUrlBuilder.clone().path("/realms/{realm-name}/clients-managements/unregister-node").build(new Object[]{this.getRealm()}).toString();
                        this.jwksUrl = config.getJwksUri();

                    } catch (Exception var6) {
                        log.warnv(var6, "Failed to parse config");
                    }
                }
            }
        }

    }

    protected void resolveUrls(KeycloakUriBuilder authUrlBuilder) {
        if (log.isDebugEnabled()) {
            log.debug("resolveUrls");
        }

        String login = authUrlBuilder.clone().path("/realms/{realm-name}/protocol/openid-connect/auth").build(new Object[]{this.getRealm()}).toString();
        this.authUrl = KeycloakUriBuilder.fromUri(login);
        this.realmInfoUrl = authUrlBuilder.clone().path("/realms/{realm-name}").build(new Object[]{this.getRealm()}).toString();
        this.tokenUrl = authUrlBuilder.clone().path("/realms/{realm-name}/protocol/openid-connect/token").build(new Object[]{this.getRealm()}).toString();
        this.logoutUrl = KeycloakUriBuilder.fromUri(authUrlBuilder.clone().path("/realms/{realm-name}/protocol/openid-connect/logout").build(new Object[]{this.getRealm()}).toString());
        this.accountUrl = authUrlBuilder.clone().path("/realms/{realm-name}/account").build(new Object[]{this.getRealm()}).toString();
        this.registerNodeUrl = authUrlBuilder.clone().path("/realms/{realm-name}/clients-managements/register-node").build(new Object[]{this.getRealm()}).toString();
        this.unregisterNodeUrl = authUrlBuilder.clone().path("/realms/{realm-name}/clients-managements/unregister-node").build(new Object[]{this.getRealm()}).toString();
        this.jwksUrl = authUrlBuilder.clone().path("/realms/{realm-name}/protocol/openid-connect/certs").build(new Object[]{this.getRealm()}).toString();
    }

    protected OIDCConfigurationRepresentation getOidcConfiguration(String discoveryUrl) throws Exception {
        HttpGet request = new HttpGet(discoveryUrl);
        request.addHeader("accept", "application/json");

        OIDCConfigurationRepresentation var4;
        try {
            HttpResponse response = this.getClient().execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                EntityUtils.consumeQuietly(response.getEntity());
                throw new Exception(response.getStatusLine().getReasonPhrase());
            }

            var4 = (OIDCConfigurationRepresentation)JsonSerialization.readValue(response.getEntity().getContent(), OIDCConfigurationRepresentation.class);
        } finally {
            request.releaseConnection();
        }

        return var4;
    }

    public RelativeUrlsUsed getRelativeUrls() {
        return this.relativeUrls;
    }

    public String getRealmInfoUrl() {
        this.resolveUrls();
        return this.realmInfoUrl;
    }

    public KeycloakUriBuilder getAuthUrl() {
        this.resolveUrls();
        return this.authUrl;
    }

    public String getTokenUrl() {
        this.resolveUrls();
        return this.tokenUrl;
    }

    public KeycloakUriBuilder getLogoutUrl() {
        this.resolveUrls();
        return this.logoutUrl;
    }

    public String getAccountUrl() {
        this.resolveUrls();
        return this.accountUrl;
    }

    public String getRegisterNodeUrl() {
        this.resolveUrls();
        return this.registerNodeUrl;
    }

    public String getUnregisterNodeUrl() {
        this.resolveUrls();
        return this.unregisterNodeUrl;
    }

    public String getJwksUrl() {
        this.resolveUrls();
        return this.jwksUrl;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public boolean isBearerOnly() {
        return this.bearerOnly;
    }

    public void setBearerOnly(boolean bearerOnly) {
        this.bearerOnly = bearerOnly;
    }

    public boolean isAutodetectBearerOnly() {
        return this.autodetectBearerOnly;
    }

    public void setAutodetectBearerOnly(boolean autodetectBearerOnly) {
        this.autodetectBearerOnly = autodetectBearerOnly;
    }

    public boolean isEnableBasicAuth() {
        return this.enableBasicAuth;
    }

    public void setEnableBasicAuth(boolean enableBasicAuth) {
        this.enableBasicAuth = enableBasicAuth;
    }

    public boolean isPublicClient() {
        return this.publicClient;
    }

    public void setPublicClient(boolean publicClient) {
        this.publicClient = publicClient;
    }

    public Map<String, Object> getResourceCredentials() {
        return this.resourceCredentials;
    }

    public void setResourceCredentials(Map<String, Object> resourceCredentials) {
        this.resourceCredentials = resourceCredentials;
    }

    public ClientCredentialsProvider getClientAuthenticator() {
        return this.clientAuthenticator;
    }

    public void setClientAuthenticator(ClientCredentialsProvider clientAuthenticator) {
        this.clientAuthenticator = clientAuthenticator;
    }

    public HttpClient getClient() {
        try {
            return (HttpClient)this.client.call();
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    public void setClient(final HttpClient client) {
        this.client = new Callable<HttpClient>() {
            public HttpClient call() {
                return client;
            }
        };
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public SslRequired getSslRequired() {
        return this.sslRequired;
    }

    public void setSslRequired(SslRequired sslRequired) {
        this.sslRequired = sslRequired;
    }

    public boolean isSSLEnabled() {
        return SslRequired.NONE != this.sslRequired;
    }

    public int getConfidentialPort() {
        return this.confidentialPort;
    }

    public void setConfidentialPort(int confidentialPort) {
        this.confidentialPort = confidentialPort;
    }

    public TokenStore getTokenStore() {
        return this.tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public String getAdapterStateCookiePath() {
        return this.adapterStateCookiePath;
    }

    public void setAdapterStateCookiePath(String adapterStateCookiePath) {
        this.adapterStateCookiePath = adapterStateCookiePath;
    }

    public String getStateCookieName() {
        return this.stateCookieName;
    }

    public void setStateCookieName(String stateCookieName) {
        this.stateCookieName = stateCookieName;
    }

    public boolean isUseResourceRoleMappings() {
        return this.useResourceRoleMappings;
    }

    public void setUseResourceRoleMappings(boolean useResourceRoleMappings) {
        this.useResourceRoleMappings = useResourceRoleMappings;
    }

    public boolean isCors() {
        return this.cors;
    }

    public void setCors(boolean cors) {
        this.cors = cors;
    }

    public int getCorsMaxAge() {
        return this.corsMaxAge;
    }

    public void setCorsMaxAge(int corsMaxAge) {
        this.corsMaxAge = corsMaxAge;
    }

    public String getCorsAllowedHeaders() {
        return this.corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    public String getCorsAllowedMethods() {
        return this.corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }

    public String getCorsExposedHeaders() {
        return this.corsExposedHeaders;
    }

    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }

    public boolean isExposeToken() {
        return this.exposeToken;
    }

    public void setExposeToken(boolean exposeToken) {
        this.exposeToken = exposeToken;
    }

    public int getNotBefore() {
        return this.notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    public void updateNotBefore(int notBefore) {
        this.notBefore = notBefore;
        this.getPublicKeyLocator().reset(this);
    }

    public boolean isAlwaysRefreshToken() {
        return this.alwaysRefreshToken;
    }

    public void setAlwaysRefreshToken(boolean alwaysRefreshToken) {
        this.alwaysRefreshToken = alwaysRefreshToken;
    }

    public boolean isRegisterNodeAtStartup() {
        return this.registerNodeAtStartup;
    }

    public void setRegisterNodeAtStartup(boolean registerNodeAtStartup) {
        this.registerNodeAtStartup = registerNodeAtStartup;
    }

    public int getRegisterNodePeriod() {
        return this.registerNodePeriod;
    }

    public void setRegisterNodePeriod(int registerNodePeriod) {
        this.registerNodePeriod = registerNodePeriod;
    }

    public String getPrincipalAttribute() {
        return this.principalAttribute;
    }

    public void setPrincipalAttribute(String principalAttribute) {
        this.principalAttribute = principalAttribute;
    }

    public boolean isTurnOffChangeSessionIdOnLogin() {
        return this.turnOffChangeSessionIdOnLogin;
    }

    public void setTurnOffChangeSessionIdOnLogin(boolean turnOffChangeSessionIdOnLogin) {
        this.turnOffChangeSessionIdOnLogin = turnOffChangeSessionIdOnLogin;
    }

    public int getTokenMinimumTimeToLive() {
        return this.tokenMinimumTimeToLive;
    }

    public void setTokenMinimumTimeToLive(int tokenMinimumTimeToLive) {
        this.tokenMinimumTimeToLive = tokenMinimumTimeToLive;
    }

    public int getMinTimeBetweenJwksRequests() {
        return this.minTimeBetweenJwksRequests;
    }

    public void setMinTimeBetweenJwksRequests(int minTimeBetweenJwksRequests) {
        this.minTimeBetweenJwksRequests = minTimeBetweenJwksRequests;
    }

    public int getPublicKeyCacheTtl() {
        return this.publicKeyCacheTtl;
    }

    public void setPublicKeyCacheTtl(int publicKeyCacheTtl) {
        this.publicKeyCacheTtl = publicKeyCacheTtl;
    }

    public void setPolicyEnforcer(Callable<PolicyEnforcer> policyEnforcer) {
        this.policyEnforcer = policyEnforcer;
    }

    public PolicyEnforcer getPolicyEnforcer() {
        if (this.policyEnforcer == null) {
            return null;
        } else {
            try {
                return (PolicyEnforcer)this.policyEnforcer.call();
            } catch (Exception var2) {
                throw new RuntimeException("Failed to obtain policy enforcer", var2);
            }
        }
    }

    public boolean isPkce() {
        return this.pkce;
    }

    public void setPkce(boolean pkce) {
        this.pkce = pkce;
    }

    public void setIgnoreOAuthQueryParameter(boolean ignoreOAuthQueryParameter) {
        this.ignoreOAuthQueryParameter = ignoreOAuthQueryParameter;
    }

    public boolean isOAuthQueryParameterEnabled() {
        return !this.ignoreOAuthQueryParameter;
    }

    public Map<String, String> getRedirectRewriteRules() {
        return this.redirectRewriteRules;
    }

    public void setRewriteRedirectRules(Map<String, String> redirectRewriteRules) {
        this.redirectRewriteRules = redirectRewriteRules;
    }

    public boolean isDelegateBearerErrorResponseSending() {
        return this.delegateBearerErrorResponseSending;
    }

    public void setDelegateBearerErrorResponseSending(boolean delegateBearerErrorResponseSending) {
        this.delegateBearerErrorResponseSending = delegateBearerErrorResponseSending;
    }

    public boolean isVerifyTokenAudience() {
        return this.verifyTokenAudience;
    }

    public void setVerifyTokenAudience(boolean verifyTokenAudience) {
        this.verifyTokenAudience = verifyTokenAudience;
    }

    public void setClient(Callable<HttpClient> callable) {
        this.client = callable;
    }
}
