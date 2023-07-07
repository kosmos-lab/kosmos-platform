package de.kosmos_lab.platform.data;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.web.data.IUser;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class KosmoSUser implements IUser {


    public static final int LEVEL_ADMIN = 100;


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("User");
    private int id;

    public boolean isAdmin() {
        return this.level >= LEVEL_ADMIN;
    }

    public void setID(int id) {
        this.id = id;
    }

    private final IController controller;
    private final String user;
    private final int level;
    private String hash;
    private String salt;

    public KosmoSUser(@Nonnull IController controller, @Nonnull String user, int id, int level, @Nonnull String hash, @Nonnull String salt) {
        this.controller = controller;
        this.user = user;
        this.id = id;
        this.level = level;
        this.hash = hash;
        this.salt = salt;
    }

    public boolean canAccess(int level) {
        return this.level >= level;
    }


    public boolean checkPassword(@Nonnull String input) {
        String toTest = controller.getPasswordHash(input, salt);

        return (toTest.equals(hash));

    }

    public int getID() {
        return id;
    }

    public @Nonnull String getHash() {
        return this.hash;
    }

    public @Nonnull String getSalt() {
        return this.salt;
    }

    public int getLevel() {
        return this.level;
    }

    public @Nonnull String getName() {
        return this.user;
    }

    @Override
    public @Nonnull UUID getUUID() {
        return new UUID(0, id);
    }

    public void setPassword(@Nonnull String salt, @Nonnull String hash) {
        this.hash = hash;
        this.salt = salt;
    }

    @Nonnull
    public JSONObject toJWT() {
        JSONObject o = new JSONObject();
        o.put("name", user);
        o.put("id", id);
        o.put("level", level);
        o.put("hash", controller.hashPepper(this.salt).substring(0, 8));
        return o;
    }

    @Override
    public boolean equals(@CheckForNull Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KosmoSUser user = (KosmoSUser) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
