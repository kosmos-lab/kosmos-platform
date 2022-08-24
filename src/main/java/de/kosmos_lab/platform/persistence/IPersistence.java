package de.kosmos_lab.platform.persistence;

import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.KosmoSUser;
import de.kosmos_lab.platform.data.LogEntry;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.platform.exceptions.GroupAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.ScopeAlreadyExistsException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IPersistence {
    
    /**
     * adds a device to persistence
     *
     * @param device
     */
    void addDevice(@Nonnull Device device);
    
    /**
     * add a group to the persistence
     *
     * @param name the name of the new group
     * @param user the initial admin/owner of the group
     * @return
     */
    @Nonnull
    Group addGroup(@Nonnull String name, @Nonnull IUser user) throws GroupAlreadyExistsException;
    
    /**
     * adds an user as an admin to the group
     *
     * @param group the group
     * @param user  the user to add as admin
     */
    void addGroupAdmin(@Nonnull Group group, @Nonnull IUser user);
    
    /**
     * adds an user as a regular user to the group
     *
     * @param group the group
     * @param user  the user to add as user
     */
    void addGroupUser(@Nonnull Group group, @Nonnull IUser user);
    
    /**
     * add a schema to persistence
     *
     * @param schema
     */
    void addSchema(@Nonnull DataSchema schema);
    
    /**
     * add a scope to persistence
     *
     * @param name
     * @param user
     * @return
     */
    @Nonnull
    Scope addScope(@Nonnull String name, @Nonnull IUser user) throws ScopeAlreadyExistsException;
    
    /**
     * add user as an admin to the scope
     *
     * @param scope
     * @param user
     */
    void addScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user);
    
    /**
     * add a group as user for the scope
     *
     * @param scope
     * @param group
     */
    void addScopeGroup(@Nonnull Scope scope, @Nonnull Group group);
    void addScopeAdminGroup(@Nonnull Scope scope, @Nonnull Group group);

    /**
     * add a user with user access to the scope
     *
     * @param scope
     * @param user
     */
    void addScopeUser(@Nonnull Scope scope, @Nonnull IUser user);
    
    /**
     * adds a new user to persistence
     *
     * @param user
     */
    void addUser(@Nonnull IUser user);
    
    /**
     * delete a device from persistence
     *
     * @param device
     */
    void delDevice(@Nonnull Device device);
    
    /**
     * delete the group from persistence
     * @param group
     */
    void delGroup(@Nonnull Group group);
    
    /**
     * revoke admin access from the group for the user
     *
     * @param group
     * @param user
     */
    void delGroupAdmin(@Nonnull Group group, @Nonnull IUser user);
    
    /**
     * revoke user access from the group for user
     * @param group
     * @param user
     */
    void delGroupUser(@Nonnull Group group, @Nonnull IUser user);
    
    /**
     * delete a schema from persistence
     *
     * @param schema
     */
    void delSchema(@Nonnull DataSchema schema);
    
    /**
     * delete the scope from persistence
     * @param scope
     */
    void delScope(@Nonnull Scope scope);
    
    /**
     * revoke admin access for the scope
     * @param scope
     * @param user
     */
    void delScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user);
    
    /**
     * revoke user access for the scope
     * @param scope
     * @param user
     */
    void delScopeUser(@Nonnull Scope scope, @Nonnull IUser user);
    
    /**
     * delete a user from persistence
     *
     * @param user
     */
    void delUser(@Nonnull IUser user);
    
    /**
     * fill the device scope
     * @param device
     */
    void fillDeviceScopes(@Nonnull Device device);
    
    /**
     * fill the group with its needed information (gets users, admins and so on from database)
     *
     * @param group
     */
    void fillGroup(@Nonnull Group group);
    
    /**
     * fill the scope with the needed information from persistence (users, admins, and so forth)
     *
     * @param scope
     */
    void fillScope(@Nonnull Scope scope);
    
    /**
     * get the given group from persistence
     *
     * @param name
     * @return
     * @throws NotFoundInPersistenceException if the given group was not found
     */
    @Nonnull Group getGroup(@Nonnull String name) throws NotFoundInPersistenceException;
    
    /**
     * get the group with the given id from persistence
     *
     * @param id
     * @return
     * @throws NotFoundInPersistenceException if the given group was not found
     */
    @Nonnull Group getGroup(int id) throws NotFoundInPersistenceException;
    
    /**
     * get the last logged state of a device
     *
     * @param uuid
     * @return
     */
    @Nonnull JSONObject getLastState(@Nonnull String uuid);
    
    /**
     * get the number of device with the given schema, needed for deletion of a scope
     *
     * @param schema
     * @return
     */
    int getNumberOfDevicesWithSchema(@Nonnull DataSchema schema);
    
    /**
     * get the schema from persistence
     *
     * @param id
     * @return
     * @throws NotFoundInPersistenceException schema was not found in persistence
     */
    @Nonnull
    DataSchema getSchema(@Nonnull String id) throws NotFoundInPersistenceException;
    
    /**
     * get the scope from persistence
     * @param id
     * @return
     * @throws NotFoundInPersistenceException the given scope was not found
     */
    @Nonnull
    Scope getScope(int id) throws NotFoundInPersistenceException;
    
    /**
     * get the scope from persistence
     * @param name
     * @return
     * @throws NotFoundInPersistenceException the given scope was not found
     */
    @Nonnull
    Scope getScope(@Nonnull String name) throws NotFoundInPersistenceException;
    
    /**
     * get the state changes logged between those two unix timestamps for the given UUIDs
     *
     * @param from  (unix timestamp) from
     * @param to    (unix timestamp) to
     * @param uuids a list of uuids to get the changes for
     * @return
     */
    @Nonnull List<LogEntry> getStates(long from, long to, @Nonnull String[] uuids);
    
    /**
     * get the state changes logged between those two unix timestamps for the given UUIDs
     *
     * @param from  (unix timestamp) from
     * @param to    (unix timestamp) to
     * @param uuids a list of uuids to get the changes for
     * @return
     */
    @Nonnull List<LogEntry> getStates(long from, long to, @Nonnull Set<String> uuids);
    
    /**
     * gets the user from persistence
     *
     * @param id
     * @return
     * @throws NotFoundInPersistenceException user not found in persistence
     */
    @Nonnull
    IUser getUser(int id) throws NotFoundInPersistenceException;
    
    /**
     * gets the user from persistence
     *
     * @param username
     * @return
     * @throws NotFoundInPersistenceException user not found in persistence
     */
    @Nonnull
    IUser getUser(@Nonnull String username) throws NotFoundInPersistenceException;
    
    /**
     * initialize the persistence
     */
    void init();
    
    /**
     * initialize devices, ie get all known devices from persistence
     *
     * @return
     */
    @Nonnull Collection<Device> initDevices();
    
    /**
     * initialize groups, ie get all known groups from persistence
     *
     * @return
     */
    @Nonnull Collection<Group> initGroups();
    
    /**
     * initialize schemas, ie get all known schemas from persistence
     *
     * @return
     */
    @Nonnull Collection<DataSchema> initSchema();
    
    /**
     * initialize scopes, ie get all known scopes from persistence
     *
     * @return
     */
    @Nonnull Collection<Scope> initScopes();
    
    /**
     * initialize users, ie get all known users from persistence
     *
     * @return
     */
    @Nonnull List<KosmoSUser> initUsers();
    
    /**
     * sets the delete scope of a device to the given scope
     *
     * @param device
     * @param scope
     */
    void setDelScope(@Nonnull Device device, @Nonnull Scope scope);
    
    /**
     * sets the friendly name of the device
     *
     * @param device
     * @param name
     */
    void setName(@Nonnull Device device, @Nonnull String name);
    
    /**
     * sets the password of the given user
     *
     * @param user the user to update
     * @param salt the new salt to set
     * @param hash the new passwordhash to set
     */
    void setPassword(@Nonnull IUser user, @Nonnull String salt, @Nonnull String hash);
    
    /**
     * sets the read scope of a device to the given scope
     *
     * @param device
     * @param scope
     */
    void setReadScope(@Nonnull Device device, @Nonnull Scope scope);
    
    /**
     * sets the write scope of a device to the given scope
     *
     * @param device
     * @param scope
     */
    void setWriteScope(@Nonnull Device device, @Nonnull Scope scope);
    
    /**
     * set the current time as the last update for the given device
     *
     * @param device the device to update
     */
    void updateLastUpdate(@Nonnull Device device);
    
    /**
     * update the location of the device from persistence
     *
     * @param device
     */
    void updateLocation(@Nonnull Device device);
}
