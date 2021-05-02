package com.example.hello

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.bluetooth.BluetoothHidDevice
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.util.*
class MapsActivity : AppCompatActivity(), OnMapReadyCallback  , LocationListener, GoogleMap.OnCameraMoveListener,GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener
{
    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    lateinit var tvCurrentAddress: TextView
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private var DEFAULT_ZOOM = 15f
    private var latitude = 0.0
    private var longitude = 0.0
    private var end_latitude = 0.0
    private var end_longitude = 0.0
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
        else
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                map.isMyLocationEnabled = true
            }
            else {
                Toast.makeText(this, "User has not granted location access permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        getCurrentLocation()
        tvCurrentAddress=findViewById(R.id.tvAdd)

    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationAccess()
        map.uiSettings.isZoomControlsEnabled=true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
            {
                map!!.isMyLocationEnabled = true
            }
        }
        else
            map!!.isMyLocationEnabled = true
        map!!.setOnCameraMoveListener (this)
        map!!.setOnCameraMoveStartedListener (this)
        map!!.setOnCameraIdleListener(this)
    }
    override fun onLocationChanged(location : Location) {
        val geocoder=Geocoder(this, Locale.getDefault())
        var addresses:List<Address>?=null
        try{
            addresses=geocoder.getFromLocation(location.latitude, location.longitude,1)
        }catch (e:IOException){
            e.printStackTrace()
        }
        setAddress(addresses!![0])
    }
    private fun setAddress(addresses: Address) {
        if(addresses.getAddressLine(0)!=null){
            // tvCurrentAddress!!.setText(addresses.getAddressLine(0))
        }
        if(addresses.getAddressLine(1)!=null){
            // tvCurrentAddress!!.setText(tvCurrentAddress.getText().toString()+addresses.getAddressLine(1))
        }
    }
    override fun onStatusChanged(addresses: String?, status: Int, extras: Bundle?) {
    }
    override fun onProviderEnabled(addresses: String) {
    }
    override fun onProviderDisabled(addresses: String) {
    }
    override fun onCameraMove() {
    }
    override fun onCameraMoveStarted(addresses: Int) {
    }
    override fun onCameraIdle() {
        var addresses: List<Address>?=null
        val geocoder=Geocoder(this,Locale.getDefault())
        try{
            addresses=geocoder.getFromLocation(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude,1)
            setAddress(addresses!![0])
        }catch (e:IndexOutOfBoundsException){
            e.printStackTrace()}
        catch (e:IOException) {
            e.printStackTrace()
        }}
    fun searchLocation(view: View) {
        val locationSearch:EditText = findViewById<EditText>(R.id.TF_location)
        lateinit var location: String
        location = locationSearch.text.toString()
        var addressList: List<Address>? = null
        if (location == null || location == "") {
            Toast.makeText(applicationContext,"provide location",Toast.LENGTH_SHORT).show()
        }
        else{
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            Toast.makeText(applicationContext, address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()
            end_latitude = address.latitude
            end_longitude = address.longitude
            var results = FloatArray(10)
            Location.distanceBetween(latitude,longitude,end_latitude,end_longitude,results)
            val s = String.format("%,1f",results[0]/1000)
            map!!.addMarker(MarkerOptions().position(latLng).title(location).snippet("Distance =$s Km"))
            map!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
           // Toast.makeText(this,"Distance : $s Km",Toast.LENGTH_SHORT).show()
            tvCurrentAddress!!.setText("Distance :$s Km")
        }
    }
    private fun getCurrentLocation() {
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        try {
            @SuppressLint("MissingPermission") val location = fusedLocationProviderClient!!.getLastLocation()
            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(p0: Task<Location>) {
                    if (p0.isSuccessful) {
                        val currentLocation = p0.result as Location?
                        if (currentLocation != null) {
                            map!!.addMarker(MarkerOptions().position(LatLng(currentLocation.latitude,currentLocation.longitude)))
                            moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM)
                            latitude = currentLocation.latitude
                            longitude = currentLocation.longitude
                        } else {
                            getLocationAccess()
                        }
                    }
                }
            })
        }
        catch (se: Exception)
        {
            Log.d("TAG", "Security Exception")
        }
    }
    private fun moveCamera(latLng: LatLng, defaultZoom: Float)
    {
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom))
    }
}