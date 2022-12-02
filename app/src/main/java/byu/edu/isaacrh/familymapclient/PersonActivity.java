package byu.edu.isaacrh.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    public static final String CURR_PERSON_KEY = "CurrPersonKey";
    private static final int EVENT_GROUP_POSITION = 0;
    private static final int PERSON_GROUP_POSITION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Intent intent = getIntent();
        Person currPerson = DataCache.getPersonById(intent.getStringExtra(CURR_PERSON_KEY));

        updateView(currPerson);

    }

    public void updateView(Person currPerson) {

        TextView personName = findViewById(R.id.personActivityName);
        String personNameString = currPerson.getFirstName() + " " + currPerson.getLastName();
        personName.setText(personNameString);

        TextView personGender = findViewById(R.id.personActivityGender);
        String personGenderString= "";
        if(currPerson.getGender().compareTo("m") == 0) {
            personGenderString = "Male";
        }
        else {
            personGenderString = "Female";
        }
        personGender.setText(personGenderString);


        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        List<Person> currPersonFamily = DataCache.getPersonFamilyMembers(currPerson);
        List<Event> currPersonEvents = DataCache.getCurrentEventsDisplay().get(currPerson);
        if(currPersonEvents == null) {
            currPersonEvents = new ArrayList<>();
        }

        expandableListView.setAdapter(new ExpandableListAdapter(currPersonFamily, currPersonEvents));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private List<Person> currPersonFamily;
        private List<Event> currPersonEvents;

        public ExpandableListAdapter(List<Person> currPersonFamily, List<Event> currPersonEvents) {
            this.currPersonFamily = currPersonFamily;
            this.currPersonEvents = currPersonEvents;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    return currPersonEvents.size();
                case PERSON_GROUP_POSITION:
                    return currPersonFamily.size();
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    return R.string.eventGroupTitle;
                case PERSON_GROUP_POSITION:
                    return R.string.personGroupTitle;
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    return currPersonEvents.get(childPosition);
                case PERSON_GROUP_POSITION:
                    return currPersonFamily.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.eventGroupTitle);
                    break;
                case PERSON_GROUP_POSITION:
                    titleView.setText(R.string.personGroupTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);
            }

            return convertView;

        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case PERSON_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized Group Position: " + groupPosition);

            }

            return itemView;
        }

        public void initializeEventView(View eventItemView, final int childPosition) {

            Event currEvent = currPersonEvents.get(childPosition);
            Person associatedPerson = DataCache.getPersonById(currEvent.getPersonID());

            TextView eventDetails = eventItemView.findViewById(R.id.expandableListEventDetails);
            String eventDetailString = currEvent.getEventType() + ": " + currEvent.getCity() + ", " +
                    currEvent.getCountry() + " (" + currEvent.getYear() + ")";
            eventDetails.setText(eventDetailString);

            TextView personName = eventItemView.findViewById(R.id.expandableListEventAssociatedPerson);
            String eventNameString = associatedPerson.getFirstName() + " " + associatedPerson.getLastName();
            personName.setText(eventNameString);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra(EventActivity.CURR_EVENT_KEY, currEvent.getEventID());
                    startActivity(intent);
                }
            });
        }

        public void initializePersonView(View personItemView, final int childPosition) {

            Person associatedPerson = currPersonFamily.get(childPosition);
            if(associatedPerson == null) {
                return;
            }

            ImageView eventGender = personItemView.findViewById(R.id.expandableListPersonGender);
            if(associatedPerson.getGender().compareTo("m") == 0) {
                eventGender.setImageResource(R.drawable.baseline_man_black_24);
            }
            else {
                eventGender.setImageResource(R.drawable.baseline_woman_black_24);
            }

            TextView personName = personItemView.findViewById(R.id.expandableListPersonName);
            String personNameString = associatedPerson.getFirstName() + " " +
                    associatedPerson.getLastName();
            personName.setText(personNameString);

            TextView personRelationship = personItemView.
                    findViewById(R.id.expandableListPersonRelationship);
            if(childPosition == 0) {
                personRelationship.setText(R.string.father);
            }
            else if (childPosition == 1) {
                personRelationship.setText(R.string.mother);
            }
            else if (childPosition == 2) {
                personRelationship.setText(R.string.spouse);
            }
            else {
                personRelationship.setText(R.string.child);
            }

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Person newPerson = currPersonFamily.get(childPosition);
                    updateView(newPerson);
                }
            });
        }


            @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}