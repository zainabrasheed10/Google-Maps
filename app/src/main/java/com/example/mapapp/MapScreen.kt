package com.example.mapapp

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun MapScreen(context: Context) {
    val searchQuery = remember { mutableStateOf("") }
    val markerPosition = remember { mutableStateOf<LatLng?>(null) }
    val defaultLatLng = LatLng(31.5204, 74.3587)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
    }

    val coroutineScope = rememberCoroutineScope()

    Column {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Search Location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context)
                        val addresses = geocoder.getFromLocationName(searchQuery.value, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val location = addresses[0]
                            val latLng = LatLng(location.latitude, location.longitude)

                            // Update marker position (still safe on background thread)
                            markerPosition.value = latLng

                            // Move camera on the main thread
                            withContext(Dispatchers.Main) {
                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Text("Search")
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            markerPosition.value?.let { latLng ->
                Marker(
                    state = MarkerState(position = latLng),
                    title = "Search Result",
                    snippet = searchQuery.value
                )
            }
        }
    }
}
