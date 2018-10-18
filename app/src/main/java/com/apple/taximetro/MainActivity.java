package com.apple.taximetro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    EditText            editBanderazo, editPrecio;
    TextView            totalPagar, statusTxt, statusTxt2;
    Button              startButton, endButton;
    LocationManager     gpsManager;
    double              banderazo, precioHora, total = 0;
    int                 contador = 0;
    boolean             inicioViaje = false;
    List <Double>       latitudesList = new ArrayList<>();
    List <Double>       longitudesList = new ArrayList<>();

    private static double EARTH_RADIUS = 6371;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        editBanderazo = findViewById(R.id.editBanderazo);
        editPrecio =    findViewById(R.id.editPrecio);
        totalPagar =    findViewById(R.id.totalPagar);
        statusTxt =     findViewById(R.id.statusTxt);
        statusTxt2 =    findViewById(R.id.statusTxt2);
        startButton =   findViewById(R.id.startButton);
        endButton =     findViewById(R.id.endButton);

        startButton.setEnabled( false );
        endButton.setEnabled( false );

        gpsManager = (LocationManager)getSystemService( LOCATION_SERVICE );
        gpsManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, this );

        editPrecio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                startButton.setEnabled( true );
            }
        });

        // BOTON DE INCIIO
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                banderazo =  Double.parseDouble( editBanderazo.getText().toString() );
                precioHora = Double.parseDouble( editPrecio.getText().toString() );
                inicioViaje = true;
                endButton.setEnabled( true );
                longitudesList.clear();
                latitudesList.clear();
                statusTxt.setText("Localizandote...");
                statusTxt2.setText("");
                totalPagar.setText("");
            }
        });


        // BOTON DE FIN
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endButton.setEnabled( false );
                int finListLat = latitudesList.size();
                int finListLon = longitudesList.size();
                statusTxt2.setText( "latitud de fin: " + latitudesList.get( finListLat - 1) + "\n" );
                statusTxt2.append( "longitud de fin:" + longitudesList.get( finListLon - 1 ) );
                inicioViaje = false;
                total += banderazo;
                DecimalFormat dosDecimales = new DecimalFormat("#.00");
                totalPagar.setText("Total a pagar: " + dosDecimales.format( total ) );
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if( location.getLatitude() != 0 && inicioViaje ){
            latitudesList.add( location.getLatitude() );
            longitudesList.add( location.getLongitude() );
            statusTxt.setText( "latitud de inicio: " + latitudesList.get(0) + "\n" );
            statusTxt.append( "longitud de inicio: " + longitudesList.get(0) + "\n" );
            statusTxt.append( "Siguiendo viaje..." + latitudesList.size() );
            if( latitudesList.size() > 1 ){
                double distanciaMetros = distanceBetween( latitudesList.get( contador ), longitudesList.get( contador ),
                                        latitudesList.get( contador + 1), longitudesList.get( contador + 1) );
                contador += 1;
                totalPago( distanciaMetros );
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        statusTxt.setText("Gracias");
    }

    @Override
    public void onProviderDisabled(String provider) {
        statusTxt.setText( "Porfavor encienda su GPS." );
    }

    public double totalPago( double distanciaMetros ){
        double totalPriori = distanciaMetros * precioHora;
        total = total + totalPriori;
        totalPagar.setText("total : " + total + "\n");
        totalPagar.append("priori : " + totalPriori + "\n");
        totalPagar.append("m : " + distanciaMetros );
        return total;
    }


    public double distanceBetween( double lat1, double lon1, double lat2, double lon2){
        double dLat = toRadians( lat2 - lat1 );
        double dLon = toRadians( lon2 - lon1);

        double a = Math.sin( dLat / 2) * Math.sin( dLat / 2 ) + Math.cos( toRadians(lat1) ) * Math.cos( toRadians(lat2) ) * Math.sin( dLon / 2 ) * Math.sin( dLon / 2 );
        double c = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ));
        double d = EARTH_RADIUS * c;

        totalPago( d );
        return d;
    }

    private double toRadians( double degrees ) {
        return degrees * ( Math.PI / 180 );
    }
}
