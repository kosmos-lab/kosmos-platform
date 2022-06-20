package de.dfki.baall.helper.webserver.data;

import org.json.JSONObject;

import java.util.UUID;

public interface IUser {
    
    boolean canAccess(int level);
    
    boolean checkPassword(String input);
    
    int getLevel();
    
    String getName();
    
    UUID getUUID();
    
    boolean isAdmin();
    
    
    JSONObject toJWT();
    
}
