package byu.edu.isaacrh.familymapclient;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Response.EventResponse;
import Response.PersonResponse;
import model.Event;
import model.Person;


public class DataCache {

    // For the singleton design
    private DataCache() { }

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    private static String serverPort;
    private static String serverHost;
    private static DataCache instance;
    private static Person currUser;
    private static Map<String, Person> people;      // The strings here are for both for personid
    private static Map<String, Event> events;       // This holds all possible events
    private static Map<String, Float> colorMap;  // string is for event type
    // This maps from each person to their associated events, sorted chronologically
    private static Map<Person, List<Event>> personEvents;
    private static Map<Person, List<Event>> currentEventsDisplay; // This holds current events according to settings filters
    private static List<Polyline> polylines = new ArrayList<>();
    //maps from a person to a list of their children
    private static Map<Person, List<Person>> childrenMap;

    // String => personid of the ancestors. This is for showing only half
    private static Map<Person, List<Event>> fatherSideEvents = new HashMap<>();
    private static Map<Person, List<Event>> motherSideEvents = new HashMap<>();
    private static Map<Person, List<Event>> maleEvents;
    private static Map<Person, List<Event>> femaleEvents;

    // settings
    private static boolean lifeStorylines = true;
    private static boolean familyTreeLines = true;
    private static boolean spouseLines = true;
    private static boolean fatherSide = true;
    private static boolean motherSide = true;
    private static boolean maleEventSwitch = true;
    private static boolean femaleEventSwitch = true;

    public static void clearCache() {
        serverPort = "";
        serverHost ="";
        currUser = null;
        people.clear();
        events.clear();
        colorMap.clear();
        personEvents.clear();
        currentEventsDisplay.clear();
        polylines.clear();
        childrenMap.clear();
        fatherSideEvents.clear();
        motherSideEvents.clear();
        maleEvents.clear();
        femaleEvents.clear();

        lifeStorylines = true;
        familyTreeLines = true;
        spouseLines = true;
        fatherSide = true;
        motherSide = true;
        maleEventSwitch = true;
        femaleEventSwitch = true;
    }

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
        Map<Person, List<Event>> maleEventMap = new HashMap<>();
        Map<Person, List<Event>> femaleEventMap = new HashMap<>();

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

            // this code is to order the events correctly
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
            if(associatedPerson.getGender().compareTo("m") == 0) {
                maleEventMap.put(associatedPerson, personEvents);
            }
            else {
                femaleEventMap.put(associatedPerson, personEvents);
            }


        }

        DataCache.setEvents(newEvents);
        DataCache.setPersonEvents(newEventMap);
        DataCache.setMaleEvents(maleEventMap);
        DataCache.setFemaleEvents(femaleEventMap);

        DataCache.setCurrentEventsDisplay(DataCache.getPersonEvents());

        Person currUser = DataCache.getPersonById(personId);
        DataCache.setCurrUser(currUser);

        DataCache.calculateParentSideEvents(DataCache.getPersonById(currUser.getFatherID()), true);
        DataCache.calculateParentSideEvents(DataCache.getPersonById(currUser.getMotherID()), false);

        String firstName = currUser.getFirstName();
        String lastName = currUser.getLastName();
        String fullName = firstName + " " + lastName;

        return fullName;
    }

    // a recursive call to calculate the mother and father side events
    // Only need to do this for the currUser
    public static void calculateParentSideEvents(Person person, boolean fatherSide) {
        if(fatherSide) {
            DataCache.getFatherSideEvents().put(person, DataCache.getPersonEvents().get(person));
        }
        else {
            DataCache.getMotherSideEvents().put(person, DataCache.getPersonEvents().get(person));
        }
        if(person.getFatherID() != null) {
            DataCache.calculateParentSideEvents(DataCache.getPersonById(person.getFatherID()), fatherSide);
            DataCache.calculateParentSideEvents(DataCache.getPersonById(person.getMotherID()), fatherSide);
        }
    }

    public static void calculateCurrentEvents() {

        Map<Person, List<Event>> newCurrentEvents = new HashMap<>();

        if (DataCache.isFatherSide()) {
            newCurrentEvents.putAll(DataCache.getFatherSideEvents());
        }
        if (DataCache.isMotherSide()) {
            newCurrentEvents.putAll(DataCache.getMotherSideEvents());
        }

        List<Person> peopleToRemoveGender = new ArrayList<>();
        if(!DataCache.isMaleEventSwitch()) {
            for(Map.Entry<Person, List<Event>> entry: newCurrentEvents.entrySet()) {
                if(entry.getKey().getGender().compareTo("m") != 0) {
                    peopleToRemoveGender.add(entry.getKey());
                }
            }
        }
        if (!DataCache.isFemaleEventSwitch()) {
            for(Map.Entry<Person, List<Event>> entry: newCurrentEvents.entrySet()) {
                if(entry.getKey().getGender().compareTo("f") != 0) {
                    peopleToRemoveGender.add(entry.getKey());
                }
            }
        }

        for(Person person : peopleToRemoveGender) {
            newCurrentEvents.remove(person);
        }

        newCurrentEvents.put(DataCache.getCurrUser(), DataCache.getPersonEvents().get(DataCache.getCurrUser()));

        DataCache.setCurrentEventsDisplay(newCurrentEvents);

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

    public static Person getPersonById(String personId) {
        return getPeople().get(personId);
    }
    public static Event getEventById(String eventId) {
        return getEvents().get(eventId);
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
    public static Person getCurrUser() {
        return currUser;
    }
    public static void setCurrUser(Person currUser) {
        DataCache.currUser = currUser;
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
    public static Map<Person, List<Event>> getCurrentEventsDisplay() {
        return currentEventsDisplay;
    }
    public static void setCurrentEventsDisplay(Map<Person, List<Event>> currentEventsDisplay) {
        DataCache.currentEventsDisplay = currentEventsDisplay;
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
    public static Map<Person, List<Event>> getFatherSideEvents() {
        return fatherSideEvents;
    }
    public static void setFatherSideEvents(Map<Person, List<Event>> fatherSideEvents) {
        DataCache.fatherSideEvents = fatherSideEvents;
    }
    public static Map<Person, List<Event>> getMotherSideEvents() {
        return motherSideEvents;
    }
    public static void setMotherSideEvents(Map<Person, List<Event>> motherSideEvents) {
        DataCache.motherSideEvents = motherSideEvents;
    }
    public static Map<Person, List<Event>> getMaleEvents() {
        return maleEvents;
    }
    public static void setMaleEvents(Map<Person, List<Event>> maleEvents) {
        DataCache.maleEvents = maleEvents;
    }
    public static Map<Person, List<Event>> getFemaleEvents() {
        return femaleEvents;
    }
    public static void setFemaleEvents(Map<Person, List<Event>> femaleEvents) {
        DataCache.femaleEvents = femaleEvents;
    }
    public static boolean isLifeStorylines() {
        return lifeStorylines;
    }
    public static void setLifeStorylines(boolean lifeStorylines) {
        DataCache.lifeStorylines = lifeStorylines;
    }
    public static boolean isFamilyTreeLines() {
        return familyTreeLines;
    }
    public static void setFamilyTreeLines(boolean familyTreeLines) {
        DataCache.familyTreeLines = familyTreeLines;
    }
    public static boolean isSpouseLines() {
        return spouseLines;
    }
    public static void setSpouseLines(boolean spouseLines) {
        DataCache.spouseLines = spouseLines;
    }
    public static boolean isFatherSide() {
        return fatherSide;
    }
    public static void setFatherSide(boolean fatherSide) {
        DataCache.fatherSide = fatherSide;
    }
    public static boolean isMotherSide() {
        return motherSide;
    }
    public static void setMotherSide(boolean motherSide) {
        DataCache.motherSide = motherSide;
    }
    public static boolean isMaleEventSwitch() {
        return maleEventSwitch;
    }
    public static void setMaleEventSwitch(boolean maleEventSwitch) {
        DataCache.maleEventSwitch = maleEventSwitch;
    }
    public static boolean isFemaleEventSwitch() {
        return femaleEventSwitch;
    }
    public static void setFemaleEventSwitch(boolean femaleEventSwitch) {
        DataCache.femaleEventSwitch = femaleEventSwitch;
    }
}
