package com.calendar.server;

import com.calendar.client.LoginService;
import com.calendar.server.repository.UserRepository;
import com.calendar.shared.dto.UserDTO;
import com.calendar.shared.entity.User;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.log4j.Logger;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.URL;

import static com.calendar.client.Oauth.GOOGLE;
import static com.calendar.client.Oauth.VK;

@Service("loginService")
public class LoginServiceImpl extends SessionService implements LoginService {
    private String name = null;
    private String surname = null;
    private static String BASIC = "https://www.googleapis.com/plus/v1/people/me?alt=json&access_token=";
    private static String BASIC_VK = "https://api.vk.com/method/users.get?&access_token=";
    public static final Logger LOG = Logger.getLogger(LoginServiceImpl.class);

    @Autowired
    BeanFactory beanFactory;

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDTO getUser(String token, String service) {
        LOG.debug("Obtaining user info from db");
        User user = createAndGetUser(token, service);
        return user == null ? null : DozerBeanMapperSingletonWrapper.getInstance().map(user, UserDTO.class);
    }

    @Override
    public void clear() {
        // Clear session
        removeAttributeFromSession("user");
    }

    @RequestMapping("/")
    private User createAndGetUser(String token, String service) {
        try {
            if (service.equals(GOOGLE)) {
                return getGoogleUser(token);
            } else if (service.equals(VK)){
                return getVkUser(token);
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        return null;
    }

    private User getGoogleUser(String token) throws IOException {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new URL(BASIC + token))) {
            String email = null;
            while (name == null || surname == null || email == null) {
                parser.nextToken();
                if (parser.getCurrentName() != null) {
                    switch (parser.getCurrentName()) {
                        case "familyName":
                            surname = parser.nextTextValue();
                            break;
                        case "givenName":
                            name = parser.nextTextValue();
                            break;
                        case "emails":
                            String address = null;
                            while (!parser.currentToken().equals(JsonToken.END_ARRAY)) {
                                parser.nextToken();
                                if (parser.getCurrentName() != null) {
                                    switch (parser.getCurrentName()) {
                                        case "value":
                                            address = parser.nextTextValue();
                                            break;
                                        case "type":
                                            if (parser.nextTextValue().equals("account")) {
                                                email = address;
                                                break;
                                            }
                                    }
                                    if (email != null) {
                                        break;
                                    }
                                }
                            }
                    }
                }
            }
            LOG.debug("Obtained with token " + token);
            LOG.debug(String.format("Identified %s %s with email %s", name, surname, email));
            User targetUser = userRepository.findByEmail(email);
            LOG.debug("User found " + (targetUser == null));
            targetUser = check(targetUser, email);
            // Save user in session
            // Now we can get access from each Service that extends BaseService:
            // User loggedUser = getAttributeInSession("user");
            setAttributeInSession("user", targetUser.getId());
            return targetUser;
        }
    }

    private User getVkUser(String token) throws IOException{
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(new URL(BASIC_VK + token))) {
            int id = -1;
            while (name == null || surname == null || id == -1) {
                parser.nextToken();
                if (parser.getCurrentName() != null) {
                    switch (parser.getCurrentName()) {
                        case "last_name":
                            surname = parser.nextTextValue();
                            break;
                        case "first_name":
                            name = parser.nextTextValue();
                            break;
                        case "uid":
                            id = parser.nextIntValue(-1);
                            break;
                    }
                }
            }
            LOG.debug("Obtained with token " + token);
            String email = VK + id;
            LOG.debug(String.format("Identified %s %s with id %s", name, surname, id));
            User targetUser = userRepository.findByEmail(email);
            LOG.debug("User found " + (targetUser == null));
            targetUser = check(targetUser, email);
            // Save user in session
            // Now we can get access from each Service that extends BaseService:
            // User loggedUser = getAttributeInSession("user");
            setAttributeInSession("user", targetUser.getId());
            return targetUser;
        }
    }

    public User check(User targetUser, String email){
        if (targetUser == null) {
            // Create user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setLastName(surname);
            newUser.setFirstName(name);
            userRepository.save(newUser);
            return newUser;
        }
        return targetUser;
    }
}
