package pt.ipp.estg.cmu.ui.Content // ou a sua pasta model

import com.google.android.gms.maps.model.LatLng

data class MobilityPoint(
    val id: String,
    val name: String,
    val type: String,
    val location: LatLng
)