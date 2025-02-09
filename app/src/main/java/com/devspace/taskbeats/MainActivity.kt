package com.devspace.taskbeats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var tasks = listOf<TaskUiData>()
    private val categoryAdapter = CategoryListAdapter()
    private lateinit var rvCategory: RecyclerView
    private lateinit var fabCreateTask: FloatingActionButton
    private lateinit var ctnEmptyView: LinearLayout
    private val taskAdapter by lazy {
        TaskListAdapter()
    }


    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDatabase::class.java, "db-taskbeat"
        ).build()
    }

    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao: TaskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvCategory = findViewById(R.id.rv_categories)
        ctnEmptyView = findViewById(R.id.ll_empty_list)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        fabCreateTask = findViewById(R.id.fab_create_task)
        val btnEmptyView = findViewById<Button>(R.id.empty_create)

        btnEmptyView.setOnClickListener{
            showCreateCategoryBottomSheet()
        }
        fabCreateTask.setOnClickListener {
            showBottomSheet()
        }

        categoryAdapter.setOnLongClickListener { categoryToBeDeleted ->
            if (categoryToBeDeleted.name != "ALL" && categoryToBeDeleted.name != "Add") {
                val title: String = this.getString(R.string.warning_info)
                val desc: String = this.getString(R.string.info_dialog)
                val btnText: String = this.getString(R.string.btn_delete)


                showBottomDialog(
                    title,
                    desc,
                    btnText
                ) {
                    val categoryEntityToBeDeleted = CategoryEntity(
                        categoryToBeDeleted.name,
                        categoryToBeDeleted.isSelected
                    )
                    deleteCategory(categoryEntityToBeDeleted)
                }
            }
        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "Add") {
                showCreateCategoryBottomSheet()
            } else {
                val categoryTemp = categories.map { item ->
                    when {

                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        else -> item
                    }
                }


                if (selected.name != "ALL") {
                    filterTaskByCategoryName(selected.name)
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        getTasksFromDatabase()
                    }

                }

                categoryAdapter.submitList(categoryTemp)

            }
        }
        rvCategory.adapter = categoryAdapter
        // Initially fetch categories from the DB on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDatabase()
        }

        rvTask.adapter = taskAdapter

        taskAdapter.setOnClickListener { task ->
            showBottomSheet(task)
        }

        GlobalScope.launch(Dispatchers.IO) {
            getTasksFromDatabase()
        }

    }


    private fun getCategoriesFromDatabase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        GlobalScope.launch(Dispatchers.Main) {
        if(categoriesFromDb.isEmpty()){
                rvCategory.isVisible = false
                fabCreateTask.isVisible = false
                ctnEmptyView.isVisible = true
        }else {

            rvCategory.isVisible = true
            fabCreateTask.isVisible = true
            ctnEmptyView.isVisible = false

        }
        }


        categoriesEntity = categoriesFromDb
        val categoriesUiData = categoriesFromDb.map {
            CategoryUiData(
                name = it.name,
                isSelected = it.isSelected
            )
        }.toMutableList()

        categoriesUiData.add(0,
            CategoryUiData(
                name = "ALL",
                isSelected = true
            )
        )
        categoriesUiData.add(
            CategoryUiData(
                name = "Add",
                isSelected = false
            )
        )


        // Update the adapter on the main thread
        GlobalScope.launch(Dispatchers.Main) {
            categories = categoriesUiData
            categoryAdapter.submitList(categoriesUiData)
        }

    }

    private fun getTasksFromDatabase() {
        // Perform database query in background thread
        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDB: List<TaskEntity> = taskDao.getAll()
            val tasksUiData = tasksFromDB.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }

            // Switch to the main thread to update the RecyclerView
            GlobalScope.launch(Dispatchers.Main) {
                tasks = tasksUiData
                taskAdapter.submitList(tasksUiData)
            }
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesFromDatabase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategory(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesFromDatabase()
            getTasksFromDatabase()
        }
    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTasksFromDatabase()
        }
    }

    private fun updateTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.update(taskEntity)
            getTasksFromDatabase()
        }
    }

    private fun deleteTask(taskEntityId: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(taskEntityId)
            getTasksFromDatabase()
        }
    }

    private fun filterTaskByCategoryName(category: String) {

        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDB: List<TaskEntity> = taskDao.getAllByCategory(category)
            val tasksUiData = tasksFromDB.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            // Switch to the main thread to update the RecyclerView
            GlobalScope.launch(Dispatchers.Main) {
                tasks = tasksUiData
                taskAdapter.submitList(tasksUiData)
            }
        }

    }

    private fun showCreateCategoryBottomSheet(){
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                isSelected = false
            )
            insertCategory(categoryEntity)

        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }

    private fun showBottomDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClick
        )
        infoBottomSheet.show(supportFragmentManager, "infoBottomSheet")
    }

    private fun showBottomSheet(task: TaskUiData? = null) {
        val createTaskBottomSheet = CreateTaskBottomSheet(
            task = task,
            categoryList = categoriesEntity,
            onCreatedClicked = { taskToBeCreated ->
                val taskEntity = TaskEntity(
                    name = taskToBeCreated.name,
                    category = taskToBeCreated.category
                )
                insertTask(taskEntity)
            },
            onUpdatedClicked = { taskToBeUpdated ->
                val taskEntity = TaskEntity(
                    id = taskToBeUpdated.id,
                    name = taskToBeUpdated.name,
                    category = taskToBeUpdated.category
                )
                updateTask(taskEntity)
            },
            onDeleteClicked = { taskToBeDeleted ->
                val taskEntity = TaskEntity(
                    id = taskToBeDeleted.id,
                    name = taskToBeDeleted.name,
                    category = taskToBeDeleted.category
                )
                deleteTask(taskEntity)
            },
        )
        createTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")

    }


}