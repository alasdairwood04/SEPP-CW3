package model;

/**
 * Abstract base class for all users.
 */
public abstract class User {
    protected String email;
    protected String role;

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
