package model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FAQSection {
    private final String topic;
    private final List<FAQItem> items = new LinkedList<>();
    private FAQSection parent;
    private final List<FAQSection> subsections = new LinkedList<>();

    public FAQSection(String topic) {
        this.topic = topic;
    }

    public void addSubsection(FAQSection section) {
        subsections.add(section);
        section.parent = this;
    }

    // add item without courseTag
    public void addItem(String question, String answer) {
        int id = items.size();
        items.add(new FAQItem(id, question, answer));
    }

    // add item with courseTag
    public void addItem(String question, String answer, String courseTag) {
        int id = items.size();
        items.add(new FAQItem(id, question, answer, courseTag));
    }

    // removes item using the ID
    public boolean removeItem(int itemID) {
        Iterator<FAQItem> it = items.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == itemID) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public String getItemsByTag(String courseCode) {
        StringBuilder result = new StringBuilder();
        for (FAQItem item : items) {
            if (item.hasTag(courseCode)) {
                result.append(item.getQuestion()).append("\n");
                result.append(item.getAnswer()).append("\n\n");
            }
        }
        return result.toString();
    }

    public boolean hasTopic(String topic) {
        return this.topic.equals(topic);
    }

    public List<FAQSection> getSubsections() {
        return subsections;
    }

    public String getTopic() {
        return topic;
    }

    public List<FAQItem> getItems() {
        return items;
    }

    public FAQSection getParent() {
        return parent;
    }

    public void setParent(FAQSection parent) {
        this.parent = parent;
    }
}
