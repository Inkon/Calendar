package com.calendar.server;

import com.calendar.client.EventsService;
import com.calendar.server.nlp.DateFact;
import com.calendar.server.nlp.TomitaParser;
import com.calendar.server.repository.EventRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.UserDTO;
import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.User;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("eventsService")
public class EventsServiceImpl extends SessionService implements EventsService {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AppConfig appConfig;

    @Override
    public Set<EventDTO> getEvents() {
        int userId = (int) getAttributeFromSession(USER);
        HashSet<EventDTO> events = new HashSet<>();
        List<Event> owned = eventRepository.findByOwnerId(userId);
        for (Event event : owned) {
            LoginServiceImpl.LOG.debug("select filter for event " + event.getId() + " " + (event.getAllFilters() == null ?
                    "null" : event.getAllFilters().size()));
            EventDTO eventDTO = DozerBeanMapperSingletonWrapper.getInstance().map(event, EventDTO.class);
            HashSet<FilterDTO> filtersDTO = new HashSet<>();
            Set<Filter> allFilters = event.getAllFilters();
            for (Filter filter : allFilters) {
                filtersDTO.add(DozerBeanMapperSingletonWrapper.getInstance().map(filter, FilterDTO.class));
            }
            eventDTO.setAllFilters(filtersDTO);
            events.add(eventDTO);
        }
        return events;
    }

    @Override
    public void deleteEvent(EventDTO eventDTO) {
        Event event = DozerBeanMapperSingletonWrapper.getInstance().map(eventDTO, Event.class);
        eventRepository.delete(event);
    }

    @Override
    public HashMap<Integer, UserDTO> addEvent(EventDTO eventDTO) {
        LoginServiceImpl.LOG.debug("Start adding event");
        Event event = DozerBeanMapperSingletonWrapper.getInstance().map(eventDTO, Event.class);
        HashSet<Filter> filters = new HashSet<>();
        for (FilterDTO filterDTO : eventDTO.getAllFilters()){
            filters.add(DozerBeanMapperSingletonWrapper.getInstance().map(filterDTO, Filter.class));
        }
        event.setAllFilters(filters);
        User user = userRepository.findOne((int) getAttributeFromSession(SessionService.USER));
        event.setOwner(user);
        Event result = eventRepository.save(event);
        UserDTO userDTO = DozerBeanMapperSingletonWrapper.getInstance().map(user, UserDTO.class);
        HashMap<Integer, UserDTO> map = new HashMap<>();
        map.put(result == null ? -1 : result.getId(),userDTO );
        return map;
    }

    @Override
    public void updateEvent(EventDTO eventDTO) {
        Event event = DozerBeanMapperSingletonWrapper.getInstance().map(eventDTO, Event.class);
        for (FilterDTO filterDTO : eventDTO.getAllFilters()) {
            Filter filter = DozerBeanMapperSingletonWrapper.getInstance().map(filterDTO, Filter.class);
            event.addFilter(filter);
        }
        eventRepository.save(event);
    }

    @Override
    public EventDTO parseQuickEventAdding(String query) {
        TomitaParser tomita = new TomitaParser(appConfig.getTomitaParserBin(), appConfig.getTomitaParserGrammar());
        System.out.println(appConfig.getTomitaParserBin());
        HashMap<String, String> rawData = tomita.parseFact(tomita.parse(query));
        Event fact = new DateFact(rawData).analyze();
        return DozerBeanMapperSingletonWrapper.getInstance().map(fact, EventDTO.class);
    }
}
