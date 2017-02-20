package com.calendar.server.nlp;

import com.calendar.shared.entity.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class DateFact extends AbstractFact {

    public DateFact(HashMap<String, String> data) {
        super(data);
    }

    // Field in tomita's response
    private static class Field {
        private static String BeginDayOfWeek = "BeginDayOfWeek";
        private static String BeginDay = "BeginDay";
        private static String BeginMonth = "BeginMonth";
        private static String BeginYear = "BeginYear";
        private static String BeginRawDate = "BeginRawDate";

        private static String EndDayOfWeek = "EndDayOfWeek";
        private static String EndDay = "EndDay";
        private static String EndMonth = "EndMonth";
        private static String EndYear = "EndYear";
        private static String EndRawDate = "EndRawDate";

        private static String BeginTime = "BeginTime";
        private static String EndTime = "EndTime";

        private static String Name = "Name";
    }

    // Day of week -> Number
    private HashMap<String, Integer> daysOfWeek = new HashMap<>();

    {
        daysOfWeek.put("понедельник", 1);
        daysOfWeek.put("вторник", 2);
        daysOfWeek.put("среда", 3);
        daysOfWeek.put("четверг", 4);
        daysOfWeek.put("пятница", 5);
        daysOfWeek.put("суббота", 6);
        daysOfWeek.put("воскресень", 7);
    }

    // Day -> Offset from today
    private HashMap<String, Integer> relativeDayOfWeek = new HashMap<>();

    {
        relativeDayOfWeek.put("сегодня", 0);
        relativeDayOfWeek.put("завтра", 1);
        relativeDayOfWeek.put("послезавтра", 2);
    }

    // Determiner -> 0, if "this"; 1 if "next"
    private HashMap<String, Integer> dayDeterminer = new HashMap<>();

    {
        dayDeterminer.put("следующий", 1);
        dayDeterminer.put("следующая", 1);
        dayDeterminer.put("следующее", 1);
        dayDeterminer.put("эта", 0);
        dayDeterminer.put("этот", 0);
        dayDeterminer.put("это", 0);
    }

    // Month -> Number
    private HashMap<String, Integer> months = new HashMap<>();

    {
        months.put("январь", 1);
        months.put("февраль", 2);
        months.put("март", 3);
        months.put("апрель", 4);
        months.put("май", 5);
        months.put("июнь", 6);
        months.put("июль", 7);
        months.put("август", 8);
        months.put("сентябрь", 9);
        months.put("октябрь", 10);
        months.put("ноябрь", 11);
        months.put("декабрь", 12);
    }

    @Override
    public Event analyze() {
        Event event = new Event();

        // DateFact should contain Name
        String name = getRawData().get(Field.Name);
        if (name == null) {
            return null;
        } else {
            // Capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            event.setName(name);
        }

        // Begin date
        // If have raw date, use it and ignore other date fields
        // Date in format "day.month.year"
        String rawBeginDate = getRawData().get(Field.BeginRawDate);
        if (rawBeginDate == null) {
            // If have day and month, use it; otherwise try to get date from BeginDayOfWeek
            if (getRawData().get(Field.BeginDay) != null && getRawData().get(Field.BeginMonth) != null) {
                rawBeginDate = getRawData().get(Field.BeginDay) + "." + months.getOrDefault(getRawData().get(Field.BeginMonth), 0);
                rawBeginDate += "." + (getRawData().get(Field.BeginYear) == null ?
                        LocalDate.now().getYear() :
                        getRawData().get(Field.BeginYear));
            } else {
                if (getRawData().get(Field.BeginDayOfWeek) != null) {
                    rawBeginDate = buildDateByWords(getRawData().get(Field.BeginDayOfWeek));
                }
            }
        }

        // Begin time
        String beginTime = getRawData().get(Field.BeginTime);

        // End date
        String rawEndDate = getRawData().get(Field.EndRawDate);
        if (rawEndDate == null) {
            // If have day and month, use it; otherwise try to get date from EndDayOfWeek
            if (getRawData().get(Field.EndDay) != null && getRawData().get(Field.EndMonth) != null) {
                rawEndDate = getRawData().get(Field.EndDay) + "." + months.getOrDefault(getRawData().get(Field.EndMonth), 0);
                rawEndDate += "." + (getRawData().get(Field.EndYear) == null ?
                        LocalDate.now().getYear() :
                        getRawData().get(Field.EndYear));
            } else {
                if (getRawData().get(Field.EndDayOfWeek) != null) {
                    rawEndDate = buildDateByWords(getRawData().get(Field.EndDayOfWeek));
                }
            }
        }

        // End time
        String endTime = getRawData().get(Field.EndTime);

        // Normalize date
        rawBeginDate = rawBeginDate != null ? normalizeDate(rawBeginDate) : null;
        rawEndDate = rawEndDate != null ? normalizeDate(rawEndDate) : null;

        // e.g "01.12 00:00 - 01:00"
        // should be "01.12 00:00 - 01:12 01:00"
        if (rawEndDate == null && endTime != null) {
            rawEndDate = rawBeginDate;
        }

        // Make object with begin timestamp
        if (beginTime == null) {
            beginTime = "00:00";
        }

        // Normalize time
        beginTime = (beginTime.length() == 4) ? "0" + beginTime : beginTime;
        if (endTime != null) {
            endTime = (endTime.length() == 4) ? "0" + endTime : endTime;
        }

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Instant instantBegin = formatterDate.parse(rawBeginDate + " " + beginTime).toInstant();

            Instant instantEnd;
            if (rawEndDate == null) {
                instantEnd = instantBegin.plusSeconds(60 * 30);
            } else {
                instantEnd = formatterDate.parse(rawEndDate + " " + endTime).toInstant();
            }

            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            event.setBeginDate(Date.from(instantBegin));
            event.setEndDate(Date.from(instantEnd));
        } catch (ParseException ignored) {

        }

        return event;
    }

    private String buildDateByWords(String description) {
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();

        int determiner;
        int dayOfWeek;
        int daysOffset = 0;

        if (description.contains(" ")) {
            // Case: Determiner + Day
            determiner = dayDeterminer.get(description.split(" ")[0]);
            dayOfWeek = daysOfWeek.get(description.split(" ")[1]);
            if (determiner == 1) {
                // "Next full week"
                // Should skip one week
                if (currentDayOfWeek <= dayOfWeek) {
                    daysOffset = (7 - currentDayOfWeek) + dayOfWeek;
                } else {
                    daysOffset = 7 + (7 - currentDayOfWeek) + dayOfWeek;
                }
            } else if (determiner == 0) {
                // "Nearest week"
                if (currentDayOfWeek <= dayOfWeek) {
                    daysOffset = (dayOfWeek - currentDayOfWeek);
                } else {
                    daysOffset = (7 - currentDayOfWeek) + dayOfWeek;
                }
            }
        } else {
            // Case: Day
            if (relativeDayOfWeek.containsKey(description)) {
                // Today, tomorrow, ...
                daysOffset = relativeDayOfWeek.get(description);
            } else if (daysOfWeek.containsKey(description)) {
                // Monday, ...
                dayOfWeek = daysOfWeek.get(description);
                if (currentDayOfWeek <= dayOfWeek) {
                    daysOffset = (dayOfWeek - currentDayOfWeek);
                } else {
                    daysOffset = (7 - currentDayOfWeek) + dayOfWeek;
                }
            }
        }

        LocalDate localDate = LocalDate.now().plusDays(daysOffset);
        return DateTimeFormatter.ofPattern("dd.MM.YYYY").format(localDate);
    }

    private String normalizeDate(String date) {
        String[] dateElements = date.split("[.]");
        if (dateElements[0].length() == 1) {
            dateElements[0] = "0" + dateElements[0];
        }
        if (dateElements[1].length() == 1) {
            dateElements[1] = "0" + dateElements[1];
        }
        if (dateElements[2].length() == 2) {
            dateElements[2] = "20" + dateElements[2];
        }
        return String.join(".", dateElements);
    }
}
