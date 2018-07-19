package com.example.vishal.ocr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    File mainDirectory, pathTrainedData, pathConvertedDirectory, pathImageDirectory, fileResult;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    NavigationView navigationView;
    int REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_STORAGE = 1;
    TessBaseAPI tessBaseAPI;
    String hour, minute, date, month, year, sec;
    NotificationCompat.Builder notification;
    private static final int notificationID = 401;
    String result;
    TextToSpeech toSpeech;
    NotificationCompat.Builder notificationSpeech;
    String locationOfImageString = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OCR/OCR-Images/";
    String fileNameOfImage, fileNameOfDocument;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiation only
        navigationView = (NavigationView) findViewById(R.id.navigation);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //getting the dates and time

        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY) + "";
        minute = c.get(Calendar.MINUTE) + "";
        date = c.get(Calendar.DATE) + "";
        month = c.get(Calendar.MONTH) + "";
        year = c.get(Calendar.YEAR) + "";
        sec = c.get(Calendar.SECOND) + "";
        fileNameOfDocument = "Extracted-doc-" + year + month + date + "-" + hour + minute + sec + ".docx";
        fileNameOfImage = locationOfImageString +"Img-" + year + month + date + "-" + hour + minute + sec + ".PNG";
        //create folders
        createFolders();
        // for creating folders and coping assets
        copyTrainedData();

        //for custom toasts
        Toasty.Config.getInstance(
        ).setErrorColor(ContextCompat.getColor(getApplicationContext(), R.color.error))
                .setInfoColor(ContextCompat.getColor(getApplicationContext(), R.color.info))
                .setSuccessColor(ContextCompat.getColor(getApplicationContext(), R.color.success)).
                setWarningColor(ContextCompat.getColor(getApplicationContext(), R.color.warning)).
                apply();


        //handle the fragments of navigation drawers

        Toasty.success(getApplicationContext(), "For better results, use internet...", Toast.LENGTH_SHORT).show();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_container, new HomeFragment()).commit();
        getSupportActionBar().setTitle("OCR");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.home:
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.main_container, new HomeFragment());
                        fragmentTransaction.commit();
                        getSupportActionBar().setTitle("OCR");
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        break;


                    case R.id.filemanager:
                        //code for opening the default file manager
                        Intent intentFile = new Intent();
                        intentFile.setAction(Intent.ACTION_GET_CONTENT);
                        intentFile.setType("image/*");
                        startActivityForResult(intentFile, REQUEST_CODE);
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        break;


                    case R.id.help:
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.main_container, new HelpFragment());
                        fragmentTransaction.commit();
                        getSupportActionBar().setTitle("Help and Support");
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        Toasty.warning(getApplicationContext(), "Help and Support!", Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.feedback:
                        String email = "vishal.sammy065@gmail";
                        Intent intentEmail = new Intent(Intent.ACTION_VIEW);
                        Uri data = Uri.parse("mailto:" + email + ".com?subject=" + "Feedback OCR" + "&body=" + "Please share the Feedback with us and help us develop better: \n\n");
                        intentEmail.setData(data);
                        startActivity(intentEmail);
                        item.setChecked(true);
                        drawerLayout.closeDrawers();
                        // Toasty.success(getApplicationContext(), "Submitting the Feedback!", Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.exit:
                        // Toasty.error(getApplicationContext(), "Exitting: OCR!", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                }

                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handling the overflow menu click events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         /* if(item.getItemId() == R.id.settings){
            startActivity(new Intent(this, SettingsActivity.class));
        } */
        if (item.getItemId() == R.id.quit) {
            this.finishAffinity();
        }
        if (item.getItemId() == R.id.aboutus) {
            startActivity(new Intent(this, AboutUs.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //do nothing

                    } else {

                        Toasty.error(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    //this is the fucntionality of the share button
    public void shareButton(MenuItem item) {

        /* AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Email of recipient");
        alert.setMessage("Something");
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String email = input.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:"+email+".com?subject=" + "Shareing OCR" + "&body=" + "Hi, I'm sending you a download link to download this cool OCR application on  Android. Link: \n");
                intent.setData(data);
                startActivity(intent);
                Toasty.success(getApplicationContext(), "Shareing to :"+email,  Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    */

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hi, I'm using an OCR Application. This application lets you extract text from image files. Try the application here, https://www.amazon.com/dp/B07BBN2S6N/ref=apps_sf_sta";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        Toasty.success(getApplicationContext(), "Sharing OCR", Toast.LENGTH_SHORT).show();
    }

    //this is for reading image from file explorer in navigation drawer and pass image bitmap to the opticalCharacterRecognition(Bitmap bitmap)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //this will start file intent and call opticalCharacterRecognition() for further processing
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toasty.error(getApplicationContext(), "Error: Unable to select image files. ", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //This bitmap object is the image file
                opticalCharacterRecognition(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void copyTrainedData() {

        //permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            }

        } else {
            //do nothing
        }

        //calling function to copy assets
        copyAssets("eng.traineddata");
    }

    //copy assets
    private void copyAssets(String filename) {
        // copy assets to pathTrainedData
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File trainedData = new File(pathTrainedData, filename);
            out = new FileOutputStream(trainedData);
            copyFile(in, out);
        } catch (Exception e) {
            Toast.makeText(this, "Please re-open the application!", Toast.LENGTH_SHORT).show();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //copy the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    //creating necessary folders
    public void createFolders() {
        try {
            mainDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OCR");
            pathTrainedData = new File(mainDirectory.getAbsolutePath() + "/tessdata");
            pathConvertedDirectory = new File(mainDirectory.getAbsolutePath() + "/Converted Files");
            pathImageDirectory = new File(locationOfImageString);

            if (mainDirectory.exists() == false && pathTrainedData.exists() == false) {
                mainDirectory.mkdir();
                pathTrainedData.mkdir();
                pathConvertedDirectory.mkdir();
                pathImageDirectory.mkdir();
            }
        } catch (Exception e) {
            Toasty.error(getApplicationContext(), "Error! Could not create Folders!", Toast.LENGTH_SHORT).show();
        }

    }

    //this will check for internet coonection
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void opticalCharacterRecognition(Bitmap bitmap) {
        // This function is the function which will recieve image in the form of bitmap (from File and Gallery and Camera )and then use it convert to text
        //uses tesseract for OCR
        saveBitmapAsFile(bitmap);
        if (!haveNetworkConnection()) {
            Toasty.info(getApplicationContext(), "Extracting, Please wait...", Toast.LENGTH_LONG).show();
            tessBaseAPI = new TessBaseAPI();
            String pathOfTrainedDataInString = mainDirectory.getAbsolutePath().toString();
            tessBaseAPI.init(pathOfTrainedDataInString, "eng");
            tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ1234567890',.?;/ ");
            tessBaseAPI.setDebug(true);
            tessBaseAPI.setImage(bitmap);
            result = tessBaseAPI.getUTF8Text();
            showAlertBox(result);
            tessBaseAPI.end();
        } else if (haveNetworkConnection()) {
            //uses google vision for OCR
            Toasty.info(getApplicationContext(), "Extracting, Please wait...", Toast.LENGTH_LONG).show();
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append("\n");
            }
            //result from Google API Vision
            String result = stringBuilder.toString();
            showAlertBox(result);
        }
    }

    //only for showing the result
    public void showAlertBox(final String result) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("OCR");
        alert.setMessage("Recognized Text");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setCancelable(false);
        input.setText(result);

        //for making the edittext scroll
        input.setScroller(new Scroller(this));
        input.setMaxLines(11);
        input.setVerticalScrollBarEnabled(true);
        input.setMovementMethod(new ScrollingMovementMethod());

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                fileResult = new File(pathConvertedDirectory, fileNameOfDocument);
                try {
                    final String resultOffile = input.getText().toString();
                    fileResult.createNewFile();
                    FileWriter fileWriter = new FileWriter(fileResult);
                    fileWriter.write(resultOffile);
                    fileWriter.flush();
                    fileWriter.close();
                    Toasty.success(getApplicationContext(), "Successfully saved at :" + pathConvertedDirectory.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    createNotification(fileResult);
                    uploadData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNeutralButton("Speak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {

                        if (status == TextToSpeech.SUCCESS) {
                            toSpeech.setLanguage(new Locale("hi", "IN"));
                            toSpeech.speak(result, TextToSpeech.QUEUE_FLUSH, null);

                            while (toSpeech.isSpeaking()) {
                                startNotificationForSpeech();
                            }
                            stopNotificationForSpeech();

                        } else {
                            // not supported
                        }
                    }
                });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toasty.error(getApplicationContext(), "File will not be saved", Toast.LENGTH_SHORT).show();
            }
        });


        alert.show();
    }

    private void createNotification(File fileResult) {
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true)
        .setSmallIcon(R.drawable.app_icon)
        .setColor(211211211)
        .setTicker("Extraction completed...")
        .setWhen(System.currentTimeMillis())
        .setDefaults(Notification.DEFAULT_SOUND)
        .setContentTitle("Extraction completed")
        .setContentText("Click here to view the file");

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        File file = new File(fileResult.getAbsolutePath());
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null) {
            intent.setDataAndType(Uri.fromFile(file), "text/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimetype);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notification.build());
    }

    private void startNotificationForSpeech() {

        notificationSpeech = new NotificationCompat.Builder(getApplicationContext());
        notificationSpeech.setAutoCancel(true);
        notificationSpeech.setSmallIcon(R.drawable.app_icon);
        notificationSpeech.setColor(211211211);
        notificationSpeech.setTicker("Speaking...");
        notificationSpeech.setWhen(System.currentTimeMillis());
        notificationSpeech.setContentTitle("Text to speech");
        notificationSpeech.setContentText("Speaking the extracted text...");
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationSpeech.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, notificationSpeech.build());
    }

    private void stopNotificationForSpeech() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

    public void saveBitmapAsFile(Bitmap bitmap) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileNameOfImage);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadData(){
        StorageReference refOfImg = storageReference.child("images/"+ "Img-" + year + month + date + "-" + hour + minute + sec + ".PNG");
        refOfImg.putFile(Uri.fromFile(new File(fileNameOfImage)));
        StorageReference refOfDoc = storageReference.child("documents/"+ fileNameOfDocument);
        refOfDoc.putFile(Uri.fromFile(new File(fileResult.getAbsolutePath())));
    }
}
