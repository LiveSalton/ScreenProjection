package com.salton123.biz_record.bean

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/31 8:45 PM
 * ModifyTime: 8:45 PM
 * Description:
 */
@Entity
data class RecordItem(
        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "fileUrl")
        var fileUrl: String,
        @ColumnInfo(name = "previewUrl")
        var previewUrl: String,
        @ColumnInfo(name = "width")
        var width: Int,
        @ColumnInfo(name = "height")
        var height: Int
) {
    companion object {
        const val TABLE_NAME = "RecordItem"
    }
}