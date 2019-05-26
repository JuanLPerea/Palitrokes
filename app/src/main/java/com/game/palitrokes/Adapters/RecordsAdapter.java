package com.game.palitrokes.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.game.palitrokes.Modelos.Jugador;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.AdapterViewHolder> {


    List<Jugador> jugadores;

    public RecordsAdapter(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }

    @NonNull
    @Override
    public RecordsAdapter.AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsAdapter.AdapterViewHolder adapterViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder {
        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
