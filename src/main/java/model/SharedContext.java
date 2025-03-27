package model;

import java.util.*;
import view.View;

public class SharedContext {
    public static final String ADMIN_STAFF_EMAIL = "inquiries@hindeburg.ac.nz";

    private final View view;
    public User currentUser;
    public final List<Inquiry> inquiries;
    private final FAQManager faqManager;
    private final CourseManager courseManager;

    public SharedContext(View view) {
        this.currentUser = new Guest();
        this.inquiries = new ArrayList<>();
        this.view = view;
        this.faqManager = new FAQManager(view);
        this.courseManager = new CourseManager();
    }

    public FAQ getFAQ() {
        return faqManager.getFAQ();
    }

    public FAQManager getFAQManager() {
        return faqManager;
    }

    public CourseManager getCourseManager() {
        return courseManager;
    }

    public String getCurrentUserEmail() {
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getEmail();
        }
        return null;
    }

    public String getCurrentUserRole() {
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getRole();
        }
        return "Guest";
    }

}
