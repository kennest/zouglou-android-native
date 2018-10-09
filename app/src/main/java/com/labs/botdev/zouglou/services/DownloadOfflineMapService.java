package com.labs.botdev.zouglou.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.labs.botdev.zouglou.R;
import com.labs.botdev.zouglou.utils.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;


import org.json.JSONObject;

public class DownloadOfflineMapService extends Service {
    private static final String TAG = "OfflineMapService";

    private boolean isEndNotified;
    private ProgressBar progressBar;
    private MapView mapView;
    private OfflineManager offlineManager;

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));
        if (Constants.isNetworkConnected(getApplicationContext())) {
            //offlineMap();
            download();
        }
    }

//    private void offlineMap() {
//        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
//                "https://api.mapbox.com/styles/v1/bumblebee47/cjmhpisnl3eep2so8gt0a4fsz.html?fresh=true&title=true&access_token=pk.eyJ1IjoiYnVtYmxlYmVlNDciLCJhIjoiY2phdjA0Ym11MHFodjJ6bjAxbnF2NXdtayJ9.WW82rcFdL6_o4pVs1itgcQ#12.0/48.866500/2.317600/0",
//                new LatLngBounds.Builder()
//                        .include(new LatLng(8.21174, -5.68668))
//                        .include(new LatLng(8.21174, -5.68668))
//                        .build(),
//                2,
//                13,
//                getResources().getDisplayMetrics().density
//        );
//
//        NotificationOptions notificationOptions = NotificationOptions.builder(this)
//                .smallIconRes(R.drawable.mapbox_logo_icon)
//                .returnActivity(MapActivity.class.getName())
//                .build();
//
//        OfflinePlugin.getInstance(this).startDownload(
//                OfflineDownloadOptions.builder()
//                        .definition(definition)
//                        .metadata(OfflineUtils.convertRegionName("Ivory-Coast"))
//                        .notificationOptions(notificationOptions)
//                        .build()
//        );
//    }

    private void download() {
        offlineManager = OfflineManager.getInstance(this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(8.21174, -5.68668)) // Northeast
                .include(new LatLng(8.21174, -5.68668)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                "https://api.mapbox.com/styles/v1/bumblebee47/cjmhpisnl3eep2so8gt0a4fsz.html?fresh=true&title=true&access_token=pk.eyJ1IjoiYnVtYmxlYmVlNDciLCJhIjoiY2phdjA0Ym11MHFodjJ6bjAxbnF2NXdtayJ9.WW82rcFdL6_o4pVs1itgcQ#12.0/48.866500/2.317600/0",
                latLngBounds,
                10,
                20,
                this.getResources().getDisplayMetrics().density);

        // Set the metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, "Ivory-Coast");
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        offlineManager.createOfflineRegion(
                definition,
                metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Display the download progress bar

                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage and update the progress bar
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                        0.0;

                                if (status.isComplete()) {
                                    // Download complete
                                    Toast.makeText(getApplicationContext(),"Téléchargement de la carte Terminé!!!",Toast.LENGTH_LONG).show();
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    // Switch to determinate state
                                    Toast.makeText(getApplicationContext(),"Erreur de telechargement",Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Toast.makeText(getApplicationContext(),"Error:"+error.getReason(),Toast.LENGTH_LONG).show();
                                Toast.makeText(getApplicationContext(),"Error:"+error.getMessage(),Toast.LENGTH_LONG).show();
                                Log.e(TAG, "onError reason: " + error.getReason());
                                Log.e(TAG, "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getApplicationContext(),"Error:"+error,Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error: " + error);
                    }
                });
    }

//    public void onPause() {
//        mapView.onPause();
//        if (offlineManager != null) {
//            offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
//                @Override
//                public void onList(OfflineRegion[] offlineRegions) {
//                    if (offlineRegions.length > 0) {
//                        // delete the last item in the offlineRegions list which will be yosemite offline map
//                        offlineRegions[(offlineRegions.length - 1)].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
//                            @Override
//                            public void onDelete() {
//                                Toast.makeText(
//                                        getApplicationContext(),
//                                        getString(R.string.basic_offline_deleted_toast),
//                                        Toast.LENGTH_LONG
//                                ).show();
//                            }
//
//                            @Override
//                            public void onError(String error) {
//                                Log.e(TAG, "On Delete error: " + error);
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onError(String error) {
//                    Log.e(TAG, "onListError: " + error);
//                }
//            });
//        }
//    }
}

