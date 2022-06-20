package de.kosmos_lab.kosmos.platform.persistence;

import de.dfki.baall.helper.persistence.exceptions.NotFoundInPersistenceException;
import de.kosmos_lab.kosmos.data.Device;
import de.kosmos_lab.kosmos.data.LogEntry;
import de.kosmos_lab.kosmos.platform.IController;
import de.kosmos_lab.utils.StringFunctions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC")

public abstract class SQLPersistence implements IPersistence {


    protected String url;
    protected LinkedList<Connection> connections;
    protected BigInteger numConnections;


    static final org.slf4j.Logger logger = LoggerFactory.getLogger("SQLPersistence");
    protected IController server;

    public static Integer getInt(final ResultSet rs, final Integer index) {
        try {
            return rs.getInt(index);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Long getLong(final ResultSet rs, final Integer index) {
        try {
            return rs.getLong(index);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public final static int[] getInta2(final IController server, final ResultSet rs, final boolean getData) {
        try {
            return new int[]{rs.getInt(1), rs.getInt(2)};
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new int[]{0, 0};
    }

    public final static int getInt(final IController server, final ResultSet rs, final boolean getData) {
        try {
            return rs.getInt(1);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public final static double getDouble(final IController server, final ResultSet rs, final boolean getData) {
        try {
            return rs.getDouble(1);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public final static long getLong(final IController server, final ResultSet rs, final boolean getData) {
        try {
            return rs.getLong(1);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public final static Date getDate(final IController server, final ResultSet rs, final boolean getData) {
        try {
            return rs.getDate(1);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Float parseNullFloat(final Float v) {
        if (v == -1) {
            return null;
        }
        return v;
    }

    protected void close(final Connection conn) {
        try {
            conn.close();
        } catch (final SQLException e) {

        }
        try {
            this.connections.remove(conn);
        } catch (final Exception e) {

        }
        try {
            // conNames.remove(conn);
        } catch (final Exception e) {

        }
    }

    protected void clean(final Statement stmt, final ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException sqlEx) {
            } // ignore

        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqlEx) {
            } // ignore

        }
    }


    protected int getConnections() {

        return this.connections.size();
    }

    protected abstract Connection connect(String query);

    final Pattern noSuchTablePattern = Pattern.compile(".*\\(no such table: (.*)\\)");
    final Pattern tableExistsPattern = Pattern.compile(".*\\(table (.*) already exists\\)");

    /**
     * Checks the exception to determine what to do
     * @param ex the given exception
     * @param query the query that was executed
     * @param params the parameters that were given
     * @return returns true if the request should be tried again
     */
    protected boolean checkException(final SQLException ex, String query, Object[] params) {

        if (ex.getMessage().startsWith("Duplicate")) {
            return false;
        }
        if (ex.getMessage().contains("UNIQUE constraint failed")) {
            return false;
        }
        if (ex.getMessage().contains("already exists")) {

            return false;
        }
        if (ex.getMessage().contains("Connection is closed")) {

            return true;
        }
        if (ex.getMessage().contains("database is locked")) {

            return true;
        }
        Matcher m = tableExistsPattern.matcher(ex.getMessage());
        if (m.matches()) {
            return false;
        }
        m = noSuchTablePattern.matcher(ex.getMessage());
        if (m.matches()) {
            final String table = m.group(1);
            logger.error("can not find table:" + table);
            this.createTable(table);
            return false;
        }
        if (ex.getMessage().contains("A NOT NULL constraint failed")) {
            logger.debug(ex.getMessage());
            return false;
        }
        if (ex.getMessage().contains("Data truncation:")) {
            logger.debug(ex.getMessage());
            return false;
        }
        if (query != null) {
            logger.error(fullPrint(query, params));
        }
        logger.error("SQLException", ex);
        return false;
    }

    protected abstract void createTable(String table);


    protected String formatDate(final Date p) {
        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(Constants.formatDateAndTime);
        return sdf.format(p);
    }

    protected void fillStatement(final PreparedStatement stmt, final Object[] params) throws SQLException {
        for (int i = 0; i < params.length; ) {
            final Object p = params[i];
            if (p == null) {
                stmt.setNull(++i, Types.OTHER);

            } else if (p instanceof Integer) {
                stmt.setInt(++i, (int) p);
            } else if (p instanceof Long) {
                stmt.setLong(++i, (long) p);
            } else if (p instanceof Float) {
                stmt.setFloat(++i, (float) p);
            } else if (p instanceof Double) {
                stmt.setDouble(++i, (double) p);
            } else if (p instanceof Byte) {
                stmt.setByte(++i, (Byte) p);
            } else if (p instanceof Short) {
                stmt.setShort(++i, (short) p);
            } else if (p instanceof Date) {

                stmt.setString(++i, this.formatDate((Date) p));

            } else if (p instanceof Boolean) {
                stmt.setBoolean(++i, (boolean) p);
            } else if (p instanceof JSONObject) {
                stmt.setString(++i, p.toString());
            } else if (p instanceof JSONArray) {
                stmt.setString(++i, p.toString());
            } else if (p instanceof NullTypes) {
                switch ((NullTypes) p) {
                    case BIT:
                        stmt.setNull(++i, Types.BIT);
                        break;
                    case TINYINT:
                        stmt.setNull(++i, Types.TINYINT);
                        break;
                    case SMALLINT:
                        stmt.setNull(++i, Types.SMALLINT);
                        break;
                    case INTEGER:
                        stmt.setNull(++i, Types.INTEGER);
                        break;
                    case BIGINT:
                        stmt.setNull(++i, Types.BIGINT);
                        break;
                    case FLOAT:
                        stmt.setNull(++i, Types.FLOAT);
                        break;
                    case REAL:
                        stmt.setNull(++i, Types.REAL);
                        break;
                    case DOUBLE:
                        stmt.setNull(++i, Types.DOUBLE);
                        break;
                    case NUMERIC:
                        stmt.setNull(++i, Types.NUMERIC);
                        break;
                    case DECIMAL:
                        stmt.setNull(++i, Types.DECIMAL);
                        break;
                    case CHAR:
                        stmt.setNull(++i, Types.CHAR);
                        break;
                    case VARCHAR:
                        stmt.setNull(++i, Types.VARCHAR);
                        break;
                    case LONGVARCHAR:
                        stmt.setNull(++i, Types.LONGVARCHAR);
                        break;
                    case DATE:
                        stmt.setNull(++i, Types.DATE);
                        break;
                    case TIME:
                        stmt.setNull(++i, Types.TIME);
                        break;
                    case TIMESTAMP:
                        stmt.setNull(++i, Types.TIMESTAMP);
                        break;
                    case BINARY:
                        stmt.setNull(++i, Types.BINARY);
                        break;
                    case VARBINARY:
                        stmt.setNull(++i, Types.VARBINARY);
                        break;
                    case LONGVARBINARY:
                        stmt.setNull(++i, Types.LONGVARBINARY);
                        break;
                    case NULL:
                        stmt.setNull(++i, Types.NULL);
                        break;
                    case JAVA_OBJECT:
                        stmt.setNull(++i, Types.JAVA_OBJECT);
                        break;
                    case DISTINCT:
                        stmt.setNull(++i, Types.DISTINCT);
                        break;
                    case STRUCT:
                        stmt.setNull(++i, Types.STRUCT);
                        break;
                    case ARRAY:
                        stmt.setNull(++i, Types.ARRAY);
                        break;
                    case BLOB:
                        stmt.setNull(++i, Types.BLOB);
                        break;
                    case CLOB:
                        stmt.setNull(++i, Types.CLOB);
                        break;
                    case REF:
                        stmt.setNull(++i, Types.REF);
                        break;
                    case DATALINK:
                        stmt.setNull(++i, Types.DATALINK);
                        break;
                    case BOOLEAN:
                        stmt.setNull(++i, Types.BOOLEAN);
                        break;
                    case ROWID:
                        stmt.setNull(++i, Types.ROWID);
                        break;
                    case NCHAR:
                        stmt.setNull(++i, Types.NCHAR);
                        break;
                    case NVARCHAR:
                        stmt.setNull(++i, Types.NVARCHAR);
                        break;
                    case LONGNVARCHAR:
                        stmt.setNull(++i, Types.LONGNVARCHAR);
                        break;
                    case NCLOB:
                        stmt.setNull(++i, Types.NCLOB);
                        break;
                    case SQLXML:
                        stmt.setNull(++i, Types.SQLXML);
                        break;
                    case REF_CURSOR:
                        stmt.setNull(++i, Types.REF_CURSOR);
                        break;
                    case TIME_WITH_TIMEZONE:
                        stmt.setNull(++i, Types.TIME_WITH_TIMEZONE);
                        break;
                    case TIMESTAMP_WITH_TIMEZONE:
                        stmt.setNull(++i, Types.TIMESTAMP_WITH_TIMEZONE);
                        break;
                    case OTHER:
                    default:
                        stmt.setNull(++i, Types.OTHER);
                        break;

                }

            } else if (p instanceof String) {
                stmt.setString(++i, (String) p);
            } else if (p instanceof String[]) {
                stmt.setArray(++i, stmt.getConnection().createArrayOf("VARCHAR", (Object[]) p));
            } else {
                logger.info("could not fill for type {}", p.getClass().toString());
                stmt.setString(++i, p.toString());
            }

        }

    }

    protected int doUpdate(final String query) {
        return this.doUpdate(query, new Object[]{});
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected int doUpdate(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        final ResultSet rs = null;
        int affected = 0;
        try {
            stmt = conn.prepareStatement(query);
            this.fillStatement(stmt, params);
            affected = stmt.executeUpdate();

        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return affected;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected <T> List<T> doSelect(final String query, final Object[] params,
                                   final Function<T, IController, ResultSet, Boolean> parser, final boolean fetchdata) {
        final List<T> list = new LinkedList<T>();
        //logger.info("query {}",query);
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(query);
            this.fillStatement(stmt, params);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final T t = parser.apply(this.server, rs, fetchdata);
                if (t != null) {
                    list.add(t);
                }
            }
        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return list;
    }

    protected <T> List<T> doSelect(final String query, final Object[] params,
                                   final Function<T, IController, ResultSet, Boolean> parser) {
        return this.doSelect(query, params, parser, true);
    }

    protected <T> T doSelectFirst(final String query, final Object[] params,
                                  final Function<T, IController, ResultSet, Boolean> parser) throws NotFoundInPersistenceException {
        return this.doSelectFirst(query, params, parser, true);
    }

    protected int doSelectFirstAsInt(final String query, final Object[] params) throws NotFoundInPersistenceException {

        return this.doSelectFirst(query, params, SQLPersistence::getInt, true);

    }

    protected int[] doSelectFirst2AsInt(final String query, final Object[] params) throws NotFoundInPersistenceException {

        return this.doSelectFirst(query, params, SQLPersistence::getInta2, true);

    }

    protected double doSelectFirstAsDouble(final String query, final Object[] params)
            throws NotFoundInPersistenceException {

        return this.doSelectFirst(query, params, SQLPersistence::getDouble, true);

    }

    protected long doSelectFirstAsLong(final String query, final Object[] params)
            throws NotFoundInPersistenceException {
        return this.doSelectFirst(query, params, SQLPersistence::getLong, true);
    }

    protected Date doSelectFirstAsDate(final String query, final Object[] params)
            throws NotFoundInPersistenceException {
        return this.doSelectFirst(query, params, SQLPersistence::getDate, true);
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected <T> T doSelectFirst(final String query, final Object[] params,
                                  final Function<T, IController, ResultSet, Boolean> parser, final boolean fetchdata)
            throws NotFoundInPersistenceException {

        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tries = 5;

        while(tries>0) {
            try {
                stmt = conn.prepareStatement(query);
                this.fillStatement(stmt, params);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    return parser.apply(this.server, rs, fetchdata);

                }

            } catch (final SQLException ex) {
                if(!this.checkException(ex, query, params)) {
                    throw new NotFoundInPersistenceException(String.format("query:%s", fullPrint(query, params)));
                }
                tries--;
                if(tries>0) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                this.clean(stmt, rs);
                this.close(conn);
            }
        }

        throw new NotFoundInPersistenceException(String.format("query:%s", fullPrint(query, params)));
    }

    public abstract List<LogEntry> getStates(long from, long to);

    public abstract Collection<Device.Location> initLocations();


    public boolean parseBoolean(final String value) {
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }


    protected int doAddI(final String query) {
        return this.doAddI(query, new Object[]{});
    }

    protected int doAddI(final String query, final Object param) {
        return this.doAddI(query, new Object[]{param});
    }

    protected int doAddI(final String query, final Object param1, final Object param2) {
        return this.doAddI(query, new Object[]{param1, param2});
    }

    protected int doAddI(final String query, final Object param1, final Object param2, final Object param3) {
        return this.doAddI(query, new Object[]{param1, param2, param3});
    }

    protected int doAddI(final String query, final Object param1, final Object param2, final Object param3,
                         final Object param4) {
        return this.doAddI(query, new Object[]{param1, param2, param3, param4});
    }

    /**
     * Executes an insert Statement with an automatic generated key and returns the id.
     *
     * @param query
     * @param params
     *
     * @return the generated ID OR 0 if there was an error
     */
    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected int doAddI(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        int id = 0;
        try {
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            this.fillStatement(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {

            this.clean(stmt, rs);

            this.close(conn);
        }
        return id;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected short doAddS(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        short id = 0;
        try {
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            this.fillStatement(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getShort(1);
            }
        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return id;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected byte doAddB(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        byte id = 0;
        try {
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            this.fillStatement(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getByte(1);
            }
        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return id;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected int doAdd(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        final ResultSet rs = null;
        int affected = 0;
        try {
            stmt = conn.prepareStatement(query);
            this.fillStatement(stmt, params);
            affected = stmt.executeUpdate();

        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return affected;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected int doAddBatch(final String query, final Collection<Object[]> params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        final ResultSet rs = null;
        int affected = 0;
        try {
            stmt = conn.prepareStatement(query);
            for (final Object[] p : params) {
                try {
                    this.fillStatement(stmt, p);
                    stmt.addBatch();
                } catch (final SQLException ex) {
                    /*for (int i = 0; i < p.length; i++) {
                        logger.warn("Query is: {}", query);
                        
                        logger.warn("Param {} is {}", i, p[i]);
                    }*/

                    this.checkException(ex, query, p);
                }

            }
            final int[] aff = stmt.executeBatch();
            for (final int i : aff) {
                affected += i;
            }

        } catch (final SQLException ex) {
            this.checkException(ex, query, new Object[]{});
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return affected;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected int doUpdateBatch(final String query, final Collection<Object[]> params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        final ResultSet rs = null;
        int affected = 0;
        try {
            stmt = conn.prepareStatement(query);
            for (final Object[] p : params) {
                try {
                    this.fillStatement(stmt, p);
                    stmt.addBatch();
                } catch (final SQLException ex) {
                    /*for (int i = 0; i < p.length; i++) {
                        logger.warn("Param {} is {}", i, p[i]);
                    }*/

                    this.checkException(ex, query, p);
                }

            }
            final int[] aff = stmt.executeBatch();
            for (final int i : aff) {
                affected += i;
            }

        } catch (final SQLException ex) {
            this.checkException(ex, query, new Object[]{});
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return affected;
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION") //we DO actually close them all - but in their own methods
    protected long doAddL(final String query, final Object[] params) {
        final Connection conn = this.connect(query);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        long id = 0;
        try {
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            this.fillStatement(stmt, params);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getLong(1);
            }
        } catch (final SQLException ex) {
            this.checkException(ex, query, params);
        } catch (final Exception e) {
            logger.debug("error", e);
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
        return id;
    }

    public void deleteAll() {
        final Connection conn = this.connect("#truncate all");

        final PreparedStatement stmt = null;
        ResultSet rs = null;
        final List<String> tables = new LinkedList<String>();
        try {

            final DatabaseMetaData md = conn.getMetaData();
            rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            if (tables.size() > 0) {

                // doUpdate("SET FOREIGN_KEY_CHECKS = 0;");
                for (final String t : tables) {
                    this.doUpdate("DELETE FROM `" + t + "`;");

                }
                // doUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            }

        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.clean(stmt, rs);
            this.close(conn);
        }
    }


    protected enum NullTypes {
        BIT, TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE, NUMERIC, DECIMAL, CHAR, VARCHAR, LONGVARCHAR,
        DATE, TIME, TIMESTAMP, BINARY, VARBINARY, LONGVARBINARY, NULL, OTHER, JAVA_OBJECT, DISTINCT, STRUCT, ARRAY,
        BLOB, CLOB, REF, DATALINK, BOOLEAN, ROWID, NCHAR, NVARCHAR, LONGNVARCHAR, NCLOB, SQLXML, REF_CURSOR,
        TIME_WITH_TIMEZONE, TIMESTAMP_WITH_TIMEZONE
    }

    @FunctionalInterface
    interface Function<One, Two, Three, Four> {
        One apply(Two two, Three tree, Four four);
    }

    public class Column {
        public int type;
        public boolean nullable;
        public int len;
        public String name;
        public boolean index = false;
        public boolean increment = false;

        public Column(final String name, final boolean autoincr, final int type, final boolean nullable,
                      final int len) {
            this.name = name;
            this.increment = autoincr;
            this.type = type;
            this.nullable = nullable;
            this.len = len;
        }

        public boolean equals(final Column c) {
            return (c.name.equalsIgnoreCase(this.name));
        }

        @Override
        public boolean equals(final Object c) {
            if (c instanceof Column) {
                return (((Column) c).name.equalsIgnoreCase(this.name));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, nullable, len, name, index, increment);
        }
    }

    public class Index {
        public String name;
        public List<Column> columns;
        public boolean unique;

        public Index(final String name) {
            this.name = name;
            this.columns = new LinkedList<Column>();
        }

        public void addColumn(final Column c) {
            this.columns.add(c);

        }
    }

    public class Table {
        public Collection<Column> columns;
        public Collection<Index> indexes;
        public String name;

        public Table(final String name, final Collection<Column> columns, final Collection<Index> indexes) {
            this.name = name;
            this.columns = columns;
            this.indexes = indexes;
        }

        public Collection<Column> getCols() {
            return this.columns;
        }

        public Collection<Index> getIndex() {
            return this.indexes;
        }
    }

    public Table describeTable(final String table) {
        final Collection<Column> cols = new LinkedHashSet<Column>();
        final Map<String, Index> indexMap = new HashMap<String, Index>();
        final Collection<Index> index = new LinkedHashSet<Index>();
        final HashMap<String, Column> colMap = new HashMap<String, Column>();
        final Connection conn = this.connect("describeTable");
        ResultSet rs = null;
        ResultSet rs2 = null;
        try {
            final DatabaseMetaData dbmeta = conn.getMetaData();
            rs = dbmeta.getColumns(null, null, table, null);
            final StringWriter sb = new StringWriter();
            while (rs.next()) {
                logger.info("----------------------------");

                final ResultSetMetaData meta = rs.getMetaData();
                final int columnCount = meta.getColumnCount();

                for (int column = 1; column <= columnCount; ++column) {

                    final Object value = rs.getObject(column);
                    logger.info("{} : {}", meta.getColumnName(column), value);


                }
                try {
                    final Column c = new Column(rs.getString("COLUMN_NAME"),
                            this.parseBoolean(rs.getString("IS_AUTOINCREMENT")), rs.getInt("DATA_TYPE"),
                            this.parseBoolean(rs.getString("IS_NULLABLE")), 0);
                    if (cols.contains(c) || colMap.containsKey(c.name)) {
                        continue;
                    }
                    cols.add(c);
                    colMap.put(c.name, c);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                //WriterLog.log(sb, Level.INFO, "----------------------------");
            }
            logger.info(sb.toString());
            rs2 = dbmeta.getIndexInfo(null, null, table, false, false);
            while (rs2.next()) {
                final Column c = colMap.get(rs2.getString("COLUMN_NAME"));
                c.index = true;
                Index ind = indexMap.get(rs2.getString("INDEX_NAME"));
                if (ind == null) {
                    ind = new Index(rs2.getString("INDEX_NAME"));
                    indexMap.put(ind.name, ind);
                    index.add(ind);

                }
                ind.unique = !rs2.getBoolean("NON_UNIQUE");
                ind.addColumn(c);
                //WriterLog.log(null, ch.qos.logback.classic.Level.INFO, "found index: {}", c.name);
            }

        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.clean(null, rs);
            this.clean(null, rs2);
            this.close(conn);
        }

        return new Table(table, cols, index);

    }

    /**
     * used to create SQL Subclass for a given table
     *
     * @param t
     *
     * @return
     */
    public String createSQL(final Table t) {
        final StringBuffer sb = new StringBuffer();
        final StringBuffer f = new StringBuffer();
        String fields = "";
        final StringBuffer ins = new StringBuffer();
        // StringBuffer onDupli = new StringBuffer();
        // {{ stanford

        String insert = "";
        final StringBuffer ins2 = new StringBuffer();
        sb.append(
                "\t	\t /**\n\t * this class is automatically generated from the actual SQL database scheme of the table `"
                        + t.name
                        +
                        "` and will be manually adapted by new queries and the parse method.\n\t */\npublic static class SQL {\n\t\t// {{ begin autogenerated code from SQL\n\t\tpublic class Field {\n");
        final StringBuffer selects = new StringBuffer();
        final StringBuffer getter = new StringBuffer();
        final StringBuffer indexes = new StringBuffer();

        final StringBuffer deletes = new StringBuffer();
        final StringBuffer updates = new StringBuffer();
        final StringBuffer removes = new StringBuffer();
        int index = 1;
        for (final Column col : t.getCols()) {

            final String c = col.name;
            final String cc = StringFunctions.toCamelCase(c);
            final String field = "Field." + c;

            sb.append("\t\t\tpublic static final String " + c + " = \"`" + c + "`\";\n");
            f.append(field + "+\",\"+");

            if (!col.increment) {
                ins.append("" + field + "+\",\"+");
                ins2.append("?,");

            }

            if (col.type == java.sql.Types.INTEGER) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Integer " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tint v = rs.getInt(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static int " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getInt(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }

            } else if ((col.type == java.sql.Types.FLOAT) || (col.type == java.sql.Types.REAL)) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Float " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tfloat v = rs.getFloat(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n\t\t\t\treturn v;\n\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static float " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getFloat(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }
            } else if (col.type == java.sql.Types.SMALLINT) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Short " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tshort v = rs.getShort(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static short " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getShort(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }

            } else if (col.type == java.sql.Types.TINYINT) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Byte " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tbyte v = rs.getByte(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static byte " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getByte(Index." +
                            cc
                            + ");\n\t\t\t}\n");

                }
            } else if (col.type == java.sql.Types.DATE) {
                if (col.nullable) {
                    getter.append(
                                    "\t\t\tpublic static java.util.Date " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tjava.util.Date v = rs.getDate(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static java.util.Date " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getDate(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }
            } else if (col.type == java.sql.Types.TIMESTAMP) {
                if (col.nullable) {
                    getter.append(
                                    "\t\t\tpublic static java.util.Date " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tfinal Timestamp ts = rs.getTimestamp(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n")
                            .append("\t\t\t\treturn new java.util.Date(ts.getTime());\n").append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static java.util.Date " + cc
                            +
                            " (ResultSet rs) throws SQLException {\n\t\t\t\treturn new java.util.Date(rs.getTimestamp(Index."
                            + cc + ").getTime());\n\t\t\t}\n");
                }
            } else if (col.type == java.sql.Types.BIGINT) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Long " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tlong v = rs.getLong(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static long " + cc
                            + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getLong(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }
            } else if ((col.type == java.sql.Types.BOOLEAN) || (col.type == java.sql.Types.BIT)) {
                if (col.nullable) {
                    getter.append("\t\t\tpublic static Boolean " + cc + " (ResultSet rs) throws SQLException {\n")
                            .append("\t\t\t\tboolean v = rs.getBoolean(Index." + cc + ");\n")
                            .append("\t\t\t\tif ( rs.wasNull()) \n").append("\t\t\t\t{\n")
                            .append("\t\t\t\t\treturn null;\n").append("\t\t\t\t}\n").append("\t\t\t\treturn v;\n")
                            .append("\t\t\t}\n");
                } else {
                    getter.append("\t\t\tpublic static boolean " + cc
                            +
                            " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getBoolean(Index." +
                            cc
                            + ");\n\t\t\t}\n");
                }
            } else if ((col.type == java.sql.Types.CHAR) || (col.type == java.sql.Types.VARCHAR)
                    || (col.type == java.sql.Types.LONGVARCHAR)) {

                getter.append("\t\t\tpublic static String " + cc
                        + " (ResultSet rs) throws SQLException {\n\t\t\t\treturn rs.getString(Index." + cc
                        + ");\n\t\t\t}\n");

            } else {
                logger.warn("cannot find type: {} for {}", col.type, col.name);
            }
            indexes.append("\t\t\tpublic static final int " + cc + " = " + index++ + ";\n");

        }
        // boolean hasUnique = false;
        final HashSet<Column> nonUniqueCols = new HashSet<Column>();
        final HashSet<Column> uniqueCols = new HashSet<Column>();

        for (final Index i : t.getIndex()) {
            final StringBuffer iFields = new StringBuffer();
            final StringBuffer iName = new StringBuffer();

            if (i.columns.size() > 0) {
                if (i.unique) {
                    // hasUnique = true;

                    for (final Column col : t.getCols()) {
                        for (final Column icol : i.columns) {
                            if (!icol.equals(col) && !icol.name.equals(col.name)) {
                                nonUniqueCols.add(col);
                            } else {
                                uniqueCols.add(col);
                            }
                        }

                    }
                }
                for (final Column col : i.columns) {
                    final String c = col.name;
                    final String cc = StringFunctions.toCamelCase(c);
                    final String field = "Field." + c;

                    iFields.append("\"+" + field + "+\"=? AND ");
                    iName.append(cc);
                    iName.append("And");

                }

                String field = iFields.toString();
                field = field.substring(0, field.length() - 4).trim();
                String name = iName.toString();
                name = name.substring(0, name.length() - 3);
                selects.append("\t\tpublic final static String selectBy" + name
                        + " =\"select \"+fields+\" from \"+table+\" where " + field + "\";\n");
                deletes.append("\t\tpublic final static String deleteBy" + name + " =\"delete from \"+table+\"  where "
                        + field + "\";\n");
                for (final Column col2 : t.getCols()) {
                    final String c2 = col2.name;
                    final String cc2 = StringFunctions.toCamelCase(c2);
                    final String field2 = "Field." + c2;

                    updates.append("\t\tpublic final static String update" + cc2 + "By" + name
                            + " =\"update \"+table+\" set \"+" + field2 + "+\"=? where " + field +
                            "\";\n");
                    if (col2.nullable) {
                        removes.append("\t\tpublic final static String remove" + cc2 + "By" + name
                                + " =\"update \"+table+\" set \"+" + field2 + "+\"=NULL where " + field +
                                "\";\n");
                    }
                }
            }

        }
        sb.append("\t\t}\n");
        sb.append("\t\tpublic static class Getter {\n");
        sb.append(getter);
        sb.append("\t\t}\n");
        sb.append("\t\tpublic static class Index {\n");
        sb.append(indexes);
        sb.append("\t\t}\n");
        sb.append("\t\tpublic final static String table = \"`" + t.name + "`\";\n");

        if (f.length() > 2) {
            fields = f.substring(0, f.length() - 5);

            sb.append("\t\tpublic final static String fields =" + fields + ";\n");
        }

        sb.append("\t\tpublic final static String selectAll =\"select \"+fields+\" from \"+table;\n");
        sb.append(selects);
        sb.append(deletes);
        sb.append(updates);
        sb.append(removes);

        if (ins.length() > 2) {
            insert = ins.substring(0, ins.length() - 3);
            sb.append("\t\tpublic final static String add =\"insert into \"+table+\" (\"+" + insert + ") values ("
                    + ins2.substring(0, ins2.length() - 1) + ")\";\n");
            if (nonUniqueCols.size() > 0) {
                sb.append("\t\tpublic final static String addOrUpdate =\"insert into \"+table+\" (\"+" + insert
                        + ") values (" + ins2.substring(0, ins2.length() - 1) + ") ON DUPLICATE KEY UPDATE ");
                for (final Column col : nonUniqueCols) {

                    sb.append("\"+Field." + col.name + "+\"=VALUES(\"+Field." + col.name + "+\"), ");
                }
                sb.deleteCharAt(sb.length() - 2);

                sb.append("\";\n");
                for (final Column ucol : uniqueCols) {

                    sb.append("\t\tpublic final static String update =\" UPDATE \"+table+\" set ");
                    for (final Column col : nonUniqueCols) {

                        sb.append("\"+Field." + col.name + "+\"=?, ");
                    }
                    sb.deleteCharAt(sb.length() - 2);

                    sb.append("WHERE \"+Field." + ucol.name + "+\"=?");
                    sb.append("\";\n");
                    // we do stupid..ish stuff here but this way we get the first element if
                    // available from unique cols
                    break;
                }

            }

        }
        sb.append("\t\t// }} end autogenerated code\n");
        sb.append("\t\tpublic final static Object parse(IController controller, ResultSet rs, boolean getData)\n"
                + "\t\t{\n" + "\t\t\ttry\n" + "\t\t\t{\n" + "\t\t\t\t\n" + "\t\t\t\t\n" + "\t\t\t}\n"
                + "\t\t\tcatch (Exception e)\n" + "\t\t\t{\n" + "\t\t\t\te.printStackTrace();\n" +

                "\t\t\t}\n" + "\t\t\treturn null;\n" + "\t\t}\n");

        sb.append("\t\t// {{ custom queries not generated:\n\t\t\n");
        sb.append("\t\t// }} \n");
        sb.append("\t}");

        return sb.toString();

    }

    public String createSQL(final String table) {
        return this.createSQL(describeTable(table));

    }

    public static String fullPrint(String query, Object[] params) {
        for (int i = 0; i < params.length; i++) {
            try {
                Object p = params[i];

                if (p != null && query.indexOf('?') > -1) {
                    //logger.info("query: {}", query);
                    query = query.replaceFirst("\\?", p.toString());
                }
            } catch (Exception exx) {
                logger.error("could not get full query! for {} ", query,exx);
            }
        }
        return query;
    }

    public static String print(Object[] params) {
        StringBuilder sb = new StringBuilder();
        for (Object o : params) {
            sb.append(o);
            sb.append(",");

        }
        String str = sb.toString();
        if (str.length() > 0) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }
}


