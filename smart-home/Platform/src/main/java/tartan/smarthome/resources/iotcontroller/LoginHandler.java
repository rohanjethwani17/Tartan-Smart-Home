package tartan.smarthome.resources.iotcontroller;

import java.util.Vector;

/**
 * Validate username and password
 *
 * Project: LG Exec Ed Program
 * Copyright: Copyright (c) 2015 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2015 - initial version
 */
public class LoginHandler {
    /** the maximum number of unsuccessful attempts before lockout */
    static final int MAX_ATTEMPTS = 3;

    /** the number of unsuccessful login attempts */
    private int times;

    /** the list of valid users */
    Vector<UserLoginInfo> validUsers;

    /**
     * The constructor for the login handler
     * @param vu list of valid users
     */
    public LoginHandler(Vector<UserLoginInfo> vu) {
        times = 0;
        validUsers = vu;
    }

    public void resetHandler() {
        times = 0;
    }

    /**
     * Authenticate the username and password
     * @param username the username
     * @param password the password
     * @return true if authenticated, false otherwise
     * @throws LoginAttemptsExceededException if
     */
    public Boolean authenticate(String username, String password) throws LoginAttemptsExceededException {

        if (times > MAX_ATTEMPTS) throw new LoginAttemptsExceededException();

        for (UserLoginInfo uli : validUsers) {
            String un = uli.getUserName();
            String pw = uli.getPassword();
            if (username.equals(un) && password.equals(pw)) {
                return true;
            }
        }
        // increment the invalid login count
        times++;
        return false;
    }
}

