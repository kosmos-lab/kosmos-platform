package de.kosmos_lab.kosmos.platform.web.servlets;

import de.dfki.baall.helper.webserver.JWT;
import de.dfki.baall.helper.webserver.data.IUser;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.kosmos.platform.smarthome.CommandSourceName;
import de.kosmos_lab.kosmos.platform.web.KosmoSHttpServletRequest;
import de.kosmos_lab.kosmos.platform.web.WebServer;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class AuthedServlet extends KosmoSServlet {
    protected final int level;
    
    
    public AuthedServlet(WebServer webServer, IController controller) {
        this(webServer, controller, 1);
    }
    
    public AuthedServlet(WebServer webServer, IController controller, int level) {
        super(webServer,controller);
        
        this.level = level;

    }
    
  
    
    
    
  
    
    protected CommandSourceName getSource(KosmoSHttpServletRequest request) {
        if ( controller != null ) {
            try {
                
                IUser u = request.getKosmoSUser();
                return controller.getSource("HTTPApi:"+u.getName());
                
            } catch (Exception e ) {
            
            }
            return controller.getSource("HTTPApi");
        }
        return null;
    }
    
    protected boolean isAllowed(HttpServletRequest request, HttpServletResponse response) {
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            auth = auth.trim();
            try {
                if (auth.startsWith("Basic")) {

                    String[] t = new String(Base64.getDecoder().decode(auth.substring(5).trim())).split(":");
                    if (t.length == 2) {
                        IUser u = controller.tryLogin(t[0], t[1]);
                        if (u != null) {
                            request.setAttribute("user", u);
                            if (u.canAccess(this.level)) {
                                return true;
                            }
                        }
                    }


                }
            } catch (Exception ex ) {
                logger.info("Exception while parsing basic: ",ex);
            }
            if (auth.startsWith("Bearer")) {
                auth = auth.substring(6).trim();
            }
            try {
                JSONObject s = controller.getJwt().verify(auth);
                IUser u = controller.getUser(s.getString("name"));
                request.setAttribute("user",u);
                if (u.canAccess(this.level)) {
                    return true;
                }
                
                response.setStatus(STATUS_FORBIDDEN);
                return false;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JWT.JWTVerifyFailed jwtVerifyFailed) {
                //jwtVerifyFailed.printStackTrace();
            }
        }
        if ( this.allow_auth == ALLOW_AUTH.PARAMETER_AND_HEADER) {
            String username = request.getHeader("username");
            String password = request.getHeader("password");

            if (username == null || password == null) {
                username = request.getParameter("username");
                password = request.getParameter("password");
            }
            if (username != null && password != null) {
                IUser u = controller.tryLogin(username, password);
                if (u != null) {
                    request.setAttribute("user", u);
                    if (u.canAccess(this.level)) {
                        return true;
                    }
                }
            }
        }
        response.setHeader("WWW-Authenticate","Bearer realm=\"example\",\n" +
                "                   error=\"invalid_token\",\n" +
                "                   error_description=\"The access token expired\"");
        response.setStatus(STATUS_NO_AUTH);
    
        return false;
        
        
    }
    
    protected boolean isMeOrAmAdmin(KosmoSHttpServletRequest request, IUser u) {
        IUser me = request.getKosmoSUser();
        logger.warn(me.toJWT()+" vs "+u.toJWT());
        if ( me.getUUID().getLeastSignificantBits() == u.getUUID().getLeastSignificantBits()) {
            return true;
        }
        return me.isAdmin() && me.getLevel() >= u.getLevel();
    }
}
