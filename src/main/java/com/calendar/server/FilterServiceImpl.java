package com.calendar.server;

import com.calendar.client.FilterService;
import com.calendar.server.repository.FilterRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.FilterDTO;
import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.User;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("filterService")
public class FilterServiceImpl extends SessionService implements FilterService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterRepository filterRepository;

    @Autowired
    BeanFactory beanFactory;


    @Override
    public boolean addFilter(String name, String colorCode) {
        User currentUser = userRepository.findOne((Integer) getAttributeFromSession(SessionService.USER));
        if (currentUser == null) {
            return false;
        }

        // Check if filter with given name exists in db
        Filter existingFilter = filterRepository.findByUserAndDescription(currentUser, name);
        if (existingFilter != null) {
            return false;
        }
        // Check if colorCode is valid with regexp
        Pattern pattern = Pattern.compile("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
        if (!pattern.matcher(colorCode).matches()) {
            return false;
        }

        Filter newFilter = new Filter();
        newFilter.setDescription(name);
        newFilter.setColor(colorCode);
        newFilter.setUser(currentUser);
        filterRepository.save(newFilter);
        return true;
    }

    @Override
    public List<FilterDTO> getAllFilters() {
        User currentUser = userRepository.findOne((Integer) getAttributeFromSession(SessionService.USER));
        List<Filter> filters = currentUser.getFilters().stream().collect(Collectors.toList());
        return filters.stream().map(filter -> DozerBeanMapperSingletonWrapper.getInstance().map(filter, FilterDTO.class)).collect(Collectors.toList());
    }

    @Override
    public void deleteFilter(FilterDTO filterDTO) {
        Filter filter = DozerBeanMapperSingletonWrapper.getInstance().map(filterDTO, Filter.class);
        filterRepository.delete(filter);
    }

    @Override
    public boolean updateFilter(FilterDTO filterDTO) {
        User currentUser = userRepository.findOne((Integer) getAttributeFromSession(SessionService.USER));
        if (currentUser == null) {
            return false;
        }

        Filter filter = DozerBeanMapperSingletonWrapper.getInstance().map(filterDTO, Filter.class);
        Filter existingFilter = filterRepository.findByUserAndDescription(currentUser, filter.getDescription());
        if (existingFilter != null && !Objects.equals(existingFilter.getId(), filterDTO.getId())) {
            return false;
        }
        filter.setAttachedEvents(existingFilter.getAttachedEvents());
        filterRepository.save(filter);
        return true;
    }

    @Override
    public boolean exists(String name) {
        User currentUser = userRepository.findOne((Integer) getAttributeFromSession(SessionService.USER));
        if (currentUser == null) {
            return false;
        }

        Filter existingFilter = filterRepository.findByUserAndDescription(currentUser, name);
        return existingFilter != null;
    }
}
