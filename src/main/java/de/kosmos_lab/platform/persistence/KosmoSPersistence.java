package de.kosmos_lab.platform.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.kosmos_lab.platform.data.DeviceText;
import de.kosmos_lab.platform.persistence.Models.SQL_Device_Text;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.platform.smarthome.CommandInterface;
import de.kosmos_lab.platform.smarthome.CommandSourceName;
import de.kosmos_lab.web.data.IUser;
import de.kosmos_lab.platform.data.DataSchema;
import de.kosmos_lab.platform.data.Device;
import de.kosmos_lab.platform.data.Group;
import de.kosmos_lab.platform.data.KosmoSUser;
import de.kosmos_lab.platform.data.LogEntry;
import de.kosmos_lab.platform.data.Scope;
import de.kosmos_lab.platform.exceptions.GroupAlreadyExistsException;
import de.kosmos_lab.platform.exceptions.ScopeAlreadyExistsException;
import de.kosmos_lab.platform.IController;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class KosmoSPersistence extends SQLPersistence implements CommandInterface {
    private static KosmoSPersistence instance;


    
    HikariDataSource dsRead;
    HikariDataSource dsWrite;
    

    @SuppressFBWarnings({"DM_EXIT", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public KosmoSPersistence(@Nonnull final JSONObject conf,@Nonnull final IController server, final boolean fastinit) {
        super();
        
        this.numConnections = BigInteger.ZERO;
        this.connections = new LinkedList<>();
        
        this.server = server;
        this.url = conf.getString("url");
        logger.info("Using db file: {}",url);
        instance = this;
        HikariConfig config = new HikariConfig();
        //config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        config.setJdbcUrl(url);
        File db = server.getFile("db/");
        if (!db.exists()) {
            if (!db.mkdirs()) {
                logger.warn("could not create database folder \"{}\" - exiting",db);
                System.exit(1);
            }
        }

        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("useUnicode", "true");
        /*config.addDataSourceProperty("maximumPoolSize",
                conf.getInt("maxConnections"));*/
        
        dsRead = new HikariDataSource(config);
        dsRead.setMaximumPoolSize(100);
        dsWrite = new HikariDataSource(config);
        dsWrite.setMaximumPoolSize(1);

		/*try {
			final Driver myDriver = new com.mysql.cj.jdbc.Driver();
			DriverManager.registerDriver(myDriver);
		} catch (final Exception ex) {
			System.out.println("Error: unable to load driver class!");
			System.exit(1);
		}*/
        server.addCommandInterface(this);
    }
    
    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static KosmoSPersistence getInstance() {
        return instance;
    }
    
    @Override
    public void addDevice(@Nonnull Device device) {
        this.doAdd(Models.SQL_Devices.add, new Object[]{
                device.getUniqueID(), device.getName(), device.getSchema().getId(), device.getSource().getSourceName(), device.getLastUpdated().getTime(),device.getOwner().getUUID().getLeastSignificantBits()});
        this.server.cacheDevice(device);
    }
    
    @Nonnull
    public Group addGroup(@Nonnull String name, @Nonnull IUser user) throws GroupAlreadyExistsException {
        int id = this.doAddI(Models.SQL_Groups.add, new Object[]{name});
        if (id > 0) {
            //logger.info("created group {} with id {}", name, id);
            Group group = new Group(id, name);
            this.server.cacheGroup(group);
            addGroupAdmin(group, user);
            return group;
        }
        
        throw new GroupAlreadyExistsException(name);
    }
    
    @Override
    public void addGroupAdmin(@Nonnull Group group, @Nonnull IUser user) {
        group.addAdmin(user);
        try {
            this.doAdd(Models.SQL_Group_Admins.add, new Object[]{user.getUUID().getLeastSignificantBits(), group.getID()});
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info(fullPrint(Models.SQL_Group_Admins.add, new Object[]{user.getUUID().getLeastSignificantBits(), group.getID()}));
        }
    }
    
    @Override
    public void addGroupUser(@Nonnull Group group, @Nonnull IUser user) {
        group.addUser(user);
        this.doAdd(Models.SQL_Group_Users.add, new Object[]{user.getUUID().getLeastSignificantBits(), group.getID()});
        
    }
    
    @Override
    public void addSchema(@Nonnull DataSchema schema) {
        this.doAdd(Models.SQL_Schemas.add, new Object[]{schema.getSchema().getId(), schema.getRawSchema()});
        this.server.cacheSchema(schema);
    }
    
    @Nonnull
    @Override
    public Scope addScope(@Nonnull String name, @Nonnull IUser user) throws ScopeAlreadyExistsException {
        int id = this.doAddI(Models.SQL_Scopes.add, new Object[]{name});
        if (id > 0) {
            Scope scope = new Scope(id, name);
            this.server.cacheScope(scope);
            addScopeAdmin(scope, user);
            return scope;
        }
        
        throw new ScopeAlreadyExistsException(name);
    }
    
    @Override
    public void addScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user) {
        scope.addAdmin(user);
        //logger.info("adding admin to scope {} {}", user.getID(), scope.getID());
        this.doAdd(Models.SQL_Scope_Admins.add, new Object[]{user.getUUID().getLeastSignificantBits(), scope.getID()});
        
        
    }

    
    @Override
    public void addScopeGroup(@Nonnull Scope scope, @Nonnull Group group) {
        scope.addUserGroup(group);
        
        this.doAdd(Models.SQL_Scope_Group_User.add, new Object[]{group.getID(), scope.getID()});
    }
    @Override
    public void addScopeAdminGroup(@Nonnull Scope scope, @Nonnull Group group) {
        scope.addAdminGroup(group);

        this.doAdd(Models.SQL_Scope_Group_Admins.add, new Object[]{group.getID(), scope.getID()});
    }

    @Override
    public void addScopeUser(@Nonnull Scope scope, @Nonnull IUser user) {
        scope.addUser(user);
        
        this.doAdd(Models.SQL_Scope_Users.add, new Object[]{user.getUUID().getLeastSignificantBits(), scope.getID()});
        
    }
    
    @Override
    public void addUser(@Nonnull IUser user) {
        if ( user instanceof  KosmoSUser) {
            KosmoSUser kuser = (KosmoSUser) user;
            int id = this.doAddI(Models.SQL_Users.add, new Object[]{user.getName(), user.getLevel(), kuser.getSalt(), kuser.getHash()});
            if (id != 0) {
                kuser.setID(id);
            }
            this.server.cacheUser(user);
        }
    }
    
    @Override
    protected Connection connect(@Nonnull String query) {
        //logger.info("currently: {}  connections - adding connection for {}",connections.size(), query);
        
        this.numConnections = this.numConnections.add(BigInteger.ONE);
        
        try {
            Connection c;
            //if its a read only query we can get the read connection, which CAN be multiple at once
            if (query.toLowerCase().startsWith("select")) {
                c = dsRead.getConnection();
                
                
            } else {
                //else we need to share ONE connection, because sqlite will lock the DB if a write operation is ongoing
                c = dsWrite.getConnection();
                
                
            }
            if ((c != null) && !c.isClosed()) {
                
                this.connections.add(c);
                
                
                return c;
            }
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        logger.warn("no connection to SQL!");
        return null;
    }
    
    @Override
    protected void createTable(@Nonnull String table) {
        String fTable = String.format("`%s`",table);
        for ( Class c : Models.class.getDeclaredClasses()) {

            try {
                Field t = c.getDeclaredField("table");
                if (table.equals(t) || fTable.equals(t)) {
                    Field f = c.getDeclaredField("create");

                    this.doUpdate(String.valueOf(f.get(null)));
                    return;
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            //if ()
        }
        
    }
    
    @Override
    public void delDevice(@Nonnull Device device) {
        this.doUpdate(Models.SQL_Devices.deleteByuuid, new Object[]{device.getUniqueID()});
        
    }
    
    @Override
    public void delGroup(@Nonnull Group group) {
    
        logger.info("deleting users from group: {} matches",this.doUpdate(Models.SQL_Group_Users.deleteBygroup,new Object[]{group.getID()}));
        logger.info("deleting admin from group: {} matches",this.doUpdate(Models.SQL_Group_Admins.deleteBygroup,new Object[]{group.getID()}));
        logger.info("deleting group: {} matches",this.doUpdate(Models.SQL_Groups.deleteByname, new Object[]{group.getName()}));
        
    }
    
    @Override
    public void delGroupAdmin(@Nonnull Group group, @Nonnull IUser user) {
        group.delAdmin(user);
        this.doUpdate(Models.SQL_Group_Users.deleteByuserAndgroup, new Object[]{user.getUUID().getLeastSignificantBits(), group.getID()});
        
    }
    
    @Override
    public void delGroupUser(@Nonnull Group group, @Nonnull IUser user) {
        group.delUser(user);
        this.doUpdate(Models.SQL_Group_Users.deleteByuserAndgroup, new Object[]{user.getUUID().getLeastSignificantBits(), group.getID()});
    }
    
    @Override
    public void delSchema(@Nonnull DataSchema schema) {
        this.doUpdate(Models.SQL_Schemas.deleteByID, new Object[]{schema.getSchema().getId()});
        
    }
    
    @Override
    public void delScope(@Nonnull Scope scope) {
        logger.info("deleting scope: {}",scope.getName());
        this.doUpdate(Models.SQL_Scope_Admins.deleteByscope, new Object[]{scope.getID()});
        this.doUpdate(Models.SQL_Scope_Group_Admins.deleteByscope, new Object[]{scope.getID()});
        this.doUpdate(Models.SQL_Scope_Users.deleteByscope, new Object[]{scope.getID()});
        this.doUpdate(Models.SQL_Scope_Group_User.deleteByscope, new Object[]{scope.getID()});
        this.doUpdate(Models.SQL_Scopes.deleteById, new Object[]{scope.getID()});
        
        
        
    }
    
    @Override
    public void delScopeAdmin(@Nonnull Scope scope, @Nonnull IUser user) {
        scope.delAdmin(user);
        this.doUpdate(Models.SQL_Scope_Admins.deleteByuserAndscope, new Object[]{user.getUUID().getLeastSignificantBits(), scope.getID()});
        
    }
    
    @Override
    public void delScopeUser(@Nonnull Scope scope, @Nonnull IUser user) {
        scope.delUser(user);
        this.doUpdate(Models.SQL_Scope_Users.deleteByuserAndscope, new Object[]{user.getUUID().getLeastSignificantBits(), scope.getID()});
    }
    
    @Override
    public void delUser(@Nonnull IUser user) {
        this.doUpdate(Models.SQL_Users.deleteByname, new Object[]{user.getName()});
    }
    
    @Override
    public void deviceAdded(@CheckForNull CommandInterface from,@Nonnull Device device,@Nonnull CommandSourceName source) {
        this.addDevice(device);
        this.deviceUpdate(this, device, null, device.getSource());
    }
    
    @Override
    public void deviceRemoved(@CheckForNull CommandInterface from, @Nonnull Device device, @Nonnull CommandSourceName source) {
        this.delDevice(device);
        
    }
    
    @Override
    public void deviceUpdate(@CheckForNull CommandInterface from, @Nonnull Device device, @CheckForNull String key,@Nonnull  CommandSourceName source) {
        //if we should not log, remove all old states
        if (!this.server.shouldDeviceBeLogged(device)) {
            this.doUpdate(Models.SQL_States.deleteAllByUUID, new Object[]{device.getUniqueID()});
        }
        //we always NEED the last state to be known, or else we get missing required key errors while starting up
        this.doAdd(Models.SQL_States.add, new Object[]{device.getUniqueID(), device.toString(), System.currentTimeMillis(), source.getSourceName()});
    }
    
    @Override
    public void fillDeviceScopes(@Nonnull Device device) {
        this.doSelect(Models.SQL_Scope_Devices.selectByuuid, new Object[]{device.getUniqueID()}, Models.SQL_Scope_Devices::parse);
    }
    @Override
    public void fillTexts(@Nonnull Device device) {
        this.doSelect(Models.SQL_Device_Text.selectByUUID, new Object[]{device.getUniqueID()}, Models.SQL_Device_Text::parse);
    }
    @Override
    public void fillGroup(@Nonnull Group group) {
        this.doSelect(Models.SQL_Group_Users.selectBygroup, new Object[]{group.getID()}, Models.SQL_Group_Users::parse);
        this.doSelect(Models.SQL_Group_Admins.selectBygroup, new Object[]{group.getID()}, Models.SQL_Group_Admins::parse);
        
        
    }
    
    @Override
    public void fillScope(@Nonnull Scope scope) {
        this.doSelect(Models.SQL_Scope_Users.selectByscope, new Object[]{scope.getID()}, Models.SQL_Scope_Users::parse);
        this.doSelect(Models.SQL_Scope_Admins.selectByscope, new Object[]{scope.getID()}, Models.SQL_Scope_Admins::parse);
        this.doSelect(Models.SQL_Scope_Group_User.selectByscope, new Object[]{scope.getID()}, Models.SQL_Scope_Group_User::parse);
        this.doSelect(Models.SQL_Scope_Group_Admins.selectByscope, new Object[]{scope.getID()}, Models.SQL_Scope_Group_Admins::parse);
        
        
    }
    
    @Nonnull
    @Override
    public Group getGroup(@Nonnull String name) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Groups.selectByname, new Object[]{name}, Models.SQL_Groups::parse);
        
        
    }
    
    @Nonnull
    @Override
    public Group getGroup(int id) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Groups.selectByID, new Object[]{id}, Models.SQL_Groups::parse);
        
        
    }

    @Nonnull private String getIn(Set<String> in) {
        if (in.size() > 0) {
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter = in.iterator();
            
            sb.append("('");
            sb.append(iter.next());
            sb.append("'");
            while (iter.hasNext()) {
                sb.append(",'");
                sb.append(iter.next());
                sb.append("'");
            }
            
            sb.append(")");
            
            return sb.toString();
        }
        return "()";
        
    }

    @Nonnull private String getIn(String[] in) {
        if (in.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("('");
            sb.append(in[0]);
            sb.append("'");
            for (int i = 1; i < in.length; i++) {
                sb.append(",'");
                sb.append(in[i]);
                sb.append("'");
            }
            
            sb.append(")");
            
            return sb.toString();
        }
        return "()";
        
    }
    
    @Nonnull
    @Override
    public JSONObject getLastState(@Nonnull String uuid) {
        try {
            return doSelectFirst(Models.SQL_States.selectLastByUUID, new Object[]{uuid}, Models.SQL_States::parse).getState();
        } catch (NotFoundInPersistenceException e) {
            //ignore, this can and should happen
            //e.printStackTrace();
        }
        return new JSONObject();
    }
    
    @Override
    public int getNumberOfDevicesWithSchema(@Nonnull DataSchema schema) {
        try {
            return this.doSelectFirstAsInt(Models.SQL_Devices.getNumberOfDevicesWithSchema, new Object[]{schema.getSchema().getId()});
        } catch (NotFoundInPersistenceException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Nonnull
    @Override
    public DataSchema getSchema(@Nonnull String id) throws NotFoundInPersistenceException {
        
        return this.doSelectFirst(Models.SQL_Schemas.selectByID, new Object[]{id}, Models.SQL_Schemas::parse);
        
        
    }
    
    @Nonnull
    @Override
    public Scope getScope(@Nonnull String name) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Scopes.selectByname, new Object[]{name}, Models.SQL_Scopes::parse);
        
        
    }
    
    @Nonnull
    @Override
    public Scope getScope(int id) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Scopes.selectByID, new Object[]{id}, Models.SQL_Scopes::parse);
        
        
    }
    
    @Override
    public String getSourceName() {
        return "Persistence";
    }
    
    @Nonnull
    @Override
    public List<LogEntry> getStates(long from, long to, @Nonnull String[] uuids) {
        return doSelect(Models.SQL_States.selectAllByUUIDS.replaceFirst("\\?", getIn(uuids)), new Object[]{from, to}, Models.SQL_States::parse);
        
    }
    
    @Nonnull
    @Override
    public List<LogEntry> getStates(long from, long to, @Nonnull Set<String> uuids) {
        return doSelect(Models.SQL_States.selectAllByUUIDS.replaceFirst("\\?", getIn(uuids)), new Object[]{from, to}, Models.SQL_States::parse);
        
    }
    
    @Override
    public List<LogEntry> getStates(long from, long to) {
        return doSelect(Models.SQL_States.selectAllBetween, new Object[]{from, to}, Models.SQL_States::parse);
        
    }
    
    @Nonnull
    @Override
    public IUser getUser(int id) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Users.selectByid, new Object[]{id}, Models.SQL_Users::parse);
    }
    
    @Nonnull
    @Override
    public IUser getUser(@Nonnull String username) throws NotFoundInPersistenceException {
        return this.doSelectFirst(Models.SQL_Users.selectByname, new Object[]{username}, Models.SQL_Users::parse);
    }
    
    @Override
    public void init() {
        
        /*this.doUpdate("CREATE TABLE \"scope_users\" (\n" +
                "\t\"user\" INTEGER NOT NULL,\n" +
                "\t\"scope\" INTEGER NOT NULL\n" +
                ")\n" +
                ";\n");
        this.doUpdate("CREATE TABLE \"scope_admins\" (\n" +
                "\t\"user\" INTEGER NOT NULL,\n" +
                "\t\"scope\" INTEGER NOT NULL\n" +
                ")\n" +
                ";\n");
        this.doUpdate("CREATE TABLE \"scopes\" (\n" +
                "\t\"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"name\" VARCHAR(64) NOT NULL\n" +
                ")\n" +
                ";\n");
        this.doUpdate("CREATE TABLE \"model_schemas\" (\n" +
                "\t\"model\" TEXT NOT NULL,\n" +
                "\t\"manufacturer\" TEXT NOT NULL,\n" +
                "\t\"schema\" TEXT NOT NULL\n" +
                ")");
        
        this.doUpdate("CREATE TABLE \"user\" (\n" +
                "\t\"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"name\" VARCHAR(64) NOT NULL,\n" +
                "\t\"level\" INTEGER NOT NULL,\n" +
                "\t\"salt\" VARCHAR(26) NOT NULL,\n" +
                "\t\"hash\" VARCHAR(64) NOT NULL\n" +
                ")\n" +
                ";\n");
        
        this.doUpdate("CREATE TABLE \"schema\" (\n" +
                "\t\"ID\" VARCHAR(256) NOT NULL,\n" +
                "\t\"schema\" TEXT NOT NULL,\n" +
                "\tPRIMARY KEY (\"ID\")\n" +
                ")\n" +
                ";");
        this.doUpdate("CREATE TABLE \"devices\" (\n" +
                "\t\"uuid\" VARCHAR(128) NOT NULL,\n" +
                "\t\"name\" VARCHAR(128) NOT NULL,\n" +
                "\t\"schema\" VARCHAR(256) NOT NULL,\n" +
                "\t\"source\" VARCHAR(256) NULL,\n" +
                "\t\"lastUpdate\" BIGINT NULL,\n" +
                "\tPRIMARY KEY (\"uuid\")\n" +
                ")\n" +
                ";\n");
        this.doUpdate("CREATE TABLE \"states\" (\n" +
                "\t\"uuid\" VARCHAR(128) NOT NULL,\n" +
                "\t\"state\" TEXT NOT NULL,\n" +
                "\t\"date\" BIGINT NOT NULL,\n" +
                "\t\"source\" TEXT NULL\n" +
                ")\n" +
                ";\n");
        this.doUpdate("CREATE TABLE \"scope_devices\" (\n" +
                "\t\"uuid\" VARCHAR(128) NOT NULL,\n" +
                "\t\"read\" INTEGER NOT NULL,\n" +
                "\t\"write\" INTEGER NOT NULL,\n" +
                "\t\"del\" INTEGER NOT NULL\n" +
                ")\n" +
                ";");
        this.doUpdate("CREATE INDEX idx_scope_device_uuid \n" +
                "ON `scope_devices` (`uuid`);");
        this.doUpdate("CREATE UNIQUE INDEX idx_user_name\n" +
                "ON `user` (`name`);");
        this.doUpdate("CREATE UNIQUE INDEX idx_scope_name \n" +
                "ON `scopes` (`name`);");
        this.doUpdate("CREATE UNIQUE INDEX idx_scope_admins \n" +
                "ON `scope_admins` (`user`,`scope`);");
        this.doUpdate("CREATE UNIQUE INDEX idx_scope_users \n" +
                "ON `scope_users` (`user`,`scope`);");
        this.doUpdate("CREATE INDEX idx_scope_admins_scope \n" +
                "ON `scope_admins` (`scope`);");
        this.doUpdate("CREATE INDEX idx_scope_users_scope \n" +
                "ON `scope_users` (`scope`);");
        */
        for (Class clazz : Models.class.getDeclaredClasses()) {
            try {
                String query = (String) clazz.getField("create").get(null);
                for (String q : query.split(";")) {
                    q = q.trim();
                    if (q.length() > 2) {
                        //logger.info("sending query for {} {}", clazz.getName(), q);
                        this.doUpdate(q);
                    }
                }
                
                
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            
        }
        
        
        this.initUsers();
        this.initGroups();
        this.initSchema();
        this.initDevices();
        this.initScopes();
        this.initLocations();
        this.initTexts();

    }
    
    @Nonnull
    @Override
    public Collection<Device> initDevices() {
        return this.doSelect(Models.SQL_Devices.selectAll, new Object[]{}, Models.SQL_Devices::parse);
    }
    
    @Nonnull
    @Override
    public Collection<Group> initGroups() {
        return this.doSelect(Models.SQL_Groups.selectAll, new Object[]{}, Models.SQL_Groups::parse);

    }
    
    @Override
    public Collection<Device.Location> initLocations() {
        return this.doSelect(Models.SQL_Device_Location.selectAll, new Object[]{}, Models.SQL_Device_Location::parse);
    }
    public Collection<DeviceText> initTexts() {
        return this.doSelect(Models.SQL_Device_Text.selectAll, new Object[]{}, Models.SQL_Device_Text::parse);
    }
    public void addDeviceText(DeviceText dt ) {
        this.doAdd(SQL_Device_Text.add,new Object[]{dt.getDevice().getUniqueID(),dt.getKey(),dt.getValue()});
    }
    public void updateDeviceText(DeviceText dt ) {
        this.doAdd(SQL_Device_Text.updateValue,new Object[]{dt.getValue(),dt.getDevice().getUniqueID(),dt.getKey()});
    }
    @Nonnull
    @Override
    public Collection<DataSchema> initSchema() {
        return this.doSelect(Models.SQL_Schemas.selectAll, new Object[]{}, Models.SQL_Schemas::parse);
    }
    
    @Nonnull
    @Override
    public Collection<Scope> initScopes() {
        List<Scope> list = this.doSelect(Models.SQL_Scopes.selectAll, new Object[]{}, Models.SQL_Scopes::parse);
        this.doSelect(Models.SQL_Scope_Devices.selectAll, new Object[]{}, Models.SQL_Scope_Devices::parse);
        return list;
        /*this.doSelect(Models.SQL_Scope_Users.selectAll, new Object[]{}, Models.SQL_Scope_Users::parse);
        this.doSelect(Models.SQL_Scope_Group_User.selectAll, new Object[]{}, Models.SQL_Scope_Group_User::parse);
        this.doSelect(Models.SQL_Scope_Admins.selectAll, new Object[]{}, Models.SQL_Scope_Admins::parse);
        this.doSelect(Models.SQL_Scope_Group_Admins.selectAll, new Object[]{}, Models.SQL_Scope_Group_Admins::parse);*/
        
    }
    
    @Nonnull
    @Override
    public List<KosmoSUser> initUsers() {
        //return this.doSelect("select * from `users`",new Object[]{},)
        return this.doSelect(Models.SQL_Users.selectAll, new Object[]{}, Models.SQL_Users::parse);
        
        
    }
    
    @Override
    public void setDelScope(@Nonnull Device device, @Nonnull Scope scope) {
        this.doUpdate(Models.SQL_Scope_Devices.updatedelByuuid, new Object[]{scope.getID(), device.getUniqueID()});
    }
    
    @Override
    public void setName(@Nonnull Device device, @Nonnull String name) {
        device.setName(name);
        this.doUpdate(Models.SQL_Devices.updatenameByuuid, new Object[]{device.getName(), device.getUniqueID()});
        
    }
    
    @Override
    public void setPassword(@Nonnull IUser user, @Nonnull String salt, @Nonnull String hash) {
        this.doUpdate(Models.SQL_Users.updatehashByname, new Object[]{hash, user.getName()});
        this.doUpdate(Models.SQL_Users.updatesaltByname, new Object[]{salt, user.getName()});
        if (user instanceof  KosmoSUser) {
            ((KosmoSUser)user).setPassword(salt, hash);
        }
    }
    
    @Override
    public void setReadScope(@Nonnull Device device, @Nonnull Scope scope) {
        this.doUpdate(Models.SQL_Scope_Devices.updatereadByuuid, new Object[]{scope.getID(), device.getUniqueID()});
        
    }
    
    @Override
    public void setWriteScope(@Nonnull Device device, @Nonnull Scope scope) {
        this.doUpdate(Models.SQL_Scope_Devices.updatewriteByuuid, new Object[]{scope.getID(), device.getUniqueID()});
    }
    
    public void stop() {
        this.dsRead.close();
        this.dsWrite.close();
    }
    
    @Override
    public String toString() {
        return "Persistence";
    }
    
    public void updateLastUpdate(@Nonnull Device device) {
        this.doUpdate(Models.SQL_Devices.updatelastupdateByuuid, new Object[]{device.getLastUpdated().getTime(), device.getUniqueID()});
    }
    
    @Override
    public void updateLocation(@Nonnull Device device) {
        Device.Location loc = device.getLocation();
        if (loc != null) {
            this.doUpdate(Models.SQL_Device_Location.add, new Object[]{device.getUniqueID(), loc.getX(), loc.getY(), loc.getZ(), loc.getW(), loc.getD(), loc.getH(), loc.getRoll(), loc.getPitch(), loc.getYaw(), loc.getArea()});
        }
        
    }
}
