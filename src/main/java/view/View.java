package view;

import model.FAQ;
import model.FAQManager;
import model.FAQSection;
import model.Inquiry;

public interface View {
    String getInput(String prompt);
    //TODO add more input methods
    int getIntegerInput(String prompt);

    boolean getYesNoInput(String prompt);
    void displayInfo(String text);
    void displaySuccess(String text);
    void displayWarning(String text);
    void displayError(String text);
    void displayException(Exception e);
    void displayDivider();
    void displayFAQ(FAQManager faq);
    void displayFAQSection(FAQSection section);
    void displayInquiry(Inquiry inquiry);
}
