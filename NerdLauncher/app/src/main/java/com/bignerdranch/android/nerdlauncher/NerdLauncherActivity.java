package com.bignerdranch.android.nerdlauncher;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//Extending to single fragment activity makes it easier to create a fragment.
//Check SingleFragmentActivity.java for the code where you set up fragment manager to help launch fragments
public class NerdLauncherActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return NerdLauncherFragment.newInstance();
    }

}
