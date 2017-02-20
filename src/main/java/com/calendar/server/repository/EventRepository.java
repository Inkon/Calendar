package com.calendar.server.repository;

import com.calendar.shared.entity.Event;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Integer> {
    List<Event> findByOwnerId(int id);
}
