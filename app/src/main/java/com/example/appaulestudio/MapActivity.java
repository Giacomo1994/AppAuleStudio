package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private GoogleMap gmap;
    Intent intent;
    Bundle bundle;
    ArrayList<Aula> array_aule;
    Spinner spinner_map;
    Adapter adapter;
    Aula aulaSelezionata;

    //posizioni
    private Double lat_uni, lng_uni;
    LatLng my_position=null;
    Marker marker_my_position=null;
    List<Marker> markerList=new LinkedList<Marker>();

    String strNomeUniversita, strUniversita, strMatricola, strNome, strCognome;
    String mode=null;

    private void initUi(){
        spinner_map=findViewById(R.id.spinner_map);
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        lat_uni=Double.parseDouble(settings.getString("latitudine", null));
        lng_uni=Double.parseDouble(settings.getString("longitudine", null));
        strNomeUniversita=settings.getString("nome_universita", null);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        //intent
        intent = getIntent();
        bundle = intent.getBundleExtra("bundle_aule");
        if(bundle!=null) {
            array_aule = bundle.getParcelableArrayList("aule");
        }else{
            Log.i("mylog","Il bundle è null");
        }
        //spinner
        array_aule.add(0, new Aula(strUniversita,strNomeUniversita,"",lat_uni,lng_uni,0,0,0,""));
        adapter = new ArrayAdapter<>(MapActivity.this, android.R.layout.simple_list_item_1, array_aule);
        spinner_map.setAdapter((SpinnerAdapter) adapter);

        spinner_map.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aulaSelezionata = (Aula) parent.getItemAtPosition(position);
                LatLng selected = new LatLng(aulaSelezionata.getLatitudine(),aulaSelezionata.getLongitudine());
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(selected,17));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @SuppressLint("WrongConstant")
    public void action_bar(){
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.my_action_bar);
        getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView txt_actionbar = view.findViewById(R.id.txt_actionbar);
        ImageView image_actionbar =view.findViewById(R.id.image_actionbar);
        txt_actionbar.setText("Mappa Aule");
        final Dialog d = new Dialog(MapActivity.this);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_user);
        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
        TextView txt_nome=d.findViewById(R.id.txt_dialog_user_nome);
        txt_nome.setText(strNome+" "+strCognome);
        TextView txt_matricola=d.findViewById(R.id.txt_dialog_user_matricola);
        txt_matricola.setText(strMatricola);
        TextView txt_universita=d.findViewById(R.id.txt_dialog_user_università);
        txt_universita.setText(strNomeUniversita);
        Button btn_logout=d.findViewById(R.id.btn_logout);
        Button btn_continue=d.findViewById(R.id.btn_continue);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("logged", false);
                editor.commit();
                Intent i = new Intent(MapActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        image_actionbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMapView(savedInstanceState);
        initUi();
        action_bar();
    }

    private void createMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle= null;
        if (savedInstanceState!=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
//!!!!! gmap.setMyLocationEnabled(true); //MOSTRA BOTTONE POSIZIONE! DA METTERE UNA VOLTA CHE HAI DATO IL PERMESSO DI GEOLOCALIZZAZIONE
        uiSettings.setMyLocationButtonEnabled(true);

        for(Aula a : array_aule){
            Marker marker=null;
            if(!a.getNome().equals(strNomeUniversita)) marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            else marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            markerList.add(marker);
        }

        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(Aula a :array_aule){
                    if(a.getNome().compareTo(marker.getTitle())==0){
                        //creoDialog
                        final Dialog d = new Dialog(MapActivity.this);
                        d.setContentView(R.layout.dialog_info_marker);
                        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                        TableRow row_luogo=d.findViewById(R.id.row_luogo);
                        TableRow row_aperta=d.findViewById(R.id.row_aperta);
                        TableRow row_posti=d.findViewById(R.id.row_posti);
                        TextView txt_nome_aula= d.findViewById(R.id.marker_nome);
                        TextView txt_indirizzo= d.findViewById(R.id.marker_indirizzo);
                        TextView txt_luogo= d.findViewById(R.id.marker_luogo);
                        TextView txt_posti= d.findViewById(R.id.marker_posti);
                        TextView txt_aperta= d.findViewById(R.id.marker_aperta);
                        Button btn_percorso= d.findViewById(R.id.button_indicazioni);
                        Button btn_to_aula= d.findViewById(R.id.button_aula);
                        if(a.getNome().equals(strNomeUniversita)){
                            row_aperta.setVisibility(View.GONE);
                            row_posti.setVisibility(View.GONE);
                            row_luogo.setVisibility(View.GONE);
                            btn_to_aula.setVisibility(View.GONE);
                        }
                        txt_nome_aula.setText(a.getNome());
                        txt_luogo.setText(a.getLuogo());
                        txt_posti.setText(""+a.getPosti_totali());
                        btn_percorso.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                d.dismiss();
                                mode=null;
                                final Dialog d_mezzo = new Dialog(MapActivity.this);
                                d_mezzo.setContentView(R.layout.dialog_mezzo_trasporto);
                                d_mezzo.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                                final LinearLayout ll_car=d_mezzo.findViewById(R.id.ll_car);
                                final LinearLayout ll_walk=d_mezzo.findViewById(R.id.ll_walk);
                                ImageView img_car=d_mezzo.findViewById(R.id.img_car);;
                                ImageView img_walk=d_mezzo.findViewById(R.id.img_walk);;
                                Button btn_start_navigation=d_mezzo.findViewById(R.id.btn_start_navigation);
                                img_car.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mode="driving";
                                        ll_car.setBackgroundResource(R.drawable.layout_icona_clicked);
                                        ll_walk.setBackgroundResource(R.drawable.layout_icona);
                                    }
                                });
                                img_walk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mode="walking";
                                        ll_walk.setBackgroundResource(R.drawable.layout_icona_clicked);
                                        ll_car.setBackgroundResource(R.drawable.layout_icona);
                                    }
                                });
                                btn_start_navigation.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(mode==null){
                                            MyToast.makeText(getApplicationContext(), "Seleziona un mezzo di trasporto!",false).show();
                                            return;
                                        }
                                        calcolaPercorso();
                                        d_mezzo.dismiss();
                                    }
                                });
                                d_mezzo.show();
                            }
                        });
                        d.show();
                    }
                }
                return false;
            }
        });
    }



    private void getLocation(){} //aggiungo marker della mia posizione

    private void calcolaPercorso(){} //chiamato una volta che ho scelto il mezzo di trasporto

    private void reverseGeocoding(){} //chiamato quando apro il dialog dell'aula

    public void verifyPermissions() {} //per geolocalizzazione



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.FIRST, 1, Menu.FIRST+1, "Home");
        menu.add(Menu.FIRST, 2, Menu.FIRST, "Gestione gruppi");
        menu.add(Menu.FIRST, 4, Menu.FIRST+2, "Prenotazioni");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent i = new Intent(this, Home.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        if (item.getItemId() == 2) {
            Intent i = new Intent(this, GroupActivity.class);
            startActivity(i);
            finish();
        }
        if(item.getItemId() == 4){
            Intent i = new Intent(this, PrenotazioniAttiveActivity.class);
            startActivity(i);
            finish();
        }
        return true;
    }


}
