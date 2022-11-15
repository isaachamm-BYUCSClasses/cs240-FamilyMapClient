package byu.edu.isaacrh.familymapclient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;


public class DataCache {

    private static String serverPort;
    private static String serverHost;
    private static DataCache instance;
    // The strings here are for personid and eventid
    private static Map<String, Person> people;
    private static Map<String, Event> events;

    // This maps from each person to their associated events, sorted chronologically
    Map<Person, List<Event>> personEvents;

    // String => personid of the ancestors. This is for showing only half
    Set<String> paternalAncestors;
    Set<String> maternalAncestors;

    //can store settings
    // can keep track of event colors here

    public static Person getPersonById(String personId) {
        return getPeople().get(personId);
    }
    public static Event getEventById(String eventId) { return null; }
    public static List<Event> getPersonEvents(String personId) { return null; }


    private DataCache() { }

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
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




}
