package com.example.vishal.ocr;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {
    int CAMERA_REQUEST_CODE = 101;
    int GALLERY_REQUEST_CODE = 102;
    int apiversion;
    Button openCameraButton, openGalleryButton;
    TextView model, cpu, ram, androidTextView, heading, copyrightTextView;
    String modelName, manufacturer, androidVersion, CPUInfo;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        openCameraButton = (Button) rootView.findViewById(R.id.openCamera);
        openGalleryButton = (Button) rootView.findViewById(R.id.openGallery);
        model = (TextView) rootView.findViewById(R.id.model_number);
        cpu = (TextView) rootView.findViewById(R.id.cpu);
        ram = (TextView) rootView.findViewById(R.id.ram);
        heading = (TextView) rootView.findViewById(R.id.textView6);
        androidTextView = (TextView) rootView.findViewById(R.id.android_version);
        copyrightTextView = (TextView) rootView.findViewById(R.id.copyright);

        //setting the model name
        modelName = Build.MODEL.toUpperCase();
        manufacturer = Build.MANUFACTURER.toUpperCase();
        model.setText(manufacturer + " " + modelName);

        //setting the api version of android
        androidVersion = Build.VERSION.RELEASE;
        apiversion = Build.VERSION.SDK_INT;
        androidTextView.setText("Android Version " + androidVersion + " API level " + apiversion);

        //setting CPU
        CPUInfo = Build.BOARD.toUpperCase();
        cpu.setText("Processor : " + CPUInfo);

        //Ram information
        ActivityManager actManager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem / (1024 * 1024);
        ram.setText("Total memory : " + totalMemory + " MB");

        openCameraButton.setOnClickListener(this);
        openGalleryButton.setOnClickListener(this);
        copyrightTextView.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openCamera:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                break;
            case R.id.openGallery:
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_REQUEST_CODE);

                break;
            case R.id.copyright:
                Intent intentCopyright = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vishalsammy065"));
                startActivity(intentCopyright);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // this will create a bitmap of the image from camera intent
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            // Bitmap sharpenBitmap = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.HDR);
            ((MainActivity) getActivity()).opticalCharacterRecognition(bitmap);
        }

        //this is image file from the gallery
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                // Bitmap sharpenBitmap = ImageFilter.applyFilter(bitmap, ImageFilter.Filter.SHARPEN);
                ((MainActivity) getActivity()).opticalCharacterRecognition(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
