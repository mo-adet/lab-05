package com.example.listycity

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CityRepository {
    private val db = Firebase.firestore
    private val citiesRef = db.collection("cities")

    private val _cities = mutableStateListOf<City>()

    val cities: List<City>
        get() = _cities

    init {
        citiesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _cities.clear()

            snapshot?.documents?.forEach { document ->
                val city = document.toObject(City::class.java)
                if (city != null) {
                    _cities.add(city)
                }
            }
        }
    }

    fun addCity(city: City) {
        citiesRef.document(city.name).set(city)
    }

    fun updateCity(oldCity: City, updatedCity: City) {
        if (oldCity.name == updatedCity.name) {
            citiesRef.document(oldCity.name).set(updatedCity)
        } else {
            // The document ID is the city name, so a rename moves the city to a new document.
            // Without this, the old ID would stay behind and deleteCity could not find it.
            db.batch()
                .delete(citiesRef.document(oldCity.name))
                .set(citiesRef.document(updatedCity.name), updatedCity)
                .commit()
        }
    }

    fun deleteCity(city: City) {
        citiesRef.document(city.name).delete()
    }
}
