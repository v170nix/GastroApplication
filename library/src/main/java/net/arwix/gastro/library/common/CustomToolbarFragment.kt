package net.arwix.gastro.library.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

abstract class CustomToolbarFragment : Fragment() {

    abstract val idResToolbar: Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        if (activity is CustomToolbarActivity) {
            activity.setCustomToolbar(view.findViewById(idResToolbar))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity()
        if (activity is CustomToolbarActivity) {
            activity.setCustomToolbar(null)
        }
    }
}

interface CustomToolbarActivity {
    fun setCustomToolbar(toolbar: Toolbar?)
}