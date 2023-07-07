package de.dfki.baall.helper.persistence;


import de.kosmos_lab.web.data.User;
import de.kosmos_lab.web.exceptions.LoginFailedException;
import de.kosmos_lab.web.persistence.exceptions.AlreadyExistsException;
import de.kosmos_lab.web.persistence.exceptions.NotFoundInPersistenceException;

import javax.annotation.CheckForNull;
import java.util.UUID;

public interface IUserPersistence extends IPersistence {
    /**
     * add a user to the persistence
     *
     * @param username
     * @param password
     *
     * @return
     */
    boolean addUser(@CheckForNull String username, @CheckForNull String password, int level) throws AlreadyExistsException;

    /**
     * get a user from persistence
     *
     * @param username
     *
     * @return
     */
    User getUser(@CheckForNull String username) throws NotFoundInPersistenceException;

    /**
     * try to login
     *
     * @param username
     * @param password
     *
     * @return returns a jwt token on success
     *
     * @throws LoginFailedException
     */
    User login(@CheckForNull String username, @CheckForNull String password) throws LoginFailedException;

    User getUser(@CheckForNull UUID uuid) throws NotFoundInPersistenceException;

    int initUsers();

}
