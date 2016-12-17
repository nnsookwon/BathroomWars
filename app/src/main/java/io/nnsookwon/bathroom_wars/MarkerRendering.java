package io.nnsookwon.bathroom_wars;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by nnsoo on 12/14/2016.
 */

class MarkerRendering extends DefaultClusterRenderer<ClusterItem> {
    SimpleDateFormat simpleDateFormat;
    public MarkerRendering(Context context, GoogleMap map,
                           ClusterManager<ClusterItem> clusterManager) {
        super(context, map, clusterManager);
        simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm", Locale.US);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
        ClusterMarkerLocation marker = (ClusterMarkerLocation) item;
        markerOptions.title(marker.getRestroom().getName());
        if (marker.getRestroomVisit() != null)
            markerOptions.snippet(simpleDateFormat.format(marker.getRestroomVisit().getTimeMilli()));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }


}