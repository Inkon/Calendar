package com.calendar.client;

import com.calendar.shared.dto.FilterDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath("rpc/filterService")
public interface FilterService extends RemoteService {
    boolean addFilter(String name, String colorCode);

    boolean updateFilter(FilterDTO filter);

    void deleteFilter(FilterDTO filter);

    List<FilterDTO> getAllFilters();

    boolean exists(String name);

}
