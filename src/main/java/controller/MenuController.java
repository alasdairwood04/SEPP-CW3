package controller;

import external.AuthenticationService;
import external.EmailService;
import model.*;
import view.View;

public class MenuController extends Controller {
    public MenuController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    public enum GuestMainMenuOption {
        LOGIN,
        CONSULT_FAQ,
        CONTACT_STAFF,
        VIEW_COURSES,
        VIEW_SPECIFIC_COURSE
    }

    public enum StudentMainMenuOption {
        LOGOUT,
        CONSULT_FAQ,
        CONTACT_STAFF,
        ADD_COURSE_TO_TIMETABLE,
        VIEW_TIMETABLE,
        CHOOSE_ACTIVITY_FOR_COURSE,
        REMOVE_COURSE_FROM_TIMETABLE,
        VIEW_COURSES,
        VIEW_SPECIFIC_COURSE
    }

    public enum TeachingStaffMainMenuOption {
        LOGOUT,
        MANAGE_RECEIVED_QUERIES,
        VIEW_COURSES,
        VIEW_SPECIFIC_COURSE
    }

    public enum AdminStaffMainMenuOption {
        LOGOUT,
        MANAGE_QUERIES,
        MANAGE_FAQ,
        MANAGE_COURSES,
        VIEW_COURSES,
        VIEW_SPECIFIC_COURSE
    }

    public void mainMenu() {
        boolean endLoop = false;
        while (!endLoop) {
            String userRole;
            if (sharedContext.currentUser instanceof Guest) {
                userRole = "Guest";
            } else if (sharedContext.currentUser instanceof AuthenticatedUser) {
                userRole = ((AuthenticatedUser) sharedContext.currentUser).getRole();
            } else {
                view.displayError("Main menu not implemented for current user type");
                return;
            }

            view.displayInfo("Hello! What would you like to do?");

            switch (userRole) {
                case "Guest" -> endLoop = handleGuestMainMenu();
                case "Student" -> endLoop = handleStudentMainMenu();
                case "TeachingStaff" -> endLoop = handleTeachingStaffMainMenu();
                case "AdminStaff" -> endLoop = handleAdminStaffMainMenu();
            }
        }

        view.displayInfo("Bye bye!");
    }

    private boolean handleGuestMainMenu() {
        int optionNo = selectFromMenu(GuestMainMenuOption.values(), "Exit");
        if (optionNo == -1) {
            return true;
        }
        GuestMainMenuOption option = GuestMainMenuOption.values()[optionNo];
        switch (option) {
            case LOGIN -> new GuestController(sharedContext, view, auth, email).login();
            case CONSULT_FAQ -> new InquirerController(sharedContext, view, auth, email).consultFAQ();
            case CONTACT_STAFF -> new InquirerController(sharedContext, view, auth, email).contactStaff();
            case VIEW_COURSES -> new ViewerController(sharedContext, view, auth, email).viewCourses();
            case VIEW_SPECIFIC_COURSE -> {
                String courseCode = view.getInput("Enter course code: ");
                new ViewerController(sharedContext, view, auth, email).viewSpecificCourse(courseCode);
            }
        }
        return false;
    }

    private boolean handleStudentMainMenu() {
        int optionNo = selectFromMenu(StudentMainMenuOption.values(), "Exit");
        if (optionNo == -1) {
            return true;
        }
        StudentMainMenuOption option = StudentMainMenuOption.values()[optionNo];
        StudentController studentController = new StudentController(sharedContext, view, auth, email);
        switch (option) {
            case LOGOUT ->
                    new AuthenticatedUserController(sharedContext, view, auth, email).logout();
            case CONSULT_FAQ ->
                    new InquirerController(sharedContext, view, auth, email).consultFAQ();
            case CONTACT_STAFF ->
                    new InquirerController(sharedContext, view, auth, email).contactStaff();
            case ADD_COURSE_TO_TIMETABLE ->
                    studentController.addCourseToTimetable();
            case VIEW_TIMETABLE ->
                    studentController.viewTimetable();
            case CHOOSE_ACTIVITY_FOR_COURSE ->
                    studentController.chooseActivityForCourse();
            case REMOVE_COURSE_FROM_TIMETABLE ->
                    studentController.removeCourseFromTimetable();
            case VIEW_COURSES ->
                    new ViewerController(sharedContext, view, auth, email).viewCourses();
            case VIEW_SPECIFIC_COURSE -> {
                String courseCode = view.getInput("Enter course code: ");
                new ViewerController(sharedContext, view, auth, email).viewSpecificCourse(courseCode);
            }
        }
        return false;
    }

    private boolean handleTeachingStaffMainMenu() {
        int optionNo = selectFromMenu(TeachingStaffMainMenuOption.values(), "Exit");
        if (optionNo == -1) {
            return true;
        }
        TeachingStaffMainMenuOption option = TeachingStaffMainMenuOption.values()[optionNo];
        switch (option) {
            case LOGOUT -> new AuthenticatedUserController(sharedContext, view, auth, email).logout();
            case MANAGE_RECEIVED_QUERIES -> new TeachingStaffController(sharedContext, view, auth, email).manageReceivedInquiries();
            case VIEW_COURSES -> new ViewerController(sharedContext, view, auth, email).viewCourses();
            case VIEW_SPECIFIC_COURSE -> {
                String courseCode = view.getInput("Enter course code: ");
                new ViewerController(sharedContext, view, auth, email).viewSpecificCourse(courseCode);
            }
        }
        return false;
    }

    private boolean handleAdminStaffMainMenu() {
        int optionNo = selectFromMenu(AdminStaffMainMenuOption.values(), "Exit");
        if (optionNo == -1) {
            return true;
        }
        AdminStaffMainMenuOption option = AdminStaffMainMenuOption.values()[optionNo];
        AdminStaffController adminStaffController = new AdminStaffController(sharedContext, view, auth, email);

        switch (option) {
            case LOGOUT ->
                    new AuthenticatedUserController(sharedContext, view, auth, email).logout();
            case MANAGE_FAQ ->
                    adminStaffController.manageFAQ();
            case MANAGE_QUERIES ->
                    adminStaffController.manageInquiries();
            case MANAGE_COURSES ->
                    adminStaffController.manageCourses();
            case VIEW_COURSES ->
                    new ViewerController(sharedContext, view, auth, email).viewCourses();
            case VIEW_SPECIFIC_COURSE -> {
                String courseCode = view.getInput("Enter course code: ");
                new ViewerController(sharedContext, view, auth, email).viewSpecificCourse(courseCode);
            }
        }
        return false;
    }
}