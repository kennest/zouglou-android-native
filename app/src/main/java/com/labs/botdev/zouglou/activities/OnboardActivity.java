package com.labs.botdev.zouglou.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.appizona.yehiahd.fastsave.FastSave;
import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.labs.botdev.zouglou.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OnboardActivity extends AhoyOnboarderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //We check if the has already passed the on board screen
        checkOnboardPassed();

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
        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnboardPassed();
    }

    @Override
    public void onFinishButtonPressed() {
        FastSave.getInstance().saveBoolean("onboardpassed", true);
        Intent map = new Intent(this, MapActivity.class);
        startActivity(map);
    }

    protected void checkOnboardPassed() {
        boolean passed=FastSave.getInstance().getBoolean("onboardpassed");
        if(passed){
            Intent map = new Intent(this, MapActivity.class);
            startActivity(map);
        }
    }

    protected void checkTermsAgreed(){

    }
}
