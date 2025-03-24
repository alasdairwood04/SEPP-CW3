package model;

import java.util.*;

public class SharedContext {
    public static final String ADMIN_STAFF_EMAIL = "inquiries@hindeburg.ac.nz";
    public User currentUser;

    public final List<Inquiry> inquiries;
    public final FAQ faq;

    public SharedContext() {
        this.currentUser = new Guest();
        this.inquiries = new ArrayList<>();
        faq = new FAQ();
    }

    public FAQ getFAQ() {
        return faq;
    }

    // TODO implement this method
    public String getCurrentUserRole(){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TODO implement this method
    public String getCurrentUserEmail(){
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
