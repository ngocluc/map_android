package project.map_android;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final double MY_HOUSE_LAT = 10.784086,
			MY_HOUSE_LNG = 106.660772;
	private static final float DEFAULT_ZOOM = 15;

	GoogleMap mMap;
	Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if (serviceOK()) {
			setContentView(R.layout.map_default);
			if (initMap()) {
				gotoLocation(MY_HOUSE_LAT, MY_HOUSE_LNG, DEFAULT_ZOOM);
				Toast.makeText(mContext, "Bản đồ đã sẵn sàng ! ",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "Bản đồ chưa sẵn sàng ! ",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			setContentView(R.layout.activity_main);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
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

	private Boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrg = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map_default);
			mMap = mapFrg.getMap();
		}
		return (mMap != null);
	}

	public void gotoLocation(double lat, double lng) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
		mMap.moveCamera(update);
	}

	private void gotoLocation(double lat, double lng, float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}

	public void geoLocate(View v) throws IOException {
		hideKeyBoard(v);

		EditText et = (EditText) findViewById(R.id.txv_location_request);
		String location = et.getText().toString();
		Geocoder gc = new Geocoder(mContext);
		List<Address> list_add = gc.getFromLocationName(location, 1);
		String locality;
		if (!list_add.isEmpty()) {
			Address address = list_add.get(0);
			locality = address.getLocality();
			double lat = address.getLatitude();
			double lng = address.getLongitude();
			gotoLocation(lat, lng, DEFAULT_ZOOM);
		} else {
			locality = "";
		}

		Toast.makeText(mContext, locality, Toast.LENGTH_SHORT).show();
	}

	private void hideKeyBoard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mapTypeNormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.mapTypeHybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case R.id.mapTypeSatellite:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.mapTypeTerrain:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.mapTypeNone:
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
