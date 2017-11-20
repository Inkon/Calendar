## Simple calendar
This is a web app, representing simple calendar (Google/Outlook-like)

The project was written collaborating with [Vladislav Kiryukhin](https://github.com/kvld) and this repository is a clone of initial private one.

## Features and frameworks
### Project structure
The general project structure described in the picture below.
![](https://pp.userapi.com/c841336/v841336993/37e71/duA7Dipcxaw.jpg)
### Client-side
GWT was used to render a page. (With GWT bootstrap to make it look neater) Different views was done by history feature. (E.g. login view is located at ```url#login``` and main view at ```url#calendar```)
##### Authorisation  
Oauth2 was used to provide authorizing. We used Google and Vkontakte public api to get user's token, that is stored in cookies and passed to the server and it writes token into the database.
### Server-side
Hibernate and spring (MVC and data access) were used at server side.
#### Importing from other services
User can import (or export) a calendar in common format exported from Google calendar, Outlook or else.  
#### Invites
One user can invite another via automatic email sending to participate in the event.
#### Finding the best time
For each group event user can send a request to server to organize the event so that as many users as possible could attend.  
#### NLP
Events can be written in natural language (Russian) and it'll be created as an internal event (Yandex library was used to implement this feature)
### Entities
The general project structure described in the picture below.
![](https://pp.userapi.com/c841336/v841336993/37e68/q-UEHx2iRXc.jpg)

The main entity is an event. Each event have begin and end date, periodic flag (and if it's set, a period, how often an event is occurring, set by frequency value and last date, till when it continues), name and description. Each event can have one or more custom filters (unique for each user). All events have a creator, who can modify and delete the event, and also invite other users to participate in it. Invited user could accept or reject it. Invites are sent via email. Invited users can only view information about the event and set filters to it.
