package byu.edu.isaacrh.familymapclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {

    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.mainFragmentFrameLayout);
        if(fragment == null) {
            fragment = createLoginFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentFrameLayout, fragment)
                    .commit();
        }
        else {
            if(fragment instanceof LoginFragment) {
                ((LoginFragment) fragment).registerListener(this);
            }
        }


    }

    private Fragment createLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.registerListener(this);
        return loginFragment;
    }

    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new MapFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.mainFragmentFrameLayout, fragment)
                .commit();
    }
}