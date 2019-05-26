package com.game.palitrokes;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.game.palitrokes.Adapters.RecordsAdapter;
import com.game.palitrokes.Modelos.Jugador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MIAPP";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference jugadoresRef;
    private EditText nickET;
    private TextView onlineTV;
    private Jugador jugador;
    private RecyclerView recordsRecycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Jugador> jugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Referencias a las vistas
        nickET = findViewById(R.id.editText);
        onlineTV = findViewById(R.id.onlineTV);
        recordsRecycler = findViewById(R.id.recordsRecycler);
        layoutManager = new LinearLayoutManager(this);
        recordsRecycler.setLayoutManager(layoutManager);

        //Lista de jugadores online
        jugadores = new ArrayList<>();

        adapter = new RecordsAdapter(jugadores);
        recordsRecycler.setAdapter(adapter);

        // Nos autenticamos de forma anónima en Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            endSignIn(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            endSignIn(null);
                        }
                    }
                });


    }


    // Al finalizar la autenticación, recuperamos nuestro usuario o lo creamos
    // También cargamos la lista de jugadores online
    //
    private void endSignIn(final FirebaseUser currentUser) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("USUARIOS").child(currentUser.getUid());
        jugadoresRef = mDatabase.child("USUARIOS");

        // Cargamos los datos del usuario o los creamos si no existen (La primera vez que instalamos la APP)
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                jugador = dataSnapshot.getValue(Jugador.class);
                if (jugador == null) {
                    String nickName = "";
                    if (nickET.getText() == null) {
                        nickName = "Jugador";
                    } else {
                        nickName = nickET.getText().toString();
                    }
                    userRef.setValue(new Jugador(currentUser.getUid(), nickName));
                } else {
                    nickET.setText(jugador.getNickname());
                }


                Log.d(TAG, "Usuario BBDD " + jugador.getNickname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Error Usuario BBDD o Crear nuevo");
            }
        };
        userRef.addValueEventListener(userListener);


        // Cargar la lista de jugadores online
        ValueEventListener jugadoresListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                jugadores.removeAll(jugadores);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Jugador jugadorTMP = snapshot.getValue(Jugador.class);
                    if (jugadorTMP.isOnline()) jugadores.add(jugadorTMP);
                }

                onlineTV.setText("Online: " + jugadores.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        jugadoresRef.addValueEventListener(jugadoresListener);



    }


    public void jugarOnline(View view) {


        // lanzar un dialog para encontrar un rival
        //

        final Dialog jugarOnline = new Dialog(this);
        jugarOnline.setContentView(R.layout.dialog_jugar);
        jugarOnline.setTitle("Jugar Online");


        // TODO Mirar si hay partidas esperando rival, si no, crear una nueva partida




        jugarOnline.show();


        /*
        Intent jugar = new Intent(this, JuegoActivity.class);
        startActivity(jugar);
        */


    }

    public void jugar(View view) {
    }





    /*

        @Override
        public void onStart() {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            endSignIn(currentUser);
        }
    */
}
