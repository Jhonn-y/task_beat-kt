package com.devspace.taskbeats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class InfoBottomSheet(
    private val title : String,
    private val description: String,
    private val btnText: String,
    private val onClick: () -> Unit,
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.info_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_info_title)
        val tvDesc = view.findViewById<TextView>(R.id.tv_info_desc)
        val btnAction = view.findViewById<Button>(R.id.btn_delete_info)

        tvTitle.text = title
        tvDesc.text = description
        btnAction.text = btnText

        btnAction.setOnClickListener{
            onClick.invoke()
            dismiss()
        }

        return view
    }
}