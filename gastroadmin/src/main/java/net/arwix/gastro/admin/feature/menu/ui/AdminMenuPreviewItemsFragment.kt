package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_admin_menu_preview_items.*
import net.arwix.gastro.admin.R
import net.arwix.gastro.library.menu.MenuUtils
import net.arwix.gastro.library.menu.ui.MenuItemsGridAdapter


class AdminMenuPreviewItemsFragment : Fragment() {

    private val args: AdminMenuPreviewItemsFragmentArgs by navArgs()
    private lateinit var itemsAdapter: MenuItemsGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_menu_preview_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemsAdapter = MenuItemsGridAdapter(
            isViewMenuGroup = true,
            onChangeSelectedItems = {}
//            onClickItem = { menu, item ->
//                Log.e("item", item.toString())
//
//            }
        )
        itemsAdapter.setItems(args.MenuGroup)
        with(admin_menu_preview_items_recycler_view) {
            adapter = itemsAdapter
            layoutManager = MenuUtils.createGridLayoutManager(requireContext(), itemsAdapter)
        }
    }


}
