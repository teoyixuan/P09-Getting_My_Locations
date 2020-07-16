package sg.edu.rp.c346.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyService extends Service {

    boolean started;
    String folderLocation;
    FusedLocationProviderClient client;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        Log.d("Service", "Service created");
        super.onCreate();

        if (checkPermission() == true){
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
        }

        client = LocationServices.getFusedLocationProviderClient(this);

        createLocationCallBack();
        // creating a new file call "/P09"
        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";

        File folder = new File(folderLocation);
        if (folder.exists() == false){
            boolean result = folder.mkdir(); //The function returns true if directory is created else returns false.
            if (result == true){
                Toast.makeText(MyService.this, "Folder created in External memory", Toast.LENGTH_SHORT).show();
                Log.d("Fole Read/Write", "Folder Created ");
                stopSelf();
            }
        }
    }

    private void createLocationCallBack() { // update and write new data inside
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult){
                if (locationResult != null){
                    Location locData = locationResult.getLastLocation();
                    String data = locData.getLatitude() + "," + locData.getLongitude();
                    Log.d("Service - loc change", data);

                    String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
                    File targetFile = new File(folderLocation, "data.txt");

                    try{
                        FileWriter writer = new FileWriter(targetFile,true);
                        writer.write(data + "\n");
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        Toast.makeText(MyService.this, "Failed to write!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            };
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (started == false){
            started = true;
            Log.d("Service", "Service started");
            if(checkPermission() == true){
                client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }

        } else {
            Log.d("Service", "Service is still running");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Service exited");
        super.onDestroy();
    }

    private  boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            return true;
        } else{
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MyService.this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
