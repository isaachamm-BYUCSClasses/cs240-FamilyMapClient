package byu.edu.isaacrh.familymapclient;

import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Response.EventResponse;
import Response.PersonResponse;
import model.AuthToken;
import model.Event;
import model.Person;
import model.User;


public class DataCache {

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    private static String serverPort;
    private static String serverHost;
    private static DataCache instance;
    // The strings here are for personid and eventid
    private static Map<String, Person> people;
    private static Map<String, Event> events;

    private static Map<String, Float> colorMap;

    // This maps from each person to their associated events, sorted chronologically
    private static Map<Person, List<Event>> personEvents;
    private static List<Polyline> polylines;
    private static Map<Person, List<Person>> childrenMap;

    // String => personid of the ancestors. This is for showing only half
    Set<String> paternalAncestors;
    Set<String> maternalAncestors;

    //can store settings
    // can keep track of event colors here

    public static Person getPersonById(String personId) {
        return getPeople().get(personId);
    }
    public static Event getEventById(String eventId) {
        return getEvents().get(eventId);
    }



    private DataCache() { }

    public static void addEventColor(String eventType, float color) {
        if(colorMap == null) {
            colorMap = new HashMap<>();
        }
        colorMap.put(eventType, color);
    }

    public static List<Person> getPersonFamilyMembers(Person person) {
        List<Person> familyMembers = new ArrayList<>();

        familyMembers.add(getPersonById(person.getFatherID()));
        familyMembers.add(getPersonById(person.getMotherID()));
        familyMembers.add(getPersonById(person.getSpouseID()));

        List<Person> children = getChildrenMap().get(person);
        if(children != null) {
            familyMembers.addAll(children);
        }

        return familyMembers;
    }
    public static String cacheData(String authToken, String personId) {
        DataCache.getInstance();

        PersonResponse personResponse = ServerProxy.getPeopleForUser(authToken);
        EventResponse eventResponse = ServerProxy.getEventsForUser(authToken);

        Map<String, Person> newPeople = new HashMap<>();
        Map<String, Event> newEvents = new HashMap<>();
        Map<Person, List<Event>> newEventMap = new HashMap<>();

        if(childrenMap == null) {
            childrenMap = new HashMap<>();
        }

        assert personResponse != null;
        for (Person person : personResponse.getAncestors()) {
            newPeople.put(person.getPersonID(), person);
        }
        DataCache.setPeople(newPeople);

        // code to find and add children
        // have to loop through again so that everyone is already added
        for (Person person : personResponse.getAncestors()) {
            newPeople.put(person.getPersonID(), person);
            if (DataCache.getPersonById(person.getFatherID()) != null) {
                addChildrenToMap(DataCache.getPersonById(person.getFatherID()), person);
            }
            if (DataCache.getPersonById(person.getMotherID()) != null) {
                addChildrenToMap(DataCache.getPersonById(person.getMotherID()), person);
            }
        }

        assert eventResponse != null;
        for (Event event : eventResponse.getData()) {
            newEvents.put(event.getEventID(), event);
            Person associatedPerson = getPersonById(event.getPersonID());
            List<Event> personEvents;
            if(newEventMap.get(associatedPerson) == null) {
                personEvents = new ArrayList<>();
                personEvents.add(event);
            }
            else {
                personEvents = newEventMap.get(associatedPerson);
                if (personEvents.get(0).getYear() > event.getYear()) {
                    personEvents.add(0, event);
                }
                else {
                    personEvents.add(event);
                }
            }

            newEventMap.put(associatedPerson, personEvents);

        }
        DataCache.setEvents(newEvents);
        DataCache.setPersonEvents(newEventMap);

        Person currUser = DataCache.getPersonById(personId);

        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String fullName = firstName + " " + lastName;

        return fullName;
    }

    protected static void addChildrenToMap(Person parent, Person child) {
        List<Person> newChildren;
        if(childrenMap.get(parent) != null) {
            newChildren = childrenMap.get(parent);
        }
        else {
            newChildren = new ArrayList<>();
        }
        assert newChildren != null;
        newChildren.add(child);

        childrenMap.put(parent, newChildren);
    }

    public static String getServerPort() {
        return serverPort;
    }
    public static void setServerPort(String serverPort) {
        DataCache.serverPort = serverPort;
    }
    public static String getServerHost() {
        return serverHost;
    }
    public static void setServerHost(String serverHost) {
        DataCache.serverHost = serverHost;
    }
    public static Map<String, Person> getPeople() {
        return people;
    }
    public static void setPeople(Map<String, Person> people) {
        DataCache.people = people;
    }
    public static Map<String, Event> getEvents() {
        return events;
    }
    public static void setEvents(Map<String, Event> events) {
        DataCache.events = events;
    }
    public static Map<String, Float> getColorMap() {
        return colorMap;
    }
    public static void setColorMap(Map<String, Float> colorMap) {
        DataCache.colorMap = colorMap;
    }
    public static Map<Person, List<Event>> getPersonEvents() {
        return personEvents;
    }
    public static void setPersonEvents(Map<Person, List<Event>> personEvents) {
        DataCache.personEvents = personEvents;
    }
    public static List<Polyline> getPolylines() {
        return polylines;
    }
    public static void setPolylines(List<Polyline> polylines) {
        DataCache.polylines = polylines;
    }
    public static Map<Person, List<Person>> getChildrenMap() {
        return childrenMap;
    }
    public static void setChildrenMap(Map<Person, List<Person>> childrenMap) {
        DataCache.childrenMap = childrenMap;
    }
}
