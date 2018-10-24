package com.labs.botdev.zouglou.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.fxn.stash.Stash;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.gson.JsonObject;
import com.jetradarmobile.rxlocationsettings.RxLocationSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.models.Artist;
import com.labs.botdev.zouglou.models.Customer;
import com.labs.botdev.zouglou.models.Place;
import com.labs.botdev.zouglou.services.APIClient;
import com.labs.botdev.zouglou.services.APIService;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.functions.Action1;

public class OnboardActivity extends AhoyOnboarderActivity {
    //private FacebookLogin facebookLogin;
    final APIService service = APIClient.getClient().create(APIService.class);
    MediaPlayer mp;
    Set<String> user_places = new HashSet<>();
    Set<String> user_artists = new HashSet<>();

    @SuppressLint("LogNotTimber")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            checkPermissions();
        }
        if (Stash.getObject("facebook_user", Customer.class) == null) {
            facebookLogin();
        }

        getKeyHash();

        AhoyOnboarderCard navigationCard = new AhoyOnboarderCard(getString(R.string.onboard2_title), getString(R.string.onboard2_description), R.mipmap.navigation_feature_round);
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
            Intent splash = new Intent(this, SplashScreenActivity.class);
            startActivity(splash);
            finish();
        }
    }

    private void facebookLogin() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivityForResult(login,200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==201 && requestCode==200){
            Customer c= (Customer) Stash.getObject("facebook_user",Customer.class);
            Log.e("Customer FB_ID",c.getFb_id());
            addCustomer(c);
            Stash.put("downloadedmap",false);
        }
    }

    private void addCustomer(Customer c){

            Call<JsonObject> call = service.addCustomer(c);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        //Toast.makeText(getApplicationContext(), "Zouglou server success:" + response.body().toString(), Toast.LENGTH_LONG).show();
                        getCustomerData(c);
                    } else {
                        Toast.makeText(getApplicationContext(), "Zouglou server Error" + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Zouglou server Error" + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

    }

    private void getCustomerData(Customer c){
    Log.e("Customer fb_id",c.getFb_id());
        Call<Customer> call= service.getCustomerInfo(c.getFb_id());
        call.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if(response.isSuccessful()) {
                    Customer c = response.body();
                    Stash.put("facebook_user", c);
                    setCustomerFavorite(c);
                    playSound("store.mp3");
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {

            }
        });
    }

    private void setCustomerFavorite(Customer c){
        if(c.places!=null) {
            if (c.places.size() > 0) {
                for (Place p : c.places) {
                    user_places.add(String.valueOf(p.getId()));
                }
                Stash.put("user_places", user_places);
            }
        }

        if(c.artists!=null) {
            if (c.artists.size() > 0) {
                for (Artist a : c.artists) {
                    user_artists.add(String.valueOf(a.getId()));
                }
                Stash.put("user_artists", user_artists);
            }
        }
    }

    private void playSound(String fileName) {
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getApplicationContext().getAssets().openFd(fileName);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
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
