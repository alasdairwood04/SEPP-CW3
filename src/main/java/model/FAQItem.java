package model;

public class FAQItem {
    private final int id;
    private final String question;
    private final String answer;
    private final String courseTag;

    public FAQItem(int id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.courseTag = null;
    }

    public FAQItem(int id, String question, String answer, String courseTag) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.courseTag = courseTag;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getCourseTag() {
        return courseTag;
    }

    public boolean hasTag(String courseTag) {
        return this.courseTag != null && this.courseTag.equals(courseTag);
    }
}

