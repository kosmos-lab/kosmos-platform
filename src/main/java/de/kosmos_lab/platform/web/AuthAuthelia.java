package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.platform.client.HTTPClient;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class AuthAuthelia implements IAuthProvider {
    public static final Logger logger = LoggerFactory.getLogger("AuthAuthelia");

    private final IController controller;

    public AuthAuthelia(IController controller) {
        this.controller = controller;
    }

    public static IAuthProvider getInstance(IController controller) {
        JSONObject authelia = controller.getConfig().optJSONObject("authelia");
        if (authelia != null) {
            return new AuthAuthelia(controller);
        }
        throw new IllegalArgumentException("did not contain authelia configuration");
    }

    @Override
    public IUser tryLogin(@Nonnull String user, @Nonnull String password) throws LoginFailedException {
        logger.info("trying to login as {} with authelia", user);

        JSONObject authelia = controller.getConfig().optJSONObject("authelia");

        if (authelia != null) {
            try {

                HTTPClient client = new HTTPClient();
                JSONObject ljson = new JSONObject();
                ljson.put("username", user);
                ljson.put("password", password);
                client.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);

                ContentResponse response = client.getResponse(String.format("%s/api/firstfactor", authelia.getString("server")), HttpMethod.POST, ljson);
                if (response != null) {
                    String s = response.getContentAsString();
                    //logger.info("authelia login {} {}",response.getStatus(),s);
                    JSONObject json = new JSONObject(s);
                    if (json.getString("status").equalsIgnoreCase("OK")) {
                        return controller.getUserCreateIfUnavailable(String.format("authelia:%s", user));
                    }
                    throw new LoginFailedException(json.getString("message"));
                }


            } catch (de.kosmos_lab.web.exceptions.LoginFailedException ex) {
                throw ex;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {

            throw new IllegalArgumentException("did not contain authelia configuration");

        }
        return null;
    }
}
