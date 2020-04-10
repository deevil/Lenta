package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.model.task.TaskSectionProducts

interface ITaskSectionRepository {
    fun getSections(): List<TaskSectionInfo>
    fun getSectionProducts(): List<TaskSectionProducts>
    fun findSection(section: TaskSectionInfo): TaskSectionInfo?
    fun findSection(sectionNumber: String): TaskSectionInfo?
    fun findSectionProductsOfSection(section: TaskSectionInfo): List<TaskSectionProducts>
    fun addSection(section: TaskSectionInfo): Boolean
    fun addSections(sections: List<TaskSectionInfo>): Boolean
    fun addSectionProduct(section: TaskSectionProducts): Boolean
    fun addSectionProductsList(sections: List<TaskSectionProducts>): Boolean
    fun changeSection(section: TaskSectionInfo): Boolean
    fun updateSections(newSections: List<TaskSectionInfo>, newSectionProducts: List<TaskSectionProducts>)
    fun getSignModification() : Boolean
    fun clear()
}