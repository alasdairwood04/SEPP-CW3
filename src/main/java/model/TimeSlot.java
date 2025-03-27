package model;

import java.time.*;

/**
 * Represents a time slot in a student's timetable.
 */
public class TimeSlot {
    private final DayOfWeek day;
    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;
    private final String courseCode;
    private final int activityId;
    private TimeSlotStatus status;

    public TimeSlot(DayOfWeek day, LocalDate startDate, LocalTime startTime,
                    LocalDate endDate, LocalTime endTime,
                    String courseCode, int activityId, TimeSlotStatus status) {
        this.day = day;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.courseCode = courseCode;
        this.activityId = activityId;
        this.status = status;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getCourseCode() { return courseCode; }
    public int getActivityId() { return activityId; }

    public boolean hasCourseCode(String code) {
        return courseCode.equals(code);
    }

    public boolean hasActivityId(int id) {
        return activityId == id;
    }

    public boolean isChosen() {
        return status == TimeSlotStatus.CHOSEN;
    }

    public void setStatus(TimeSlotStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + courseCode + " - " + activityId + " - " + status + "] "
                + day + " " + startTime + "-" + endTime
                + " (" + startDate + " to " + endDate + ")";
    }
}
