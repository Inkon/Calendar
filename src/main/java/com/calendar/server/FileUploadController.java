package com.calendar.server;

import com.calendar.server.repository.EventRepository;
import com.calendar.server.repository.FilterRepository;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.entity.Event;
import com.calendar.shared.entity.Filter;
import com.calendar.shared.entity.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class FileUploadController extends SessionService {
    public static final Logger LOG = Logger.getLogger(FileUploadController.class);

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterServiceImpl filterService;

    @Autowired
    FilterRepository filterRepository;

    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public int downloadAndImportCalendar(HttpServletRequest request) {
        int result = 0;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        CommonsMultipartFile file = (CommonsMultipartFile) multipartRequest.getFile("file");
        try (InputStream stream = file.getInputStream()) {
            ICalendarWrapper calendarWrapper = new ICalendarWrapper(stream);
            List<Event> eventList = calendarWrapper.getWrappedEvents();

            User currentUser = userRepository.findOne((int) getAttributeFromSession(SessionService.USER));
            if (currentUser != null) {
                eventList = eventList.stream().filter(event -> event != null).map(event -> {
                    event.setOwner(currentUser);
                    return event;
                }).collect(Collectors.toList());
                result = eventList.size();

                // Add filter
                LOG.debug("Finding name for new filter...");
                String filterName = "Импорт календаря №";
                int filterImportNum = 0;
                Filter attachedFilter = null;
                while (true) {
                    filterImportNum++;
                    if (!filterService.exists(filterName + filterImportNum)) {
                        String newFilterName = filterName + filterImportNum;
                        Color color = new Color((int) (Math.random() * 0x1000000));
                        String colorCode = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

                        if (!filterService.addFilter(newFilterName, colorCode)) {
                            LOG.error("Error while creating new filter for group of imported events!");
                            return -1;
                        }
                        attachedFilter = filterRepository.findByUserAndDescription(currentUser, newFilterName);
                        LOG.debug("New filter added: " + attachedFilter);
                        break;
                    }
                }

                Filter finalAttachedFilter = attachedFilter;
                eventList = eventList.stream().map(event -> {
                    event.addFilter(finalAttachedFilter);
                    return event;
                }).collect(Collectors.toList());

                eventRepository.save(eventList);
            }
        } catch (Exception ignored) {
            LOG.error(ignored.getMessage());
            return -1;
        }

        return result;
    }
}
