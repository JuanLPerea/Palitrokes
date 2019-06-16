package com.game.palitrokes.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.game.palitrokes.Modelos.Records;
import com.game.palitrokes.R;
import com.game.palitrokes.Utilidades.Utilidades;
import com.game.palitrokes.Utilidades.UtilsFirebase;

import org.w3c.dom.Text;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.AdapterViewHolder> {


    List<Records> records;

    public RecordsAdapter(List<Records> records) {
        this.records = records;
    }

    public RecordsAdapter() {
    }

    @NonNull
    @Override
    public RecordsAdapter.AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_records, viewGroup, false);
        RecordsAdapter.AdapterViewHolder holder = new RecordsAdapter.AdapterViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsAdapter.AdapterViewHolder holder, int i) {

        Records recordRow = records.get(i);

        UtilsFirebase.descargarImagenFirebase(recordRow.getIdJugador(), holder.avatarRecord);
        holder.nickRecord.setText(recordRow.getNickname());
        holder.levelRecord.setText(recordRow.getLevel() + "");
        holder.victoriasRecord.setText(recordRow.getVictorias() + "");

    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder {

        TextView nickRecord, levelRecord,  victoriasRecord;
        ImageView avatarRecord;

        public AdapterViewHolder(@NonNull View itemView)
        {
            super(itemView);

            avatarRecord = itemView.findViewById(R.id.avatarRecordIV);
            nickRecord = itemView.findViewById(R.id.nickRecordET);
            levelRecord = itemView.findViewById(R.id.recordlevelET);
            victoriasRecord = itemView.findViewById(R.id.recordVictoriasET);

        }
    }
}
