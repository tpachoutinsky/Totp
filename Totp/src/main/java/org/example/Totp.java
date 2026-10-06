package org.example;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import java.util.Timer;
import java.util.TimerTask;

public class Totp {
    String clefTotp = "1";
    GoogleAuthenticator gAuth = new GoogleAuthenticator();
    Timer timer;
    TimerTask timerTask;
    int codeTOTP = 0;

    public void generateTotp(){
        timer = new Timer();
        timerTask = new TimerTask(){
            @Override
            public void run() {
                codeTOTP = gAuth.getTotpPassword(clefTotp);

            }
        };
        timer.scheduleAtFixedRate(timerTask,0,1000);
    }
    public void arreterTOTP(){
        timer.cancel();
    }
    public boolean testCodeTOTP(int code){
        return codeTOTP == code;
    }
}
