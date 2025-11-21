package com.static1.fishylottery.view.events;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.mlkit.vision.barcode.common.Barcode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class QrScanActivityTest {

    @Test
    public void barcodeProcessing_setsResultAndFinishes() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), QrScanActivity.class);

        try (ActivityScenario<QrScanActivity> scenario = ActivityScenario.launch(intent)) {

            scenario.onActivity(activity -> {

                // Fake barcode
                QrScanActivity.BarcodeData fake = () -> "HELLO123";

                List<QrScanActivity.BarcodeData> list = Collections.singletonList(fake);

                // Call the real method
                activity.handleBarcodes(list);

                // Verify result
                Intent result = activity.getIntent();
                assertEquals("HELLO123", result.getStringExtra("qrResult"));

                // Activity should finish immediately
                assertTrue(activity.isFinishing());
            });
        }
    }
}
