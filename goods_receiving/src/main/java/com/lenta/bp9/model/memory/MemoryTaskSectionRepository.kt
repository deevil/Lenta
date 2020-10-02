package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskSectionRepository
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.model.task.TaskSectionProducts

class MemoryTaskSectionRepository : ITaskSectionRepository {

    private val sectionInfo: ArrayList<TaskSectionInfo> = ArrayList()

    private val sectionProductsInfo: ArrayList<TaskSectionProducts> = ArrayList()

    private var isModifications: Boolean = false

    override fun getSections(): List<TaskSectionInfo> {
        return sectionInfo
    }

    override fun getSectionProducts(): List<TaskSectionProducts> {
        return sectionProductsInfo
    }

    override fun findSection(section: TaskSectionInfo): TaskSectionInfo? {
        return findSection(section.sectionNumber)
    }

    override fun findSection(sectionNumber: String): TaskSectionInfo? {
        return sectionInfo.firstOrNull { it.sectionNumber == sectionNumber}
    }

    override fun findSectionProductsOfSection(section: TaskSectionInfo): List<TaskSectionProducts> {
        val foundSectionProducts = ArrayList<TaskSectionProducts>()
        for (i in sectionProductsInfo.indices) {
            if (section.sectionNumber == sectionProductsInfo[i].sectionNumber) {
                foundSectionProducts.add(sectionProductsInfo[i])
            }
        }
        return foundSectionProducts
    }

    override fun addSection(section: TaskSectionInfo): Boolean {
        var index = -1
        for (i in sectionInfo.indices) {
            if (section.sectionNumber == sectionInfo[i].sectionNumber) {
                index = i
            }
        }

        if (index == -1) {
            sectionInfo.add(section)
            return true
        } else {
            sectionInfo.getOrNull(index)?.let {
                sectionInfo.removeAt(index)
                sectionInfo.add(it)
            }
        }

        return false
    }

    override fun addSections(sections: List<TaskSectionInfo>): Boolean {
        return sections.map {
            addSection(it)
        }.any()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun addSectionProduct(sectionProducts: TaskSectionProducts): Boolean {
        var index = -1
        for (i in sectionProductsInfo.indices) {
            if (sectionProducts.sectionNumber == sectionProductsInfo[i].sectionNumber && sectionProducts.materialNumber == sectionProductsInfo[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            sectionProductsInfo.add(sectionProducts)
            return true
        } else {
            sectionProductsInfo.getOrNull(index)?.let {
                sectionProductsInfo.removeAt(index)
                sectionProductsInfo.add(it)
            }
        }

        return false
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun addSectionProductsList(sectionProducts: List<TaskSectionProducts>): Boolean {
        return sectionProducts.map {
            addSectionProduct(it)
        }.any()
    }

    override fun changeSection(section: TaskSectionInfo): Boolean {
        var index = -1
        for (i in sectionInfo.indices) {
            if (section.sectionNumber == sectionInfo[i].sectionNumber) {
                index = i
            }
        }

        if (index != -1) {
            sectionInfo.removeAt(index)
        }
        isModifications = true
        return addSection(section)
    }

    override fun updateSections(newSections: List<TaskSectionInfo>, newSectionProducts: List<TaskSectionProducts>) {
        clear()
        addSections(newSections)
        addSectionProductsList(newSectionProducts)
    }

    override fun getSignModification(): Boolean {
        return isModifications
    }

    override fun clear() {
        sectionInfo.clear()
        sectionProductsInfo.clear()
        isModifications = false
    }
}