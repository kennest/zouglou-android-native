package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class OnboardActivity extends AhoyOnboarderActivity {
    //private FacebookLogin facebookLogin;

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkPermissions();
        }
        if (Stash.getObject("facebook_user", User.class) == null) {
            facebookLogin();
        }

        AhoyOnboarderCard navigationCard = new AhoyOnboarderCard(getString(R.string.onboard2_title), getString(R.string.onboard2_description), R.drawable.navigation);
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

    private void getKeyHash(){
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.labs.botdev.zouglou",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

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
        finish();
    }

    protected void checkOnboardPassed() {
        boolean passed = Stash.getBoolean("onboardpassed");
        if (passed) {
            Intent map = new Intent(this, MapActivity.class);
            startActivity(map);
            finish();
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
                if (report.areAllPermissionsGranted()) {
                    checkOnboardPassed();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }
}
