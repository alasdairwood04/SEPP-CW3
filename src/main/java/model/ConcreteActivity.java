package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A concrete implementation of Activity used for general course activities.
 */
public class ConcreteActivity extends Activity {

    public ConcreteActivity(int id, LocalDate startDate, LocalTime startTime,
                            LocalDate endDate, LocalTime endTime,
                            String location, DayOfWeek day) {
        super(id, startDate, startTime, endDate, endTime, location, day);
    }

    @Override
    public String getType() {
        return "GeneralActivity";
    }
}
