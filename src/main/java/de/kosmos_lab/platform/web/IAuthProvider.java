package de.kosmos_lab.platform.web;

import de.kosmos_lab.platform.IController;
import de.kosmos_lab.web.data.IUser;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;

public interface IAuthProvider {
    IUser tryLogin(@Nonnull String user, @Nonnull String password) throws de.kosmos_lab.web.exceptions.LoginFailedException;

    static IAuthProvider getInstance(IController controller) {
        throw new NotImplementedException("this is used for importing only");

    }
}
