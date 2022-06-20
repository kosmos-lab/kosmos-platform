package de.dfki.cps.javaservice.webserver;


import de.dfki.baall.helper.webserver.data.User;
import de.dfki.baall.helper.webserver.exceptions.LoginFailedException;
import org.json.JSONObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

public interface IPersistence {
    
    
    void addJWT(@Nonnull String token);
    
    /**
     * add a user to the persistence
     *
     * @param username
     * @param password
     * @return
     */
    boolean addUser(@CheckForNull String username, @CheckForNull String password, int level);
    
    /**
     * get the JWT with a specific jwtid back, if it is in the system and still valid
     * @param jwtid the id to find
     * @return
     */
    JSONObject getJWT(@Nonnull String jwtid);
    
    /**
     * get the jwt for a user
     * @param user
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    String getJWT(@Nonnull User user) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException;
    
    Collection<JSONObject> getMySessions(String username);
    
    
    /**
     * get a user from persistence
     *
     * @param username
     * @return
     */
    User getUser(@CheckForNull String username);
    
    void killJWT(String jwtid);
    
    /**
     * try to login
     *
     * @param username
     * @param password
     * @return returns a jwt token on success
     * @throws LoginFailedException
     */
    User login(@CheckForNull String username, @CheckForNull String password) throws LoginFailedException;
    
    /**
     * verify the given jwt
     * @param token
     * @return
     */
    JSONObject verifyJWT(@Nonnull String token);
}
