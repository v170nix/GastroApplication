package net.arwix.gastro.admin.feature.menu.ui


import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_admin_menu_item_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.arwix.gastro.admin.R
import net.arwix.gastro.admin.data.AddEditMode
import net.arwix.gastro.library.common.hideKeyboard
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.getNextCell
import org.koin.android.viewmodel.ext.android.sharedViewModel
import kotlin.math.roundToLong

class AdminMenuItemEditFragment : Fragment(), CoroutineScope by MainScope() {

    private val itemViewModel: AdminMenuItemViewModel by sharedViewModel()
    private val args: AdminMenuItemEditFragmentArgs by navArgs()
    private lateinit var inputMenuItem: MenuGroupData.PreMenuItem
    private var isProgress: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_admin_menu_item_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.Mode == AddEditMode.Edit && args.MenuItem == null)
            throw IllegalArgumentException()
        admin_menu_item_edit_color_dialog_button.setOnClickListener {
            ColorPickerDialogBuilder
                .with(requireContext())
                .setTitle("ColorPicker Dialog")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setOnColorSelectedListener {

                }
                .setPositiveButton("Select") { _, selectedColor, _ ->
                    admin_menu_item_edit_color.editText!!.setText(Integer.toHexString(selectedColor))
                    setColorView(selectedColor)
                }
                .setNegativeButton("Cancel") { dialog, _: Int ->
                    dialog?.dismiss()
                }
                .build()
                .show()
        }
        admin_menu_item_edit_color.editText?.addTextChangedListener {
            setColorView(getColorInt(it))
        }

        admin_menu_item_edit_back_button.setOnClickListener {
            hideKeyboard()
            findNavController().navigateUp()
        }

        if (args.Mode == AddEditMode.Edit) {
            inputMenuItem = args.MenuItem!!
            setData(inputMenuItem)
//            (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.admin_title_menu_group_edit)
        }

        val cell = args.MenuGroup.getNextCell()

        if (args.Mode == AddEditMode.Add) {
            inputMenuItem = MenuGroupData.PreMenuItem(
                name = "",
                color = args.MenuGroup.metadata.color,
                row = cell.first,
                col = cell.second,
                price = 0,
                printer = args.MenuGroup.printer
            )
            setData(inputMenuItem)
//            (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.admin_title_menu_group_add)
        }

        admin_menu_item_edit_submit_button.setOnClickListener {
            if (isProgress) return@setOnClickListener
            val menuGroupData = getEditedMenuItem() ?: return@setOnClickListener
            if (args.Mode == AddEditMode.Add) {
                launch {
                    isProgress = true
                    itemViewModel.add(menuGroupData)
                    isProgress = false
                    hideKeyboard()
                    findNavController().navigateUp()
                }
            }
            if (args.Mode == AddEditMode.Edit) {
                launch {
                    isProgress = true
                    itemViewModel.edit(args.MenuItem!!, menuGroupData)
                    isProgress = false
                    hideKeyboard()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun getEditedMenuItem(): MenuGroupData.PreMenuItem? {
        var isErrors = false
        val name = admin_menu_item_edit_name.editText?.text?.toString().orEmpty().trim()
        admin_menu_item_edit_name.isErrorEnabled = false
        if (name.isEmpty()) {
            admin_menu_item_edit_name.isErrorEnabled = true
            admin_menu_item_edit_name.error = "Name is empty"
            isErrors = true
        }

        admin_menu_item_edit_price.isErrorEnabled = false
        val priceString = admin_menu_item_edit_price.editText?.text?.toString().orEmpty().trim()
        val priceDouble = priceString.toDoubleOrNull()
        if (priceDouble == null || priceDouble < 0.0) {
            admin_menu_item_edit_price.isErrorEnabled = true
            admin_menu_item_edit_price.error = "Price incorrect"
            isErrors = true
        }

        val printer =
            admin_menu_item_edit_printer_address.editText?.text?.toString().orEmpty().trim()
        admin_menu_item_edit_printer_address.isErrorEnabled = false
        if (printer.isEmpty()) {
            admin_menu_item_edit_printer_address.isErrorEnabled = true
            admin_menu_item_edit_printer_address.error = "Printer address is empty"
            isErrors = true
        }

        val row = admin_menu_item_edit_row.editText?.text?.toString()?.toIntOrNull() ?: 1
        val col = admin_menu_item_edit_col.editText?.text?.toString()?.toIntOrNull() ?: 1

        val color = getColorInt(admin_menu_item_edit_color.editText?.editableText)
        if (isErrors) return null
        return MenuGroupData.PreMenuItem(
            name = name,
            printer = printer,
            color = color,
            row = row,
            col = col,
            price = (priceDouble!! * 100.0).roundToLong()
        )
    }

    private fun setData(data: MenuGroupData.PreMenuItem) {
        admin_menu_item_edit_name.editText?.setText(data.name)
        admin_menu_item_edit_row.editText?.setText(data.row.toString())
        admin_menu_item_edit_col.editText?.setText(data.col.toString())
        admin_menu_item_edit_printer_address.editText?.setText(data.printer ?: "")
//        admin_menu_item_edit_printer_address.editText?.isEnabled = false
//        admin_menu_item_edit_printer_address.editText?.focusable = NOT_FOCUSABLE
//        admin_menu_item_edit_printer_address.editText?.inputType = InputType.TYPE_NULL
//        admin_menu_item_edit_printer_address.isEnabled = false
        val color = data.color?.run(Integer::toHexString) ?: ""
        admin_menu_item_edit_color.editText?.setText(color)
        admin_menu_item_edit_price.editText?.setText(
            if (data.price != 0L) (data.price / 100.0).toString() else ""
        )
        setColorView(data.color)
    }

    private fun getColorInt(editable: Editable?): Int? = runCatching {
        editable!!.toString().toLong(16).toInt()
    }.getOrNull()

    private fun setColorView(@ColorInt color: Int?) {
        color?.run {
            admin_menu_item_edit_color_view.setBackgroundColor(this)
        } ?: run {
            admin_menu_item_edit_color_view.background = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

}
