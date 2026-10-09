package com.example.listycity

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

/**
 * The only class that talks to Firestore. The UI calls these methods and reads [cities].
 *
 * Writes go to Firestore only. The snapshot listener in init picks up every change
 * (including ones made from the Firebase console) and rebuilds [_cities], and Compose
 * then redraws the list.
 */
class CityRepository {
    private val db = Firebase.firestore
    private val citiesRef = db.collection("cities")

    private val _cities = mutableStateListOf<City>()

    val cities: List<City>
        get() = _cities

    private val listener: ListenerRegistration

    init {
        listener = citiesRef.orderBy("name").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Listening to cities failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _cities.clear()
                _cities.addAll(snapshot.toObjects(City::class.java))
            }
        }
    }

    fun addCity(city: City) {
        citiesRef.add(city)
            .addOnFailureListener { Log.e(TAG, "Adding ${city.name} failed", it) }
    }

    fun updateCity(oldCity: City, updatedCity: City) {
        if (oldCity.id.isBlank()) return
        citiesRef.document(oldCity.id).set(updatedCity)
            .addOnFailureListener { Log.e(TAG, "Updating ${oldCity.name} failed", it) }
    }

    fun deleteCity(city: City) {
        if (city.id.isBlank()) return
        citiesRef.document(city.id).delete()
            .addOnSuccessListener { Log.d(TAG, "Deleted ${city.name} (${city.id})") }
            .addOnFailureListener { Log.e(TAG, "Deleting ${city.name} failed", it) }
    }

    /** Stops the snapshot listener. Called when the activity is destroyed. */
    fun close() {
        listener.remove()
    }

    private companion object {
        const val TAG = "CityRepository"
    }
}
