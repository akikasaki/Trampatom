package com.trampatom.game.trampatom.activity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.trampatom.game.trampatom.R;
import com.trampatom.game.trampatom.canvas.Canvas1;
import com.trampatom.game.trampatom.canvas.GameOver;
import com.trampatom.game.trampatom.utils.GameTimeAndScore;
import com.trampatom.game.trampatom.utils.RandomCoordinate;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Game1 extends AppCompatActivity implements Runnable, View.OnTouchListener{
    //Duration of the game
    private static final long GAME_TIME = 10000;

    //Classes
        GameTimeAndScore gameTimeAndScore;
        RandomCoordinate randomCoordinate;
        Canvas1 canvas;
    //For the SurfaceView to work
        SurfaceHolder ourHolder;
        SurfaceView mSurfaceView;
        Canvas mCanvas;
        Thread ourThread = null;
        boolean isRunning=true;
    //Balls and Background Bitmaps
        Bitmap possitiveBall, negativeBall, background;
    //Other variables
        TextView tvScore, tvTime;
        int deviceWidth, surfaceViewHeight;
        static int ballWidth, ballHeight;
    //Used for scoring
        int ballType=4;
        int previous=4;
        int score;
    //coordinates of the currently drawn ball, coordinates where we clicked
        int x, clickedX;
        int y, clickedY;
    //used for drawing the first ball
        boolean initialDraw;
    //used for drawing a new ball whenever we scored
        boolean scored;
    //used for ending the game when the time ends
        boolean gameover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game);
        init();
        new CountDownTimer(GAME_TIME, 250) {
            public void onTick(long millisUntilFinished) {
                gameTimeAndScore.setTimeAndScore(millisUntilFinished, score);
            }
            public void onFinish() {
                //finish the game when the timer ends
                gameover= true;
            }
        }.start();

    }

    private void init() {

        //set up the SurfaceView
            mSurfaceView = (SurfaceView) findViewById(R.id.SV1);
            ourHolder = mSurfaceView.getHolder();
            mSurfaceView.setOnTouchListener(this);
        //Current score and remaining time
            tvScore=(TextView) findViewById(R.id.tvScore);
            tvTime = (TextView) findViewById(R.id.tvTime);
            gameTimeAndScore = new GameTimeAndScore(tvScore, tvTime);
        //get device's width and height
            deviceWidth= MainActivity.getWidth();
            surfaceViewHeight = MainActivity.getHeight();
        //Bitmaps
            possitiveBall= BitmapFactory.decodeResource(getResources(),R.drawable.atomplava);
            negativeBall = BitmapFactory.decodeResource(getResources(),R.drawable.atomcrvena);
            //background = BitmapFactory.decodeResource(getResources(),R.drawable.atompozadina);
            //background = Bitmap.createScaledBitmap(background, deviceWidth, deviceHeight, true);
        //ball Height and Width
            ballHeight=possitiveBall.getHeight();
            ballWidth=possitiveBall.getWidth();
        //Initial coordinates for the ball
            randomCoordinate = new RandomCoordinate();
            x=randomCoordinate.randomX(ballWidth);
            y=randomCoordinate.randomY(ballHeight, surfaceViewHeight);

        initialDraw=true;
        scored=false;
    }

    @Override
    public void run() {
        canvas = new Canvas1(ourHolder,mCanvas);
        while (isRunning) {
            //Do until you get the surface
            if (!ourHolder.getSurface().isValid())
                continue;

            //The initial draw
            if(initialDraw) {
            initialDraw= canvas.firstDraw(possitiveBall,x,y);
            }
            //draw a ball after the score changes
            if(scored && !gameover){
                if(ballType >2)
                    scored= canvas.redraw(possitiveBall, x, y);
                else {
                    scored = canvas.redraw(negativeBall, x, y);
                }
            }
            //after the timer runs out finish the game
            if (gameover) {
                GameOver gameover = new GameOver(ourHolder,mCanvas);
                gameover.gameOver(score);
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                finish();
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //get coordinates where we touched
            clickedX =(int) event.getX();
            clickedY =(int) event.getY();
            //if we click on the ball
            if( clickedX>x && clickedX<(x+ballWidth) && clickedY>y && clickedY<(y+ballHeight)){
                ballclick();
                score();
            }
            //if we click anywhere else
            else{
                outsideClick();
            }
        }
        return false;
    }

    /**
     * Draw a new ball after a click
     */
    private void ballclick() {
        //sets new coordinates
        x = randomCoordinate.randomX(ballWidth);
        y = randomCoordinate.randomY(ballHeight, surfaceViewHeight);

        //determines if the ball will be positive or negative
        Random number3= new Random();
        ballType= number3.nextInt(10);
    }
    /**
     * Add or substract score depending on the ballType
     */
    private void score() {
        //sets previous depending on the new ballType
        //and scores depending on the previous type
        if(ballType>2 && previous>2)
        {
            score +=100;
            previous=3;
        }
        else if(ballType>2 && previous<=2)
        {
            score -=100;
            previous=3;
        }
        else if(ballType<=2 && previous >2 )
        {
            score +=100;
            previous=1;
        }
        else if(ballType<=2 && previous <=2 )
        {
            score -=100;
            previous=1;
        }
        scored = true;
    }
    /**
     * Method called In case we clicked outside the ball
     */
    private void outsideClick() {
        if(previous<=2){
            ballclick();
            if(ballType<=2 )
            {

                score +=100;
                previous=1;

            }
            else if(ballType>2 )
            {

                score +=100;
                previous=5;
            }
            scored=true;
        }
    }





    public void pause()
    {
        isRunning=false;
        try{
            ourThread.join();//unistava thread
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
    public void resume()
    {
        isRunning=true;
        ourThread = new Thread(this);
        ourThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }
}
