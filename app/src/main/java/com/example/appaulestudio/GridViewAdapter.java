package com.example.appaulestudio;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    private final Context mContext;
    private final Gruppo[] gruppi;

    // 1
    public GridViewAdapter(Context context, Gruppo[] gruppi) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView dummyTextView = new TextView(mContext);
        Gruppo item = gruppi[position];
        dummyTextView.setText(String.valueOf(item.getCodice_gruppo()));
        return dummyTextView;
    }
}
