package com.static1.fishylottery.services;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * This class is responsible for generating QR codes from a string of content
 */
public class QrUtils {
    /**
     * Generates a bitmap for a QR code given the text to encode and a size.
     *
     * @param text The string to encode, usually a URL.
     * @param size The square size of the image in pixels.
     * @return A bitmap of the QR code.
     */
    public static Bitmap generateQrCode(String text, int size) {
        if (text == null) {
            throw new IllegalArgumentException("Input text cannot be null");
        }

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be empty");
        }

        QRCodeWriter writer = new QRCodeWriter();
        try {
            var bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e("QrUtils", "Unable to generate QR code bitmap", e);
            return null;
        }
    }
}
