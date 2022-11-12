import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;


public class DataCache {

    private static DataCache instance;

    private DataCache() { }

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }

        return instance;
    }

    // The strings here are for personid and eventid
    Map<String, Person> people;
    Map<String, Event> events;

    // This maps from each person to their associated events, sorted chronologically
    Map<Person, List<Event>> personEvents;

    // String => personid of the ancestors. This is for showing only half
    Set<String> paternalAncestors;
    Set<String> maternalAncestors;

    //can store settings
    // can keep track of event colors here

    public Person getPersonById(String personId) { return null; }
    public Event getEventById(String eventId) { return null; }
    public List<Event> getPersonEvents(String personId) { return null; }





}
