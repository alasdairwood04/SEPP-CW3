package model;

public class Guest extends User {
    public Guest() {
        this.email = "guest@system.local";
        this.role = "Guest";
    }
}
