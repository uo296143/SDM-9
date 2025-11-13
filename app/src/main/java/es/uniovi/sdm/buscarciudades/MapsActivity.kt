package es.uniovi.sdm.buscarciudades

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import es.uniovi.sdm.buscarciudades.data.GestorCiudades
import es.uniovi.sdm.buscarciudades.data.Resultados
import es.uniovi.sdm.buscarciudades.databinding.ActivityMapsBinding
import java.util.stream.IntStream.range

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

	// Para recuperar las ciudades aleatoriamente y gestionar puntuaciones
    private val DEV_CIUDADES= 5 // Ciudades devueltas en cada partida
    private val gc = GestorCiudades(DEV_CIUDADES)
    private val res= Resultados()

    private lateinit var posCiudad: LatLng
    private var marcadorUsuario: Marker?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
		
		activateEvents()
    }

    private fun activateEvents() {
        binding.botonAceptar.setOnClickListener {
            onClickAceptar()
        }

        binding.botonSiguiente.setOnClickListener {
            onClickSiguiente()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val valdesSalas= LatLng(43.355115, -5.851297)
        //mMap.addMarker(MarkerOptions().position(valdesSalas).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(valdesSalas))
        // Mueve la cámara instantáneamente a Oviedo con zoom 15

        // mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled =false
        mMap.uiSettings.isScrollGesturesEnabled = false
        setUpMap();
        setEvent()
    }

    private fun setEvent() {
        mMap.setOnMapClickListener { punto ->
            marcadorUsuario?.remove()
            // Crea un marcador en el punto y lo añade al mapa
            val marcadorOpciones = MarkerOptions()
                .position(punto)
                .title("Marcador creado por el usuario")
            marcadorUsuario = mMap.addMarker(marcadorOpciones)

        }
    }

    private fun setUpMap(){
        val peninsulaBounds = LatLngBounds(LatLng(35.5, -10.0),LatLng(44.0, 3.5) );
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(peninsulaBounds, 15f))
        // Zoom para visualizar un rectángulo definido por las esquinas
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(peninsulaBounds, 1080,
        1080, 0))
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        siguienteCiudad()
    }

    private fun siguienteCiudad() {
        // pasamos a la siguiente ciudad y la ponemos en el campo
        try {
            val c= gc.nextCiudad()
            if (c!=null) {
                binding.campoCiudad.text = c.nombre
                posCiudad = LatLng(c.latitud, c.longitud)
            }
        } catch (e: NoSuchElementException) {
            finalCiudades(res)
        }
    }

    private fun finalCiudades(res: Resultados) {
        // 1. Instancia un <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code>
        // con su constructor
        val builder = AlertDialog.Builder(this@MapsActivity)

        // 2. Encadena varios setters para establecer las
        // características del diálogo
        builder.setMessage("No hay más ciudades\nTu puntuación final es: ${res.puntos} puntos")//R.string.dialog_message)
            .setTitle("Fin del juego")//R.string.dialog_title)

        // 3. Obtiene la referencia a <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code>
        // desde <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        val dialog = builder.create()
        dialog.show()

        // reinicio juego
        gc.reiniciarCiudades(DEV_CIUDADES)
        res.reiniciaPuntos()
    }
	
	fun onClickAceptar() {
        var radio:Double = 50000.0
        mMap.addMarker(MarkerOptions()
            .position(posCiudad)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.estrella32r))
            .title(binding.campoCiudad.text.toString()))

        val colores = listOf(0xFF0000FF, 0xFFFF0000, 0xFF00FF00)
        for(i in range(0,3) ){
            mMap.addCircle(
                CircleOptions().center(posCiudad)
                .radius(radio))
                //.strokeColor(colores[i-1].toInt())
                //.fillColor(0x0F00FF00)

            radio = radio * 2
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posCiudad, 6.7f))

        mMap.addPolyline(
            PolylineOptions()
                .add(posCiudad)
                .add(marcadorUsuario!!.position)
            )

        val resultado = FloatArray(1)

        Location.distanceBetween(posCiudad.latitude, posCiudad.longitude, marcadorUsuario!!.position.latitude, marcadorUsuario!!.position.longitude,
            resultado)

        res.addPuntos(resultado[0])
    }
    fun onClickSiguiente() {
        mMap.clear()
        siguienteCiudad()
        setUpMap()
    }

}