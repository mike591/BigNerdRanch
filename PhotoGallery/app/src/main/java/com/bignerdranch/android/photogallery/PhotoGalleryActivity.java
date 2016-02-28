package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }

//Will display results using recyclerView. First we have to add recycler view as a dependency.
    @Override
    protected Fragment createFragment() {
        //The Activity returns a new fragment. We need to create the fragment class and its newInstance() method.
        return PhotoGalleryFragment.newInstance();
    }

}
