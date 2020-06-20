package com.example.appaulestudio;

import androidx.annotation.NonNull;
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
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    static final String URL_AULE_APERTE= "http://pmsc9.altervista.org/progetto/map_check_aule_aperte.php";
    static final String URL_AULE_POSTI= "http://pmsc9.altervista.org/progetto/map_check_posti_liberi.php";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mapView;
    private GoogleMap gmap;
    Intent intent;
    Bundle bundle;
    ArrayList<Aula> array_aule;
    Spinner spinner_map;
    Button btn_to_home;
    Adapter adapter;
    LinearLayout ll_dist_dur;
    ImageView img_dist;
    TextView txt_dist, txt_dur;

    //posizioni
    private Double lat_uni, lng_uni;
    LatLng my_position=null;
    LatLng destinazione=null;
    Marker marker_my_position=null;
    List<Marker> markerList=new LinkedList<Marker>();
    LocationManager locationManager;
    LocationListener locationListener;
    boolean percorso_mostrato=false;
    String mode=null;
    List<LatLng> polylinesPoints=new LinkedList<LatLng>();
    List<LatLng> polylinesRoads=new LinkedList<LatLng>();
    PolylineOptions plo;
    DownloadTask task;
    String lunghezza=null;
    String durata=null;

    String strNomeUniversita, strUniversita, strMatricola, strNome, strCognome,strIngresso, strPausa;
    boolean connesso_orari=false, connesso_posti=false;
    SqliteManager database;
    Aula aulaSelezionata_spinner=null;
    Aula aulaSelezionata_marker=null;

    private void initUi(){
        btn_to_home=findViewById(R.id.btn_to_listaaule);
        spinner_map=findViewById(R.id.spinner_map);
        ll_dist_dur=findViewById(R.id.ll_dist_dur);
        img_dist=findViewById(R.id.img_distance);
        txt_dist=findViewById(R.id.txt_distance);
        txt_dur=findViewById(R.id.txt_duration);
        ll_dist_dur.setVisibility(View.GONE);

        //preferenze
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        lat_uni=Double.parseDouble(settings.getString("latitudine", null));
        lng_uni=Double.parseDouble(settings.getString("longitudine", null));
        strNomeUniversita=settings.getString("nome_universita", null);
        strUniversita=settings.getString("universita", null);
        strMatricola=settings.getString("matricola", null);
        strNome=settings.getString("nome", null);
        strCognome=settings.getString("cognome", null);
        strIngresso=settings.getString("ingresso",null);
        strPausa=settings.getString("pausa",null);
        //
        MyToast.makeText(getApplicationContext(),strIngresso+" "+strPausa
                +" "+settings.getString("slot",null)+" "+settings.getString("first_slot",null)
                +" "+settings.getString("last_update",null),true).show();

        //intent
        intent = getIntent();
        bundle = intent.getBundleExtra("bundle_aule");
        array_aule = bundle.getParcelableArrayList("aule");


        //spinner
        array_aule.add(0, new Aula(strUniversita,strNomeUniversita,"",lat_uni,lng_uni,0,0,0,""));
        adapter = new ArrayAdapter<>(MapActivity.this, android.R.layout.simple_list_item_1, array_aule);
        spinner_map.setAdapter((SpinnerAdapter) adapter);
        spinner_map.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aulaSelezionata_spinner = (Aula) parent.getItemAtPosition(position);
                LatLng selected = new LatLng(aulaSelezionata_spinner.getLatitudine(),aulaSelezionata_spinner.getLongitudine());
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(selected,17));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //back home
        btn_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, Home.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
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
        d.setCancelable(true);
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
        database=new SqliteManager(MapActivity.this);
        getLocation();
        verifyPermissions();
        new auleAperte().execute();
        new posti_aule().execute();
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

        //IMPOSTO MAPPA
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        try{
            gmap.setMyLocationEnabled(true);
        } catch (Exception e) { }
        uiSettings.setMyLocationButtonEnabled(true);

        //AGGIUNGO MARKERS
        for(Aula a : array_aule){
            Marker marker=null;
            if(!a.getNome().equals(strNomeUniversita)) marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            else marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            markerList.add(marker);
        }

        //MARKER CLICK LISTENER
        gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTitle().equals("Tu sei qui")) return false;
                for(final Aula a :array_aule){
                    if(a.getNome().compareTo(marker.getTitle())==0){
                        aulaSelezionata_marker=a;
                        //creoDialog
                        final Dialog d = new Dialog(MapActivity.this);
                        d.setContentView(R.layout.dialog_info_marker);
                        d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);
                        TableRow row_luogo=d.findViewById(R.id.row_luogo);
                        TableRow row_aperta=d.findViewById(R.id.row_aperta);
                        TableRow row_posti=d.findViewById(R.id.row_posti);
                        TableRow row_posti_liberi=d.findViewById(R.id.row_posti_liberi);
                        TextView txt_nome_aula= d.findViewById(R.id.marker_nome);
                        TextView txt_indirizzo= d.findViewById(R.id.marker_indirizzo);
                        TextView txt_luogo= d.findViewById(R.id.marker_luogo);
                        TextView txt_posti= d.findViewById(R.id.marker_posti);
                        TextView txt_posti_liberi= d.findViewById(R.id.marker_posti_liberi);
                        TextView txt_aperta= d.findViewById(R.id.marker_aperta);
                        Button btn_percorso= d.findViewById(R.id.button_indicazioni);
                        Button btn_to_aula= d.findViewById(R.id.button_aula);
                        if(a.getNome().equals(strNomeUniversita)){
                            row_aperta.setVisibility(View.GONE);
                            row_posti.setVisibility(View.GONE);
                            row_posti_liberi.setVisibility(View.GONE);
                            row_luogo.setVisibility(View.GONE);
                            btn_to_aula.setVisibility(View.GONE);
                        }
                        if(connesso_orari==false || connesso_posti==false){
                            if(connesso_orari==false) row_aperta.setVisibility(View.GONE);
                            if(connesso_posti==false) row_posti_liberi.setVisibility(View.GONE);
                        }
                        if(my_position==null) btn_percorso.setVisibility(View.GONE);
                        txt_nome_aula.setText(a.getNome());
                        txt_luogo.setText(a.getLuogo());
                        txt_posti.setText(""+a.getPosti_totali());
                        String indirizzo=reverseGeocoding(a.getLatitudine(),a.getLongitudine());
                        if(indirizzo!=null) txt_indirizzo.setText(indirizzo);
                        if(connesso_orari==true){
                            if(a.isAperta()==true){
                                txt_aperta.setText("Aperta");
                                txt_aperta.setTextColor(Color.argb(255, 12, 138, 17));
                            }
                            else{
                                txt_aperta.setText("Chiusa");
                                txt_aperta.setTextColor(Color.RED);
                                row_posti_liberi.setVisibility(View.GONE);
                            }
                        }
                        if(connesso_orari==true && connesso_posti==true && a.isAperta()) txt_posti_liberi.setText(""+a.getPosti_liberi());
                        else txt_posti_liberi.setVisibility(View.GONE);

                        //bottone vai ad aula
                        btn_to_aula.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(MapActivity.this,InfoAulaActivity.class);
                                Bundle bundle=new Bundle();
                                bundle.putParcelable("aula",a);
                                HashMap<Integer,Orario> orari_aula=database.readOrariAula(a.getIdAula());
                                bundle.putSerializable("orari",orari_aula);
                                intent.putExtra("bundle", bundle);
                                startActivityForResult(intent, 3);
                                finish();
                            }
                        });

                        //bottone calcola percorso --> mostra dialog successivo
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
                                ImageView img_car=d_mezzo.findViewById(R.id.img_car);
                                ImageView img_walk=d_mezzo.findViewById(R.id.img_walk);
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
                                        percorso_mostrato=true;
                                        destinazione=new LatLng(aulaSelezionata_marker.getLatitudine(),aulaSelezionata_marker.getLongitudine());
                                        resetMap();
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

    //resetta markers e polylines quando cambia posizione oppure scelgo un altra aula
    private void resetMap(){
        gmap.clear(); //pulisco mappa
        marker_my_position=gmap.addMarker(new MarkerOptions()
                .position(my_position)
                .title("Tu sei qui")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        for(Aula a : array_aule){
            Marker marker=null;
            if(!a.getNome().equals(strNomeUniversita)) marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            else marker=gmap.addMarker(new MarkerOptions().position(new LatLng(a.getLatitudine(), a.getLongitudine())).title(a.getNome()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            markerList.add(marker);
        }
        if (percorso_mostrato==true){
            calcolaPercorso(destinazione);
        }
        //gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(my_position,17));
    }

    //rileva posizione
    private void getLocation(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                my_position=new LatLng(location.getLatitude(),location.getLongitude()); // prendo mia posizione
                resetMap();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    //calcola percorso
    public void calcolaPercorso(LatLng destinazione){
        task=new DownloadTask();
        task.execute("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                my_position.latitude+","+my_position.longitude +
                "&destination=" +
                destinazione.latitude+","+destinazione.longitude +
                "&mode="+ mode +
                "&key=AIzaSyC7a_cSKvoRh6u-ccqs8WF-1XBXT6crVkY");
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result="";
            URL url;
            HttpsURLConnection urlConnection;
            try {
                url=new URL(strings[0]);
                urlConnection= (HttpsURLConnection) url.openConnection();
                InputStream in=urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                int data=reader.read();
                while(data!=-1){
                    char cur = (char) data;
                    result+=cur;
                    data=reader.read();
                }
                return result;
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String stringa) {
            super.onPostExecute(stringa);
            try {
                JSONObject jsonObject=new JSONObject(stringa);

                String routes=jsonObject.getString("routes");
                JSONArray arrayRoutes=new JSONArray(routes);
                JSONObject primaRoute=arrayRoutes.getJSONObject(0);

                String legs=primaRoute.getString("legs");
                JSONArray arrayLegs=new JSONArray(legs);
                JSONObject primaLeg=arrayLegs.getJSONObject(0);

                String lenght=primaLeg.getString("distance");
                JSONObject obj_lunghezza = new JSONObject(lenght);
                lunghezza=obj_lunghezza.getString("text");
                String duration=primaLeg.getString("duration");
                JSONObject obj_duration=new JSONObject(duration);
                durata=obj_duration.getString("text");

                String steps=primaLeg.getString("steps");
                JSONArray arraySteps=new JSONArray(steps);

                polylinesPoints.clear();
                polylinesPoints.add(new LatLng(my_position.latitude,my_position.longitude));
                for(int i=0;i<arraySteps.length();i++){
                    JSONObject step=arraySteps.getJSONObject(i);
                    String lat=step.getJSONObject("end_location").getString("lat");
                    String lon=step.getJSONObject("end_location").getString("lng");
                    polylinesPoints.add(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                }

                //disegno percorso
                    if(mode.equals("walking")) drawPolylines(); //usa solo Directions API
                    else calcolaStrade(polylinesPoints); //usa Roads API
                //mostro lunghezza e durata
                ll_dist_dur.setVisibility(View.VISIBLE);
                if(mode.equals("walking")) img_dist.setImageDrawable(getResources().getDrawable(R.drawable.walk));
                else img_dist.setImageDrawable(getResources().getDrawable(R.drawable.auto));
                txt_dist.setText(lunghezza);
                txt_dur.setText(durata);
                //centro mappa su posizione attuale
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(my_position,17));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //calcola strade
    public void calcolaStrade(List<LatLng> lista_pos){
        DownloadRoads task_roads=new DownloadRoads();
        String uri="";
        uri+="https://roads.googleapis.com/v1/snapToRoads?path=";

        for (LatLng trackPoint : lista_pos) {
            uri+=""+trackPoint.latitude;
            uri+=",";
            uri+=""+trackPoint.longitude;
            uri+="|";
        }
        uri=uri.substring(0,uri.length()-1);
        uri+="&interpolate=true";
        uri+="&key=AIzaSyC7a_cSKvoRh6u-ccqs8WF-1XBXT6crVkY";
        Log.i("myLog", uri);
        task_roads.execute(uri);
    }
    private class DownloadRoads extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String result="";
            URL url;
            HttpsURLConnection urlConnection;
            try {
                url=new URL(strings[0]);
                urlConnection= (HttpsURLConnection) url.openConnection();
                InputStream in=urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                int data=reader.read();
                while(data!=-1){
                    char cur = (char) data;
                    result+=cur;
                    data=reader.read();
                }
                return result;
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String stringa) {
            super.onPostExecute(stringa);
            try {
                JSONObject jsonObject = new JSONObject(stringa);
                JSONArray snappedPointsArr = jsonObject.getJSONArray("snappedPoints");
                polylinesRoads.clear();
                for (int i = 0; i < snappedPointsArr.length(); i++) {
                    JSONObject snappedPointLocation = ((JSONObject) (snappedPointsArr.get(i))).getJSONObject("location");
                    double lattitude = snappedPointLocation.getDouble("latitude");
                    double longitude = snappedPointLocation.getDouble("longitude");
                    polylinesRoads.add(new LatLng(lattitude, longitude));
                    //disegno percorso
                    drawPolylinesRoads();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public void drawPolylines(){
        plo=new PolylineOptions();
        plo.color(Color.RED);
        plo.width(10);
        for(LatLng latLng:polylinesPoints){
            //map.clear();
            plo.add(latLng);
            plo.color(Color.GREEN);
            plo.width(10);
            plo.geodesic(true);
        }
        gmap.addPolyline(plo);
    }
    public void drawPolylinesRoads(){
        plo=new PolylineOptions();
        plo.color(Color.RED);
        plo.width(10);
        for(LatLng latLng:polylinesRoads){
            plo.add(latLng);
            plo.color(Color.RED);
            plo.width(10);
        }
        gmap.addPolyline(plo);
    }

    //reverse geocoding
    private String reverseGeocoding(double lat, double lng){ //chiamato quando apro il dialog dell'aula
        String indirizzo=null;
        Geocoder geocoder=new Geocoder(MapActivity.this, Locale.ITALY);
        try {
            List<Address> addresses=geocoder.getFromLocation(lat, lng,1);
            Log.i("myLog", addresses.toString());
            indirizzo=addresses.get(0).getAddressLine(0);
            //Toast.makeText(getApplicationContext(),"Ecco l'indirizzo: "+indirizzo, Toast.LENGTH_LONG).show();
            return indirizzo;
        } catch (IOException e) {
            e.printStackTrace();
            return indirizzo;
        }
    }

    //permessi
    public void verifyPermissions() {
        if(Build.VERSION.SDK_INT>=23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            }
            else locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
        }
        else locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==3 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                gmap.setMyLocationEnabled(true);
            }
        }
    }


    //task aule aperte e posti liberi
    private class auleAperte extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArray;
                url = new URL(URL_AULE_APERTE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "codice_universita="+ URLEncoder.encode(strUniversita, "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
                dos.flush();
                dos.close();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                sb = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                jArray = new JSONArray(result);
                for(int i = 0; i<jArray.length(); i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    String id=json_data.getString("id_aula");
                    for(Aula a: array_aule){
                        if(a.getIdAula().equals(id)) a.setAperta(true);
                    }
                }
                return "ok";
            }  catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null) connesso_orari=true;
        }
    }

    private class posti_aule extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArray;
                url = new URL(URL_AULE_POSTI); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                params = "codice_universita="+ URLEncoder.encode(strUniversita, "UTF-8") +
                        "&ingresso="+ URLEncoder.encode(strIngresso, "UTF-8") +
                        "&pausa="+ URLEncoder.encode(strPausa, "UTF-8");
                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
                dos.flush();
                dos.close();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                sb = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                jArray = new JSONArray(result);
                for(Aula aa:array_aule) aa.setPosti_liberi(aa.getPosti_totali());
                for(int i = 0; i<jArray.length(); i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    String id=json_data.getString("id_aula");
                    int posti_occupati=json_data.getInt("posti_occupati");
                    for(Aula a: array_aule){
                        if(a.getIdAula().equals(id)) a.setPosti_liberi(a.getPosti_totali()-posti_occupati);
                    }
                }
                return "ok";
            }  catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result!=null) connesso_posti=true;
        }
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

    @Override
    protected void onRestart() {
        super.onRestart();
        getLocation();
        verifyPermissions();
        new auleAperte().execute();
        new posti_aule().execute();
    }
}
