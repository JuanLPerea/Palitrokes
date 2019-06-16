package com.game.palitrokes.Utilidades;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
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
