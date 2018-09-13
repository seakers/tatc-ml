/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.evaluation.reductionmetrics;

import org.orekit.errors.OrekitException;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeScalesFactory;

/**
 *
 * @author Prachi
 */

public class AbsoluteDate extends org.orekit.time.AbsoluteDate {

    private static final long serialVersionUID = -4643124175775554737L;
    

    /**
     *
     * @param year Gregorian calendar 0000-9999 year.
     * @param month Gregorian calendar 01-12 month.
     * @param day Gregorian calendar 01-31 day. Supports leap-days.
     * @param hour UTC 00-23 hour.
     * @param minute UTC 00-59 hour.
     * @param second UTC 00-60 second. Supports leap-seconds.
     * @throws OrekitException
     */
    public AbsoluteDate(int year, int month, int day, int hour, int minute, double second) throws OrekitException {
        super(year, month, day, hour, minute, second, TimeScalesFactory.getUTC());
    }

    /**
     * cast Orekit date to TATC date.
     *
     * @param date
     */
    public static AbsoluteDate cast(org.orekit.time.AbsoluteDate date) throws OrekitException {
        DateTimeComponents dtc = date.getComponents(TimeScalesFactory.getUTC());
        int year = dtc.getDate().getYear();
        int month = dtc.getDate().getMonth();
        int day = dtc.getDate().getDay();
        int hour = dtc.getTime().getHour();
        int minute = dtc.getTime().getMinute();
        double second = dtc.getTime().getSecond();
        return new AbsoluteDate(year, month, day, hour, minute, second);
    }

    /**
     * Return result of advancing time by seconds. Side effect: New stored time
     * is advanced.
     *
     * @param seconds the number of seconds to advance the date
     * @return new date that is advanced by a specified number of seconds beyond
     * this date.
     */
    public AbsoluteDate advance(double seconds) throws OrekitException {
        return AbsoluteDate.cast(super.shiftedBy(seconds));
    }

    /**
     * Return seconds elapsed from given to this. Notes: from expected non-Null.
     * Elapsed seconds are negative when this occurs before from, zero when this
     * == from, and positive otherwise.
     *
     * @param from
     * @return
     */
    public double getElapsed(AbsoluteDate from) {
        return super.durationFrom(from);
    }
    
    public int getYear() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getDate().getYear();
    }
    
    public int getMonth() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getDate().getMonth();
    }
    
    public int getDay() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getDate().getDay();
    }
    
    public int getHour() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getTime().getHour();
    }
    
    public int getMinute() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getTime().getMinute();
    }
    
    public double getSecond() throws OrekitException{
        return this.getComponents(TimeScalesFactory.getUTC()).getTime().getSecond();
    }
}
