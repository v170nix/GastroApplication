package net.arwix.gastro.client.feature.admin.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.arwix.gastro.client.R

/**
 * A simple [Fragment] subclass.
 */
class AdminMenuEditFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_menu_edit, container, false)
    }


}
