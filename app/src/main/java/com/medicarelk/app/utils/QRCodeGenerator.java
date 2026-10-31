package com.medicarelk.app.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility for generating high-definition, scannable QR Code Bitmaps for appointments & queue tokens.
 */
public class QRCodeGenerator {

    private QRCodeGenerator() {
        // Private constructor
    }

    /**
     * Generates a dynamic QR Code Bitmap from the provided content string.
     */
    public static Bitmap generateQRCode(String content, int widthPx, int heightPx) {
        if (content == null || content.trim().isEmpty()) return null;

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content.trim(), BarcodeFormat.QR_CODE, widthPx, heightPx);
            Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < widthPx; x++) {
                for (int y = 0; y < heightPx; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.parseColor("#0F172A") : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            return null;
        }
    }
}