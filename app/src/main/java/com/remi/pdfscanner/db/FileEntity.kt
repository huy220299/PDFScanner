package com.remi.pdfscanner.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.remi.pdfscanner.util.Constant.FILE_TABLE

@Entity(tableName = FILE_TABLE)
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    var fileId: Int = 0,
    @ColumnInfo(name = "file_name")
    var fileName: String = "",
    @ColumnInfo(name = "file_path")
    var filePathParent: String = "",
    @ColumnInfo(name = "image_preview_path")
    val imagePreviewPath: String = "",
    @ColumnInfo(name = "tag")
    val fileTag: String = "",
    @ColumnInfo(name = "page_size")
    val filePageSize: Int = 0,
    @ColumnInfo(name = "time_created")
    val fileCreatedTime: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "time_modified")
    val fileModifiedTime: Long = System.currentTimeMillis(),

){
    @Ignore
    var isSelect: Boolean = false
}
