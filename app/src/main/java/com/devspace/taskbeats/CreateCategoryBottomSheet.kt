package com.devspace.taskbeats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateCategoryBottomSheet(
    private val onCreatedClicked : (String) -> Unit,
): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_category_bottom_sheet, container,false)

        val btnCreate = view.findViewById<Button>(R.id.btn_save_category)
        val tieCategoryName = view.findViewById<TextInputEditText>(R.id.it_new_category)

        btnCreate.setOnClickListener {
            if(tieCategoryName.text?.isEmpty() == true){
                Snackbar.make(
                    view,
                    "Preencha todos os campos!",
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                val name = tieCategoryName.text.toString()
                onCreatedClicked.invoke(name)
                dismiss()
            }
        }

        return view
    }
}