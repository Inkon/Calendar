package com.calendar.server;

import com.calendar.client.InviteService;
import com.calendar.client.ui.UIUtils;
import com.calendar.server.repository.EventRepository;
import com.calendar.server.repository.InviteRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.EventDTO;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.dto.UserDTO;
import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.Invite;
import com.calendar.shared.entity.User;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("inviteService")
public class InviteServiceImpl extends SessionService implements InviteService {
    public static final Logger LOG = Logger.getLogger(LoginServiceImpl.class);

    @Autowired
    AppConfig appConfig;

    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    @Override
    public List<InviteDTO> getInvites() {
        int userId = (int) getAttributeFromSession(USER);
        ArrayList<InviteDTO> result = new ArrayList<>();
        List<Invite> invites = inviteRepository.findByUserId(userId);
        for (Invite invite : invites) {
            InviteDTO inviteDTO = DozerBeanMapperSingletonWrapper.getInstance().map(invite, InviteDTO.class);
            HashSet<FilterDTO> filtersDTO = new HashSet<>();
            Set<Filter> allFilters = invite.getEvent().getAllFilters();
            for (Filter filter : allFilters) {
                filtersDTO.add(DozerBeanMapperSingletonWrapper.getInstance().map(filter, FilterDTO.class));
            }
            inviteDTO.getEvent().setAllFilters(filtersDTO);
            inviteDTO.getEvent().setOwner(DozerBeanMapperSingletonWrapper.getInstance().map(invite.getEvent().getOwner(), UserDTO.class));
            if (inviteDTO.getEvent().getOwner().getId() != userId) {
                result.add(inviteDTO);
            }
        }
        return result;
    }

    @Override
    public void deleteInvite(InviteDTO inviteDTO) {
        Invite invite = DozerBeanMapperSingletonWrapper.getInstance().map(inviteDTO, Invite.class);
        inviteRepository.delete(invite);
    }

    @Override
    public void updateInvite(InviteDTO inviteDTO) {
        Event event = DozerBeanMapperSingletonWrapper.getInstance().map(inviteDTO.getEvent(), Event.class);
        for (FilterDTO filterDTO : inviteDTO.getEvent().getAllFilters()) {
            Filter filter = DozerBeanMapperSingletonWrapper.getInstance().map(filterDTO, Filter.class);
            event.addFilter(filter);
        }
        eventRepository.save(event);
        Invite invite = DozerBeanMapperSingletonWrapper.getInstance().map(inviteDTO, Invite.class);
        inviteRepository.save(invite);
    }

    @Override
    public InviteDTO activateInvite(String token) {
        Invite invite = inviteRepository.findByInviteToken(token);
        if (invite == null) {
            return null;
        }

        if (!invite.getStatus().equals(Invite.InviteStatus.WAIT)) {
            return null;
        }

        User currentUser = userRepository.findOne((int) getAttributeFromSession(SessionService.USER));
        if (currentUser.getId().equals(invite.getEvent().getOwner().getId())) {
            return null;
        }

        inviteRepository.acceptInvite(invite, currentUser);

        InviteDTO inviteDTO = DozerBeanMapperSingletonWrapper.getInstance().map(invite, InviteDTO.class);
        HashSet<FilterDTO> filtersDTO = new HashSet<>();
        Set<Filter> allFilters = invite.getEvent().getAllFilters();
        for (Filter filter : allFilters) {
            filtersDTO.add(DozerBeanMapperSingletonWrapper.getInstance().map(filter, FilterDTO.class));
        }
        inviteDTO.getEvent().setAllFilters(filtersDTO);
        inviteDTO.getEvent().setOwner(DozerBeanMapperSingletonWrapper.getInstance().map(invite.getEvent().getOwner(), UserDTO.class));

        return inviteDTO;
    }

    @Override
    public boolean sendInvite(EventDTO event, String email) {
        Event targetEvent = DozerBeanMapperSingletonWrapper.getInstance().map(event, Event.class);
        User currentUser = userRepository.findOne((int) getAttributeFromSession(SessionService.USER));

        Invite invite = new Invite();
        invite.setEvent(targetEvent);
        invite.setInviteEmail(email);
        inviteRepository.createInvite(invite);

        String finalInviteLink = String.format(appConfig.getInviteMessageLink(), invite.getInviteToken());
        return Mail.send(email, String.format(appConfig.getInviteMessageSubject(), currentUser.getFirstName()),
                String.format(appConfig.getInviteMessageText(), targetEvent.getName(), finalInviteLink));
    }

    @Override
    public List<InviteDTO> getAllInvitesForEvent(EventDTO event) {
        Event targetEvent = eventRepository.findOne(event.getId());
        return getInvites(targetEvent);
    }

    private List<InviteDTO> getInvites(Event targetEvent) {
        return targetEvent.getInvites().stream().map(invite -> DozerBeanMapperSingletonWrapper.getInstance().map
                (invite, InviteDTO.class)).collect(Collectors.toList());
    }

    @Override
    public void rejectInvite(InviteDTO invite) {
        Invite targetInvite = DozerBeanMapperSingletonWrapper.getInstance().map(invite, Invite.class);
        inviteRepository.rejectInvite(targetInvite);
    }

    @Override
    public void removeInvite(InviteDTO invite) {
        Invite targetInvite = DozerBeanMapperSingletonWrapper.getInstance().map(invite, Invite.class);
        inviteRepository.delete(targetInvite);
    }

    @Override
    public TreeMap<Date, List<InviteDTO>> pickUpDate(int id, Date happenAfter, Date happenBefore, long duration) {
        System.out.println("Init values: " + happenAfter + " " + happenBefore);
        Date endDate = new Date(happenBefore.getTime() + duration);
        int userId = (int) getAttributeFromSession(USER);
        HashSet<Event> events = new HashSet<>();
        HashSet<Invite> invites = new HashSet<>();
        HashMap<Integer, Event> allEvents = new HashMap<>();
        HashMap<Integer, Invite> allInvites = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> state = new HashMap<>();

        Event current = eventRepository.findOne(id);
        ArrayList<Integer> participators = new ArrayList<>();
        participators.add(userId);
        for (Invite invite : current.getInvites()) {
            if (invite.getUser() != null) {
                participators.add(invite.getUser().getId());
            }
        }
        for (int participatorId : participators) {
            List<Event> participatorEvents = eventRepository.findByOwnerId(participatorId);
            for (Event event : participatorEvents) {
                if (event.getIsPeriodic() == 0) {
                    if (!event.getEndDate().before(happenAfter) && !event.getBeginDate().after(endDate)) {
                        events.add(event);
                        allEvents.put(event.getId(), event);
                    }
                } else {
                    for (int i = 0; ; i++) {
                        Date begin = UIUtils.getPeriodicDate(event.getBeginDate(), i, event.getFrequency(), event.getPeriod());
                        Date end = new Date((event.getEndDate().getTime() - event.getBeginDate().getTime()) + begin.getTime());
                        System.out.println("periodic value" + " " + i + " " + begin + " " + end);
                        if (end.after(event.getLastDate())) {
                            break;
                        }
                        if (!end.before(happenAfter) && !begin.after(endDate)) {
                            events.add(event);
                            allEvents.put(event.getId(), event);
                            break;
                        }
                    }
                }
            }

            List<Invite> participatorInvites = inviteRepository.findByUserId(participatorId);
            for (Invite invite : participatorInvites) {
                if (invite.getEvent().getId() != id && !invite.getEvent().getEndDate().before(happenAfter) &&
                        !invite.getEvent().getBeginDate().after(endDate)) {
                    invites.add(invite);
                    allInvites.put(invite.getId(), invite);
                }
            }
        }
        events.remove(current);
        current.setBeginDate(happenAfter);
        current.setEndDate(new Date(happenAfter.getTime() + duration));
        events.add(current);

        Date ans = happenAfter;
        int min = Integer.MAX_VALUE;
        //list of pairs: first is event/invite id (determined by positive or negative number) and second is begin/end date
        //each event and invite have two pairs in list, for begin and end date
        ArrayList<Pair<Integer, Date>> list = new ArrayList<>();
        HashSet<Integer> curEvents = new HashSet<>();
        HashSet<Integer> curInvites = new HashSet<>();
        HashSet<Integer> curUsers = new HashSet<>();
        if (list.isEmpty()) {
            for (Event event : events) {
                if (event.getIsPeriodic() == 1) {
                    for (int i = 0; ; i++) {
                        Date begin = UIUtils.getPeriodicDate(event.getBeginDate(), i, event.getFrequency(), event.getPeriod());
                        System.out.println("periodic value" + " " + i + " " + begin);
                        Date end = new Date(begin.getTime() + (event.getEndDate().getTime() - event.getBeginDate().getTime()));
                        if (end.after(event.getLastDate())) {
                            break;
                        }
                        if (!end.before(happenAfter) && !begin.after(endDate)) {
                            list.add(new Pair<>(event.getId(), begin));
                            list.add(new Pair<>(event.getId(), end));
                        }
                    }
                } else {
                    list.add(new Pair<>(event.getId(), event.getBeginDate()));
                    list.add(new Pair<>(event.getId(), event.getEndDate()));
                }
            }
            for (Invite invite : invites) {
                list.add(new Pair<>(-invite.getId() - 1, invite.getEvent().getBeginDate()));
                list.add(new Pair<>(-invite.getId() - 1, invite.getEvent().getEndDate()));
            }
            list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        }
        System.out.println("List values: " + list);
        System.out.println("Event values: " + allEvents.keySet());

        //scan line, that detects states of all events and invites in each point in list
        for (int i = 0; i < list.size(); i++) {
            Pair<Integer, Date> pair = list.get(i);
            if (pair.getKey() == id) {
                state.putIfAbsent(i, new HashSet<>(curUsers));
                continue;
            }
            boolean begin = true;
            int userFindId = -1;
            if (pair.getKey() >= 0) {
                //Event
                Event scannedEvent = allEvents.get(pair.getKey());
                if (scannedEvent.getBeginDate().equals(pair.getValue()) || scannedEvent.getIsPeriodic() == 1 &&
                        UIUtils.isPeriodicBeginDate(scannedEvent.getBeginDate(), pair.getValue(), scannedEvent.getFrequency(),
                                scannedEvent.getPeriod())) {
                    curEvents.add(scannedEvent.getId());
                    curUsers.add(scannedEvent.getOwner().getId());
                } else {
                    begin = false;
                    curEvents.remove(scannedEvent.getId());
                    userFindId = scannedEvent.getOwner().getId();
                }
            } else {
                //Invite
                int inviteId = -pair.getKey() - 1;
                Invite scannedInvite = allInvites.get(inviteId);
                if (scannedInvite.getEvent().getBeginDate().equals(pair.getValue())) {
                    curInvites.add(inviteId);
                    curUsers.add(scannedInvite.getUser().getId());
                } else {
                    begin = false;
                    curInvites.remove(inviteId);
                    userFindId = scannedInvite.getUser().getId();
                }
            }
            if (!begin) {
                boolean removable = true;
                for (int iterId : curEvents) {
                    if (allEvents.get(iterId).getOwner().getId() == userFindId) {
                        removable = false;
                        break;
                    }
                }
                if (removable) {
                    for (int iterId : curInvites) {
                        if (allInvites.get(iterId).getUser().getId() == userFindId) {
                            removable = false;
                            break;
                        }
                    }
                    if (removable) {
                        curUsers.remove(userFindId);
                    }
                }
            }
            state.putIfAbsent(i, new HashSet<>(curUsers));
        }
        System.out.println("Counter values: ");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("counter" + i + " " + state.get(i).size());
        }
        int real = 0;
        HashSet<Integer> ansUsers = new HashSet<>();

        //find time interval with fewest number of users who can't attend (least size of hash set in points that belong to interval)
        for (int i = 0; i < list.size(); i++) {
            Pair<Integer, Date> pair = list.get(i);
            if (pair.getValue().after(happenBefore)) {
                break;
            }
            if (pair.getValue().before(happenAfter)) {
                ansUsers = state.get(i);
                continue;
            }
            real++;
            HashSet<Integer> intervalIds = new HashSet<>(state.get(i));
            for (int j = i + 1; j < list.size(); j++) {
                Pair<Integer, Date> intermediate = list.get(j);
                if (pair.getValue().getTime() + duration > intermediate.getValue().getTime()) {
                    intervalIds.addAll(state.get(j));
                } else {
                    break;
                }
            }
            System.out.println(i + " " + intervalIds.size() + " " + min);
            if (intervalIds.size() < min) {
                min = intervalIds.size();
                //first possible date, so can be shifted to the start date
                if (real != 1) {
                    ans = pair.getValue();
                }
                ansUsers = intervalIds;
            }
        }

        System.out.println(String.format("Picked up date %s with user's id that can't participate: %s", ans.toString(), ansUsers.toString()));

        List<InviteDTO> eventInvites = getInvites(eventRepository.findOne(current.getId()));
        InviteDTO ownerInvite = new InviteDTO();
        UserDTO idUser = new UserDTO();
        idUser.setId(userId);
        ownerInvite.setUser(idUser);
        ownerInvite.setStatus(Invite.InviteStatus.ACCEPT);
        ownerInvite.setInviteEmail(userRepository.findOne(userId).getEmail());
        eventInvites.add(ownerInvite);
        ArrayList<InviteDTO> ansInvites = new ArrayList<>();
        if (!ansUsers.isEmpty()) {
            for (InviteDTO invite : eventInvites) {
                if (ansUsers.contains(invite.getUser().getId())) {
                    ansInvites.add(invite);
                }
            }
        }
        TreeMap<Date, List<InviteDTO>> result = new TreeMap<>();
        result.put(ans, ansInvites);

        return result;
    }
}
