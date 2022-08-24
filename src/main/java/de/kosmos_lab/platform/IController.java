package de.kosmos_lab.platform;

import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.exceptions.*;
import de.kosmos_lab.platform.gesture.GestureProvider;
import de.kosmos_lab.platform.persistence.Constants.CacheMode;
import de.kosmos_lab.platform.persistence.IPersistence;
import de.kosmos_lab.platform.rules.RulesService;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.platform.smarthome.SmartHomeInterface;
import de.kosmos_lab.platform.web.KosmoSWebServer;
import de.kosmos_lab.web.server.JWT;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.web.exceptions.ParameterNotFoundException;
import de.kosmos_lab.platform.data.Config;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.LogEntry;
import de.kosmos_lab.platform.data.LoggingRequest;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.platform.data.StateUpdates;
import de.kosmos_lab.platform.plugins.camera.ICamera;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * interface used for the methods needed for the platform to work
 */
public interface IController {

    void addCommandInterface(@Nonnull CommandInterface commandInterface);

    /**
     * cache the device to memory
     *
     * @param device the device to cache
     */
    void cacheDevice(@Nonnull Device device);

    /**
     * set the device location to memory
     *
     * @param uuid     the uuid of the device this location belongs to
     * @param location the location to cache
     */
    void setDeviceLocation(@Nonnull String uuid, @Nonnull Device.Location location);

    /**
     * cache the group to memory
     *
     * @param group
     */
    void cacheGroup(@Nonnull Group group);


    /**
     * cache the Schema to memory
     *
     * @param schema
     */
    void cacheSchema(@Nonnull DataSchema schema);

    /**
     * cache the scope to memory
     *
     * @param scope
     */
    void cacheScope(@Nonnull Scope scope);

    /**
     * cache the user to memory
     *
     * @param user
     */
    void cacheUser(@Nonnull IUser user);

    /**
     * check if we are currently in testing mode, this is needed for some special cases
     *
     * @return
     */
    boolean currentlyInTesting();

    /**
     * used to determine if the given device should be logged or not
     *
     * @param device
     *
     * @return
     */
    boolean shouldDeviceBeLogged(@Nonnull Device device);


    /**
     * add a device to the system
     *
     * @param from         the source it was added from
     * @param device       the device to add
     * @param addPermanent should the device be permament, ie be recreated after a restart
     */
    void addDevice(@Nonnull CommandInterface from, @Nonnull Device device, boolean addPermanent);

    /**
     * add a scope to the system
     *
     * @param name the name of the scope
     * @param user the owner of the new scope, will be added as its first admin
     *
     * @return
     */
    Scope addScope(@Nonnull String name, @Nonnull IUser user) throws ScopeAlreadyExistsException;


    /**
     * add a group to the scope
     *
     * @param scope
     * @param group
     */
    void addScopeGroup(@Nonnull Scope scope, @Nonnull Group group);
    void addScopeAdminGroup(@Nonnull Scope scope, @Nonnull Group group);

    /**
     * add a user to the scope
     *
     * @param scope
     * @param u
     */
    void addScopeUser(@Nonnull Scope scope, @Nonnull IUser u);
    void addScopeAdmin(@Nonnull Scope scope, @Nonnull IUser u);

    /**
     * add a smart home
     *
     * @param smartHomeInterface
     */
    void addSmartHome(@Nonnull SmartHomeInterface smartHomeInterface);

    /**
     * delete a device
     *
     * @param from
     * @param device
     */
    void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device);


    /**
     * find schema for the given device
     *
     * @param manufacturer
     * @param model
     *
     * @return
     *
     * @throws SchemaNotFoundException
     */
    DataSchema findSchema(@Nonnull String manufacturer, @Nonnull String model) throws SchemaNotFoundException;


    /**
     * return all devices
     *
     * @return
     */
    Collection<Device> getAllDevices();

    /**
     * get the device specified by the id
     *
     * @param uuid
     *
     * @return
     */
    @Nonnull
    Device getDevice(@Nonnull String uuid) throws DeviceNotFoundException;

    /**
     * get the device specified by the id
     *
     * @param uuid
     * @param user
     *
     * @return
     */
    @Nonnull
    Device getDevice(@Nonnull String uuid, @Nonnull IUser user) throws DeviceNotFoundException, NoAccessToScope;

    /**
     * @param uuid
     * @param user
     * @param scopeType
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws NoAccessToScope
     */
    @Nonnull
    Device getDevice(@Nonnull String uuid, @Nonnull IUser user, @Nonnull Scope.ScopeType scopeType) throws DeviceNotFoundException, NoAccessToScope;


    /**
     * get the fallback schema
     *
     * @return
     */
    @Nonnull
    DataSchema getFallBackSchema();

    /**
     * set the delete scope for the given device
     *
     * @param device
     * @param scope
     */
    void setDelScope(@Nonnull Device device, @Nonnull Scope scope);

    /**
     * get the jwt instance
     *
     * @return
     */
    JWT getJwt();

    /**
     * return a hash with salt and pepper from the given input
     *
     * @param input the input to hash
     * @param salt  the salt to use
     *
     * @return
     */
    String hashSaltPepper(@Nonnull String input, @Nonnull String salt);

    /**
     * @param from
     * @param uuid
     * @param json
     * @param source
     * @param user
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws ValidationException
     * @throws NoAccessToScope
     */
    Device parseHASet(@Nonnull CommandInterface from, @Nonnull String uuid, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope;

    /**
     * parse set is used to parse a set command given by an external source
     *
     * @param from
     * @param uuid
     * @param json
     * @param source
     * @param user
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws ValidationException
     * @throws NoAccessToScope
     */
    Device parseSet(@Nonnull CommandInterface from, @Nonnull String uuid, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope;

    Device parseSet(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull JSONObject json, @Nonnull CommandSourceName source, @Nonnull IUser user) throws DeviceNotFoundException, ValidationException, NoAccessToScope;

    void updateFromSource(@Nonnull CommandInterface from, @Nonnull CommandSourceName source, @Nonnull String uuid, @Nonnull String property, @Nonnull Object value) throws ValidationException, DeviceNotFoundException;

    void setName(@Nonnull Device d, @Nonnull String name);

    Device.Location setLocation(@Nonnull IUser user, @Nonnull JSONObject json, @Nonnull CommandSourceName source) throws DeviceNotFoundException, NoAccessToScope, ParameterNotFoundException;

    DataSchema getSchema(@Nonnull String s) throws SchemaNotFoundException;

    void setReadScope(@Nonnull Device device, @Nonnull Scope scope);

    String hashPepper(@Nonnull String input);

    Group addGroup(@Nonnull String name, @Nonnull IUser user) throws GroupAlreadyExistsException;

    void stop();

    void addGroupAdmin(@Nonnull Group group, @Nonnull IUser user);

    void addGroupUser(@Nonnull Group group, @Nonnull IUser user);

    void setWriteScope(@Nonnull Device device, @Nonnull Scope scope);

    /**
     * update the users password
     *
     * @param user the user
     * @param salt the new salt
     * @param hash the new hash
     */
    void setUserPassword(@Nonnull IUser user, @Nonnull String salt, @Nonnull String hash);

    CommandSourceName getSource(@Nonnull String sourceName);

    IUser getUser(@Nonnull String name);

    IPersistence getPersistence();

    IUser getUserCreateIfUnavailable(@Nonnull String username);

    Scope getScope(int id, CacheMode cacheMode) throws NotFoundInPersistenceException;

    IUser getUser(int userid) throws UserNotFoundException;

    void parseAddDevice(@Nonnull CommandInterface from, @Nonnull JSONObject o, @Nonnull CommandSourceName source, @Nonnull IUser user) throws ValidationException, DeviceAlreadyExistsException, ParameterNotFoundException, SchemaNotFoundException;

    void parseAddDevice(@Nonnull CommandInterface from, @Nonnull JSONObject o, @Nonnull CommandSourceName source, @Nonnull IUser user, boolean permanent) throws ValidationException, DeviceAlreadyExistsException, ParameterNotFoundException, SchemaNotFoundException;

    void publishUpdate(@CheckForNull CommandInterface from, @Nonnull Device device, @CheckForNull String key, @Nonnull CommandSourceName source);

    HashMap<Device, StateUpdates> getUpdates(@Nonnull Collection<String> uuids, long maxage);

    @CheckForNull
    IUser tryLogin(@CheckForNull String username, @CheckForNull String password);

    void updateFromSource(@Nonnull CommandInterface from, @Nonnull CommandSourceName source, @Nonnull Device device, @Nonnull String key, Object o);

    void updateLastUpdate(@Nonnull Device device);

    @Nonnull
    Scope getScope(@Nonnull String name, CacheMode cacheMode) throws NotFoundInPersistenceException;

    RulesService getRulesService();

    String getPasswordHash(String input, String salt);

    List<LogEntry> getLogs(@Nonnull Calendar calStart, @Nonnull Calendar calEnd, @Nonnull Set<String> uuids);

    List<LogEntry> getLogs(@Nonnull Calendar calStart, @Nonnull Calendar calEnd, @Nonnull String[] uuids);

    /**
     * get the location of a given device
     *
     * @param user
     * @param uuid
     *
     * @return
     *
     * @throws DeviceNotFoundException
     * @throws NoAccessToScope
     */
    @CheckForNull
    Device.Location getLocation(@Nonnull IUser user, @Nonnull String uuid) throws DeviceNotFoundException, NoAccessToScope;

    /**
     * get the group with the given name
     *
     * @param name
     * @param cacheMode should we only query the cache?
     *
     * @return
     *
     * @throws GroupNotFoundException
     */
    @Nonnull
    Group getGroup(@Nonnull String name, CacheMode cacheMode) throws GroupNotFoundException;


    /**
     * get the group with the given id
     *
     * @param id
     * @param cacheMode should we only query the cache?
     *
     * @return
     *
     * @throws GroupNotFoundException
     */
    @Nonnull
    Group getGroup(int id, CacheMode cacheMode) throws GroupNotFoundException;


    /**
     * get the GestureProvider instance
     *
     * @return
     */
    GestureProvider getGestureProvider();

    /**
     * get the config instance
     *
     * @return
     */
    Config getConfig();

    /**
     * get the webserver instance
     *
     * @return
     */
    KosmoSWebServer getWebServer();

    /**
     * get all scopes in which the given user has user access
     *
     * @param user
     *
     * @return
     */
    Collection<Scope> getAllScopesWithUser(@Nonnull IUser user);

    /**
     * get all scopes in which the given user has admin access
     *
     * @param user
     *
     * @return
     */
    Collection<Scope> getAllScopesWithAdmin(@Nonnull IUser user);

    /**
     * get all known schemas
     *
     * @return
     */
    Collection<DataSchema> getAllSchemas();

    /**
     * get a list of all group in which the user has user access
     *
     * @param user
     *
     * @return
     */
    Collection<Group> getAllGroupsWithUser(@Nonnull IUser user);

    /**
     * get a list of all groups in which the user has admin access
     *
     * @param user
     *
     * @return
     */
    Collection<Group> getAllGroupsWithAdmin(@Nonnull IUser user);


    /**
     * delete a user
     *
     * @param user
     */
    void deleteUser(@Nonnull IUser user);

    /**
     * delete a scope
     *
     * @param scope
     */
    void deleteScope(@Nonnull Scope scope);

    /**
     * delete a schema
     *
     * @param schema
     */
    void deleteSchema(@Nonnull DataSchema schema);

    /**
     * delete a device
     *
     * @param from
     * @param device
     * @param source
     */
    void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull String source);

    /**
     * delete a device
     *
     * @param from
     * @param device
     * @param source
     */
    void deleteDevice(@Nonnull CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source);

    /**
     * remove user access for the user in the given scope
     *
     * @param scope
     * @param user
     *
     * @return
     */
    boolean delScopeUser(@Nonnull Scope scope, @Nonnull IUser user);


    /**
     * remove the admin access for the user in the given scope
     *
     * @param scope
     * @param user
     *
     * @return
     */
    boolean delScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user);

    /**
     * remove a user from group
     *
     * @param group
     * @param user
     *
     * @return
     */
    boolean delGroupUser(@Nonnull Group group, IUser user);


    /**
     * delete the group
     *
     * @param group
     */
    void delGroup(@Nonnull Group group);


    /**
     * create subs for the given uuids with the filename
     *
     * @param path       the path in which the file should be saved
     * @param filename   the name of the created subtitle file
     * @param videoStart the start of the video
     * @param videoEnd   the end of the video
     * @param uuids      the uuids we want to create subs for
     * @param delta      the delta between video and database time
     */
    void createSubs(@Nonnull String path, @Nonnull String filename, @Nonnull Calendar videoStart, @Nonnull Calendar videoEnd, @Nonnull List<LoggingRequest> uuids, long delta);


    /**
     * revokes the user as an group admin
     *
     * @param group
     * @param user
     *
     * @return
     */
    boolean delGroupAdmin(@Nonnull Group group, @Nonnull IUser user);

    /**
     * adds the user
     *
     * @param u
     */
    void addUser(@Nonnull IUser u);


    /**
     * add the schema
     *
     * @param ds
     */
    void addSchema(@Nonnull DataSchema ds);

    /**
     * gets the UUIds that match the given list
     *
     * @param uuidlist
     *
     * @return
     */
    Collection<String> getMatchingUUID(@Nonnull String uuidlist);

    String getFileString(String name);

    File getFile(String name);

    KosmosPluginManager getPluginManager();

    @Nonnull
    Collection<ICamera> getAllCameras();

    @Nonnull
    ICamera getCamera(@Nonnull String name) throws CameraNotFoundException;

    void startRecording(@Nonnull ICamera cam, @Nonnull IUser user);

    void stopRecording(@Nonnull ICamera cam, @Nonnull IUser user) throws NoAccessException;

    @Nonnull
    Collection<File> listRecordings(@Nonnull ICamera cam, @Nonnull IUser user);

    byte[] getRecording(@Nonnull IUser user, @Nonnull String filename) throws NoAccessToRecording;
}
