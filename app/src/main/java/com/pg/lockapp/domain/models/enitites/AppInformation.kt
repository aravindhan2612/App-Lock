package com.pg.lockapp.domain.models.enitites

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
class AppInformation() {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "app_name")
    var appName: String? = null

    @ColumnInfo(name = "package_name")
    var packageName: String? = null

    @ColumnInfo(name = "icon")
    var icon: String? = null

    @ColumnInfo(name = "is_locked")
    var isLocked: Boolean = false
}
