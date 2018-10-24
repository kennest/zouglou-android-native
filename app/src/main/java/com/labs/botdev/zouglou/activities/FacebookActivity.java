package com.labs.botdev.zouglou.activities;

import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.models.Artist;
import com.labs.botdev.zouglou.models.Event;
import com.labs.botdev.zouglou.utils.AppController;
import com.labs.botdev.zouglou.utils.Constants;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FacebookActivity extends AppCompatActivity {

    private static final int ACTION_PICK_IMAGE = 1;

    private CallbackManager callbackManager;
    private LoginManager manager;
    private ImageView imgTest;
    private EditText edtURL;
    private EditText edtSaySmth;
    private Bitmap thePic;
    List<Event> events=new ArrayList<>();
    Event e=new Event();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        Intent intent = getIntent();
        int id = intent.getIntExtra("event_id", 0);

        events = Stash.getArrayList("events", Event.class);
        for (Event n : events) {
            if (n.getId() == id) {
                e = n;
            }
        }


        List<String> permissionNeeds = Arrays.asList("publish_actions");

        manager = LoginManager.getInstance();
        manager.logOut();
        manager.logInWithPublishPermissions(this, permissionNeeds);
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(FacebookActivity.this, "Facebook login success!", Toast.LENGTH_SHORT).show();
                updateStatusClick();
            }

            @Override
            public void onCancel() {
                Toast.makeText(FacebookActivity.this, "Facebook login canceled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(FacebookActivity.this, "Facebook login error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateStatusClick() {
        String artist_str = "";
        for (Artist a : e.artists) {
            if (artist_str.equals("")) {
                artist_str = a.getName();
            } else {
                artist_str = artist_str + "," + a.getName();
            }
        }

        String pic_url = AppController.getInstance().downloadedPicture(Constants.UPLOAD_URL + e.getPicture());
        String geoUri = "http://maps.google.com/maps?q=loc:" + e.place.address.getLatitude() + "," +
                e.place.address.getLongitude();
        String text = new StringBuilder()
                .append("\n" + e.getTitle().toUpperCase())
                .append("\n" + "ARTISTES INVITES:" + artist_str)
                .append("\n" + "Voir sur Google Maps:\n" + geoUri)
                .toString();

        Bitmap image = BitmapFactory.decodeFile(pic_url);

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setCaption(text)
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, null);
        Toast.makeText(FacebookActivity.this, "Picture posted with success on Facebook!", Toast.LENGTH_SHORT).show();
    }

    public void loadImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        startActivityForResult(photoPickerIntent, ACTION_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        if (requestCode == ACTION_PICK_IMAGE) {
            if (responseCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    thePic = getBitmapFromUri(selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgTest.setImageBitmap(thePic);
            }
        }
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

}


