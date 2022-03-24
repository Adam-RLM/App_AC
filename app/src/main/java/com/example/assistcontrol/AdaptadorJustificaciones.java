package com.example.assistcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdaptadorJustificaciones extends RecyclerView.Adapter<AdaptadorJustificaciones.MyViewHolder> {
    Context context;
    ArrayList<Justificaciones> justificacionesArrayList;
    private RecyclerViewClickListener listener;

    public AdaptadorJustificaciones(Context context, ArrayList<Justificaciones> justificacionesArrayList, RecyclerViewClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.justificacionesArrayList = justificacionesArrayList;
    }

    @NonNull
    @Override
    public AdaptadorJustificaciones.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorJustificaciones.MyViewHolder holder, int position) {

        Justificaciones justificaciones = justificacionesArrayList.get(position);

        holder.txt_hora.setText(justificaciones.getHora());
        holder.txt_anio.setText(justificaciones.getAño());
        holder.txt_mes.setText(justificaciones.getMes());
        holder.txt_dia.setText(justificaciones.getDia());
        holder.txt_justificacion.setText(justificaciones.getJustificacion());
    }

    @Override
    public int getItemCount() {
        return justificacionesArrayList.size();
    }

    public interface RecyclerViewClickListener{
        void onClick(View v, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView txt_anio, txt_mes, txt_dia, txt_hora, txt_justificacion;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_hora = itemView.findViewById(R.id.tv_hora);
            txt_anio = itemView.findViewById(R.id.text_view_año);
            txt_mes = itemView.findViewById(R.id.text_view_mes);
            txt_dia = itemView.findViewById(R.id.text_view_dia);
            txt_justificacion = itemView.findViewById(R.id.text_view_justificacion);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }
}
