package net.arwix.gastro.client.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.CheckData

class CheckRepository(
    private val checkReference: CollectionReference
) {
    suspend fun getChecks(checksRef: List<DocumentReference>): List<CheckData> = supervisorScope {
        checksRef.chunked(10)
            .map {
                async(Dispatchers.IO) {
                    checkReference.whereIn(
                        FieldPath.documentId(),
                        it
                    ).get().await()
                }
            }
            .awaitAll()
            .filterNotNull()
            .map { it.toObjects(CheckData::class.java) }
            .flatten()
    }

    fun submitCheck(checkData: CheckData): Pair<DocumentReference, (transaction: Transaction) -> Unit> {
        val checkDoc = checkReference.document()
        return checkDoc to { transaction ->
            transaction.set(checkDoc, checkData)
        }
    }


}