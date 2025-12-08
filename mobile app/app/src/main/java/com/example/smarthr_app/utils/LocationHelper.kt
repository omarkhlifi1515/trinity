package com.example.smarthr_app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        // Try to get last known location first
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Validate location - check if it's not null and has valid coordinates
                if (location != null && isValidLocation(location)) {
                    continuation.resume(location)
                } else {
                    // If last location is invalid or too old, request a fresh one
                    requestFreshLocation(continuation)
                }
            }
            .addOnFailureListener {
                // If last location fails, request a fresh one
                requestFreshLocation(continuation)
            }
    }

    private fun requestFreshLocation(continuation: kotlin.coroutines.Continuation<Location?>) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        val handler = Handler(Looper.getMainLooper())
        var timeoutHandler: Handler? = null
        
        var isCompleted = false
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (isCompleted) return
                fusedLocationClient.removeLocationUpdates(this)
                timeoutHandler?.removeCallbacksAndMessages(null)
                isCompleted = true
                
                val location = locationResult.lastLocation
                try {
                    if (location != null && isValidLocation(location)) {
                        continuation.resume(location)
                    } else {
                        continuation.resume(null)
                    }
                } catch (e: Exception) {
                    // Continuation already completed
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Set a timeout
            timeoutHandler = handler
            val timeoutRunnable = Runnable {
                if (!isCompleted) {
                    isCompleted = true
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    try {
                        continuation.resume(null)
                    } catch (e: Exception) {
                        // Continuation already completed
                    }
                }
            }
            handler.postDelayed(timeoutRunnable, 10000) // 10 second timeout
        } catch (e: SecurityException) {
            try {
                continuation.resume(null)
            } catch (ex: Exception) {
                // Continuation already completed
            }
        }
    }

    private fun isValidLocation(location: Location): Boolean {
        // Check if coordinates are valid (not 0.0, 0.0 which is in the ocean off Africa)
        // Also check if accuracy is reasonable (not too high/invalid)
        return location.latitude != 0.0 && 
               location.longitude != 0.0 &&
               location.latitude >= -90 && 
               location.latitude <= 90 &&
               location.longitude >= -180 && 
               location.longitude <= 180 &&
               location.accuracy > 0 &&
               location.accuracy < 10000 // Less than 10km accuracy
    }

    fun isWithinRadius(
        currentLat: String,
        currentLng: String,
        officeLat: String,
        officeLng: String,
        radiusInMeters: String
    ): Boolean {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLat.toDouble(), currentLng.toDouble(),
                officeLat.toDouble(), officeLng.toDouble(),
                results
            )
            results[0] <= radiusInMeters.toDouble()
        } catch (e: Exception) {
            false
        }
    }

    fun getDistanceInMeters(
        lat1: String,
        lng1: String,
        lat2: String,
        lng2: String
    ): Float {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                lat1.toDouble(), lng1.toDouble(),
                lat2.toDouble(), lng2.toDouble(),
                results
            )
            results[0]
        } catch (e: Exception) {
            Float.MAX_VALUE
        }
    }
}