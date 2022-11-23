package byu.edu.isaacrh.familymapclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import model.Person;

public class PersonActivity extends AppCompatActivity {

    public static final String CURR_PERSON_KEY = "CurrPersonKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Intent intent = getIntent();
        Person currPerson = DataCache.getPersonById(intent.getStringExtra(CURR_PERSON_KEY));
        Toast.makeText(this, currPerson.getFirstName(), Toast.LENGTH_SHORT).show();

    }
}