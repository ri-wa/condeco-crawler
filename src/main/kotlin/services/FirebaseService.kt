package services

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.CollectionReference
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import config.collectionName
import config.firestoreName
import config.projectId
import models.BookingConfiguration
import models.Credentials
import java.io.FileInputStream
import java.util.logging.Logger
import kotlin.io.path.Path
import kotlin.io.path.exists

class FirebaseService() {

    private var collection: CollectionReference
    private val logger: Logger = Logger.getLogger(FirebaseService::class.java.name)

    init {
        val path = Path("/usr/app/credentials.json");
        val credentials: GoogleCredentials = if (path.exists()) {
            GoogleCredentials.fromStream(FileInputStream("/usr/app/credentials.json"))
        } else {
            GoogleCredentials.getApplicationDefault()
        }

        val firebaseOptions = FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build()

        FirebaseApp.initializeApp(firebaseOptions)
        collection = FirestoreClient.getFirestore(firestoreName).collection(collectionName)
    }


    fun getCredentials(): Credentials {
        logger.info("Getting Credentials")
        val future = collection.document("credentials").get()
        val credentials = future.get()
        if (credentials.exists()) {
            return credentials.toObject(Credentials::class.java)!!
        } else {
            throw IllegalStateException("Credentials not found")
        }
    }

    fun getBookingConfiguration(): BookingConfiguration {
        logger.info("Getting BookingConfiguration")
        val future = collection.document("configuration").get()
        val bookingConfiguration = future.get()
        if (bookingConfiguration.exists()) {
            return bookingConfiguration.toObject(BookingConfiguration::class.java)!!
        } else {
            throw IllegalStateException("BookingConfiguration not found")
        }
    }
}