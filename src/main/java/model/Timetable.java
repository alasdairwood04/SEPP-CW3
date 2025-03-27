package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Represents a student's personal timetable.
 */
public class Timetable {

    private final String studentEmail;
    private final List<TimeSlot> timeSlots;

    public Timetable(String studentEmail) {
        this.studentEmail = studentEmail;
        this.timeSlots = new ArrayList<>();
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    /**
     * Adds a time slot to the timetable.
     */
    public void addTimeSlot(DayOfWeek day, LocalDate startDate, LocalTime startTime,
                            LocalDate endDate, LocalTime endTime, String courseCode,
                            int activityId, TimeSlotStatus status) {
        timeSlots.add(new TimeSlot(day, startDate, startTime, endDate, endTime,
                courseCode, activityId, status));
    }

    /**
     * Returns true if there's any slot for the given course code.
     */
    public boolean hasSlotsForCourse(String courseCode) {
        return timeSlots.stream().anyMatch(slot -> slot.hasCourseCode(courseCode));
    }

    /**
     * Removes all slots for the given course.
     */
    public void removeSlotsForCourse(String courseCode) {
        timeSlots.removeIf(slot -> slot.hasCourseCode(courseCode));
    }

    /**
     * Returns the number of "CHOSEN" activities for a course.
     */
    public int numChosenActivities(String courseCode) {
        int count = 0;
        for (TimeSlot slot : timeSlots) {
            if (slot.hasCourseCode(courseCode) && slot.isChosen()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sets an activity as "CHOSEN" if found. Returns true if successful.
     */
    public boolean chooseActivity(String courseCode, int activityId) {
        for (TimeSlot slot : timeSlots) {
            if (slot.hasCourseCode(courseCode) && slot.hasActivityId(activityId)) {
                slot.setStatus(TimeSlotStatus.CHOSEN);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for time conflicts with existing activities.
     * Returns an array: [conflictingCourseCode, activityId] or null if no conflict.
     */
    public String[] checkConflicts(LocalDate startDate, LocalTime startTime,
                                   LocalDate endDate, LocalTime endTime) {
        for (TimeSlot slot : timeSlots) {
            boolean overlaps = !startDate.isAfter(slot.getEndDate()) &&
                    !endDate.isBefore(slot.getStartDate()) &&
                    !startTime.isAfter(slot.getEndTime()) &&
                    !endTime.isBefore(slot.getStartTime());

            if (overlaps) {
                return new String[]{slot.getCourseCode(), String.valueOf(slot.getActivityId())};
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if (timeSlots.isEmpty()) {
            return "No scheduled activities.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(studentEmail).append(":\n");
        for (TimeSlot slot : timeSlots) {
            sb.append(slot).append("\n");
        }
        return sb.toString().trim();
    }
}
