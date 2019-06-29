package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;


public class Utilidades {

    //creamos el fichero donde irá la imagen
    public static Uri crearFicheroImagen() {
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
    public static void desactivarModoEstricto() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);

            } catch (Exception e) {
                Log.e(Constantes.TAG, "Error al trucar el método disableDeathOnFileUriExposure", e);
            }
        }
    }


    // Redimensionar Bitmap
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public static String crearNombreArchivo() {
        // Creamos un nombre de fichero para guardar la foto
        String nombre_fichero = Constantes.PREFIJO_FOTOS + Constantes.SUFIJO_FOTOS;
        String ruta_captura_foto = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + nombre_fichero;
        return ruta_captura_foto;
    }

    public static void guardarImagenMemoriaInterna(Context context, String archivo, byte[] byteArray) {
        try {
            FileOutputStream outputStream = context.openFileOutput(archivo + ".jpg", Context.MODE_PRIVATE);
            outputStream.write(byteArray);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static Bitmap recuperarImagenMemoriaInterna(Context context, String archivo) {
        Bitmap bitmap = null;

        if (archivo == null) {
            archivo = Constantes.ARCHIVO_IMAGEN_JUGADOR;
        }

        try {
            FileInputStream fileInputStream =
                    new FileInputStream(context.getFilesDir().getPath() + "/" + archivo + ".jpg");
            bitmap = BitmapFactory.decodeStream(fileInputStream);
        } catch (IOException io) {
            io.printStackTrace();
        }

        return bitmap;
    }

    public static void eliminarArchivo(Context context, String archivo) {

        boolean eliminado = false;

        File file = new File(context.getFilesDir().getPath() + "/" + archivo + ".jpg");
        if (file.exists()) eliminado = file.delete();

    }


    public static byte[] bitmapToArrayBytes(Bitmap bitmap) {

        byte[] arrayBytes = null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        arrayBytes = stream.toByteArray();

        return arrayBytes;

    }


    public static Drawable getAssetImage(Context context, String filename) {

        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = null;
        try {
            buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }



        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);


    }


}
