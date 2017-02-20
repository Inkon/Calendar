package com.calendar.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

import java.util.ArrayList;

public class EventBus extends com.google.gwt.event.shared.EventBus {
    private static EventBus instance = new EventBus();
    private SimpleEventBus wrappedEventBus;
    private static ArrayList<HandlerRegistration> registrations = new ArrayList<>();

    private EventBus() {
        wrappedEventBus = new SimpleEventBus();
    }

    public static EventBus getInstance() {
        return instance;
    }

    @Override
    public <H extends EventHandler> HandlerRegistration addHandler(GwtEvent.Type<H> type, H h) {
        HandlerRegistration registration = wrappedEventBus.addHandler(type, h);
        registrations.add(registration);
        return registration;
    }

    @Override
    public <H extends EventHandler> HandlerRegistration addHandlerToSource(GwtEvent.Type<H> type, Object o, H h) {
        HandlerRegistration registration = wrappedEventBus.addHandlerToSource(type, o, h);
        registrations.add(registration);
        return registration;
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {
        wrappedEventBus.fireEvent(gwtEvent);
    }

    @Override
    public void fireEventFromSource(GwtEvent<?> gwtEvent, Object o) {
        wrappedEventBus.fireEventFromSource(gwtEvent, o);
    }

    public static void clear(){
        for (HandlerRegistration registration : registrations){
            registration.removeHandler();
        }
    }
}
