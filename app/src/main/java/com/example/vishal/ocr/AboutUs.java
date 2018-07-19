package com.example.vishal.ocr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class AboutUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
    }

    public void openFacebook(View view) {
        Intent facebookIntent = getOpenFacebookIntent(this);
        startActivity(facebookIntent);
    }

    public void openLinkedIn(View view) {
        try {
            String mLinkedInurlString = "vishal-tiwari-184189100";
            String url = "https://www.linkedin.com/in/" + mLinkedInurlString;
            Intent linkedInAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            linkedInAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(linkedInAppIntent);
        } catch(Exception exception){
            Toasty.error(this, "Error opening LinkedIn", Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent getOpenFacebookIntent(Context context) {
        try {
            context.getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://profile/100003656902351")); //Trys to make intent with FB's URI
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/vishal.tiwari.5203")); //catches and opens a url to the desired page
        }
    }

}
