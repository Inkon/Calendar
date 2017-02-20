package com.calendar.server;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionService {
    public static final String USER = "user";

    public void setAttributeInSession(String name, Object value) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(name, value, ServletRequestAttributes.SCOPE_SESSION);
        }
    }

    public Object getAttributeFromSession(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getAttribute(name, ServletRequestAttributes.SCOPE_SESSION);
        }
        return null;
    }

    public void removeAttributeFromSession(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.removeAttribute(name, ServletRequestAttributes.SCOPE_SESSION);
        }
    }
}
