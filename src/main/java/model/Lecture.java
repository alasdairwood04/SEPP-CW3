package model;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Represents a Lecture activity.
 */
public class Lecture extends Activity {
    private boolean recorded;

    public Lecture(int id, LocalDate startDate, LocalTime startTime,
                   LocalDate endDate, LocalTime endTime,
                   String location, DayOfWeek day, boolean recorded) {
        super(id, startDate, startTime, endDate, endTime, location, day);
        this.recorded = recorded;
    }

    public boolean isRecorded() {
        return recorded;
    }

    @Override
    public String getType() {
        return "Lecture" + (recorded ? " (Recorded)" : " (Unrecorded)");
    }
}
