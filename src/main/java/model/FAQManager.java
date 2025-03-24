package model;

import java.util.*;
import model.*;

public class FAQManager {
    private final FAQ faq;
    private final List<FAQSection> sections = new LinkedList<>();

    public FAQManager() {
        this.faq = new FAQ();
    }

    public void addSection(String topic){
        FAQSection section = new FAQSection(topic);
        sections.add(section);
        section.setParent(null);
    }

    // dont think i need to implement this since team size of 3
    public void removeSection(String topic){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // dont think i need to implement this since team size of 3
    private void promoteSection(FAQSection section){
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<FAQSection> getSections() {
        return sections;
    }

    public FAQ getFAQ() {
        return this.faq;
    }
}
