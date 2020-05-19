package com.example.appaulestudio;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast extends Toast {

    public MyToast(Context context, boolean success) {
        super(context);
        if(success==true) this.setView(LayoutInflater.from(context).inflate(R.layout.toast_success,null));
        else this.setView(LayoutInflater.from(context).inflate(R.layout.toast_error,null));
    }

    public void show(){
        super.show();
    }

    public static MyToast makeText(Context context,String message, boolean success) {
        MyToast toast = new MyToast(context, success);
        View view = toast.getView();
        TextView txt=view.findViewById(R.id.txt_toast);
        txt.setText(message);
        toast.setDuration(Toast.LENGTH_LONG);
        return toast;
    }


}
