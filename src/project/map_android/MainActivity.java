package project.map_android;

import java.util.Calendar;
import java.util.HashMap;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements RoutingListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final double LAT_BV_BINH_DAN = 10.774571,
			LNG_BV_BINH_DAN = 106.681412, LAT_TT_CONG_NGHE = 10.780789,
			LNG_TT_CONG_NGHE = 106.686176, LAT_BCH_QUAN_SU = 10.777245,
			LNG_BCH_QUAN_SU = 106.675188;
	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final float DEFAULT_ZOOM = 15;
	GoogleMap mMap;
	Context mContext;
	LocationClient mLocationClient;
	HashMap<String, Marker> marker = new HashMap<String, Marker>();
	Polyline polyline;
	Marker startMarker, endMarker;
	LatLng start = null, end = null;
	String end_locality;
	Boolean blankpage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (serviceOK() && networkOK()) {
			setContentView(R.layout.map_default);
			blankpage = false;
			if (initMap()) {
				gotoLocation(LAT_BV_BINH_DAN, LNG_BV_BINH_DAN, DEFAULT_ZOOM);
				Calendar c = Calendar.getInstance();
				int time = c.get(Calendar.HOUR_OF_DAY);
				Toast.makeText(mContext,
						"Bây giờ là " + time + " giờ. Bản đồ đã sẵn sàng ! ",
						Toast.LENGTH_SHORT).show();
				setMarker("BV Bình Dân", LAT_BV_BINH_DAN, LNG_BV_BINH_DAN);
				setMarker("Trung Tâm Công Nghệ", LAT_TT_CONG_NGHE,
						LNG_TT_CONG_NGHE);
				setMarker("BCH Quân sự", LAT_BCH_QUAN_SU, LNG_BCH_QUAN_SU);
				// must user this because mContext is a Context but not a
				// ConnectionCallBack..v..v..
				mLocationClient = new LocationClient(this, this, this);
				mLocationClient.connect();

			} else {
				Toast.makeText(mContext, "Bản đồ chưa sẵn sàng ! ",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			setContentView(R.layout.activity_main);
			blankpage = true;
		}
	}

	private void gotoLocation(double lat, double lng, float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public Boolean serviceOK() {
		int isAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mContext);
		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		} else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,
					this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		} else {
			Toast.makeText(mContext, "Không kết nối được với Google Play ! ",
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	public Boolean networkOK() {
		try {
			ConnectivityManager nInfo = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				Log.i("Network", "Network available: true");
				return true;
			} else {
				Log.i("Network", "Network available: false");
				Toast.makeText(mContext, "Không có kết nối mạng! ",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private Boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrg = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_default);
			mMap = mapFrg.getMap();
			if (mMap != null) {
				mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

					@Override
					public View getInfoWindow(Marker arg0) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public View getInfoContents(Marker marker) {
						View v = getLayoutInflater().inflate(
								R.layout.marker_info, null);
						TextView tvLocality = (TextView) v
								.findViewById(R.id.marker_info_name);
						tvLocality.setText(marker.getTitle());
						return v;
					}

				});
				mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker marker) {
						getDirection(marker);
					}
				});
			}

		}
		return (mMap != null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (blankpage == false) {
			MapStateManager mgr = new MapStateManager(mContext);
			mgr.saveMapState(mMap, endMarker);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MapStateManager mgr = new MapStateManager(mContext);
		CameraPosition position = mgr.getSavedMapLocation();
		int mapType = mgr.getSavedMapType();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory
					.newCameraPosition(position);
			mMap.moveCamera(update);
			mMap.setMapType(mapType);
		}
	}

	public LatLng getCurrentLocation() {
		LatLng ll = null;
		Location currentLocation = mLocationClient.getLastLocation();
		if (currentLocation == null) {
			Toast.makeText(mContext, "Current location isn't available",
					Toast.LENGTH_SHORT);
		} else {
			ll = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
		}
		return ll;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(mContext, "Connected to location service",
				Toast.LENGTH_SHORT).show();

		// can not call getDirection in onResume like getStateMap because method
		// getLastLocation() can't be called if not Connected
		MapStateManager mgr = new MapStateManager(mContext);
		String loc_end_marker = mgr.getLocalityEndMarker();
		if (!loc_end_marker.equals("")) {
			Log.i("my location", "My location is: "
					+ mLocationClient.getLastLocation().getLatitude() + " - "
					+ mLocationClient.getLastLocation().getLongitude());
			getDirection(marker.get(loc_end_marker));
		}

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	public void setMarker(String locality, double lat, double lng) {
		MarkerOptions options = new MarkerOptions();
		options.title(locality)
				.position(new LatLng(lat, lng))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.default_marker));
		marker.put(locality, mMap.addMarker(options));
	}

	public void setStartMarker(String locality, double lat, double lng) {
		MarkerOptions options = new MarkerOptions();
		options.title(locality)
				.position(new LatLng(lat, lng))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.start_blue));
		startMarker = mMap.addMarker(options);
	}

	public void setEndMarker(String locality, double lat, double lng) {
		MarkerOptions options = new MarkerOptions();
		options.title(locality)
				.position(new LatLng(lat, lng))
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.end_green));
		endMarker = mMap.addMarker(options);
		marker.get(locality).remove();
	}

	public void removeRoutingMarker() {
		if (startMarker != null) {
			startMarker.remove();
		}

		if (endMarker != null) {
			MarkerOptions options = new MarkerOptions();
			options.title(endMarker.getTitle())
					.position(endMarker.getPosition())
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.default_marker));
			marker.put(endMarker.getTitle(), mMap.addMarker(options));
			endMarker.remove();
		}
	}

	@Override
	public void onRoutingFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRoutingStart() {
		// TODO Auto-generated method stub
	}

	public void getDirection(Marker endMarker) {
		start = getCurrentLocation();
		end = endMarker.getPosition();
		end_locality = endMarker.getTitle();
		Routing routing = new Routing(Routing.TravelMode.DRIVING);
		routing.registerListener(this);
		routing.execute(start, endMarker.getPosition());
	}

	@Override
	public void onRoutingSuccess(PolylineOptions mPolyOptions) {
		removeRoutingMarker();
		setPolyline(mPolyOptions);
		setStartMarker("You Here", start.latitude, start.longitude);
		setEndMarker(end_locality, end.latitude, end.longitude);
	}

	public void removePolylin() {
		if (polyline != null) {
			polyline.remove();
		}
	}

	public void setPolyline(PolylineOptions mPolyOptions) {
		removePolylin();
		PolylineOptions polyoptions = new PolylineOptions();
		polyoptions.color(Color.BLUE);
		polyoptions.width(7);
		polyoptions.addAll(mPolyOptions.getPoints());
		polyline = mMap.addPolyline(polyoptions);
	}
}
