package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_admin_menu_group_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.arwix.gastro.admin.R
import net.arwix.gastro.admin.data.AddEditMode
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.menu.data.MenuGroupData
import org.koin.android.viewmodel.ext.android.sharedViewModel

class AdminMenuGroupEditFragment : Fragment(), CoroutineScope by MainScope() {

    private val groupViewModel: AdminMenuGroupViewModel by sharedViewModel()
    private val args: AdminMenuGroupEditFragmentArgs by navArgs()
    private lateinit var inputMenuGroup: MenuGroupData
    private var isProgress: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_menu_group_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.EditMenuGroupMode == AddEditMode.Edit && args.EditMenuGroup == null)
            throw IllegalArgumentException()
        admin_menu_group_edit_color_dialog_button.setOnClickListener {
            ColorPickerDialogBuilder
                .with(requireContext())
                .setTitle("ColorPicker Dialog")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setOnColorSelectedListener {

                }
                .setPositiveButton("Select") { _, selectedColor, _ ->
                    admin_menu_group_edit_color.editText!!.setText(Integer.toHexString(selectedColor))
                    setColorView(selectedColor)
                }
                .setNegativeButton("Cancel") { dialog, _: Int ->
                    dialog?.dismiss()
                }
                .build()
                .show()
        }
        admin_menu_group_edit_color.editText?.addTextChangedListener {
            setColorView(getColorInt(it))
        }

        admin_menu_group_edit_back_button.setOnClickListener {
            hideKeyboard()
            findNavController().navigateUp()
        }

        if (args.EditMenuGroupMode == AddEditMode.Edit) {
            inputMenuGroup = args.EditMenuGroup!!
            setData(inputMenuGroup)
            (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.admin_title_menu_group_edit)
        }
        if (args.EditMenuGroupMode == AddEditMode.Add) {
            inputMenuGroup = MenuGroupData(
                "", null, null, MenuGroupData.Metadata(
                    10
                )
            )
            setData(inputMenuGroup)
            (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.admin_title_menu_group_add)
        }

        admin_menu_group_edit_submit_button.setOnClickListener {
            if (isProgress) return@setOnClickListener
            val menuGroupData = getFilterData() ?: return@setOnClickListener
            if (args.EditMenuGroupMode == AddEditMode.Add) {
                launch {
                    isProgress = true
                    groupViewModel.add(menuGroupData)
                    isProgress = false
                    hideKeyboard()
                    findNavController().navigate(
                        AdminMenuGroupEditFragmentDirections.actionToAdminMenuGroupListFragment(
                            menuGroupData
                        )
                    )
                }
            }
            if (args.EditMenuGroupMode == AddEditMode.Edit) {
                launch {
                    isProgress = true
                    groupViewModel.edit(args.EditMenuGroup!!, menuGroupData)
                    isProgress = false
                    hideKeyboard()
                    findNavController().navigate(
                        AdminMenuGroupEditFragmentDirections.actionToAdminMenuGroupListFragment(
                            menuGroupData
                        )
                    )
                }
            }
        }
    }

    private fun getFilterData(): MenuGroupData? {
        var isErrors = false
        val name = admin_menu_group_edit_name.editText?.text?.toString().orEmpty().trim()
        admin_menu_group_edit_name.isErrorEnabled = false
        if (name.isEmpty()) {
            admin_menu_group_edit_name.isErrorEnabled = true
            admin_menu_group_edit_name.error = "Name is empty"
            isErrors = true
        }

        if (args.EditMenuGroupMode == AddEditMode.Add) {
            val menus = groupViewModel.liveState.value?.menuGroups
            menus?.find { it.name == name }?.run {
                admin_menu_group_edit_name.isErrorEnabled = false
                admin_menu_group_edit_name.isErrorEnabled = true
                admin_menu_group_edit_name.error = "Name already exist"
                isErrors = true
            }
        }

        val printer =
            admin_menu_group_edit_printer_address.editText?.text?.toString().orEmpty().trim()
        admin_menu_group_edit_printer_address.isErrorEnabled = false
        if (printer.isEmpty()) {
            admin_menu_group_edit_printer_address.isErrorEnabled = true
            admin_menu_group_edit_printer_address.error = "Printer address is empty"
            isErrors = true
        }

        val order = admin_menu_group_edit_position.editText?.text?.toString()?.toIntOrNull() ?: 0
        val color = getColorInt(admin_menu_group_edit_color.editText?.editableText)
        if (isErrors) return null
        return MenuGroupData(name, printer, null, MenuGroupData.Metadata(order, color))
    }

    private fun setData(data: MenuGroupData) {
        admin_menu_group_edit_name.editText?.setText(data.name)
        admin_menu_group_edit_position.editText?.setText(data.metadata.order!!.toString())
        admin_menu_group_edit_printer_address.editText?.setText(data.printer ?: "")
        val color = data.metadata.color?.run(Integer::toHexString) ?: ""
        admin_menu_group_edit_color.editText?.setText(color)
        setColorView(data.metadata.color)
    }

    private fun getColorInt(editable: Editable?): Int? = runCatching {
        editable!!.toString().toLong(16).toInt()
    }.getOrNull()

    private fun setColorView(@ColorInt color: Int?) {
        color?.run {
            admin_menu_group_edit_color_view.setBackgroundColor(this)
        } ?: run {
            admin_menu_group_edit_color_view.background = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
