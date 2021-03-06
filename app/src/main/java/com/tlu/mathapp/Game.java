package com.tlu.mathapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import androidx.appcompat.app.AppCompatActivity;


public class Game extends AppCompatActivity {
    private int gameLevel = 3; // See, mitmendast levelist alustad, hetkel LVL 3. Kui on 3, on kaks arvu, kui on 5 - 3 arvu, 7 - 4 arvu.
    private String calAnswer; // Right answer
    private int wrongAnswers;
    private int currentCorrect;
    private int score;
    private long timeLeft;  // currently not needed
    private long timeDelay = 250;
    //Creates a timer
    final CountDownTimer timer = new CountDownTimer(6000, 1000) {

        //Calls every tick
        public void onTick(long msUntilFinished) {
            timeLeft = msUntilFinished / 1000;
            setTimeLeft(Long.toString(timeLeft));
        }
        //Calls on finish
        public void onFinish() {
            newRandom();
            updateLives();
            setStatusImage("out_of_time");
        }
    };
    // Initialize Class
    private SoundPlayer sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wrongAnswers = 0;
        currentCorrect = 0;
        timer.cancel(); // Security measure for timer component
        final Button genBtn1, genBtn2, genBtn3;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Sätestab layouti

        sound = new SoundPlayer(this); // Toob playeri sisse
        newRandom(); // Alguses lisab juba ühe värgi

        genBtn1 = findViewById(R.id.answer1);
        genBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(1);
                bounceAnimation(genBtn1);
            }
        });

        genBtn2 = findViewById(R.id.answer2);
        genBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(2);
                bounceAnimation(genBtn2);
            }
        });


        genBtn3 = findViewById(R.id.answer3);
        genBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(3);
                bounceAnimation(genBtn3);
            }
        });

    }


    //We have to control user input on back button pressed
    @Override
    public void onBackPressed() {
        timer.cancel(); // cancel timer, which cancels the game
        Intent intent = new Intent(this, LandingPageActivity.class);
        startActivity(intent);
    }

    private void checkAnswer(int btnId){
        String guessedAnswer = getButtonText(btnId);
        if(calAnswer.equals(guessedAnswer)){
            currentCorrect++;
            sound.playCorrectSound(); // Mängib õigesti vastanud heli
            int nextLevelQuota = 4;
            if(currentCorrect >= nextLevelQuota){
                currentCorrect = 0;
                setStatusImage("level_up");
                calculateScore();
                gameLevel += 2;
                newRandom();
            } else {
                newRandom();
                setStatusImage("thumb_up");
                calculateScore();
            }

        } else {
            updateLives();
            sound.playWrongSound(); // Mängib valesti läinud heli
            setStatusImage("thumb_down");
        }
    }

    //We only calculate score on correct answers
    private void calculateScore(){
        score += (gameLevel * 10) + timeLeft;
        setScoreText(Integer.toString(score));
    }

    private void updateLives(){
        wrongAnswers += 1;
        int lives = 5 - wrongAnswers;
        setLivesText(lives +"/5");
        //if user has input 5 wrong answers, redirect to game over page
        if(wrongAnswers >= 5){
            timer.cancel();
            Intent intent = new Intent(this, GameOver.class);
            sound.playOverSound(); // Game over heli mängu lõpus
            intent.putExtra("SCORE", this.score + "");
            startActivity(intent);
        }else {
            newRandom();
        }
    }
    private void newRandom() { // funktsioon, mis käivitub pärast vajutamist
        String[] calculation = this.returnArray(gameLevel); // Tekitab array gamelevel kohaga. See vajalik pärast uute tasemete lisamiseks. Gamelevel on 3, siis tagastatakse kolmekohaline array
        //double calAnswer = this.getCorrectAnswer(calculation); // Arvutab välja õige väärtuse
        StringBuilder sum = new StringBuilder(); // Siia liidetakse stringidena arvud ja märgid
        for (String calc: calculation) {
            sum.append(calc);
        }
        calAnswer = getCorrectAnswer(sum.toString());
        String calAnswerWrong1 = getWrongAnswer(calAnswer); // Arvutab randomilt õigele vastusele midagi juurde või maha, et tulemused oleks suht samas kandis, et oleks raskem pakkuda huupi
        String calAnswerWrong2 = getWrongAnswer(calAnswer);
        boolean done = false; // while tsükkli jaoks
        while (!done) {
            if((calAnswerWrong1.equals(calAnswerWrong2)) || (calAnswerWrong1.equals(calAnswer)) || (calAnswerWrong2.equals(calAnswer))){ // Tingimus, et ükski arv ei ühtiks üksteisega, tuleks võibolla lisada veel infinity jne ka.
                calAnswerWrong1 = getWrongAnswer(calAnswer);
                calAnswerWrong2 = getWrongAnswer(calAnswer);
            } else {
                done = true;
            }
        }
        setCalcText(sum.toString());
        String [] shuffledAnswers = new String[] {calAnswer, calAnswerWrong1, calAnswerWrong2 };
        Collections.shuffle(Arrays.asList(shuffledAnswers));
        setButtonsText(shuffledAnswers[0], shuffledAnswers[1], shuffledAnswers[2]);
        timer.cancel();
        timer.start();
        animateProgressBar();
    }

    private int randomNumber(double max)
    {
        double rand = (int)(Math.random()*((max-1)+1))+1;
        return (int)rand;
    }
    private String[] returnArray(int nr){
        String[] arr = new String[nr];
        for (int i = 0; i < nr; i++) {
            if((i % 2) == 0){ // paarisarv kohtadele lisan mingi arvu vahemikus 1-10
                arr[i] = String.valueOf(randomNumber(10)); //suvalise arvu random
            } else { // paarituarvu kohale arrays lisan tehtemärgi numbri(1 - + , 2 - - , 3 - * )
                int rand = randomNumber(3);//tehtemärgi random
                if(rand == 1){arr[i] = "+";}
                else if(rand == 2){arr[i] = "-";}
                else if(rand == 3){arr[i] = "*";}
            }
        }
        return arr;
    }
    private String getCorrectAnswer(String ex) {
        Object result = new Object();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino"); //Vajalik, et saada eval funktsioon Javascriptist kätte
        try {
            result = engine.eval(ex);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return String.format("%.0f", result); //Tagastab ilma komakohata väärtuse
    }
    private String getWrongAnswer(String correctAnswer) {
        double sum = 0;
        int random = this.randomNumber(3);
        if(random == 1){sum = Double.parseDouble(correctAnswer) + randomNumber(10);}
        else if(random == 2){sum = Double.parseDouble(correctAnswer) - randomNumber(10);}
        else if(random == 3){sum = Double.parseDouble(correctAnswer) * randomNumber(10);}
        return String.valueOf((int)sum); // Tagastab ilma komakohata väärtuse
    }

    //Getters & Setters
    private void setCalcText(final String s){
        final TextView calc = findViewById(R.id.calc);
        slideAnimation(calc);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                calc.setText(s);
            }
        }, timeDelay);
    }

    private void setLivesText(String s){
        final TextView timerDisplay = findViewById(R.id.lives);
        timerDisplay.setText(s);
    }

    private void setTimeLeft(String s){
        final TextView timerDisplay = findViewById(R.id.timeNumber);
        timerDisplay.setText(s);
    }

    private void animateProgressBar(){
        final ProgressBar pb = findViewById(R.id.determinateBar);
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", 6000, 0);
        animation.setDuration(7000);
        animation.setInterpolator(new DecelerateInterpolator()); // animates towards 0
        animation.start();
    }

    private void setScoreText(String s){
        final TextView timerDisplay = findViewById(R.id.score);
        timerDisplay.setText(s);
    }

    private void setStatusImage(String s){
        final ImageView statusMessage = findViewById(R.id.statusMessage);
        statusMessage.setVisibility(View.VISIBLE);
        switch (s){
            case "thumb_up": statusMessage.setImageResource(R.drawable.thumb_up); break;
            case "level_up": statusMessage.setImageResource(R.drawable.level_up); break;
            case "out_of_time": statusMessage.setImageResource(R.drawable.out_of_time); break;
            default: statusMessage.setImageResource(R.drawable.thumb_down);
        }
        //Show the status text only for 1 second
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                statusMessage.setVisibility(View.INVISIBLE);
            }
        }, 1000);
    }
    private void setButtonsText(String button1, String button2, String button3){
        final TextView answer1 = findViewById(R.id.answer1);
        answer1.setText(button1);
        final TextView answer2 = findViewById(R.id.answer2);
        answer2.setText(button2);
        final TextView answer3 = findViewById(R.id.answer3);
        answer3.setText(button3);
    }
    private String getButtonText(int id){
        String txt = "";
        if(id == 1) {
            final TextView answer1 = findViewById(R.id.answer1);
            txt = (String) answer1.getText();
        } else if(id == 2){
            final TextView answer2 = findViewById(R.id.answer2);
            txt = (String) answer2.getText();
        } else if(id == 3){
            final TextView answer3 = findViewById(R.id.answer3);
            txt = (String) answer3.getText();
        }
        return txt;
    }

    // Button animation
    public void bounceAnimation(Button btn) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        btn.startAnimation(myAnim);
    }
    // Slide animation
    public void slideAnimation(final View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.slide);
        final Animation myAnim2 = AnimationUtils.loadAnimation(this, R.anim.slide2);
        view.startAnimation(myAnim2);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                view.startAnimation(myAnim);
            }
        }, timeDelay);
    }
}
