package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.fxn.stash.Stash;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.models.User;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnboardActivity extends AhoyOnboarderActivity {
    //private FacebookLogin facebookLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Stash.getObject("facebook_user", User.class)==null) {
            facebookLogin();
        }
        //We check if the has already passed the on board screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkPermissions();
        }
        Drawable d=null;
        try {
            d = Drawable.createFromStream(getAssets().open("navigation.png"), "navigation.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        AhoyOnboarderCard navigationCard = new AhoyOnboarderCard(getString(R.string.onboard2_title), getString(R.string.onboard2_description), d);
        navigationCard.setBackgroundColor(R.color.black_transparent);
        navigationCard.setTitleColor(R.color.white);
        navigationCard.setDescriptionColor(R.color.grey_200);
        navigationCard.setTitleTextSize(dpToPixels(10, this));
        navigationCard.setDescriptionTextSize(dpToPixels(12, this));
        navigationCard.setIconLayoutParams(250, 250, 50, 50, 50, 50);

        AhoyOnboarderCard lastCard = new AhoyOnboarderCard(getString(R.string.onboard3_title), getString(R.string.onboard3_description), R.drawable.success);
        lastCard.setBackgroundColor(R.color.black_transparent);
        lastCard.setTitleColor(R.color.white);
        lastCard.setDescriptionColor(R.color.grey_200);
        lastCard.setTitleTextSize(dpToPixels(10, this));
        lastCard.setDescriptionTextSize(dpToPixels(12, this));
        lastCard.setIconLayoutParams(250, 250, 50, 50, 50, 50);

        //add cards to List
        List<AhoyOnboarderCard> pages = new ArrayList<>();
        pages.add(navigationCard);
        pages.add(lastCard);
        setOnboardPages(pages);
        setImageBackground(R.drawable.img_background);
        setFinishButtonTitle(R.string.start);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkOnboardPassed();
    }

    @Override
    public void onFinishButtonPressed() {
        Stash.put("onboardpassed", true);
        Intent map = new Intent(this, MapActivity.class);
        startActivity(map);
    }

    protected void checkOnboardPassed() {
        boolean passed = Stash.getBoolean("onboardpassed");
        if (passed) {
            Intent map = new Intent(this, MapActivity.class);
            startActivity(map);
        }
    }

    private void facebookLogin() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
if(report.areAllPermissionsGranted()){
    checkOnboardPassed();
}
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }
}
