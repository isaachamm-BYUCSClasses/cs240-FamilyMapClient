package byu.edu.isaacrh.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch lifeStoryLines = findViewById(R.id.lifeStoryLineSwitch);
        Switch familyTreeLines = findViewById(R.id.familyTreeLineSwitch);
        Switch spouseLines = findViewById(R.id.spouseLineSwitch);
        Switch fatherSide = findViewById(R.id.fatherSideSwitch);
        Switch motherSide = findViewById(R.id.motherSideSwitch);
        Switch maleEvents = findViewById(R.id.maleEventSwitch);
        Switch femaleEvents = findViewById(R.id.femaleEventSwitch);
        LinearLayout logout = findViewById(R.id.logout);



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
}