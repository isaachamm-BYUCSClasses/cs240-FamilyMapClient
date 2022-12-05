package byu.edu.isaacrh.familymapclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import static org.junit.Assert.*;

import Request.LoginRequest;
import Request.RegisterRequest;
import Response.LoginResponse;
import Response.RegisterResponse;

public class ServerProxyTests {

    private final static String serverHost = "localhost";
    private final static String serverPort = "8080";
    private final RegisterRequest registerRequest = new RegisterRequest("HARRY", "POTTER",
            "harrypot@gmail.com", "ron", "weasley", "m");
    private final LoginRequest loginRequest = new LoginRequest("HARRY", "POTTER");

    @Before
    public void eachSetup() {
        DataCache.setServerHost(serverHost);
        DataCache.setServerPort(serverPort);
        ServerProxy.clear();

    }

    // Register new user
    @Test
    public void validRegister() {

        RegisterResponse registerResponse = ServerProxy.register(registerRequest);

        assertNotNull(registerResponse.getAuthtoken());
        assertNotNull(registerResponse.getPersonID());
        assertTrue(registerResponse.isSuccess());
        assertEquals(registerResponse.getUsername(), registerRequest.getUsername());

    }

    @Test
    public void invalidRegister() {
        RegisterRequest registerRequest1 = new RegisterRequest("HARRY", "POTTER",
                "harrypot@gmail.com", "ron", "weasley", "m");
        RegisterRequest registerRequest2 = new RegisterRequest("HARRY", "POTTER",
                "harrypot@gmail.com", "ron", "weasley", "m");

        RegisterResponse registerResponse1 = ServerProxy.register(registerRequest1);
        RegisterResponse registerResponse2 = ServerProxy.register(registerRequest2);

        assertTrue(registerResponse1.isSuccess());

        assertFalse(registerResponse2.isSuccess());
        assertNull(registerResponse2.getAuthtoken());
        assertNull(registerResponse2.getPersonID());

    }

    // Login
    @Test
    @DisplayName("Valid login")
    public void login() {
        ServerProxy.register(registerRequest);

        LoginResponse loginResponse = ServerProxy.login(loginRequest);

        assertTrue(loginResponse.isSuccess());
        assertNotNull(loginResponse.getAuthtoken());
        assertNotNull(loginResponse.getPersonID());
        assertEquals(loginResponse.getUsername(), loginRequest.getUsername());

    }

    @Test
    public void invalidLogin() {
        LoginResponse loginResponse = ServerProxy.login(loginRequest);

        assertFalse(loginResponse.isSuccess());
        assertNull(loginResponse.getAuthtoken());
        assertNull(loginResponse.getPersonID());

    }

    // retrieving people related to a logged in / registered user
    @Test
    public void successfulPersonRetrieval() {

        RegisterResponse registerResponse = ServerProxy.register(registerRequest);
        DataCache.cacheData(registerResponse.getAuthtoken(), registerResponse.getPersonID());


        assertNotNull(DataCache.getCurrUser());
        assertEquals(DataCache.getCurrUser().getFirstName(), registerRequest.getFirstName());
        assertEquals(DataCache.getCurrUser().getLastName(), registerRequest.getLastName());

        assertNotNull(DataCache.getPeople());
        assertEquals(DataCache.getPeople().size(), 31);

        DataCache.clearCache();

        DataCache.setServerPort(serverPort);
        DataCache.setServerHost(serverHost);

        assertEquals(DataCache.getPeople().size(), 0);

        LoginResponse loginResponse = ServerProxy.login(loginRequest);
        DataCache.cacheData(loginResponse.getAuthtoken(), loginResponse.getPersonID());

        assertNotNull(DataCache.getCurrUser());

        assertNotNull(DataCache.getPeople());
        assertEquals(DataCache.getPeople().size(), 31);



    }

    @Test
    public void unsuccessfulPersonRetrieval() {

        // nonexistent user
        ServerProxy.login(loginRequest);

        assertNull(DataCache.getCurrUser());

        assertNull(DataCache.getPeople());

    }

    // retrieving events related to a logged in / registered user
    @Test
    public void successfulEventRetrieval() {

        RegisterResponse registerResponse = ServerProxy.register(registerRequest);
        DataCache.cacheData(registerResponse.getAuthtoken(), registerResponse.getPersonID());

        assertNotNull(DataCache.getCurrUser());
        assertEquals(DataCache.getCurrUser().getFirstName(), registerRequest.getFirstName());
        assertEquals(DataCache.getCurrUser().getLastName(), registerRequest.getLastName());

        assertNotNull(DataCache.getEvents());
        assertEquals(DataCache.getEvents().size(), 92);

    }

    @Test
    public void unsuccessfulEventRetrieval() {

        // nonexistent user
        ServerProxy.login(loginRequest);

        assertNull(DataCache.getCurrUser());

        assertNull(DataCache.getEvents());

    }

}
