package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class AuthLDAP implements IAuthProvider {
    public static final Logger logger = LoggerFactory.getLogger("AuthLDAP");

    private final IController controller;

    public AuthLDAP(IController controller) {
        this.controller = controller;
    }

    public static IAuthProvider getInstance(IController controller) {
        JSONObject ldap = controller.getConfig().optJSONObject("ldap");
        if (ldap != null) {
            return new AuthLDAP(controller);
        }
        throw new IllegalArgumentException("did not contain ldap configuration");
    }

    @Override
    public IUser tryLogin(@Nonnull String user, @Nonnull String password) throws LoginFailedException {
        logger.info("trying to login as {} with ldap", user);

        JSONObject ldap = controller.getConfig().optJSONObject("ldap");

        if (ldap != null) {
            try {
                //logger.info("found ldap config");

                try {
                    Hashtable<String, String> environment = new Hashtable<String, String>();

                    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    environment.put(Context.PROVIDER_URL, ldap.getString("server"));
                    environment.put(Context.SECURITY_AUTHENTICATION, "simple");
                    environment.put(Context.SECURITY_PRINCIPAL, String.format("uid=%s,%s", user, ldap.getString("userdn")));

                    environment.put(Context.SECURITY_CREDENTIALS, password);
//logger.info("almost ready to login");
                    DirContext context = new InitialDirContext(environment);

                    NamingEnumeration<? extends Attribute> a = context.getAttributes(String.format("uid=%s,%s", user, ldap.getString("userdn"))).getAll();
                    while (a.hasMore()) {
                        Attribute atr = a.next();
                        logger.info("attribute {}", atr);
                    }
                    /*a = context.getAttributes("cn=BAALL,ou=groups,dc=baall,dc=de").getAll();
                    while ( a.hasMore()) {
                        Attribute atr = a.next();
                        logger.info("attribute {}",atr);
                    }*/


                    //logger.info("did login");
                    context.close();
                    //logger.info("did close");
                    return controller.getUserCreateIfUnavailable(String.format("ldap:%s", user));
                } catch (Exception e) {
                    logger.error("error while logging in via LDAP!", e);
                    throw new LoginFailedException(e.getMessage());
                }


            } catch (de.kosmos_lab.web.exceptions.LoginFailedException ex) {
                throw ex;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            logger.error("did not find ldap config");
            throw new IllegalArgumentException("did not contain ldap configuration");

        }
        return null;
    }
}
