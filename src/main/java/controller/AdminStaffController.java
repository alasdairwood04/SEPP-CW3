package controller;

import org.tinylog.Logger;
import external.AuthenticationService;
import external.EmailService;
import model.*;
import view.View;

import java.io.IOException;

public class AdminStaffController extends StaffController {
    public AdminStaffController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    public void manageFAQ() {
        FAQSection currentSection = null;

        while (true) {
            if (currentSection == null) {
                view.displayFAQ(sharedContext.getFAQManager().getFAQ());
                view.displayInfo("[-1] Return to main menu");
            } else {
                view.displayFAQSection(currentSection);
                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));
            }
            view.displayInfo("[-2] Add FAQ item");
            String input = view.getInput("Please choose an option: ");
            try {
                int optionNo = Integer.parseInt(input);

                if (optionNo == -2) {
                    addFAQItem(currentSection);
                } else if (optionNo == -1) {
                    if (currentSection == null) {
                        break;
                    } else {
                        currentSection = currentSection.getParent();
                    }
                } else {
                    try {
                        if (currentSection == null) {
                            currentSection = sharedContext.getFAQManager().getFAQ().getSections().get(optionNo);
                        } else {
                            currentSection = currentSection.getSubsections().get(optionNo);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        view.displayError("Invalid option: " + optionNo);
                    }
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid option: " + input);
            }
        }
    }

    private void addFAQItem(FAQSection currentSection) {
        // When adding an item at root of FAQ, creating a section is mandatory
        boolean createSection = (currentSection == null);
        if (!createSection) {
            createSection = view.getYesNoInput("Would you like to create a new topic for the FAQ item?");
        }

        if (createSection) {
            String newTopic = view.getInput("Enter new topic title: ");
            FAQSection newSection = new FAQSection(newTopic);

            if (currentSection == null) {
                if (sharedContext.getFAQManager().getSections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists!");
                    newSection = sharedContext.getFAQManager().getSections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    sharedContext.getFAQManager().addSection(newTopic);
                    view.displayInfo("Created topic '" + newTopic + "'");
                }
            } else {
                if (currentSection.getSubsections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists under '" + currentSection.getTopic() + "'!");
                    newSection = currentSection.getSubsections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    currentSection.addSubsection(newSection);
                    view.displayInfo("Created topic '" + newTopic + "' under '" + currentSection.getTopic() + "'");
                }
            }
            currentSection = newSection;
        }

        // Display header for clarity
        view.displayInfo("=== Add New FAQ Question-Answer Pair===");


        // Get current user's email for logging
        String currentUserEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        String sectionTopic = currentSection.getTopic();

        // get question
        String question = view.getInput("Enter the question for new FAQ item: ");
        if (question.isBlank()) {
            Logger.error("{}, {}, addFAQItem, {}, FAILURE (Error: the question cannot be empty)",
                    System.currentTimeMillis(),
                    currentUserEmail,
                    sectionTopic);
            view.displayError("The question cannot be empty");
            return;
        }
        String answer = view.getInput("Enter the answer for new FAQ item: ");

        if (answer.isBlank()) {
            Logger.error("{}, {}, addFAQItem, {}, FAILURE (Error: the answer cannot be empty)",
                    System.currentTimeMillis(),
                    currentUserEmail,
                    sectionTopic);
            view.displayError("The answer cannot be empty");
            return;
        }

        boolean addTag = view.getYesNoInput("Would you like to add a course tags");

        FAQItem newItem;

        // TODO have to wait to implement this - need methods getCourseManager(), viewCourses() etc to do this part of the addFAQ part
        if (addTag) {

            // Get course manager from shared context
            CourseManager courseManager = sharedContext.getCourseManager();

            // Get course list
            String fullCourseDetailsAsString  = courseManager.viewCourses();

            // check if there is any courses
            if (fullCourseDetailsAsString.equals("No courses available.")) {
                view.displayInfo("No courses available in the system");

            } else {
                view.displayInfo("Available courses:");

                String[] courseLines = fullCourseDetailsAsString.split("\n");
                for (String line : courseLines) {
                    if (!line.isEmpty()) {
                        String[] parts = line.split(" - ", 2);
                        if (parts.length >= 2) {
                            String courseCode = parts[0].trim();
                            String courseName = parts[1].trim();
                            view.displayInfo(courseCode + " : " + courseName);
                        }
                    }
                }
                // get course code input
                String courseTag = view.getInput("Enter course code to add as tag:");

                // validate input
                if (!courseManager.hasCourse(courseTag)) {
                    Logger.error("{}, {}, addFAQItem, {}, FAILURE (Error: The tag must correspond to a course code)",
                            System.currentTimeMillis(),
                            currentUserEmail,
                            sectionTopic);
                    view.displayError("The tag must correspond to a course code");
                    return; // Exit method
                }
                // add with tag
                currentSection.addItem(question, answer, courseTag);
            }
        } else {
            // add without tag
            currentSection.addItem(question, answer);
        }

        Logger.info("{}, {}, addFAQItem, {}, SUCCESS (A new FAQ item was added)",
                System.currentTimeMillis(),
                currentUserEmail,
                sectionTopic);
        view.displaySuccess("Created new FAQ item");
    }

    public void manageInquiries() {
        String[] inquiryTitles = getInquiryTitles(sharedContext.inquiries);

        while (true) {
            view.displayInfo("Pending inquiries");
            int selection = selectFromMenu(inquiryTitles, "Back to main menu");
            if (selection == -1) {
                return;
            }
            Inquiry selectedInquiry = sharedContext.inquiries.get(selection);

            while (true) {
                view.displayDivider();
                view.displayInquiry(selectedInquiry);
                view.displayDivider();
                String[] followUpOptions = { "Redirect inquiry", "Respond to inquiry" };
                int followUpSelection = selectFromMenu(followUpOptions, "Back to all inquiries");

                if (followUpSelection == -1) {
                    break;
                } else if (followUpOptions[followUpSelection].equals("Redirect inquiry")) {
                    redirectInquiry(selectedInquiry);
                } else if (followUpOptions[followUpSelection].equals("Respond to inquiry")) {
                    respondToInquiry(selectedInquiry);
                    inquiryTitles = getInquiryTitles(sharedContext.inquiries); // required to remove responded inquiry from titles
                    break;
                }
            }
        }
    }

    private void redirectInquiry(Inquiry inquiry) {
        inquiry.setAssignedTo(view.getInput("Enter assignee email: "));
        email.sendEmail(
                SharedContext.ADMIN_STAFF_EMAIL,
                inquiry.getAssignedTo(),
                "New inquiry from " + inquiry.getInquirerEmail(),
                "Subject: " + inquiry.getSubject() + "\nPlease log into the Self Service Portal to review and respond to the inquiry."
        );
        view.displaySuccess("Inquiry has been reassigned");
    }
}
