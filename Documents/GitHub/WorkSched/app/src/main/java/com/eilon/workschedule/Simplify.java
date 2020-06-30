package com.eilon.workschedule;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Simplify {

    public Simplify(){}

    public String getLastWeekDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM");

        while (calendar.get(Calendar.DAY_OF_WEEK) > calendar.getFirstDayOfWeek()) {
            calendar.add(Calendar.DATE, -1); // Substract 1 day until first day of week.
        }
        calendar.add(Calendar.DATE, -1);
        Date end = calendar.getTime();
        String end1 = formatter.format(end);

        for(int i = 1; i<7; i++) {
            calendar.add(Calendar.DATE, -1);
        }
        Date start = calendar.getTime();
        String start1 = formatter.format(start);

        return (start1+"-"+end1);
    }
    public String getThisWeekDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM");

        while (calendar.get(Calendar.DAY_OF_WEEK) > calendar.getFirstDayOfWeek()) {
            calendar.add(Calendar.DATE, -1);
        }
        Date start = calendar.getTime();
        String start1 = formatter.format(start);

        while (calendar.get(Calendar.DAY_OF_WEEK) < 7) {
            calendar.add(Calendar.DATE, +1);
        }
        Date end = calendar.getTime();
        String end1 = formatter.format(end);

        return (start1+"-"+end1);
    }
    public String getNextWeekDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM");
        while (calendar.get(Calendar.DAY_OF_WEEK) < 7) {
            calendar.add(Calendar.DATE, +1);
        }
        calendar.add(Calendar.DATE, +1);
        Date start = calendar.getTime();
        String start1 = formatter.format(start);
        for(int i = 1; i<7; i++) {
            calendar.add(Calendar.DATE, +1);
        }
        Date end = calendar.getTime();
        String end1 = formatter.format(end);

        return (start1+"-"+end1);
    }

    public String getShiftName(String shiftDay){
        int shift = Integer.valueOf(String.valueOf(shiftDay.charAt(0)));
        int day = Integer.valueOf(String.valueOf(shiftDay.charAt(1)));
        return getShiftName(shift,day);
    }

    public String getShiftName(int shift, int day) {
        if (shift > 2 || shift < 0 || day > 6 || day < 0) return "משמרת שגויה";
        String shiftName = "";
        switch (day) {
            case 0:
                shiftName = "ראשון";
                break;
            case 1:
                shiftName = "שני";
                break;
            case 2:
                shiftName = "שלישי";
                break;
            case 3:
                shiftName = "רביעי";
                break;
            case 4:
                shiftName = "חמישי";
                break;
            case 5:
                shiftName = "שישי";
                break;
            case 6:
                shiftName = "שבת";
                break;
        }
        switch (shift) {
            case 0:
                shiftName += " בבוקר";
                break;
            case 1:
                shiftName += " בערב";
                break;
            case 2:
                shiftName += " בלילה";
                break;
        }
        return shiftName;
    }

}
