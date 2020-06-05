package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private GoogleMap gmap;


    //dialog
    TextView title_dialog_marker;
    TextView nome_dialog_marker;
    TextView indirizzo_dialog_marker;
    TextView postitotali_dialog_marker;

    Intent intent;
    Bundle bundle;
    ArrayList<Aula> array_aule;
    Spinner spinner_map;
    Adapter adapter;
    Aula aulaSelezionata;

    private Double lat_pref, lng_pref; //latlng dell'università a cui l'utente loggato è iscritto
    String strNomeUniversita;
    private void initUi(){
        spinner_map=findViewById(R.id.spinner_map);
        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        lat_pref=Double.parseDouble(settings.getString("latitudine", null));
        lng_pref=Double.parseDouble(settings.getString("longitudine", null));
        strNomeUniversita=settings.getString("nome_universita", null);
        //intent
        intent = getIntent();
        bundle = intent.getBundleExtra("bundle_aule");
        if(bundle!=null) {
            array_aule = bundle.getParcelableArrayList("aule");
        }else{
            Log.i("mylog","Il bundle è null");
        }
        //spinner
        array_aule.add(0, new Aula("",strNomeUniversita,"",lat_pref,lng_pref,0,0,0," "));
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
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMapView(savedInstanceState);
        initUi();
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
        uiSettings.setZoomControlsEnabled(true);//inserisce i controlli sullo zoom, il piu e il meno
        uiSettings.setCompassEnabled(true);//inserisce la bussola muovendosi nello spazio della mappa
        uiSettings.setMyLocationButtonEnabled(true);

        for(Aula a : array_aule){
            if(!a.getNome().equals(strNomeUniversita)) gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            else gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(Aula a :array_aule){
                    if(a.getNome().compareTo(marker.getTitle())==0){
                        //creoDialog
                        final Dialog d = new Dialog(MapActivity.this);

                        d.setContentView(R.layout.dialog_info_marker);
                        //inizializzo variabili dialog
                        title_dialog_marker = d.findViewById(R.id.title_dialog_marker);
                        title_dialog_marker.setText(marker.getTitle());
                        nome_dialog_marker = d.findViewById(R.id.i_1a);
                        nome_dialog_marker.setText(a.getNome());

                        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                        d.show();

                    }

                }

                return false;
            }
        });

    }




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


}
