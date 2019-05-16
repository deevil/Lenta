package com.lenta.bp10.features.section_info

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.features.message.MessageViewModel

class SectionInfoViewModel : MessageViewModel() {
    val sectionNumber: MutableLiveData<String> = MutableLiveData()
}
