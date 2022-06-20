package de.dfki.baall.helper.persistence;

import de.dfki.baall.helper.persistence.exceptions.NoPersistenceException;
import de.dfki.baall.helper.webserver.JWT;
import org.json.JSONObject;

import java.util.UUID;

public interface ControllerWithPersistence {
    void addJWT(String jwtid, JSONObject o) throws NoPersistenceException;
    
    void addPersistence(IPersistence perstistence, Class<?> clazz);
    
    void addPersistence(IPersistence p);
    
    void addUUID(UUID uuid, Object object);
    
    IPersistence createPersistence(JSONObject config);
    
    UUID generateUUID();
    
    Object getByUUID(UUID uuid);
    
    Class getDefaultPersistenceClass();
    
    JWT getJwt();
    

    
    <T> T getPersistence(Class<T> clazz) throws NoPersistenceException;
    
    String hashPepper(String input);
    
    String hashSaltPepper(String input, String salt);
    
    boolean isKnownJWTID(String jwtid);
}
