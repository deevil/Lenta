package com.lenta.bp14.platform.resource

import android.content.Context
import com.lenta.bp14.R
import com.lenta.shared.utilities.extentions.unsafeLazy

import javax.inject.Inject

class ResourceManager @Inject constructor(val context: Context) : IResourceManager {
    override val serverConnectionError: String by unsafeLazy {
        context.getString(SERVER_CONNECTION_ERROR)
    }

    override val zPartInfoPattern: String by unsafeLazy {
        context.getString(Z_PART_INFO_PATTERN)
    }

    override val storageZPartsPattern: String by unsafeLazy {
        context.getString(STORAGE_Z_PARTS_PATTERN)
    }

    companion object {
        private const val SERVER_CONNECTION_ERROR = R.string.server_connection_error
        private const val Z_PART_INFO_PATTERN = R.string.z_part_info_pattern
        private const val STORAGE_Z_PARTS_PATTERN = R.string.storage_z_parts_pattern
    }
}

