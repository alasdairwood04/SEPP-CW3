package model;

import java.util.*;
import view.View;

public class SharedContext {
    public static final String ADMIN_STAFF_EMAIL = "inquiries@hindeburg.ac.nz";

    private static final SharedContext instance = new SharedContext();

    public static SharedContext getInstance() {
        return instance;
    }

    private final View view;
    public User currentUser;
    public final List<Inquiry> inquiries;
    private final FAQManager faqManager;
    private final Map<String, Timetable> studentTimetables; // TODO: Store each student's timetable, keyed by their email
    private final CourseManager courseManager;

    public SharedContext(View view) {
        this.currentUser = new Guest();
        this.inquiries = new ArrayList<>();
        this.view = view;
        this.faqManager = new FAQManager(view);
        this.studentTimetables = new HashMap<>(); // TODO: initialize timetable storage
        this.courseManager = new CourseManager(); //TODO : initialize course manager
    }

    public FAQ getFAQ() {
        return faqManager.getFAQ();
    }

    //TODO : Add a method to get the course manager

    public FAQManager getFAQManager() {
        return this.faqManager;
    }

    public CourseManager getCourseManager() {
        return this.courseManager;
    }

    // TODO: Get the student's timetable by email. If not present, create a new one.
    public Timetable getOrCreateTimetable(String studentEmail) {
        return studentTimetables.computeIfAbsent(studentEmail, Timetable::new);
    }

    // TODO: Get the student's timetable if it exists, or return null
    public Timetable getTimetable(String studentEmail) {
        return studentTimetables.get(studentEmail);
    }

    // TODO: Return all student timetables for iteration (e.g., when removing a course)
    public Collection<Timetable> getAllTimetables() {
        return studentTimetables.values();
    }

    //TODO:  Add a method to get the current user's email
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "unknown@hindeburg.ac.nz";
    }

    public String getCurrentUserRole() {
        if (currentUser instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) currentUser).getRole();
        }
        return "";
    }
}
