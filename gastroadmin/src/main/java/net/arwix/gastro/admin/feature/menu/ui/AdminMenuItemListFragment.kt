package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.arwix.gastro.admin.R

/**
 * A simple [Fragment] subclass.
 */
class AdminMenuItemListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_menu_item_list, container, false)
    }


}
