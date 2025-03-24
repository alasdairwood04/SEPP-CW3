package controller;

import external.AuthenticationService;
import external.EmailService;
import model.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.tinylog.Logger;
import view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InquirerController extends Controller {
    public InquirerController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    public void consultFAQ() {
        FAQSection currentSection = null;
        String userEmail;
        if (sharedContext.currentUser instanceof AuthenticatedUser) {
            userEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        } else {
            userEmail = null;
        }

        String courseTag = null;
        if (view.getYesNoInput("Would you like to filter by course code?")) {
            courseTag = view.getInput("Enter the course code");

            // validate course code if provided
            if (courseTag.trim().isEmpty()) {
                courseTag = null;
            } else {
                // verify the course code exists
                if (!sharedContext.getCourseManager().hasCourse(courseTag)) {
                    view.displayError("Course with code " + courseTag + " does not exist");
                    String actionDetails = "consultFAQ - filter by course - " + courseTag;
                    org.tinylog.Logger.error("User: {} - Action: consultFAQ - Input: {} - Status: FAILURE (Error: The tag must correspond to a course code)",
                            userEmail != null ? userEmail : "Guest", courseTag);
                    courseTag = null;
                }
            }
        }

        int optionNo = 0;
        while (currentSection != null || optionNo != -1) {
            if (currentSection == null) {
                view.displayFAQ(sharedContext.getFAQManager().getFAQ());
                view.displayInfo("[-1] Return to main menu");
            } else {
                view.displayInfo(currentSection.getTopic());
                view.displayDivider();

                // Find items matching course tag if filtering is active
                List<FAQItem> relevantItems = new ArrayList<>();
                if (courseTag != null) {
                    for (FAQItem item : currentSection.getItems()) {
                        if (item.hasTag(courseTag)) {
                            relevantItems.add(item);
                        }
                    }
                } else {
                    relevantItems.addAll(currentSection.getItems());
                }

                // Display items or notification if no matches
                if (!relevantItems.isEmpty()) {
                    for (FAQItem item : relevantItems) {
                        view.displayInfo(item.getId() + ". " + item.getQuestion());
                        view.displayInfo("> " + item.getAnswer());
                    }
                } else if (courseTag != null) {
                    view.displayInfo("There are no questions for course '" + courseTag + "' in this topic.");
                    view.displayInfo("You can navigate to other topics to find relevant questions.");
                }

                view.displayInfo("Subsections:");
                int i = 0;
                for (FAQSection subsection : currentSection.getSubsections()) {
                    view.displayInfo("[" + i++ + "] " + subsection.getTopic());
                }

                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));

                if (courseTag != null) {
                    view.displayInfo("Note: Currently filtering by course: " + courseTag);
                }
            }

            String input = view.getInput("Please choose an option: ");

            try {
                optionNo = Integer.parseInt(input);

                if (optionNo != -1) {
                    try {
                        if (currentSection == null) {
                            currentSection = sharedContext.getFAQManager().getFAQ().getSections().get(optionNo);
                        } else {
                            currentSection = currentSection.getSubsections().get(optionNo);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        view.displayError("Invalid option: " + optionNo);
                    }
                } else if (optionNo == -1 && currentSection != null) {
                    currentSection = currentSection.getParent();
                    optionNo = 0;
                }

            } catch (NumberFormatException e) {
                view.displayError("Invalid option: " + input);
            }
        }

        // Log successful completion
        org.tinylog.Logger.info("User: {} - Action: consultFAQ - Input: {} - Status: SUCCESS",
                userEmail != null ? userEmail : "Guest",
                courseTag != null ? "courseTag=" + courseTag : "-");
    }
    public void contactStaff() {
        String inquirerEmail;
        if (sharedContext.currentUser instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) sharedContext.currentUser;
            inquirerEmail = user.getEmail();
        } else {
            inquirerEmail = view.getInput("Enter your email address: ");
            // From https://owasp.org/www-community/OWASP_Validation_Regex_Repository
            if (!inquirerEmail.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
                view.displayError("Invalid email address! Please try again");
                return;
            }
        }

        String subject = view.getInput("Describe the topic of your inquiry in a few words: ");
        if (subject.strip().isBlank()) {
            view.displayError("Inquiry subject cannot be blank! Please try again");
            return;
        }

        String text = view.getInput("Write your inquiry:" + System.lineSeparator());
        if (text.strip().isBlank()) {
            view.displayError("Inquiry content cannot be blank! Please try again");
            return;
        }

        Inquiry inquiry = new Inquiry(inquirerEmail, subject, text);
        sharedContext.inquiries.add(inquiry);

        email.sendEmail(
                SharedContext.ADMIN_STAFF_EMAIL,
                SharedContext.ADMIN_STAFF_EMAIL,
                "New inquiry from " + inquirerEmail,
                "Subject: " + subject + System.lineSeparator() + "Please log into the Self Service Portal to review and respond to the inquiry."
        );
        view.displaySuccess("Your inquiry has been recorded. Someone will be in touch via email soon!");
    }
}
