package de.kosmos_lab.platform.web;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.authentication.ClientCredentialsProviderUtils;
import org.keycloak.adapters.authorization.PolicyEnforcer;
import org.keycloak.adapters.rotation.HardcodedPublicKeyLocator;
import org.keycloak.adapters.rotation.JWKPublicKeyLocator;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.common.util.PemUtils;
import org.keycloak.enums.TokenStore;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.util.SystemPropertiesJsonParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.concurrent.Callable;

public class KosmosOIDDeploymentBuilder extends KeycloakDeploymentBuilder {
    private static final Logger log = Logger.getLogger(KosmosOIDDeploymentBuilder.class);
    protected KosmosOIDDeployment deployment = new KosmosOIDDeployment();
    private JSONObject config;

    protected KosmosOIDDeploymentBuilder() {
    }

    protected KeycloakDeployment internalBuild(final AdapterConfig adapterConfig, JSONObject config) {
        this.deployment.config = config;
        if (adapterConfig.getRealm() == null) {
            throw new RuntimeException("Must set 'realm' in config");
        } else {
            this.deployment.setRealm(adapterConfig.getRealm());
            String resource = adapterConfig.getResource();
            if (resource == null) {
                throw new RuntimeException("Must set 'resource' in config");
            } else {
                this.deployment.setResourceName(resource);
                String realmKeyPem = adapterConfig.getRealmKey();
                if (realmKeyPem != null) {
                    try {
                        PublicKey realmKey = PemUtils.decodePublicKey(realmKeyPem);
                        HardcodedPublicKeyLocator pkLocator = new HardcodedPublicKeyLocator(realmKey);
                        this.deployment.setPublicKeyLocator(pkLocator);
                    } catch (Exception var6) {
                        throw new RuntimeException(var6);
                    }
                } else {
                    JWKPublicKeyLocator pkLocator = new JWKPublicKeyLocator();
                    this.deployment.setPublicKeyLocator(pkLocator);
                }

                if (adapterConfig.getSslRequired() != null) {
                    this.deployment.setSslRequired(SslRequired.valueOf(adapterConfig.getSslRequired().toUpperCase()));
                } else {
                    this.deployment.setSslRequired(SslRequired.EXTERNAL);
                }

                if (adapterConfig.getConfidentialPort() != -1) {
                    this.deployment.setConfidentialPort(adapterConfig.getConfidentialPort());
                }

                if (adapterConfig.getTokenStore() != null) {
                    this.deployment.setTokenStore(TokenStore.valueOf(adapterConfig.getTokenStore().toUpperCase()));
                } else {
                    this.deployment.setTokenStore(TokenStore.SESSION);
                }

                if (adapterConfig.getTokenCookiePath() != null) {
                    this.deployment.setAdapterStateCookiePath(adapterConfig.getTokenCookiePath());
                }

                if (adapterConfig.getPrincipalAttribute() != null) {
                    this.deployment.setPrincipalAttribute(adapterConfig.getPrincipalAttribute());
                }

                this.deployment.setResourceCredentials(adapterConfig.getCredentials());
                this.deployment.setClientAuthenticator(ClientCredentialsProviderUtils.bootstrapClientAuthenticator(this.deployment));
                this.deployment.setPublicClient(adapterConfig.isPublicClient());
                this.deployment.setUseResourceRoleMappings(adapterConfig.isUseResourceRoleMappings());
                this.deployment.setExposeToken(adapterConfig.isExposeToken());
                if (adapterConfig.isCors()) {
                    this.deployment.setCors(true);
                    this.deployment.setCorsMaxAge(adapterConfig.getCorsMaxAge());
                    this.deployment.setCorsAllowedHeaders(adapterConfig.getCorsAllowedHeaders());
                    this.deployment.setCorsAllowedMethods(adapterConfig.getCorsAllowedMethods());
                    this.deployment.setCorsExposedHeaders(adapterConfig.getCorsExposedHeaders());
                }

                if (adapterConfig.isPkce()) {
                    this.deployment.setPkce(true);
                }

                this.deployment.setBearerOnly(adapterConfig.isBearerOnly());
                this.deployment.setAutodetectBearerOnly(adapterConfig.isAutodetectBearerOnly());
                this.deployment.setEnableBasicAuth(adapterConfig.isEnableBasicAuth());
                this.deployment.setAlwaysRefreshToken(adapterConfig.isAlwaysRefreshToken());
                this.deployment.setRegisterNodeAtStartup(adapterConfig.isRegisterNodeAtStartup());
                this.deployment.setRegisterNodePeriod(adapterConfig.getRegisterNodePeriod());
                this.deployment.setTokenMinimumTimeToLive(adapterConfig.getTokenMinimumTimeToLive());
                this.deployment.setMinTimeBetweenJwksRequests(adapterConfig.getMinTimeBetweenJwksRequests());
                this.deployment.setPublicKeyCacheTtl(adapterConfig.getPublicKeyCacheTtl());
                this.deployment.setIgnoreOAuthQueryParameter(adapterConfig.isIgnoreOAuthQueryParameter());
                this.deployment.setRewriteRedirectRules(adapterConfig.getRedirectRewriteRules());
                this.deployment.setVerifyTokenAudience(adapterConfig.isVerifyTokenAudience());
                if (realmKeyPem == null && adapterConfig.isBearerOnly() && adapterConfig.getAuthServerUrl() == null) {
                    throw new IllegalArgumentException("For bearer auth, you must set the realm-public-key or auth-server-url");
                } else if (adapterConfig.getAuthServerUrl() != null || this.deployment.isBearerOnly() && realmKeyPem != null) {
                    this.deployment.setClient(this.createHttpClientProducer(adapterConfig));
                    this.deployment.setAuthServerBaseUrl(adapterConfig);
                    if (adapterConfig.getTurnOffChangeSessionIdOnLogin() != null) {
                        this.deployment.setTurnOffChangeSessionIdOnLogin(adapterConfig.getTurnOffChangeSessionIdOnLogin());
                    }

                    PolicyEnforcerConfig policyEnforcerConfig = adapterConfig.getPolicyEnforcerConfig();
                    if (policyEnforcerConfig != null) {
                        this.deployment.setPolicyEnforcer(new Callable<PolicyEnforcer>() {
                            PolicyEnforcer policyEnforcer;

                            public PolicyEnforcer call() {
                                if (this.policyEnforcer == null) {
                                    synchronized (KosmosOIDDeploymentBuilder.this.deployment) {
                                        if (this.policyEnforcer == null) {
                                            this.policyEnforcer = new PolicyEnforcer(KosmosOIDDeploymentBuilder.this.deployment, adapterConfig);
                                        }
                                    }
                                }

                                return this.policyEnforcer;
                            }
                        });
                    }

                    return this.deployment;
                } else {
                    throw new RuntimeException("You must specify auth-server-url");
                }
            }
        }
    }

    private Callable<HttpClient> createHttpClientProducer(final AdapterConfig adapterConfig) {
        return new Callable<HttpClient>() {
            private HttpClient client;

            public HttpClient call() {
                if (this.client == null) {
                    synchronized (KosmosOIDDeploymentBuilder.this.deployment) {
                        if (this.client == null) {
                            this.client = (new HttpClientBuilder()).build(adapterConfig);
                        }
                    }
                }

                return this.client;
            }
        };
    }

    public static KeycloakDeployment build(JSONObject config) {
        CryptoIntegration.init(KosmosOIDDeploymentBuilder.class.getClassLoader());
        AdapterConfig adapterConfig = loadAdapterConfig(new ByteArrayInputStream(config.toString().getBytes()));
        return (new KosmosOIDDeploymentBuilder()).internalBuild(adapterConfig, config);
    }

    public static AdapterConfig loadAdapterConfig(InputStream is) {
        ObjectMapper mapper = new ObjectMapper(new SystemPropertiesJsonParserFactory());
        mapper.setSerializationInclusion(Include.NON_DEFAULT);

        try {
            AdapterConfig adapterConfig = mapper.readValue(is, AdapterConfig.class);
            return adapterConfig;
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    public static KeycloakDeployment build(AdapterConfig adapterConfig) {
        return (new KosmosOIDDeploymentBuilder()).internalBuild(adapterConfig);
    }
}
