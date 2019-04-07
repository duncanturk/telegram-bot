package de.twoyang.telegram.bot.tb.functions.reminder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author chrisotpher
 * @since 3/9/17
 */
class Helper {

    static private DateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    static private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    static private DateFormat timeFormat = new SimpleDateFormat("H:mm:ss");


    private static Calendar getDate(String parts, Calendar currCal) {
        if (parts.startsWith(".")) {
            if (!parts.substring(1, parts.length()).contains("."))
                parts += ".";
            if (parts.endsWith(".")) {
                int month = Integer.parseInt(parts.substring(1, parts.length() - 1));
                int year = currCal.get(Calendar.YEAR);
                if (month < currCal.get(Calendar.MONTH) + 1)
                    year++;
                parts += year;
            }
            parts = currCal.get(Calendar.DAY_OF_MONTH) + parts;
        } else {
            if (!parts.endsWith("."))
                parts += ".";
            if (parts.substring(0, parts.length() - 1).contains(".")) {
                int day = Integer.parseInt(parts.substring(0, parts.indexOf(".")));
                int month = Integer.parseInt(parts.substring(parts.indexOf(".") + 1, parts.length() - 1));
                int year = currCal.get(Calendar.YEAR);
                if (month < currCal.get(Calendar.MONTH) + 1 || day <= currCal.get(Calendar.DAY_OF_MONTH) && month == currCal.get(Calendar.MONTH) + 1)
                    year++;
                parts += year;
            } else {
                int month = currCal.get(Calendar.MONTH) + 1;
                int day = Integer.parseInt(parts.substring(0, parts.indexOf(".")));
                if (day <= currCal.get(Calendar.DAY_OF_MONTH))
                    month++;
                parts += month + "." + currCal.get(Calendar.YEAR);
            }
        }
        Calendar dueCal = Calendar.getInstance();
        try {
            dueCal.setTime(dateTimeFormat.parse(timeFormat.format(currCal.getTime()) + " " + parts));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dueCal;
    }

    static Calendar getDate(String parts) {
        return getDate(parts, Calendar.getInstance());
    }

    static Calendar getTime(String parts, Calendar currCal, boolean adjustTime) {
        if (parts.startsWith(":")) {
            if (!parts.substring(1).contains(":")) {
                parts += ":";
            }
            if (parts.endsWith(":")) {
                parts += "00";
            }
            int secs = Integer.valueOf(parts.substring(parts.indexOf(":", 1) + 1, parts.length()));
            int mins = Integer.valueOf(parts.substring(1, parts.indexOf(":", 1)));
            int hours = currCal.get(Calendar.HOUR_OF_DAY);
            if (mins <= currCal.get(Calendar.MINUTE) && adjustTime)
                hours++;
            parts = hours + ":" + mins + ":" + secs;
        } else if (parts.endsWith(":")) {
            parts += "00";
            if (parts.length() < 6)
                parts += ":00";
        } else if (parts.contains(":") && parts.length() < 6) {
            parts += ":00";
        }
        Calendar dueCal = Calendar.getInstance();
        try {
            dueCal.setTime(dateTimeFormat.parse(parts + " " + dateFormat.format(currCal.getTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dueCal.before(currCal) && adjustTime) {
            dueCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dueCal;
    }

    static Calendar getTime(String parts) {
        return getTime(parts, Calendar.getInstance(), true);
    }
}
