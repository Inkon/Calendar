package com.calendar.client.ui;

import com.calendar.client.*;
import com.calendar.client.event.*;
import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.InviteDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.fullcalendar.client.ui.*;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;

import java.util.*;

import static com.calendar.client.ui.MainPage.failAuth;
import static com.calendar.client.ui.UIUtils.DAY;

public class Calendar extends Composite {
    private HashMap<Integer, EventDTO> events = new HashMap<>();
    private HashMap<Integer, InviteDTO> invites = new HashMap<>();
    private HashMap<Integer, Integer> periodicRange = new HashMap<>();
    private ArrayList<FilterDTO> filters = new ArrayList<>();

    static EventsServiceAsync eventsService = GWT.create(EventsService.class);
    private InviteServiceAsync inviteService = GWT.create(InviteService.class);
    private boolean finished[] = new boolean[FINISHED];
    private Icon spinner;
    private int counter = 0;
    private static int FINISHED = 3, FILTER = 0;
    private FilterDTO initFilter = null;

    @UiTemplate("Calendar.ui.xml")
    interface CalendarUiBinder extends UiBinder<HTMLPanel, Calendar> {
    }

    @UiField
    HTMLPanel calendarContainer;

    @UiField
    Button quickCreateButton;

    @UiField
    TextBox quickCreateTextBox;

    @UiField
    InputGroup quickCreate;

    private static CalendarUiBinder ourUiBinder = GWT.create(CalendarUiBinder.class);
    private CalendarConfig config = new CalendarConfig();
    private FullCalendar fullCalendar;
    private Button button;

    @UiHandler("quickCreateButton")
    public void onQuickCreateButtonClick(final ClickEvent event) {
        if (!quickCreateTextBox.getText().isEmpty()) {
            eventsService.parseQuickEventAdding(quickCreateTextBox.getText(), new AsyncCallback<EventDTO>() {
                @Override
                public void onFailure(Throwable throwable) {
                    // TODO
                }

                @Override
                public void onSuccess(EventDTO eventDTO) {
                    EventBus.getInstance().fireEvent(new AddEventEvent(filters, eventDTO.getBeginDate(), eventDTO.getEndDate(), eventDTO.getName()));
                }
            });
        }
    }

    public Calendar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        load();

        EventBus.getInstance().addHandler(FilterListLoadedEvent.TYPE, clickOnEvent -> {
            filters.clear();
            filters.addAll(clickOnEvent.getList());
            if (counter != FINISHED) {
                checkAndInit(FILTER);
            } else {
                safeDeleteAllEvents();
                addEvents(null);
            }
        });
        EventBus.getInstance().addHandler(FilterListChangedEvent.TYPE, clickOnEvent -> {
            safeDeleteAllEvents();
            addEvents(null);
        });
        EventBus.getInstance().addHandler(CalendarDeleteEvent.TYPE, clickOnEvent ->
                deleteEvent(clickOnEvent.getEvent().getId()));
        EventBus.getInstance().addHandler(CalendarAddEvent.TYPE, clickOnEvent -> {
            if (counter == FINISHED) {
                addEvent(clickOnEvent.getEvent(), clickOnEvent.getId());
            } else {
                events.put(clickOnEvent.getId(), clickOnEvent.getEvent());
            }
            if (clickOnEvent.getInviteDTO() != null) {
                invites.put(clickOnEvent.getId(), clickOnEvent.getInviteDTO());
            }
        });
        EventBus.getInstance().addHandler(CalendarUpdateEvent.TYPE, clickOnEvent ->
                updateEvent(clickOnEvent.getEvent()));

        EventBus.getInstance().addHandler(CalendarUpdateInvite.TYPE, event -> {
            invites.put(event.getInvite().getEvent().getId(), event.getInvite());
            updateEvent(event.getInvite().getEvent());
        });

        EventBus.getInstance().addHandler(SelectFilterInListEvent.TYPE, event -> {
            if (counter == FINISHED) {
                safeDeleteAllEvents();
                addEvents(event.getSelectedFilter());
            } else {
                initFilter = event.getSelectedFilter();
            }
        });
    }

    private void load() {
        fullCalendar = new FullCalendar("fullCalendar", ViewOption.agendaWeek, config, true);
        spinner = UIUtils.createSpinner(IconSize.TIMES3);
        calendarContainer.add(spinner);

        eventsService.getEvents(new AsyncCallback<Set<EventDTO>>() {
            @Override
            public void onFailure(Throwable throwable) {
                failAuth();
            }

            @Override
            public void onSuccess(Set<EventDTO> set) {
                for (EventDTO eventDTO : set) {
                    events.put(eventDTO.getId(), eventDTO);
                }
                checkAndInit(1);
            }
        });
        inviteService.getInvites(new AsyncCallback<List<InviteDTO>>() {
            @Override
            public void onFailure(Throwable throwable) {
                failAuth();
            }

            @Override
            public void onSuccess(List<InviteDTO> list) {
                for (InviteDTO inviteDTO : list) {
                    events.put(inviteDTO.getEvent().getId(), inviteDTO.getEvent());
                    invites.put(inviteDTO.getEvent().getId(), inviteDTO);
                }
                checkAndInit(2);
            }
        });
    }

    private void reload() {
        calendarContainer.remove(fullCalendar);
        calendarContainer.remove(button);
        quickCreate.setVisible(false);
        events.clear();
        invites.clear();
        periodicRange.clear();
        filters.clear();
        fullCalendar.destroy();
        counter = 0;
        for (int i = 0; i < FINISHED; i++) {
            finished[i] = false;
        }
        load();
    }

    private void checkAndInit(int i) {
        synchronized (this) {
            if (!finished[i]) {
                counter++;
                finished[i] = true;
            }
        }
        if (counter == FINISHED) {
            initCalendar(initFilter);
            calendarContainer.remove(spinner);
            calendarContainer.add(button);
            calendarContainer.add(fullCalendar);
            quickCreate.setVisible(true);
        }
    }

    private void initCalendar(FilterDTO filterDTO) {
        config.setLangauge(Language.Russian);

        ClickAndHoverConfig clickHover = new ClickAndHoverConfig(new ClickAndHoverEventCallback() {
            @Override
            public void eventMouseover(JavaScriptObject calendarEvent, NativeEvent event, JavaScriptObject viewObject) {
            }

            @Override
            public void eventMouseout(JavaScriptObject calendarEvent, NativeEvent event, JavaScriptObject viewObject) {
            }

            @Override
            public void eventClick(JavaScriptObject calendarEvent, NativeEvent nativeEvent, JavaScriptObject viewObject) {
                EventDTO eventDto = getEvent(new Event(calendarEvent));
                if (invites.containsKey(eventDto.getId())) {
                    EventBus.getInstance().fireEvent(new ClickOnInvite(invites.get(eventDto.getId()), filters));
                } else {
                    EventBus.getInstance().fireEvent(new ClickOnEventInCalendar(eventDto, filters));
                }
            }

            @Override
            public void dayClick(JavaScriptObject moment, NativeEvent event, JavaScriptObject viewObject) {
            }
        });
        config.setClickHoverConfig(clickHover);

        SelectConfig selectConfig = new SelectConfig(new SelectEventCallback() {
            @Override
            public void select(JavaScriptObject start, JavaScriptObject end, NativeEvent nativeEvent, JavaScriptObject javaScriptObject2) {
                Event tempEvent = new Event("" + System.currentTimeMillis(), "New event");
                tempEvent.setStart(start);
                tempEvent.setEnd(end);

                Date beginDate = UIUtils.toDataBaseTimeZone(new Date((long) tempEvent.getStart().getTime()));
                Date endDate = UIUtils.toDataBaseTimeZone(new Date((long) tempEvent.getEnd().getTime()));
                EventBus.getInstance().fireEvent(new AddEventEvent(filters, beginDate, endDate, null));
                fullCalendar.unselect();
            }

            @Override
            public void unselect(JavaScriptObject javaScriptObject, NativeEvent nativeEvent) {
            }
        });
        config.setSelectConfig(selectConfig);
        config.setSelectable(true);
        config.setSelectHelper(true);

        DragAndResizeConfig dragAndResizeConfig = new DragAndResizeConfig(new DragAndResizeCallback() {
            private void changeEvent(JavaScriptObject calendarEvent) {
                // calendarEvent contains time in format: (time in UTC + timezone) + timezone
                // e.g. in calendar we set time 07:00 and local timezone is +3.
                // calendarEvent contains 10:00 (04:00 + 3) + 3
                Event event = new Event(calendarEvent);
                // -timezone: now we have time in UTC + timezone
                Date newBeginDate = UIUtils.toDataBaseTimeZone(new Date((long) event.getStart().getTime()));
                Date newEndDate = UIUtils.toDataBaseTimeZone(new Date((long) event.getEnd().getTime()));

                EventDTO eventDto = getEvent(event);
                // -timezone: now we have time in UTC
                eventDto.setBeginDate(UIUtils.toDataBaseTimeZone(newBeginDate));
                eventDto.setEndDate(UIUtils.toDataBaseTimeZone(newEndDate));

                if (!invites.containsKey(eventDto.getId())) {
                    eventsService.updateEvent(eventDto, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            UIUtils.pushNotify("Ошибка при сохранении события.", NotifyType.DANGER);
                            updateEvent(getEvent(event));
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            UIUtils.pushNotify("Событие обновлено.", NotifyType.INFO);
                            updateEvent(eventDto);
                        }
                    });
                }
            }

            @Override
            public void eventDragStart(JavaScriptObject javaScriptObject, NativeEvent nativeEvent) {
            }

            @Override
            public void eventDragStop(JavaScriptObject javaScriptObject, NativeEvent nativeEvent) {
            }

            @Override
            public void eventDrop(JavaScriptObject calendarEvent, JavaScriptObject revertFunction, NativeEvent nativeEvent) {
                changeEvent(calendarEvent);
            }

            @Override
            public void eventResizeStart(JavaScriptObject javaScriptObject, NativeEvent nativeEvent) {
            }

            @Override
            public void eventResizeStop(JavaScriptObject javaScriptObject, NativeEvent nativeEvent) {
            }

            @Override
            public void eventResize(JavaScriptObject calendarEvent, JavaScriptObject revertFunction, NativeEvent nativeEvent) {
                changeEvent(calendarEvent);
            }
        });
        config.setDragResizeConfig(dragAndResizeConfig);

        GeneralDisplay gd = new GeneralDisplay();
        config.setGeneralDisplay(gd);
        fullCalendar.addLoadHandler(event -> addEvents(filterDTO));

        button = new Button();
        button.setSize(ButtonSize.EXTRA_SMALL);
        button.setType(ButtonType.DEFAULT);
        button.setIcon(IconType.PLUS);
        button.setText("Добавить событие");
        button.addClickHandler(clickEvent -> EventBus.getInstance().fireEvent(new AddEventEvent(filters)));
    }

    private void addEvents(FilterDTO filterDTO) {
        for (int eventId : events.keySet()) {
            EventDTO eventDTO = events.get(eventId);
            if (filterDTO == null || eventDTO.getAllFilters().contains(filterDTO)) {
                addEvent(eventDTO, eventId);
            }
        }
    }

    private EventDTO getEvent(Event event) {
        int start = event.getId().indexOf("period");
        int id = Integer.parseInt(start == -1 ? event.getId() : event.getId().substring(0, start));
        return events.get(id);
    }

    private void deleteEvent(int id) {
        fullCalendar.removeEvent(Integer.toString(id));
        if (events.get(id).getIsPeriodic() == 1) {
            for (int i = 1; i <= periodicRange.get(id); i++) {
                fullCalendar.removeEvent(Integer.toString(id) + "period" + i);
            }
        }
        events.remove(id);
    }

    private void updateEvent(EventDTO eventDTO) {
        int id = eventDTO.getId();
        deleteEvent(id);
        addEvent(eventDTO, id);
    }

    private void safeDeleteAllEvents() {
        for (int id : events.keySet()) {
            fullCalendar.removeEvent(Integer.toString(id));
            if (events.get(id).getIsPeriodic() == 1) {
                for (int i = 1; i <= periodicRange.get(id); i++) {
                    fullCalendar.removeEvent(Integer.toString(id) + "period" + i);
                }
            }
        }
    }

    private void addEvent(EventDTO eventDTO, int eventId) {
        if (!events.containsKey(eventId)) {
            events.put(eventId, eventDTO);
        }
        Event event = new Event(Integer.toString(eventId), eventDTO.getName());
        Date beginDate = UIUtils.toUserTimeZone(eventDTO.getBeginDate());
        Date endDate = UIUtils.toUserTimeZone(eventDTO.getEndDate());
        event.setStart(beginDate);
        event.setEnd(endDate);

        // disable dragging for invited events
        if (invites.containsKey(eventDTO.getId())) {
            event.setConstraint("ID_FOR_FAKE_EVENT");
        }

        String color = "";
        int total = 0;
        FilterDTO filter = null;
        Set<FilterDTO> set = eventDTO.getAllFilters();
        for (FilterDTO filterDTO : filters) {
            if (set.contains(filterDTO)) {
                total++;
                filter = filterDTO;
            }
            if (total > 1) {
                break;
            }
        }
        if (total == 1) {
            color = filter.getColor();
        }
        if (endDate.getTime() - beginDate.getTime() > DAY) {
            event.setAllDay(true);
        }
        if (eventDTO.getIsPeriodic() == 1) {
            String name = eventDTO.getName();
            String initId = Integer.toString(eventId) + "period";
            for (int id = 1; ; id++) {
                Event periodic = new Event(initId + id, name);
                Date start = UIUtils.getPeriodicDate(beginDate, id, eventDTO.getFrequency(), eventDTO.getPeriod());
                Date end = new Date(endDate.getTime() - beginDate.getTime() + start.getTime());
                if (end.after(UIUtils.toUserTimeZone(eventDTO.getLastDate()))) {
                    break;
                }
                periodic.setStart(start);
                periodic.setEnd(end);
                if (end.getTime() - start.getTime() > DAY) {
                    periodic.setAllDay(true);
                }
                if (!color.equals("")) {
                    setColor(periodic, color);
                }
                fullCalendar.addEvent(periodic);
                periodicRange.put(eventId, id);
            }
        }
        if (!color.equals("")) {
            setColor(event, color);
        }
        fullCalendar.addEvent(event);
    }

    private void setColor(Event event, String color) {
        event.setColor(color);
        event.setTextColor(UIUtils.textColor(color));
    }
}