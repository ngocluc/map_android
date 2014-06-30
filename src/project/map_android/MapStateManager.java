package project.map_android;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import android.content.Context;
import android.content.SharedPreferences;

public class MapStateManager {
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String ZOOM = "zoom";
	private static final String BEARING = "bearing";
	private static final String TILT = "tilt";
	private static final String MAPTYPE = "maptype";
	private static final String PREFS_NAME = "mapCameraManager";
	private static final String LOC_ENDMARKER = "localityEndMarker";

	private SharedPreferences mapStatePrefs;

	public MapStateManager(Context context) {
		mapStatePrefs = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
	}

	public void saveMapState(GoogleMap map, Marker endMarker) {
		SharedPreferences.Editor editor = mapStatePrefs.edit();
		CameraPosition camera = map.getCameraPosition();
		editor.putFloat(LATITUDE, (float) camera.target.latitude);
		editor.putFloat(LONGITUDE, (float) camera.target.longitude);
		editor.putFloat(ZOOM, camera.zoom);
		editor.putFloat(BEARING, camera.bearing);
		editor.putFloat(TILT, camera.tilt);
		editor.putInt(MAPTYPE, map.getMapType());
		editor.putString(LOC_ENDMARKER, endMarker.getTitle());
		editor.commit();

	}

	public CameraPosition getSavedMapLocation() {
		double latitude = (double) mapStatePrefs.getFloat(LATITUDE, 0);
		if (latitude == 0) {
			return null;
		}
		double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
		LatLng target = new LatLng(latitude, longitude);

		float bearing = mapStatePrefs.getFloat(BEARING, 0);
		float zoom = mapStatePrefs.getFloat(ZOOM, 0);
		float tilt = mapStatePrefs.getFloat(TILT, 0);
		CameraPosition positon = new CameraPosition(target, zoom, tilt, bearing);
		return positon;

	}

	public String getLocalityEndMarker() {
		if (mapStatePrefs.contains(LOC_ENDMARKER)) {
			return mapStatePrefs.getString(LOC_ENDMARKER, "");
		}
		return "";
	}

	public int getSavedMapType() {
		int mapType = mapStatePrefs.getInt(MAPTYPE, 0);
		return mapType;
	}
}