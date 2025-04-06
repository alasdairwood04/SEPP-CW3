package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * This method is case-insensitive to make the lookup more robust.
     */
    public boolean hasSlotsForCourse(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return false;
        }

        // Case-insensitive comparison to be more user-friendly
        String normalizedCode = courseCode.trim().toUpperCase();

        return timeSlots.stream()
                .anyMatch(slot -> slot.getCourseCode() != null &&
                        slot.getCourseCode().toUpperCase().equals(normalizedCode));
    }

    /**
     * Removes all slots for the given course.
     * This method is case-insensitive to make the removal more robust.
     */
    public void removeSlotsForCourse(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return;
        }

        // Case-insensitive comparison to be more user-friendly
        String normalizedCode = courseCode.trim().toUpperCase();

        timeSlots.removeIf(slot -> slot.getCourseCode() != null &&
                slot.getCourseCode().toUpperCase().equals(normalizedCode));
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
     * Checks for conflicts with a proposed new activity time.
     * Returns the conflicting course and activity ID if found.
     */
    public String[] checkConflicts(LocalDate startDate, LocalTime startTime,
                                   LocalDate endDate, LocalTime endTime) {
        // Combine the dates and times into LocalDateTime objects
        LocalDateTime newStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime newEnd = LocalDateTime.of(endDate, endTime);

        for (TimeSlot slot : timeSlots) {
            LocalDateTime slotStart = LocalDateTime.of(slot.getStartDate(), slot.getStartTime());
            LocalDateTime slotEnd = LocalDateTime.of(slot.getEndDate(), slot.getEndTime());

            // Overlap condition: newStart is before slotEnd and newEnd is after slotStart.
            if (newStart.isBefore(slotEnd) && newEnd.isAfter(slotStart)) {
                return new String[]{slot.getCourseCode(), String.valueOf(slot.getActivityId())};
            }
        }
        return null;
    }

    /**
     * Returns a string representation of the timetable, showing only activities scheduled for the working week (Monday to Friday).
     * Enhanced to ensure activities are properly displayed.
     */
    public String toWorkingWeekString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(studentEmail).append(" (Working Week):\n");

        // Sort time slots by day of week and start time for better readability
        List<TimeSlot> sortedSlots = new ArrayList<>(timeSlots);
        sortedSlots.sort(Comparator
                .comparing(TimeSlot::getDay)
                .thenComparing(TimeSlot::getStartTime));

        boolean hasWorkingWeekSlots = false;

        for (TimeSlot slot : sortedSlots) {
            // Check if the day is between Monday and Friday (inclusive)
            if (slot.getDay().getValue() >= DayOfWeek.MONDAY.getValue() &&
                    slot.getDay().getValue() <= DayOfWeek.FRIDAY.getValue()) {
                sb.append(slot.toString()).append("\n");
                hasWorkingWeekSlots = true;
            }
        }

        if (!hasWorkingWeekSlots) {
            return "No scheduled activities for the working week.";
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        if (timeSlots.isEmpty()) {
            return "No scheduled activities.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(studentEmail).append(":\n");

        // Sort time slots by day of week and start time for better readability
        List<TimeSlot> sortedSlots = new ArrayList<>(timeSlots);
        sortedSlots.sort(Comparator
                .comparing(TimeSlot::getDay)
                .thenComparing(TimeSlot::getStartTime));

        for (TimeSlot slot : sortedSlots) {
            sb.append(slot).append("\n");
        }
        return sb.toString().trim();
    }
}