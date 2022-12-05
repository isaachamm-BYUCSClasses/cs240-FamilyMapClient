package byu.edu.isaacrh.familymapclient;

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

    LoginRequest sheilaLogin = new LoginRequest("sheila", "parker");
    LoginRequest patrickLogin = new LoginRequest("patrick", "spencer");
    private final static String serverHost = "localhost";
    private final static String serverPort = "8080";

    @Before
    public void eachSetup() {
        DataCache.setServerHost(serverHost);
        DataCache.setServerPort(serverPort);

    }

    //calculate family relationships
    @Test
    @DisplayName("Calculate family relationships")
    public void calculateRelationships() {

        //getpersonfamilymembers function
        LoginResponse loginResponse = ServerProxy.login(sheilaLogin);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());

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

        //getpersonfamilymembersfunction
        LoginResponse loginResponse = ServerProxy.login(sheilaLogin);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());

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

        //calculatecurrentevents

    }

    @Test
    @DisplayName("Filter and return NO results")
    public void filterEventsAbnormal() {

        //calculatecurrentevents


    }


    //Chronologically sort an individuals' events
    @Test
    @DisplayName("Normal sort events")
    public void sortEvents() {

        //cachedata

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
