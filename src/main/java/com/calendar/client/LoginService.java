package com.calendar.client;

import com.calendar.shared.dto.UserDTO;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rpc/loginService")
public interface LoginService extends RemoteService {

    UserDTO getUser(String token, String service);

    void clear();
}
