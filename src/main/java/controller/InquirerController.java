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
        String userEmail = null;

        // Set the user email if they're authenticated
        if (sharedContext.currentUser instanceof AuthenticatedUser) {
            userEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        }

        String courseTag = null;
        if (view.getYesNoInput("Would you like to filter by course code?")) {
            courseTag = view.getInput("Enter the course code");

            // validate course code if provided
            if (courseTag.trim().isEmpty()) {
                courseTag = null;
            } else {
                // verify the course code exists
                CourseManager courseManager = sharedContext.getCourseManager();
                if (!courseManager.hasCourse(courseTag)) {
                    view.displayError("Course with code " + courseTag + " does not exist. Showing all FAQ items");
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
                // display top-level FAQ sections
                view.displayFAQ(sharedContext.getFAQManager().getFAQ());
                view.displayInfo("[-1] Return to main menu");
            } else {
                view.displayInfo(currentSection.getTopic() + (courseTag != null ? " (Filtered by  " + courseTag  + ")" : ""));
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
                    // if no tag, it will just display all (no filter)
                    relevantItems.addAll(currentSection.getItems());
                }

                // Display items or notification if no matches
                if (!relevantItems.isEmpty()) {
                    for (FAQItem item : relevantItems) {
                        view.displayInfo(item.getId() + " " + item.getQuestion());
                        view.displayInfo("> " + item.getAnswer());

                        // only show course tag if not filterning and the item has a tag
                        if (courseTag == null && item.getCourseTag() != null && !item.getCourseTag().isEmpty()) {
                            view.displayInfo("> " + item.getCourseTag());
                        }
                        view.displayDivider();
                    }
                } else if (courseTag != null) {
                    view.displayInfo("There are no questions for course '" + courseTag + "' in this topic.");
                    view.displayInfo("You can navigate to other topics to find relevant questions.");
                }

                if (!currentSection.getSubsections().isEmpty()) {
                    view.displayInfo("Subsections:");
                    int i = 0;
                    for (FAQSection subsection : currentSection.getSubsections()) {
                        view.displayInfo("[" + i++ + "] " + subsection.getTopic());

                    }
                }

                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));

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
                } else if (currentSection != null) {
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
