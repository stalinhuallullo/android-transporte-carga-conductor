package online.transporteari.transportecargaconductor.providers

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import online.transporteari.transportecargaconductor.models.Booking

class BookingProvider {
    val db = Firebase.firestore.collection("Bookings")
    val authProvider: AuthProvider = AuthProvider()

    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.getId()).set(booking).addOnFailureListener {
            Log.e("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }
}