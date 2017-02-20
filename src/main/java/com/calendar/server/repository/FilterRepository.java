package com.calendar.server.repository;

import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface FilterRepository extends CrudRepository<Filter, Integer> {
    Filter findByUserAndDescription(User user, String description);
}
