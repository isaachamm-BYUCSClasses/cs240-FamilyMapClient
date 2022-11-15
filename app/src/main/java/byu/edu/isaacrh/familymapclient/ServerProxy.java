package byu.edu.isaacrh.familymapclient;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import Request.LoginRequest;
import Request.RegisterRequest;
import Response.EventResponse;
import Response.LoginResponse;
import Response.PersonResponse;
import Response.RegisterResponse;
import model.AuthToken;

public class ServerProxy {

    //register user
    public static RegisterResponse register(RegisterRequest registerRequest) {

        try{
            URL url = new URL("http://" + DataCache.getServerHost() + ":" +
                    DataCache.getServerPort() + "/user/register");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.connect();

            Gson gson = new Gson();
            String requestData = gson.toJson(registerRequest);
            OutputStream requestBody = http.getOutputStream();
            writeString(requestData, requestBody);

            requestBody.close();

            if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("Register Successful");
            }

            InputStream responseBody = http.getInputStream();
            String responseData = readString(responseBody);

            RegisterResponse registerResponse = gson.fromJson(responseData, RegisterResponse.class);
            return registerResponse;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //login user
    public static LoginResponse login(LoginRequest loginRequest) {

        try {
            URL url = new URL("http://" + DataCache.getServerHost() + ":" +
                    DataCache.getServerPort() + "/user/login");

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.connect();

            Gson gson = new Gson();
            String requestData = gson.toJson(loginRequest);
            OutputStream requestBody = http.getOutputStream();
            writeString(requestData, requestBody);

            requestBody.close();

            if(http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("Login Successful");
            }

            InputStream responseBody = http.getInputStream();
            String responseData = readString(responseBody);

            LoginResponse loginResponse = gson.fromJson(responseData, LoginResponse.class);
            return loginResponse;

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

        //get people
    public static PersonResponse getPeopleForUser(AuthToken authToken) {




        return null;
    }

    //get events
    public static EventResponse getEventsForUser(AuthToken authToken) {
        return null;
    }

    //fill
    //load
    //clear
    //getPersonByID
    //getEventById

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

}
