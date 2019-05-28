package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.game.palitrokes.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Utilidades {

    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private static String TAG = "MIAPP";
    private static Bitmap imagen;

    public static void subirImagenFirebase(Context context, String jugadorID){

        // Referencia a Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.paco)).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child("AVATAR").child(jugadorID).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Error Subiendo Imágen " + exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "Foto Subida Correctamente");
            }
        });
    }


    public static void descargarImagenFirebase(Context context, String jugadorID, final ImageView imageView) {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("AVATAR").child(jugadorID);
        imagen = null;

        final long ONE_MEGABYTE = 1024 * 1024;
        avatarRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                imagen = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                imageView.setImageBitmap(imagen);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Error Descargando Imágen " + exception);
            }
        });

    }
}
