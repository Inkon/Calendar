package com.calendar.server;

import biweekly.Biweekly;
import biweekly.component.VEvent;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.RecurrenceRule;
import biweekly.property.Summary;
import com.calendar.shared.entity.Event;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ICalendarWrapper {

    public class VEventWrapper {
        private VEvent event = new VEvent();

        public VEventWrapper(VEvent vEvent) {
            event = vEvent;
        }

        public VEvent getEvent() {
            return event;
        }

        public void setEvent(VEvent event) {
            this.event = event;
        }

        private Date dateToUTC(Date date) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
            return Date.from(zdt.toInstant());
        }

        public Event toEvent() {
            Event targetEvent = new Event();

            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            // Name
            Summary summary = event.getSummary();
            if (summary != null) {
                targetEvent.setName(summary.getValue());
            } else {
                return null;
            }

            // Description (optional)
            targetEvent.setDescription(event.getDescription() == null ? "" : event.getDescription().getValue());

            // Begin date
            DateStart dateStart = event.getDateStart();
            if (dateStart != null) {
                targetEvent.setBeginDate(dateToUTC(dateStart.getValue().getRawComponents().toDate()));
            } else {
                return null;
            }

            // End date
            DateEnd dateEnd = event.getDateEnd();
            if (dateEnd != null) {
                targetEvent.setEndDate(dateToUTC(dateEnd.getValue().getRawComponents().toDate()));
            } else {
                return null;
            }

            // Rules (period, frequency, last date)
            RecurrenceRule rrule = event.getRecurrenceRule();
            if (rrule != null) {
                String freq = rrule.getValue().getFrequency() != null ? rrule.getValue().getFrequency().toString() : "NONE";
                int interval = rrule.getValue().getInterval() != null ? rrule.getValue().getInterval() : 1;

                Event.EventFrequency eventFrequency = null;
                for (Event.EventFrequency f : Event.EventFrequency.values()) {
                    if (f.toString().equals(freq)) {
                        eventFrequency = f;
                        break;
                    }
                }
                if (eventFrequency != null) {
                    targetEvent.setIsPeriodic((byte) 1);
                    targetEvent.setPeriod(interval);
                    targetEvent.setFrequency(eventFrequency);
                }

                if (rrule.getValue().getUntil() != null) {
                    Date lastDate = rrule.getValue().getUntil().getRawComponents().toDate();
                    targetEvent.setLastDate(dateToUTC(lastDate));
                }
            }
            if (targetEvent.getIsPeriodic() == null) {
                targetEvent.setIsPeriodic((byte) 0);
                targetEvent.setPeriod(0);
                targetEvent.setFrequency(Event.EventFrequency.DAILY);
            }

            return targetEvent;
        }
    }

    private List<VEventWrapper> events = new ArrayList<>();

    public ICalendarWrapper() {

    }

    public ICalendarWrapper(String s) {
        events = Biweekly.parse(s).first().getEvents().stream().map(VEventWrapper::new).collect(Collectors.toList());
    }

    public ICalendarWrapper(InputStream is) throws IOException {
        events = Biweekly.parse(is).first().getEvents().stream().map(VEventWrapper::new).collect(Collectors.toList());
    }

    public static ICalendarWrapper createFromInputStream(InputStream is) throws IOException {
        return new ICalendarWrapper(is);
    }

    public List<VEventWrapper> getEvents() {
        return events;
    }

    public void setEvents(List<VEventWrapper> events) {
        this.events = events;
    }

    public List<Event> getWrappedEvents() {
        return events.stream().map(VEventWrapper::toEvent).collect(Collectors.toList());
    }
}
