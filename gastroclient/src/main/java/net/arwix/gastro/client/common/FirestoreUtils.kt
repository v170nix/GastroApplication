package net.arwix.gastro.client.common

//private class Firestore {
//    companion object {
//        val instance: FirebaseFirestore by lazy {
//            return@lazy synchronized(Firestore::class){
//                FirebaseFirestore.getInstance().apply { lock() }
//    }
//}

fun createCharString(count: Int, char: String) = run {
    buildString {
        repeat(count) {
            append(char)
        }
        append("\n")
    }
}