package com.game.palitrokes.Utilidades;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;


public class Utilidades {

    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private static Bitmap imagen;

    public static void subirImagenFirebase(String jugadorID, Drawable drawable){

        // Referencia a Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child("AVATAR").child(jugadorID).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(Constantes.TAG, "Error Subiendo Imágen " + exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(Constantes.TAG, "Foto Subida Correctamente");
            }
        });
    }


    public static void descargarImagenFirebase(String jugadorID, final ImageView imageView) {
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
                Log.d(Constantes.TAG, "Error Descargando Imágen " + exception);
             //   imageView.setImageDrawable(   R.drawable.camera);
            }
        });

    }


    //creamos el fichero donde irá la imagen
    public static Uri crearFicheroImagen () {
        Uri uri_destino = null;
        String nombre_fichero = null;
        File file = null;

        crearNombreArchivo();

        String ruta_captura_foto = crearNombreArchivo();
        Log.d("MIAPP", "RUTA FOTO " + ruta_captura_foto);
        file = new File(ruta_captura_foto);

        try { //INTENTA ESTO

            if (file.createNewFile()) {
                Log.d("MIAPP", "FICHERO CREADO");
            } else {
                Log.d("MIAPP", "FICHERO NO CREADO");
            }
        } catch (IOException e) { // Y SI FALLA SE METE POR AQUÍ
            Log.e("MIAPP", "Error al crear el fichero", e);
        }

        uri_destino = Uri.fromFile(file);
        Log.d("MIAPP", "URI = " + uri_destino.toString());

        return uri_destino;
    }


    // Gracias Vale!!
    public static void desactivarModoEstricto ()
    {
        if (Build.VERSION.SDK_INT >= 24)
        {
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);

            }catch (Exception e)
            {

            }
        }
    }
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public static String crearNombreArchivo () {
        // Creamos un nombre de fichero para guardar la foto
         String nombre_fichero = Constantes.PREFIJO_FOTOS + Constantes.SUFIJO_FOTOS;
         String ruta_captura_foto = Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_PICTURES).getPath() + "/" + nombre_fichero;
    return ruta_captura_foto;
    }


}
