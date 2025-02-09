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

class CreateTaskBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val task: TaskUiData? = null,
    private val onCreatedClicked: (TaskUiData) -> Unit,
    private val onUpdatedClicked: (TaskUiData) -> Unit,
    private val onDeleteClicked: (id: TaskUiData) -> Unit,
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_task_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val btnCreateOrUpdate = view.findViewById<Button>(R.id.btn_save_task)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete_task)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.it_new_task)
        val spinner = view.findViewById<Spinner>(R.id.sp_task)
        val categoryListMap = categoryList.map {
            it.name
        }.toMutableList()

        categoryListMap.add(
            0,
            "Select a Category"
        )

        if (task != null) {
            tvTitle.setText(R.string.update_task_title)
            btnCreateOrUpdate.setText(R.string.btn_update)
            tieTaskName.setText(task.name)
            btnDelete.visibility = View.VISIBLE

            val currentCategory = categoryList.first { it.name == task.category }
            val index = categoryList.indexOf(currentCategory)
            spinner.setSelection(index)
        }

        var taskCategory: String? = null


        btnCreateOrUpdate.setOnClickListener {
            if (tieTaskName.text?.isEmpty() == true || taskCategory == "Select a Category") {
                Snackbar.make(
                    view,
                    "Preencha todos os campos!",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                if (task == null) {
                    val name = tieTaskName.text.toString()
                    onCreatedClicked.invoke(
                        TaskUiData(
                            id = 0,
                            name = name,
                            category = taskCategory!!,
                        )
                    )
                } else {
                    val name = tieTaskName.text.toString()
                    onUpdatedClicked.invoke(
                        TaskUiData(
                            id = task.id,
                            name = name,
                            category = taskCategory!!,
                        )
                    )
                }
                dismiss()
            }
        }

        btnDelete.setOnClickListener {
            if (task != null) {
                onDeleteClicked.invoke(
                    TaskUiData(
                        id = task.id,
                        name = task.name,
                        category = task.category,
                    )
                )
                dismiss()
            }
        }

        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            categoryListMap,
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                taskCategory = categoryListMap[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


        return view
    }
}