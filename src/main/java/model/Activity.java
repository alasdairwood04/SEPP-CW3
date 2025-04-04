package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a general activity in a course (abstract class).
 */
public abstract class Activity {
    protected int id;
    protected LocalDate startDate;
    protected LocalTime startTime;
    protected LocalDate endDate;
    protected LocalTime endTime;
    protected String location;
    protected DayOfWeek day;

    public Activity(int id, LocalDate startDate, LocalTime startTime,
                    LocalDate endDate, LocalTime endTime, String location, DayOfWeek day) {
        this.id = id;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.location = location;
        this.day = day;
    }

    public boolean hasId(int id) {
        return this.id == id;
    }

    public int getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public abstract String getType();

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(startDate, startTime);
    }

    
    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(endDate, endTime);
    }

    public boolean overlapsWith(Activity other) {
        if (!this.day.equals(other.day)) return false;

        LocalDateTime thisStart = LocalDateTime.of(this.startDate, this.startTime);
        LocalDateTime thisEnd = LocalDateTime.of(this.endDate, this.endTime);

        LocalDateTime otherStart = LocalDateTime.of(other.startDate, other.startTime);
        LocalDateTime otherEnd = LocalDateTime.of(other.endDate, other.endTime);

        return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }

    @Override
    public String toString() {
        return "[" + getType() + " #" + id + "] "
                + day + " " + startTime + "-" + endTime + " "
                + location + " from " + startDate + " to " + endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;
        Activity activity = (Activity) o;
        return id == activity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
