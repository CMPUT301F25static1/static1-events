package com.static1.fishylottery.services;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * This class is responsible for uploading images to Firebase Storage and deleting them.
 */
public class StorageManager {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    /**
     * Uploads an image to Firebase Storage for specified path and returns the image URL.
     *
     * @param imageUri The local image URI to upload
     * @param path The path within the Firebase Storage bucket to upload to
     * @return A string with the image URL
     */
    public static Task<String> uploadImage(Uri imageUri, String path) {
        if (imageUri == null) {
            return Tasks.forException(new IllegalArgumentException("imageUri cannot be null"));
        }

        String fileName = System.currentTimeMillis() + "_" + imageUri.getLastPathSegment();
        StorageReference fileRef = storage.getReference().child(path).child(fileName);

        UploadTask uploadTask = fileRef.putFile(imageUri);

        return uploadTask
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Uri downloadUrl = task.getResult();
                    return Tasks.forResult(downloadUrl.toString());
                });

    }

    /**
     * Deletes an image from the Firebase Storage bucket given the image URL string.
     *
     * @param imageUrl The image URL to be removed.
     * @return A Task indicating success or failure
     */
    public static Task<Void> deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("imageUrl cannot be empty or null"));
        }

        StorageReference imageRef = storage.getReference(imageUrl);

        return imageRef.delete();
    }
}
