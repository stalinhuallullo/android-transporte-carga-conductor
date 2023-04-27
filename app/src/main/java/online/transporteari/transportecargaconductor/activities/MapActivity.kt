package online.transporteari.transportecargaconductor.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import online.transporteari.transportecargaconductor.R
import online.transporteari.transportecargaconductor.databinding.ActivityMapBinding
import online.transporteari.transportecargaconductor.fragments.ModalBottomSheetBooking
import online.transporteari.transportecargaconductor.fragments.ModalBottomSheetMenu
import online.transporteari.transportecargaconductor.models.Booking
import online.transporteari.transportecargaconductor.providers.AuthProvider
import online.transporteari.transportecargaconductor.providers.BookingProvider
import online.transporteari.transportecargaconductor.providers.GeoProvider
import java.util.Timer

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val modalBooking = ModalBottomSheetBooking()
    private val modalMenu = ModalBottomSheetMenu()

    val timer = object : CountDownTimer(20000, 1000) {
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            modalBooking.dismiss()
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //getWindow().setStatusBarColor(getResources().getColor(R.color.green));
        //getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green)); //Define color blanco.


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this,locationRequest, false, false, this)

        localtionPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
        listenerBookings()

        // CLicking
        binding.btnConnect.setOnClickListener{ connectDriver()}
        binding.btnDisconnect.setOnClickListener{ disconnectDriver()}
        binding.imageViewMenu.setOnClickListener { showModalMenu() }
    }



    private fun listenerBookings() {
        bookingListener =  bookingProvider.getBooking().addSnapshotListener{ snapshot, e ->
            if(e != null) {
                Log.e("FIRESTORE", "Error getting bookings: ${e.message}")
                return@addSnapshotListener
            }
            if(snapshot != null) {
                if(snapshot.documents.size > 0) {
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if (booking?.status == "create") {
                        showModalBooking(booking!!)
                    }
                }
            }
        }
    }



    private fun showModalMenu() {
        modalMenu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
    }

    private fun showModalBooking(booking: Booking) {
        Toast.makeText(this, "MODAL MOSTRADO", Toast.LENGTH_SHORT).show()
        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET
        modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)
        timer.start()
    }




    val localtionPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission ->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ->{
                    Log.d("LOCALIZACION", "Permiso concedido")
                    checkIfDriverIsConnection()
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) ->{
                    Log.d("LOCALIZACION", "Permiso concedido con limitacion")
                    checkIfDriverIsConnection()
                }
                else ->{
                    Log.d("LOCALIZACION", "Permiso no concedido")
                }
            }
        }

    }

    private fun checkIfDriverIsConnection() {
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener {document ->
            if(document.exists()) {
                if(document.contains("l")) connectDriver()
                else showButtonDisconnect()
            }
            else {
                showButtonConnect()
            }
        }
    }

    private fun saveLocalization() {
        if(myLocationLatLng != null) {
            geoProvider.saveLocation(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if(myLocationLatLng != null) {
            showButtonConnect()
            geoProvider.removeLocation(authProvider.getId())
        }
    }
    private fun connectDriver() {
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        showButtonDisconnect()
    }

    private fun showButtonConnect() {
        binding.btnDisconnect.visibility = View.GONE
        binding.btnConnect.visibility = View.VISIBLE
    }
    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE
        binding.btnConnect.visibility = View.GONE
    }

    private fun addMarker() {
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if(markerDriver != null) {
            markerDriver!!.remove()
        }
        if(myLocationLatLng != null){
            markerDriver = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }
    }
    fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            70,
            150,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, 70, 150)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap?.isMyLocationEnabled = false
        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,R.raw.style_map_silver)
            )
            if(!success!!){
                Log.d("MAPAS", "No se encontro los estilos del mapa")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Error ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) {
        myLocationLatLng = LatLng(location.latitude, location.longitude)
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition
                .builder()
                .target(myLocationLatLng!!)
                .zoom(17f)
                .build()
        ))
        addMarker()
        saveLocalization()
    }

    override fun locationCancelled() {

    }
}