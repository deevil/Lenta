package com.lenta.shared.platform.navigation.pictogram

import com.lenta.shared.fmp.resources.dao_ext.IconCode
import com.lenta.shared.fmp.resources.dao_ext.getIconDescriptionByCode
import com.lenta.shared.fmp.resources.fast.ZmpUtz38V001
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject


class IconDescriptionHelper @Inject constructor(hyperHive: HyperHive) : IIconDescriptionHelper {
    private val descriptionForIconMap = mutableMapOf<String, String?>()

    private val zmpUtz38V001 by lazy {
        ZmpUtz38V001(hyperHive)
    }

    override fun getDescription(iconCode: IconCode): String? {

        if (descriptionForIconMap.containsKey(iconCode.code)) {
            return descriptionForIconMap[iconCode.code]
        }

        return (zmpUtz38V001.getIconDescriptionByCode(iconCode.code)).apply {
            descriptionForIconMap[iconCode.code] = this
        }

    }

    override fun clearCache() {
        descriptionForIconMap.clear()
    }

}

interface IIconDescriptionHelper {
    fun getDescription(iconCode: IconCode): String?
    fun clearCache()
}