package com.tlu.mathapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class LandingPageActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Button gameBtn, resBtn;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        gameBtn = (Button) findViewById(R.id.play_button);
        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
                bounceAnimation(v, gameBtn);
            }
        });

        resBtn = (Button) findViewById(R.id.results_button);
        resBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                results();
                bounceAnimation(v, resBtn);
            }
        });
    }
    // Start game
    public void startGame() {
        Intent intent = new Intent(this, Game.class);
        startActivity(intent);
    }

    // Results page
    public void results() {
        Intent intent = new Intent(this, ResultsTable.class);
        startActivity(intent);
    }

    // Button animation
    public void bounceAnimation(View view, Button btn) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.1, 20);
        myAnim.setInterpolator(interpolator);
        btn.startAnimation(myAnim);
    }
}
