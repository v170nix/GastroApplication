package net.arwix.gastro.library

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// https://github.com/EddyVerbruggen/nativescript-plugin-firebase/issues/631
suspend fun <T> Task<T>.await(): T? {
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled)
                throw CancellationException("Task $this was cancelled normally")
            else result
        } else throw e
    }

    return suspendCancellableCoroutine { cont ->
        addOnSuccessListener {
            if (isCanceled) cont.cancel() else cont.resume(it)
        }
        addOnFailureListener {
            cont.resumeWithException(it)
        }
    }
}

//suspend fun <T> Task<T>.await(): T? {
//    if (isComplete) {
//        val e = exception
//        return if (e == null) {
//            if (isCanceled)
//                throw CancellationException("Task $this was cancelled normally")
//            else result
//        } else throw e
//    }
//
//    return suspendCancellableCoroutine { cont ->
//        addOnCompleteListener {
//            val e = exception
//            if (e == null) {
//                if (isCanceled) cont.cancel() else cont.resume(result)
//            } else {
//                cont.resumeWithException(e)
//            }
//        }
//    }
//}