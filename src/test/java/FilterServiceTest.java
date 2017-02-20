import com.calendar.server.FilterServiceImpl;
import com.calendar.server.repository.FilterRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.User;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static com.calendar.server.SessionService.USER;
import static org.junit.Assert.*;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContextTest.xml")
@WebAppConfiguration
public class FilterServiceTest {
    @Autowired
    FilterServiceImpl filterService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterRepository filterRepository;

    private User user;
    private Filter filter;

    @Before
    public void createObjects() {
        user = new User();
        user.setEmail(UUID.randomUUID().toString());
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        user = userRepository.save(user);

        filterService.setAttributeInSession(USER, user.getId());

        filter = new Filter();
        filter.setColor("000000");
        filter.setUser(user);
        filter.setDescription("Filter");
        filter = filterRepository.save(filter);
    }

    @After
    public void deleteObjects() {
        user.setFilters(new HashSet<>());
        userRepository.save(user);

        filterRepository.delete(filter);
        userRepository.delete(user);
    }

    @Test(expected = Exception.class)
    public void testAddingFilterForIncorrectUser() {
        // Incorrect user in the session
        filterService.setAttributeInSession(USER, null);
        assertFalse(filterService.addFilter("Filter name", "000000"));
    }

    @Test
    public void testAddingFilterForCorrectUser() {

        // Filter already exists
        assertFalse(filterService.addFilter(filter.getDescription(), "111111"));

        // Bad color :(
        assertFalse(filterService.addFilter("Filter name", "312sav"));

        // Save and check fields
        assertTrue(filterService.addFilter("Filter name", "000000"));
        Filter currentFilter = filterRepository.findByUserAndDescription(user, "Filter name");
        assertNotNull(currentFilter.getUser());
        assertTrue(currentFilter.getColor().equals("000000"));

    }

    @Test
    public void testGettingAllFilters() {
        ArrayList<Filter> fl = new ArrayList<>();
        fl.add(filter);
        user.setFilters(new HashSet<>(fl));
        userRepository.save(user);
        filterRepository.save(filter);

        assertArrayEquals(fl.toArray(), filterService.getAllFilters().stream().map(Filter::new).toArray());

        Filter newFilter = makeFilter();
        newFilter.setUser(user);
        fl.add(newFilter);
        filterRepository.save(newFilter);
        user.setFilters(new HashSet<>(fl));
        userRepository.save(user);

        ArrayList<Filter> flG = new ArrayList<>();
        for (FilterDTO f : filterService.getAllFilters()) {
            flG.add(DozerBeanMapperSingletonWrapper.getInstance().map(f, Filter.class));
        }

        assertTrue(fl.containsAll(flG) && flG.containsAll(fl));
    }

    @Test(expected = Exception.class)
    public void testUpdatingFilterForIncorrectUser() {
        // Incorrect user in the session
        filterService.setAttributeInSession(USER, null);

        FilterDTO filterDTO = DozerBeanMapperSingletonWrapper.getInstance().map(filter, FilterDTO.class);
        assertFalse(filterService.updateFilter(filterDTO));
    }

    @Test
    public void testUpdatingFilterForCorrectUser() {
        ArrayList<Filter> fl = new ArrayList<>();
        fl.add(filter);
        user.setFilters(new HashSet<>(fl));
        userRepository.save(user);
        filterRepository.save(filter);

        FilterDTO flDTO = DozerBeanMapperSingletonWrapper.getInstance().map(fl.get(0), FilterDTO.class);
        String oldName = flDTO.getDescription();
        flDTO.setDescription("Filter");
        // Filter with given name already exists
        assertFalse(filterService.updateFilter(flDTO));

        // Successful update
        flDTO.setDescription("New filter name");
        assertTrue(filterService.updateFilter(flDTO));

        // Check
        user = userRepository.findOne(user.getId());
        ArrayList<Filter> flCurrent = Lists.newArrayList(user.getFilters());
        assertFalse(flCurrent.get(0).getDescription().equals(oldName));
        assertTrue(flCurrent.get(0).getDescription().equals("New filter name"));
    }

    @Test
    public void testDeletingFilter() {
        ArrayList<Filter> fl = new ArrayList<>();
        fl.add(filter);
        user.setFilters(new HashSet<>(fl));
        userRepository.save(user);
        filterRepository.save(filter);

        FilterDTO flDTO = DozerBeanMapperSingletonWrapper.getInstance().map(fl.get(0), FilterDTO.class);
        filterService.deleteFilter(flDTO);

        user = userRepository.findOne(user.getId());
        assertTrue(user.getFilters().isEmpty());
    }

    @Test
    public void checkExistingFilter() {
        ArrayList<Filter> fl = new ArrayList<>();
        fl.add(filter);
        user.setFilters(new HashSet<>(fl));
        userRepository.save(user);
        filterRepository.save(filter);

        assertTrue(filterService.exists("Filter"));
        assertFalse(filterService.exists("Filter1"));
    }

    private Filter makeFilter() {
        Filter filter = new Filter();
        filter.setColor("000000");
        filter.setDescription(UUID.randomUUID().toString().substring(0, 10));
        return filter;
    }
}
