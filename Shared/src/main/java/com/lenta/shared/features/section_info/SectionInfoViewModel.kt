package com.lenta.shared.features.section_info

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.features.message.MessageViewModel

class SectionInfoViewModel : MessageViewModel() {
    val sectionNumber: MutableLiveData<String> = MutableLiveData()
}
