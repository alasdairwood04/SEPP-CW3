package model;

import java.util.*;
import model.*;

public class FAQManager {
    private final List<FAQSection> sections = new ArrayList<>();

    public FAQManager() {
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

}
