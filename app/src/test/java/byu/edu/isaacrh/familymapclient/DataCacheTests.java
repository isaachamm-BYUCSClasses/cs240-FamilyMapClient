package byu.edu.isaacrh.familymapclient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.Assert.*;

import java.util.List;

import Request.LoginRequest;
import Request.RegisterRequest;
import Response.LoginResponse;
import model.Person;

public class DataCacheTests {

    /*  ESSENTIAL NOTE: ALL TESTS DEPEND ON LOAD DATA FOR CS 240
        SHEILA AND PATRICK'S DATA MUST BE LOADED INTO THE SERVER
        PRIOR TO RUNNING TESTS FOR THESE TESTS TO PASS
     */

    private final static LoginRequest sheilaLogin = new LoginRequest("sheila", "parker");
    private final static LoginRequest patrickLogin = new LoginRequest("patrick", "spencer");
    private static LoginResponse loginResponse = null;
    private final static String serverHost = "localhost";
    private final static String serverPort = "8080";

    @Before
    public void eachSetup() {
        DataCache.setServerHost(serverHost);
        DataCache.setServerPort(serverPort);

        loginResponse = ServerProxy.login(sheilaLogin);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());


    }

    @After
    public void eachCleanup() {
        DataCache.clearCache();
    }

    //calculate family relationships
    @Test
    @DisplayName("Calculate family relationships")
    public void calculateRelationships() {

        //Blaine McGary has 4 family members
        Person blaine = DataCache.getPersonById("Blaine_McGary");
        List<Person> blaineFamily = DataCache.getPersonFamilyMembers(blaine);

        Person blaineFather = blaineFamily.get(0);
        Person blaineMother = blaineFamily.get(1);
        Person blaineSpouse = blaineFamily.get(2);
        Person blaineChild = blaineFamily.get(3);

        String fatherName = blaineFather.getFirstName() + " " + blaineFather.getLastName();
        assertEquals("Ken Rodham", fatherName);
        String motherName = blaineMother.getFirstName() + " " + blaineMother.getLastName();
        assertEquals("Mrs Rodham", motherName);
        String spouseName = blaineSpouse.getFirstName() + " " + blaineSpouse.getLastName();
        assertEquals("Betty White", spouseName);
        String childName = blaineChild.getFirstName() + " " + blaineChild.getLastName();
        assertEquals("Sheila Parker", childName);

    }

    @Test
    @DisplayName("Calculate family relationships, missing people")
    public void calculateRelationshipsAbnormal() {

        //Sheila Parker has no child
        Person sheila = DataCache.getPersonById("Sheila_Parker");
        List<Person> sheilaFamily = DataCache.getPersonFamilyMembers(sheila);

        assertThrows(IndexOutOfBoundsException.class, () -> {sheilaFamily.get(3);});

        //Ken Rodham as no parents
        Person ken = DataCache.getPersonById("Ken_Rodham");
        List<Person> kenFamily = DataCache.getPersonFamilyMembers(ken);
        assertNull(kenFamily.get(0));
        assertNull(kenFamily.get(1));

        DataCache.clearCache();
        DataCache.setServerHost(serverHost);
        DataCache.setServerPort(serverPort);

        //Switch to Patrick Spencer because he doesn't have a spouse

        loginResponse = ServerProxy.login(patrickLogin);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());

        Person patrick = DataCache.getPersonById(loginResponse.getPersonID());
        List<Person> patrickFamily = DataCache.getPersonFamilyMembers(patrick);
        assertNull(patrickFamily.get(2));

    }


    //filter events according to current filter settings
    @Test
    @DisplayName("Filter and return results")
    public void filterEvents() {

        Person sheila = DataCache.getPersonById("Sheila_Parker");
        Person ken = DataCache.getPersonById("Ken_Rodham");

        assertNotNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

        DataCache.setFemaleEventSwitch(false);
        DataCache.calculateCurrentEvents();
        assertNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

        DataCache.setFemaleEventSwitch(true);
        DataCache.calculateCurrentEvents();
        assertNotNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

        DataCache.setMaleEventSwitch(false);
        DataCache.calculateCurrentEvents();
        assertNull(DataCache.getCurrentEventsDisplay().get(ken));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(sheila));

        DataCache.setMaleEventSwitch(true);
        DataCache.calculateCurrentEvents();
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

        // ken is on the father side
        // mrs jones is on the mother side
        Person mrsJones = DataCache.getPersonById("Mrs_Jones");

        assertNotNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

        DataCache.setMotherSide(false);
        DataCache.calculateCurrentEvents();
        assertNull(DataCache.getCurrentEventsDisplay().get(mrsJones));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

        DataCache.setMotherSide(true);
        DataCache.calculateCurrentEvents();
        assertNotNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

        DataCache.setFatherSide(false);
        DataCache.calculateCurrentEvents();
        assertNull(DataCache.getCurrentEventsDisplay().get(ken));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

        DataCache.setFatherSide(true);
        DataCache.calculateCurrentEvents();
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));

    }

    @Test
    @DisplayName("Filter and return NO results")
    public void filterEventsAbnormal() {

        Person sheila = DataCache.getPersonById("Sheila_Parker");
        Person ken = DataCache.getPersonById("Ken_Rodham");
        Person mrsJones = DataCache.getPersonById("Mrs_Jones");

        assertNotNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(ken));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

        // this should return completely with 0 events and 0 people
        DataCache.setFemaleEventSwitch(false);
        DataCache.setMaleEventSwitch(false);
        DataCache.calculateCurrentEvents();
        assertEquals(0, DataCache.getCurrentEventsDisplay().size());
        assertNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNull(DataCache.getCurrentEventsDisplay().get(ken));
        assertNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

        // sheila and her spouse (David) should still return true here becuase she's the current user
        DataCache.setFemaleEventSwitch(true);
        DataCache.setMaleEventSwitch(true);
        DataCache.setFatherSide(false);
        DataCache.setMotherSide(false);
        DataCache.calculateCurrentEvents();

        Person davis = DataCache.getPersonById("Davis_Hyer");

        assertNotNull(DataCache.getCurrentEventsDisplay().get(davis));
        assertNotNull(DataCache.getCurrentEventsDisplay().get(sheila));
        assertNull(DataCache.getCurrentEventsDisplay().get(ken));
        assertNull(DataCache.getCurrentEventsDisplay().get(mrsJones));

    }


    //Chronologically sort an individuals' events
    @Test
    @DisplayName("Normal sort events")
    public void sortEvents() {

        LoginResponse loginResponse = ServerProxy.login(sheilaLogin);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());


    }

    @Test
    @DisplayName("Abnormal sort events, no birth and no death")
    public void sortEventsAbnormal() {

        //cachedata
        //Patrick wilson in Patrick Spencer's Data has no birth/death events

    }


    //search for people and events
    @Test
    @DisplayName("Search and return results")
    public void searchReturnsResults() {

        //searchfunction sets Datacache.getEventSearch

    }

    @Test
    @DisplayName("Search and return no results")
    public void searchReturnsNoResults() {

        //searchfunction sets Datacache.getEventSearch

    }

}
