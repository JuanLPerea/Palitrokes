package com.game.palitrokes.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.palitrokes.Modelos.Records;
import com.game.palitrokes.R;
import com.game.palitrokes.Utilidades.Utilidades;
import com.game.palitrokes.Utilidades.UtilsFirebase;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Random;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.AdapterViewHolder> {


    List<Records> records;
    Context context;

    public RecordsAdapter(Context context, List<Records> records) {
        this.records = records;
        this.context = context;
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

       // UtilsFirebase.descargarImagenFirebaseYGuardarla(recordRow.getIdJugador(), holder.avatarRecord);
        //Cargar imagen de los records siempre de los archivos guardados en memoria interna
        // Estos archivos se actualizan cuando se descargan de Firebase al producirse el evento OnDataChange en los Records

        Bitmap imagenRecord = Utilidades.recuperarImagenMemoriaInterna(context, recordRow.getIdJugador() );
        if (imagenRecord != null && recordRow.getIdJugador() != "idJugador") {
            holder.avatarRecord.setImageBitmap(imagenRecord);
        } else {
            holder.avatarRecord.setImageResource(R.drawable.picture);
        }
        holder.nickRecord.setText(recordRow.getNickname());
        holder.levelRecord.setText(recordRow.getLevel() + "");
        holder.victoriasRecord.setText(recordRow.getVictorias() + "");
        Random rnd = new Random();
        holder.linearLayout.setBackgroundColor( Color.argb(255, rnd.nextInt(20) + 194, rnd.nextInt(20) + 113, rnd.nextInt(20)+71));

    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder {

        TextView nickRecord, levelRecord,  victoriasRecord;
        ImageView avatarRecord;
        LinearLayout linearLayout;

        public AdapterViewHolder(@NonNull View itemView)
        {
            super(itemView);

            avatarRecord = itemView.findViewById(R.id.avatarRecordIV);
            nickRecord = itemView.findViewById(R.id.nickRecordET);
            levelRecord = itemView.findViewById(R.id.recordlevelET);
            victoriasRecord = itemView.findViewById(R.id.recordVictoriasET);
            linearLayout = itemView.findViewById(R.id.rowLinear);

        }
    }
}
