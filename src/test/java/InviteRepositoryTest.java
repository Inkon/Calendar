import com.calendar.server.repository.EventRepository;
import com.calendar.server.repository.InviteRepository;
import com.calendar.server.repository.UserRepository;
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

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContextTest.xml")
@WebAppConfiguration
public class InviteRepositoryTest {

    @Autowired
    InviteRepository inviteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventRepository eventRepository;

    private User user;
    private Event event;
    private Invite invite;


    @Before
    public void createObjects() {
        User user = new User();
        user.setEmail(UUID.randomUUID().toString());
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        this.user = userRepository.save(user);

        Event event = new Event();
        event.setName("Test event");
        event.setBeginDate(Date.from(Instant.now()));
        event.setOwner(this.user);
        this.event = eventRepository.save(event);

        invite = new Invite();
        invite.setEvent(event);
        invite.setInviteEmail(UUID.randomUUID().toString());
        invite = inviteRepository.createInvite(invite);
    }

    @After
    public void deleteObjects() {
        eventRepository.delete(event);
        userRepository.delete(user);
    }

    @Test
    public void testCreatingInvite() throws Exception {
        // New invite should have token
        assertNotNull(invite.getInviteToken());
        // Invite status should be 'WAIT'
        assertTrue(invite.getStatus().equals(Invite.InviteStatus.WAIT));

        inviteRepository.delete(invite);
    }

    @Test
    public void testAcceptingInvite() throws Exception {
        User acceptingUser = new User();
        acceptingUser.setEmail(UUID.randomUUID().toString());
        acceptingUser.setFirstName("Ivan");
        acceptingUser.setLastName("Petrov");
        acceptingUser = userRepository.save(acceptingUser);

        inviteRepository.acceptInvite(invite, acceptingUser);

        acceptingUser = userRepository.findOne(acceptingUser.getId());

        // Invite should have user
        assertNotNull(invite.getUser());
        // Invite should have status 'ACCEPT'
        assertTrue(invite.getStatus().equals(Invite.InviteStatus.ACCEPT));

        acceptingUser.setInvites(new HashSet<>());
        userRepository.save(acceptingUser);

        inviteRepository.delete(invite);
        userRepository.delete(acceptingUser);
    }

    @Test
    public void testRejectingInvite() throws Exception {
        inviteRepository.rejectInvite(invite);

        // Invite should have status 'REJECT'
        assertTrue(invite.getStatus().equals(Invite.InviteStatus.REJECT));

        inviteRepository.delete(invite);
    }
}
