package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AuthKeyCloak implements IAuthProvider {
    public static final Logger logger = LoggerFactory.getLogger("AuthKeyCloak");

    private final IController controller;

    public AuthKeyCloak(IController controller) {
        this.controller = controller;
    }

    public static IAuthProvider getInstance(IController controller) {
        JSONObject keycloak = controller.getConfig().optJSONObject("keycloak");
        if (keycloak != null) {
            return new AuthKeyCloak(controller);
        }
        throw new IllegalArgumentException("did not contain keycloak configuration");
    }

    @Override
    public IUser tryLogin(@Nonnull String user, @Nonnull String password) throws LoginFailedException {
        logger.info("trying to login as {} with keycloak", user);

        JSONObject keycloak = controller.getConfig().optJSONObject("keycloak");

        if (keycloak != null) {
            try {

                try {
                    HttpClient httpclient = HttpClients.createDefault();
                    HttpPost httppost = new HttpPost(String.format("%s/realms/%s/protocol/openid-connect/token", keycloak.getString("server"), keycloak.getString("realm")));

// Request parameters and other properties.
                    List<NameValuePair> params = new ArrayList<NameValuePair>(5);
                    params.add(new BasicNameValuePair("client_id", keycloak.getString("clientId")));
                    params.add(new BasicNameValuePair("client_secret", keycloak.getString("clientSecret")));

                    params.add(new BasicNameValuePair("username", user));

                    params.add(new BasicNameValuePair("password", password));
                    params.add(new BasicNameValuePair("grant_type", "password"));

                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

//Execute and get the response.
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {
                        try (InputStream instream = entity.getContent()) {
                            // do something useful
                            String s = new String(instream.readAllBytes());
//                        logger.info("kc login: {}", s);
                            JSONObject json = new JSONObject(s);
                            String at = json.optString("access_token", null);
                            if (json.has("error") && json.has("error_description")) {
                                throw new de.kosmos_lab.web.exceptions.LoginFailedException(json.getString("error_description"));
                            }
                            if (at != null && !json.has("error")) {
                                //logger.info("kc login success");
                                return controller.getUserCreateIfUnavailable(String.format("keycloak:%s", user));

                            }


                        }
                    }

                } catch (de.kosmos_lab.web.exceptions.LoginFailedException ex) {
                    throw ex;

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            } catch (LoginFailedException ex) {
                throw ex;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {

            throw new IllegalArgumentException("did not contain keycloak configuration");

        }
        return null;
    }
}
