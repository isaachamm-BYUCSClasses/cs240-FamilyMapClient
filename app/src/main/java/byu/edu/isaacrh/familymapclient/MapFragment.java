package byu.edu.isaacrh.familymapclient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import model.Event;
import model.Person;

public class MapFragment extends Fragment  implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private TextView eventInfo;
    private ImageView genderImage;
    private boolean eventIsSelected = false; // we use this to know if the person activity can be started
    private String currAssociatedPersonId;
    private Event currEvent;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        eventInfo = view.findViewById(R.id.mapEventInfo);
        genderImage = view.findViewById(R.id.mapGenderImage);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getArguments() == null) {
            // We're displaying menu options inside of a fragment, so we need this
            setHasOptionsMenu(true);
        }

        LinearLayout linearLayout = view.findViewById(R.id.mapLinearLayout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(eventIsSelected) {
                    Intent intent = new Intent(getActivity(), PersonActivity.class);
                    intent.putExtra(PersonActivity.CURR_PERSON_KEY, currAssociatedPersonId);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getActivity(), R.string.eventNotSelected, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_activity_map_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intent;
        switch(item.getItemId()) {
            case R.id.search_button:
                intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                return true;

            case R.id.settings_button:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawMarkers();

    }

    public void drawMarkers() {

        mMap.clear();


        float permColorCode = 0.0F;
        float tempColorCode = permColorCode;

        for(Map.Entry<String, Event> entry : DataCache.getEvents().entrySet()) {
            if(DataCache.getColorMap() == null) {
                DataCache.addEventColor(entry.getValue().getEventType(), permColorCode);
            }
            else if (DataCache.getColorMap().get(entry.getValue().getEventType()) != null) {
                tempColorCode = DataCache.getColorMap().get(entry.getValue().getEventType());
            }
            else {
                permColorCode += 30;
                if(permColorCode > 360) {
                    permColorCode %= 30;
                    permColorCode += 3;
                }
                tempColorCode = permColorCode;
                DataCache.addEventColor(entry.getValue().getEventType(), permColorCode);
            }

            Marker marker = mMap.addMarker(new MarkerOptions().
                    position(new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude())).
                    icon(BitmapDescriptorFactory.defaultMarker(tempColorCode)));
            marker.setTag(entry.getValue());
        }

        mMap.setOnMarkerClickListener(this);

        if(getArguments() != null) {
            String currEventId = getArguments().getString(EventActivity.CURR_EVENT_KEY);
            Event currEvent = DataCache.getEventById(currEventId);
            setMapOnEvent(currEvent);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        Event clickedEvent = (Event) marker.getTag();

        this.currEvent = clickedEvent;

        assert clickedEvent != null;
        setMapOnEvent(clickedEvent);

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mMap != null) {
            drawMarkers();
        }

        boolean displayCurrEvent = false;

        if(this.currEvent != null) {
            for(Map.Entry<Person, List<Event>> entry : DataCache.getCurrentEventsDisplay().entrySet()) {
                if(entry.getValue() != null) {
                    for(Event event : entry.getValue()) {
                        if(this.currEvent.getEventID().compareTo(event.getEventID()) == 0) {
                            displayCurrEvent = true;
                        }
                    }
                }
            }

            if(displayCurrEvent == true) {
                setMapOnEvent(currEvent);
            } else {
                genderImage.setImageResource(R.drawable.baseline_android_black_24);
                eventInfo.setText(R.string.mapFragmentClickMessage);
            }
        }
    }

    public void setMapOnEvent(Event clickedEvent) {

        eventIsSelected = true;

        Person associatedPerson = DataCache.getPersonById(clickedEvent.getPersonID());
        currAssociatedPersonId = associatedPerson.getPersonID();

        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(clickedEvent.getLatitude(),
                clickedEvent.getLongitude())));

        String eventInfoString = associatedPerson.getFirstName() + " " + associatedPerson.getLastName() +
                "\n" + clickedEvent.getEventType() + ": " + clickedEvent.getCity() + ", " +
                clickedEvent.getCountry() + " (" + clickedEvent.getYear() + ")";

        eventInfo.setText(eventInfoString);
        if(associatedPerson.getGender().compareTo("m") == 0) {
            genderImage.setImageResource(R.drawable.baseline_man_black_24);
        }
        else {
            genderImage.setImageResource(R.drawable.baseline_woman_black_24);
        }

        if(DataCache.getPolylines() != null) {
            for (Polyline p : DataCache.getPolylines()) {
                p.remove();
            }
        }

        List<Polyline> polylineList = new ArrayList<>();
        // Code for the spouse line
        if(DataCache.isSpouseLines()) {
            if (DataCache.getPersonById(associatedPerson.getSpouseID()) != null) {
                Person associatedSpouse = DataCache.getPersonById(associatedPerson.getSpouseID());
                if(Objects.requireNonNull(DataCache.getCurrentEventsDisplay().get(associatedSpouse) != null)) {
                    Event spouseBirthday = DataCache.getCurrentEventsDisplay().get(associatedSpouse).get(0);

                    LatLng startPoint = new LatLng(clickedEvent.getLatitude(), clickedEvent.getLongitude());
                    LatLng endPoint = new LatLng(spouseBirthday.getLatitude(), spouseBirthday.getLongitude());

                    PolylineOptions options = new PolylineOptions()
                            .add(startPoint)
                            .add(endPoint)
                            .color(Color.BLUE)
                            .geodesic(true)
                            .width(20);
                    Polyline polyline = mMap.addPolyline(options);
                    polylineList.add(polyline);
                }
            }
        }

        // Code for the family tree line
        if(DataCache.isFamilyTreeLines()) {
            familyTreeLineRecurse(associatedPerson, clickedEvent, 20, polylineList);
        }

        // Code for the life story line
        if(DataCache.isLifeStorylines()) {
            if(DataCache.getCurrentEventsDisplay().get(associatedPerson) != null) {
                Event birthEvent = DataCache.getCurrentEventsDisplay().get(associatedPerson).get(0);
                lifeStoryLineRecurse(associatedPerson, birthEvent, 0, polylineList);
            }
        }

        DataCache.setPolylines(polylineList);
    }

    public void familyTreeLineRecurse(Person person, Event event, int width, List<Polyline> polylineList) {
        if(DataCache.getPersonById(person.getFatherID()) != null) {
            Person father = DataCache.getPersonById(person.getFatherID());
            Person mother = DataCache.getPersonById(person.getMotherID());
            Event fatherBirth = null;
            Event motherBirth = null;

            if (DataCache.getCurrentEventsDisplay().get(father) != null) {
                fatherBirth = DataCache.getCurrentEventsDisplay().get(father).get(0);
                familyTreeLineRecurse(father, fatherBirth, width - 5, polylineList);
                LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
                LatLng endPoint = new LatLng(fatherBirth.getLatitude(), fatherBirth.getLongitude());
                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(endPoint)
                        .color(Color.BLACK)
                        .geodesic(true)
                        .width(width);
                Polyline polyline = mMap.addPolyline(options);
                polylineList.add(polyline);
            }
            if (DataCache.getCurrentEventsDisplay().get(mother) != null) {
                motherBirth = DataCache.getCurrentEventsDisplay().get(mother).get(0);
                familyTreeLineRecurse(mother, motherBirth, width - 5, polylineList);
                LatLng startPoint = new LatLng(event.getLatitude(), event.getLongitude());
                LatLng endPoint = new LatLng(motherBirth.getLatitude(), motherBirth.getLongitude());
                PolylineOptions options = new PolylineOptions()
                        .add(startPoint)
                        .add(endPoint)
                        .color(Color.BLACK)
                        .geodesic(true)
                        .width(width);
                Polyline polyline = mMap.addPolyline(options);
                polylineList.add(polyline);
            }

        }
    }
    public void lifeStoryLineRecurse(Person person, Event prevEvent, int currIndex, List<Polyline> polylineList) {
        if((DataCache.getCurrentEventsDisplay().get(person) != null) &&
            ((currIndex + 1) < DataCache.getCurrentEventsDisplay().get(person).size())) {
            currIndex += 1;

            Event nextEvent = DataCache.getCurrentEventsDisplay().get(person).get(currIndex);

            LatLng startPoint = new LatLng(prevEvent.getLatitude(), prevEvent.getLongitude());
            LatLng endPoint = new LatLng(nextEvent.getLatitude(), nextEvent.getLongitude());

            PolylineOptions options = new PolylineOptions()
                    .add(startPoint)
                    .add(endPoint)
                    .color(Color.RED)
                    .geodesic(true)
                    .width(20);
            Polyline polyline = mMap.addPolyline(options);
            polylineList.add(polyline);

            lifeStoryLineRecurse(person, nextEvent, currIndex, polylineList);
        }
    }
}