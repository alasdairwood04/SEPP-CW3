package model;

import java.util.*;

public class SharedContext {
    public static final String ADMIN_STAFF_EMAIL = "inquiries@hindeburg.ac.nz";

    private static final SharedContext instance = new SharedContext();

    public static SharedContext getInstance() {
        return instance;
    }

    public User currentUser;

    public final List<Inquiry> inquiries;
    public final FAQ faq;
    private final Map<String, Set<String>> faqTopicsUpdateSubscribers;


    // TODO: Store each student's timetable, keyed by their email
    private final Map<String, Timetable> studentTimetables;
    // TODO
    private final CourseManager courseManager;

    public SharedContext() {
        this.currentUser = new Guest();
        this.inquiries = new ArrayList<>();
        faq = new FAQ();
        faqTopicsUpdateSubscribers = new HashMap<>();
        this.studentTimetables = new HashMap<>(); // TODO: initialize timetable storage
        this.courseManager = new CourseManager(); //TODO : initialize course manager
    }

    public FAQ getFAQ() {
        return faq;
    }

    public boolean registerForFAQUpdates(String email, String topic) {
        if (faqTopicsUpdateSubscribers.containsKey(topic)) {
            return faqTopicsUpdateSubscribers.get(topic).add(email);
        } else {
            Set<String> subscribers = new HashSet<>();
            subscribers.add(email);
            faqTopicsUpdateSubscribers.put(topic, subscribers);
            return true;
        }
    }

    public boolean unregisterForFAQUpdates(String email, String topic) {
        return faqTopicsUpdateSubscribers.getOrDefault(topic, new HashSet<>()).remove(email);
    }

    public Set<String> usersSubscribedToFAQTopic(String topic) {
        return faqTopicsUpdateSubscribers.getOrDefault(topic, new HashSet<>());
    }

    //TODO : Add a method to get the course manager

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
}
