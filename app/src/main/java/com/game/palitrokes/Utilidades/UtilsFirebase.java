package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.game.palitrokes.MainActivity;
import com.game.palitrokes.Modelos.Jugador;
import com.game.palitrokes.Modelos.Records;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilsFirebase {


    public static void subirImagenFirebase(String jugadorID, Bitmap bitmap) {

        FirebaseStorage storage;
        StorageReference storageRef;

        // Referencia a Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

       // Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.child("AVATAR").child(jugadorID).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(Constantes.TAG, "Error Subiendo imagen " + exception);
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


    public static void descargarImagenFirebaseYGuardarla(final Context context, String jugadorID, final String nombreArchivo) {

        FirebaseStorage storage;
        StorageReference storageRef;
        final Bitmap[] imagen = new Bitmap[1];

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("AVATAR").child(jugadorID);
       // imagen[0] = null;

        final long ONE_MEGABYTE = 1024 * 1024;
        avatarRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
               // imagen[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Utilidades.guardarImagenMemoriaInterna(context, nombreArchivo, bytes);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(Constantes.TAG, "Error Descargando Imagen " + exception);
                //   imageView.setImageDrawable(   R.drawable.camera);
            }
        });


    }


    public static void descargarImagenFirebaseView(final Context context, String jugadorID, final ImageView view) {

        FirebaseStorage storage;
        StorageReference storageRef;
        final Bitmap[] imagen = new Bitmap[1];

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("AVATAR").child(jugadorID);
        // imagen[0] = null;

        final long ONE_MEGABYTE = 1024 * 1024;
        avatarRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                 imagen[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                 view.setImageBitmap(imagen[0]);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(Constantes.TAG, "Error Descargando Imagen " + exception);
                //   imageView.setImageDrawable(   R.drawable.camera);
            }
        });


    }

    public static void guardarRecords(Context context , final Jugador jugador , final int level) {

        final DatabaseReference recordsRef = FirebaseDatabase.getInstance().getReference().child("RECORDS");

        //Subimos nuestro avatar a Firebase (Aquí es seguro que tenemos internet)
        UtilsFirebase.subirImagenFirebase(jugador.getJugadorId(), Utilidades.recuperarImagenMemoriaInterna(context, Constantes.ARCHIVO_IMAGEN_JUGADOR));

        // Recuperar la lista de records guardada en Firebase
        // Cargar los records y mostrarlos en el Recycler

        ValueEventListener cargarRecords = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Records> listaRecordsNueva = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Records recordTmp = snapshot.getValue(Records.class);
                    listaRecordsNueva.add(recordTmp);
                }

                listaRecordsNueva.add(new Records(jugador.getJugadorId(), jugador.getNickname(), jugador.getVictorias(), level));
                Collections.sort(listaRecordsNueva);

                // Guardamos en Firebase
                for (int n = 0; n < listaRecordsNueva.size(); n++) {
                    recordsRef.child("" + n).setValue(listaRecordsNueva.get(n));
                    if (n == 9) break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        recordsRef.addListenerForSingleValueEvent(cargarRecords);


    }


}
