package multikernel;

import java.util.Date;

/**
 * Custom SimpleTimePeriod class used to define task durations for Gantt charting.
 * No dependency on org.jfree.data.gantt.SimpleTimePeriod.
 */
public class SimpleTimePeriod {

    private final Date start;
    private final Date end;

    public SimpleTimePeriod(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "SimpleTimePeriod[" + start + " to " + end + "]";
    }
}
