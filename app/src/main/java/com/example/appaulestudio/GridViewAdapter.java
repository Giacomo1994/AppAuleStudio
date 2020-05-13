package com.example.appaulestudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    private final Context mContext;
    private final Gruppo[] gruppi;
    private LayoutInflater thisInflater;

    // 1
    public GridViewAdapter(Context context, Gruppo[] gruppi) {
        thisInflater=LayoutInflater.from(context);
        this.mContext = context;
        this.gruppi = gruppi;
    }

    // 2
    @Override
    public int getCount() {
        return gruppi.length;
    }

    // 3
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 4
    @Override
    public Object getItem(int position) {
        return null;
    }

    // 5
    @Override
    /*public View getView(int position, View convertView, ViewGroup parent) {
        TextView dummyTextView = new TextView(mContext);
        Gruppo item = gruppi[position];
        dummyTextView.setText(String.valueOf(item.getNome_gruppo())+"\n"+String.valueOf(item.getCodice_gruppo()));
        return dummyTextView;
    }*/


    public View getView(int position, View convertView, ViewGroup parent) {
        TextView codiceGruppo, nomeGruppo;
        Gruppo item= gruppi[position];
        convertView = thisInflater.inflate(R.layout.row_layout_scegli_gruppo, parent, false);
        //txtComponenti = convertView.findViewById(R.id.txtComponente);
        //codiceGruppo = convertView.findViewById(R.id.codiceGruppo);
        nomeGruppo= convertView.findViewById(R.id.nomeGruppo);
        nomeGruppo.setText(String.valueOf(item.getNome_gruppo()));
        //codiceGruppo.setText(String.valueOf(""));
        //codiceGruppo.setText(String.valueOf(item.getCodice_gruppo()));
        //txtComponenti.setText(item.getNome()+" "+item.getCognome());

        return convertView;
    }



}
