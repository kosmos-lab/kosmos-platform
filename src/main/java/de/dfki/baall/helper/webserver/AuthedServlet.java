package de.dfki.baall.helper.webserver;

import de.dfki.baall.helper.persistence.ISesssionPersistence;
import de.dfki.baall.helper.persistence.IUserPersistence;
import de.dfki.baall.helper.persistence.exceptions.NoPersistenceException;
import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.dfki.baall.helper.webserver.data.User;
import de.dfki.baall.helper.webserver.exceptions.LoginFailedException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AuthedServlet extends MyServlet {
    protected final int level;
    
    
    public AuthedServlet(WebServer webServer) {
        this(webServer, 1);
    }
    
    public AuthedServlet(WebServer webServer, int level) {
        super(webServer);
        
        this.level = level;
        logger.info("created servlet {}", this.getClass());
    }
    
    
    protected boolean isAllowed(HttpServletRequest request, HttpServletResponse response) {
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            auth = auth.trim();
            if (auth.startsWith("Bearer")) {
                auth = auth.substring(6).trim();
            }
            JSONObject s = null;
            try {
                s = server.getPersistence(ISesssionPersistence.class).verifyJWT(auth);
                if (s != null) {
                    
                    try {
                        User u = server.getPersistence(IUserPersistence.class).getUser(s.getString("name"));
                        request.setAttribute("user", u);
                        if (u.canAccess(this.level)) {
                            return true;
                        }
                    } catch (NotFoundInPersistenceException e) {
                        e.printStackTrace();
                    }
                    response.setStatus(STATUS_FORBIDDEN);
                    return false;
                }
            } catch (NoPersistenceException e) {
            
            }
            
            response.setHeader("WWW-Authenticate", "Bearer realm=\"example\",\n" +
                    "                   error=\"invalid_token\",\n" +
                    "                   error_description=\"The access token expired\"");
            response.setStatus(STATUS_NO_AUTH);
            return false;
        }
        String username = request.getHeader("username");
        String password = request.getHeader("password");
        
        if (username == null || password == null) {
            username = request.getParameter("username");
            password = request.getParameter("password");
        }
        if (username != null && password != null) {
            
            try {
                
                try {
                    User u = server.getPersistence(IUserPersistence.class).login(username, password);
                    if (u != null) {
                        request.setAttribute("user", u);
                        if (u.canAccess(this.level)) {
                            return true;
                        }
                    }
                } catch (NoPersistenceException e) {
                    e.printStackTrace();
                }
                
            } catch (LoginFailedException e) {
                e.printStackTrace();
            }
            
        }
        response.setStatus(STATUS_NO_AUTH);
        return false;
        
        
    }
    
    protected boolean isMeOrAmAdmin(MyHttpServletRequest request, User u) {
        User me = request.getUser();
        //logger.warn(me.toJWT() + " vs " + u.toJWT());
        
        return me.isAdmin() && me.getLevel() >= u.getLevel();
    }
}