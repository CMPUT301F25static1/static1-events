package com.static1.fishylottery.view.events;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.static1.fishylottery.R;

import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that scans QR codes using the device camera.
 * Returns the scanned QR code value as a result.
 */
public class QrScanActivity extends AppCompatActivity {

    /** Request code for camera permission. */
    private static final int CAMERA_PERMISSION_CODE = 100;

    /** PreviewView for displaying the camera feed. */
    private PreviewView previewView;

    /** Executor for running camera-related tasks. */
    private ExecutorService cameraExecutor;

    /** Flag to prevent multiple scans. */
    private boolean scanned = false;

    /**
     * Interface for accessing barcode data.
     */
    public interface BarcodeData {
        /**
         * Retrieves the raw value of the barcode.
         *
         * @return the raw barcode value
         */
        String getRawValue();
    }

    /**
     * Initializes the activity, sets up the UI, and requests camera permission.
     *
     * @param savedInstanceState the saved instance state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        previewView = findViewById(R.id.camera_preview_view);
        cameraExecutor = Executors.newSingleThreadExecutor();

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Starts the camera for QR code scanning.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Binds the camera to the lifecycle and sets up QR code scanning.
     *
     * @param cameraProvider the camera provider
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        analysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (imageProxy.getImage() == null) {
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(image)
                    .addOnSuccessListener(this::processBarcodes)
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
    }

    /**
     * Processes the scanned barcodes and converts them to BarcodeData.
     *
     * @param barcodes the list of scanned barcodes
     */
    private void processBarcodes(List<Barcode> barcodes) {
        List<BarcodeData> converted = new ArrayList<>();

        for (Barcode b : barcodes) {
            converted.add(b::getRawValue);
        }

        handleBarcodes(converted);
    }

    /**
     * Handles the scanned barcodes and returns the first valid result.
     *
     * @param barcodes the list of scanned barcodes
     */
    void handleBarcodes(List<BarcodeData> barcodes) {
        if (scanned) return;

        for (BarcodeData barcode : barcodes) {
            String value = barcode.getRawValue();
            if (value != null && !value.isEmpty()) {
                scanned = true;
                runOnUiThread(() -> {
                    Log.d("QrScanner", "Scanned QR code: " + value);
                    getIntent().putExtra("qrResult", value);
                    setResult(Activity.RESULT_OK, getIntent());
                    finish();
                });
                break;
            }
        }
    }

    /**
     * Handles the result of the camera permission request.
     *
     * @param requestCode  the request code
     * @param permissions  the requested permissions
     * @param grantResults the permission grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            finish();
        }
    }

    /**
     * Handles the navigation up action.
     *
     * @return true if the action was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    /**
     * Cleans up resources when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}