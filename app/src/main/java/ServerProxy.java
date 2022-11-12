import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import Request.LoginRequest;
import Request.RegisterRequest;
import Response.EventResponse;
import Response.LoginResponse;
import Response.PersonResponse;
import Response.RegisterResponse;
import model.AuthToken;

public class ServerProxy {

    //contains exactly the same api methods as our actual server

    public static void main(String args[]) {

        String serverHost = args[0];
        String serverPort = args[1];



    }
    //probably only need to worry about these 4? the others are for testing I think

    //register user
    private static RegisterResponse register(RegisterRequest registerRequest) {

//        try{
//            URL url = new URL("http://" + serverHost + ":" + serverPort + "/user/register");
//
//            HttpURLConnection http = (HttpURLConnection) url.openConnection();
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return null;
    }

    //login user
    private static LoginResponse login(LoginRequest loginRequest) {
        return null;
    }

    //get people
    private static PersonResponse getPeopleForUser(AuthToken authToken) {
        return null;
    }

    //get events
    private static EventResponse getEventsForUser(AuthToken authToken) {
        return null;
    }

    //fill
    //load
    //clear
    //getPersonByID
    //getEventById


}
