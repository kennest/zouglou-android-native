package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fxn.stash.Stash;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.models.User;

import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;
    private AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        initFacebook();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                mAccessToken = loginResult.getAccessToken();
                //Toast.makeText(getApplicationContext(), "Token: " + mAccessToken.getToken(), Toast.LENGTH_LONG).show();
                getUserProfile(mAccessToken);
                finish();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(getApplicationContext(), "FB Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FB Login Error: ",exception.getMessage());
            }

        });
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                (object, response) -> {
                    try {
                        User user=new User();
                        user.setEmail(object.getString("email"));
                        user.setId(object.getString("id"));
                        user.setName(object.getString("name"));
                        user.setPicture(object.getJSONObject("picture").getJSONObject("data").getString("url"));
                        user.setToken(currentAccessToken.getToken());
                        Stash.put("facebook_user",user);
                        Toast.makeText(getApplicationContext(), object.getString("name"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(80)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void Logout() {
        LoginManager.getInstance().logOut();
    }
}
