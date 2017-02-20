package com.calendar.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${inviteMessage.subject}")
    private String inviteMessageSubject;

    @Value("${inviteMessage.text}")
    private String inviteMessageText;

    @Value("${inviteMessage.link}")
    private String inviteMessageLink;

    @Value("${tomitaParser.bin}")
    private String tomitaParserBin;

    @Value("${tomitaParser.grammar}")
    private String tomitaParserGrammar;

    public String getInviteMessageSubject() {
        return inviteMessageSubject;
    }

    public String getInviteMessageText() {
        return inviteMessageText;
    }

    public String getInviteMessageLink() {
        return inviteMessageLink;
    }

    public String getTomitaParserBin() {
        return tomitaParserBin;
    }

    public String getTomitaParserGrammar() {
        return tomitaParserGrammar;
    }
}