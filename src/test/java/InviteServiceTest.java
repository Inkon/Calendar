import com.calendar.client.ui.UIUtils;
import com.calendar.server.InviteServiceImpl;
import com.calendar.server.repository.EventRepository;
import com.calendar.server.repository.InviteRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.InviteDTO;
import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.Invite;
import com.calendar.shared.entity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static com.calendar.client.ui.UIUtils.HOUR;
import static com.calendar.client.ui.UIUtils.MINUTE;
import static com.calendar.server.SessionService.USER;
import static org.junit.Assert.*;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContextTest.xml")
@WebAppConfiguration
public class InviteServiceTest {
    @Autowired
    InviteServiceImpl inviteService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    EventRepository eventRepository;

    private User owner, user;
    private Event mainEvent;
    private HashSet<Event> events;
    private HashSet<Invite> invites;
    private HashSet<User> users;
    private long duration;

    @Before
    public void addUsers() {
        owner = saveAndGetUser();
        user = saveAndGetUser();
        invites = new HashSet<>();
        events = new HashSet<>();
        users = new HashSet<>();

        inviteService.setAttributeInSession(USER, owner.getId());
    }

    @After
    public void clear() {
        for (Invite invite : invites) {
            inviteRepository.delete(invite);
        }
        eventRepository.delete(mainEvent);
        for (Event event : events) {
            eventRepository.delete(event);
        }
        for (User user : users) {
            userRepository.delete(user);
        }
        userRepository.delete(user);
        userRepository.delete(owner);
    }

    @Test
    public void noEvents() {
        singleUserInvite(UIUtils.HOUR);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), mainEvent.getEndDate());
        assertEquals(1, result.size());
        assertTrue(result.get(result.firstKey()).isEmpty());
        assertEquals(mainEvent.getBeginDate(), result.firstKey());
    }

    @Test
    public void singleEvent() {
        singleUserInvite(UIUtils.HOUR);
        Event userEvent = createEvent(user, mainEvent.getBeginDate(), new Date(mainEvent.getBeginDate().getTime() + MINUTE));
        events.add(userEvent);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), mainEvent.getEndDate());
        assertEquals(1, result.size());
        assertTrue(result.get(result.firstKey()).isEmpty());
        assertEquals(userEvent.getEndDate(), result.firstKey());
    }

    @Test
    public void twoEvents() {
        twoEvents(true);
    }

    @Test
    public void ownedByTwoUsers() {
        twoEvents(false);
    }

    @Test
    public void periodic() {
        singleUserInvite(UIUtils.HOUR);
        Event periodic = createPeriodicEvent(user, new Date(mainEvent.getBeginDate().getTime() - 30 * MINUTE),
                new Date(mainEvent.getBeginDate().getTime() - 29 * MINUTE), new Date(mainEvent.getEndDate().getTime() + HOUR),
                Event.EventFrequency.MINUTELY, 70);
        events.add(periodic);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), mainEvent.getEndDate());
        assertEquals(1, result.size());
        assertTrue(result.get(result.firstKey()).isEmpty());
        assertEquals(result.firstKey().getTime(), periodic.getEndDate().getTime() + MINUTE * 70);
    }

    @Test
    public void cantParticipate() {
        singleUserInvite(UIUtils.HOUR);
        Event firstEvent = createEvent(user, mainEvent.getBeginDate(), new Date(mainEvent.getBeginDate().getTime() + HOUR));
        events.add(firstEvent);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), new Date(mainEvent.getEndDate().getTime() - MINUTE * 30));
        assertEquals(1, result.size());
        assertEquals(1, result.get(result.firstKey()).size());
        assertEquals(user.getId(), result.get(result.firstKey()).get(0).getUser().getId());
        assertEquals(mainEvent.getBeginDate(), result.firstKey());
    }


    @Test
    public void twoCantParticipate() {
        singleUserInvite(UIUtils.HOUR);
        Event firstEvent = createEvent(user, mainEvent.getBeginDate(), new Date(mainEvent.getBeginDate().getTime() + HOUR));
        events.add(firstEvent);
        User third = saveAndGetUser();
        inviteUser(third);
        Event secondEvent = createEvent(third, new Date(mainEvent.getBeginDate().getTime() + MINUTE * 30),
                new Date(mainEvent.getBeginDate().getTime() + MINUTE * 30));
        events.add(secondEvent);
        users.add(third);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), new Date(mainEvent.getEndDate().getTime() - MINUTE * 30));
        assertEquals(2, result.get(result.firstKey()).size());

        List<InviteDTO> invites = result.get(result.firstKey());
        invites.sort((o1, o2) -> Integer.compare(o1.getUser().getId(), o2.getUser().getId()));
        assertEquals(Math.min(third.getId(), user.getId()), (long) invites.get(0).getUser().getId());
        assertEquals(Math.max(third.getId(), user.getId()), (long) invites.get(1).getUser().getId());

        assertEquals(mainEvent.getBeginDate(), result.firstKey());
    }

    private void twoEvents(boolean sameOwner) {
        singleUserInvite(UIUtils.HOUR);
        Event firstEvent = createEvent(user, mainEvent.getBeginDate(), new Date(mainEvent.getBeginDate().getTime() + MINUTE));
        Event secondEvent = createEvent(sameOwner ? user : owner, new Date(mainEvent.getEndDate().getTime() + MINUTE),
                new Date(mainEvent.getEndDate().getTime() + 2 * MINUTE));
        events.add(firstEvent);
        events.add(secondEvent);
        TreeMap<Date, List<InviteDTO>> result = pickUp(mainEvent.getBeginDate(), mainEvent.getEndDate());
        assertEquals(1, result.size());
        assertTrue(result.get(result.firstKey()).isEmpty());
        assertEquals(firstEvent.getEndDate(), result.firstKey());
    }

    private User saveAndGetUser() {
        User user = new User();
        user.setEmail(UUID.randomUUID().toString());
        user.setFirstName("Name");
        user.setLastName("Surname");
        return userRepository.save(user);
    }

    private void createMainEvent(long duration) {
        Event event = new Event();
        event.setName("Main event");
        event.setBeginDate(new Date());
        event.setEndDate(new Date(new Date().getTime() + duration));
        event.setOwner(owner);
        event.setIsPeriodic((byte) 0);
        mainEvent = eventRepository.save(event);
    }

    private void singleUserInvite(long eventDuration) {
        duration = eventDuration;
        createMainEvent(eventDuration);

        inviteUser(user);

        mainEvent = eventRepository.findOne(mainEvent.getId());
        //get updated event after adding invite
    }

    private TreeMap<Date, List<InviteDTO>> pickUp(Date happenAfter, Date happenBefore) {
        return inviteService.pickUpDate(mainEvent.getId(), happenAfter, happenBefore, duration);
    }

    private Event createBasicEvent(User user, Date begin, Date end) {
        Event event = new Event();
        event.setName(user.getId() + begin.toString());
        event.setBeginDate(begin);
        event.setEndDate(end);
        event.setOwner(user);
        event.setIsPeriodic((byte) 0);
        return event;
    }

    private Event createEvent(User user, Date begin, Date end) {
        Event event = createBasicEvent(user, begin, end);
        return eventRepository.save(event);
    }

    private Event createPeriodicEvent(User user, Date begin, Date end, Date until, Event.EventFrequency frequency, int period) {
        Event event = createBasicEvent(user, begin, end);
        event.setIsPeriodic((byte) 1);
        event.setFrequency(frequency);
        event.setPeriod(period);
        event.setLastDate(until);
        return eventRepository.save(event);
    }

    private void inviteUser(User user) {
        Invite userInvite = new Invite();
        userInvite.setEvent(mainEvent);
        userInvite.setInviteEmail(UUID.randomUUID().toString());
        userInvite.setUser(user);
        userInvite = inviteRepository.createInvite(userInvite);
        invites.add(userInvite);
    }
}
