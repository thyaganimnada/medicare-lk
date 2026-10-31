package com.medicarelk.app.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.medicarelk.app.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Utility for loading Firebase Storage, Base64 strings, drawable resource names, and network image URLs cleanly into ImageViews.
 * Handles profile photo decoding, circular cropping, Base64 conversion, and default fallbacks.
 */
public class ImageUtils {

    private ImageUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if the context is safe for Glide operations (Activity not finishing or destroyed).
     */
    private static boolean isSafeContext(Context context) {
        if (context == null) return false;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    /**
     * Loads a doctor or general image URL into an ImageView with placeholder fallbacks.
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (imageView == null || !isSafeContext(context)) return;

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(context)
                    .load(imageUrl.trim())
                    .placeholder(R.drawable.ic_doctor)
                    .error(R.drawable.ic_doctor)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_doctor);
        }
    }

    /**
     * Loads a profile photo (Base64 string, drawable name, or URL) into an ImageView with circular cropping.
     */
    public static void loadProfileImage(Context context, String imageSource, ImageView imageView) {
        if (imageView == null || !isSafeContext(context)) return;

        if (imageSource != null && !imageSource.trim().isEmpty()) {
            String trimmed = imageSource.trim();

            // 1. Base64 Image String
            if (trimmed.startsWith("data:image") || (!trimmed.startsWith("http") && !trimmed.startsWith("content://") && trimmed.length() > 100)) {
                try {
                    String cleanBase64 = trimmed.contains(",") ? trimmed.split(",")[1] : trimmed;
                    byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        Glide.with(context)
                                .load(bitmap)
                                .transform(new CircleCrop())
                                .placeholder(R.drawable.ic_doctor)
                                .error(R.drawable.ic_doctor)
                                .into(imageView);
                        return;
                    }
                } catch (Exception ignored) {}
            }

            // 2. Drawable resource names (e.g. "doc_selini", "ic_doctor", "drawable/doc_selini")
            if (!trimmed.startsWith("http") && !trimmed.startsWith("content://")) {
                String resName = trimmed.contains("/") ? trimmed.substring(trimmed.lastIndexOf('/') + 1) : trimmed;
                int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
                if (resId > 0) {
                    Glide.with(context)
                            .load(resId)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.ic_doctor)
                            .error(R.drawable.ic_doctor)
                            .into(imageView);
                    return;
                }
            }

            // 3. HTTP / HTTPS Network URLs
            Glide.with(context)
                    .load(trimmed)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_doctor)
                    .error(R.drawable.ic_doctor)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_doctor);
        }
    }

    /**
     * Loads a rounded image URL or drawable resource name.
     */
    public static void loadRoundedImage(Context context, String imageUrl, ImageView imageView, int cornerRadiusDp) {
        if (imageView == null || !isSafeContext(context)) return;

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String trimmed = imageUrl.trim();
            int radiusPx = (int) (cornerRadiusDp * context.getResources().getDisplayMetrics().density);

            // Support drawable resource names like "doc_selini" or "drawable/doc_selini"
            if (!trimmed.startsWith("http") && !trimmed.startsWith("content://") && !trimmed.startsWith("data:image")) {
                String resName = trimmed.contains("/") ? trimmed.substring(trimmed.lastIndexOf('/') + 1) : trimmed;
                int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
                if (resId > 0) {
                    Glide.with(context)
                            .load(resId)
                            .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(radiusPx)))
                            .placeholder(R.drawable.ic_doctor)
                            .error(R.drawable.ic_doctor)
                            .into(imageView);
                    return;
                }
            }

            Glide.with(context)
                    .load(trimmed)
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(radiusPx)))
                    .placeholder(R.drawable.ic_doctor)
                    .error(R.drawable.ic_doctor)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_doctor);
        }
    }

    /**
     * Converts a Uri from photo picker to a compressed Base64 String for Firebase persistence.
     */
    public static String convertUriToBase64(Context context, Uri imageUri) {
        if (context == null || imageUri == null) return null;
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            if (original == null) return null;

            // Scale down to max 512x512 for fast network synchronization
            int maxDim = 512;
            int width = original.getWidth();
            int height = original.getHeight();
            float ratio = Math.min((float) maxDim / width, (float) maxDim / height);
            int newWidth = Math.round(ratio * width);
            int newHeight = Math.round(ratio * height);

            Bitmap scaled = Bitmap.createScaledBitmap(original, Math.max(1, newWidth), Math.max(1, newHeight), true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}
