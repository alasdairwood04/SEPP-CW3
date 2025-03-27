package model;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
/**
 * Represents a Lab activity.
 */
public class Lab extends Activity {
    private int capacity;

    public Lab(int id, LocalDate startDate, LocalTime startTime,
               LocalDate endDate, LocalTime endTime,
               String location, DayOfWeek day, int capacity) {
        super(id, startDate, startTime, endDate, endTime, location, day);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String getType() {
        return "Lab";
    }
}
