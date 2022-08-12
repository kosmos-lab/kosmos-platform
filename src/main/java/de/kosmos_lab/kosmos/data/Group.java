package de.kosmos_lab.kosmos.data;

import de.dfki.baall.helper.webserver.data.IUser;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.HashSet;


/**
 * The representation of a Group.
 * A Group is used to optionally restrict the access to a device to specific users.
 * There are two levels of access:
 * - user can access the device/function locked behind a scope
 * - admin can additionally manage the users of a scope
 */
public class Group {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_USERS = "users";
    public static final String FIELD_ADMINS = "admins";
    /**
     * add a user as an admin to the scope
     * @param user the user to add as a new admin
     */
    public void addAdmin(@Nonnull IUser user) {
        this.admins.add(user);
    }
    
    
    /**
     * add a user as an user to the scope
     * @param user the user to add as a new user
     */
    public void addUser(@Nonnull IUser user) {
        this.users.add(user);
    }
    
    /**
     * remove an user from the scope
     * @param user the user to be removed from this scope
     */
    public void delUser(@Nonnull IUser user) {
        this.users.remove(user);
    }
    /**
     * remove an admin from the scope - does NOT remove the user level of the same user
     * @param user the admin to be removed from this scope
     */
    public void delAdmin(@Nonnull IUser user) {
        this.admins.remove(user);
    }
    @Nonnull public String getName() {
        return this.name;
    }
    public int getID() {
        return this.id;
    }
    
    public boolean hasUser(@Nonnull IUser user) {
        return users.contains(user);
    }
    public boolean hasAdmin(@Nonnull IUser user) {
        return admins.contains(user);
    }
    
    public Group(@Nonnull Integer id,@Nonnull  String name) {
        this.id = id;
        this.name = name;
    }
    
    private final String name;
    private final int id;
    HashSet<IUser> admins = new HashSet<>();
    HashSet<IUser> users = new HashSet<>();
    boolean hasAccess(@Nonnull IUser user ) {
        if ( admins.contains(user)) {
            return true;
        }
        return users.contains(user);
    }
    @Nonnull public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put(FIELD_NAME,name);
        o.put(FIELD_ID,id);
        JSONArray arr = new JSONArray();
        for (IUser u : admins) {
            arr.put(new JSONObject().put("id",u.getUUID()).put("name",u.getName()));
        }
        o.put(FIELD_ADMINS,arr);
        arr = new JSONArray();
        for (IUser u : users) {
            arr.put(new JSONObject().put("id",u.getUUID()).put("name",u.getName()));
        }
        o.put(FIELD_USERS,arr);
        
        return o;
    }
    @Override
    @Nonnull public String toString() {
        return this.name;
    }
}
