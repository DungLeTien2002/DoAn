package com.mhss.app.mybrain.domain.use_case.tasks

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.presentation.glance_widgets.TasksHomeWidget
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val tasksRepository: TaskRepository,
    private val context: Context
) {
    suspend operator fun invoke(task: Task): Long {
        val id = tasksRepository.insertTask(task)
        TasksHomeWidget().updateAll(context)
        return id
    }
}