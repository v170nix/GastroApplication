package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import java.io.File

class PdfPrint(private val printAttributes: PrintAttributes) {


    fun print(
        adapter: PrintDocumentAdapter,
        path: File,
        fileName: String,
        callback: CallbackPrint
    ) {
        adapter.onLayout(null,
            printAttributes,
            null,
            object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                    super.onLayoutFinished(info, changed)
                    adapter.onWrite(arrayOf(PageRange.ALL_PAGES),
                        getOutputFile(path, fileName),
                        CancellationSignal(),
                        object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<out PageRange>) {
                                super.onWriteFinished(pages)
                                if (pages.isNotEmpty()) {
                                    callback.success(File(path, fileName).absolutePath)
                                } else {
                                    callback.onFailure("Pages length not found")
                                }
                            }
                        })


                }
            }, null
        )

    }

    private fun getOutputFile(path: File, fileName: String): ParcelFileDescriptor {
        if (!path.exists()) path.mkdirs()
        val file = File(path, fileName)
        file.createNewFile()
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
    }


    interface CallbackPrint {
        fun success(path: String)
        fun onFailure(errorMsg: String)
    }

}