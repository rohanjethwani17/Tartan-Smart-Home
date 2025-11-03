package tartan.smarthome.resources.iotcontroller;

/**
 * Represents user login information
 */
public class UserLoginInfo {

    /** the user credentials */
    private String userName;
    private String password;

    public UserLoginInfo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     *  Get the username
     * @return the username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the username
     * @param userName username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get password
     * @return password of the user
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password
     * @param password password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
