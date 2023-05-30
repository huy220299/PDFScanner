package com.remi.pdfscanner.repository

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.remi.pdfscanner.db.FileDao
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.ui.main.BottomSheetSort
import com.remi.pdfscanner.util.FileUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

import javax.inject.Inject


class DbRepository
@Inject constructor(
    private val dao: FileDao,
) {
     fun saveFile(file: FileEntity) = dao.insertFile(file)

     fun saveFile1(file: FileEntity): Long = dao.insertFile1(file)

    fun duplicateFile(file: FileEntity) = dao.duplicateFile( FileEntity(
//         fileId  =0,  //auto create id
         fileName="${file.fileName}_Copy",
         filePathParent= file.filePathParent,
         imagePreviewPath = file.imagePreviewPath,
         fileTag = file.fileTag,
         filePageSize = file.filePageSize,
         fileCreatedTime= System.currentTimeMillis(),
         fileModifiedTime = System.currentTimeMillis()
    ))

    fun updateFile(file: FileEntity) = dao.updateFile(file)

    fun deleteFile(file: FileEntity) = dao.deleteFile(file)

    fun getFile(id: Int): FileEntity = dao.getFile(id)

    fun getFileByName(name: String): List<FileEntity> = dao.getFileByName(name)

    fun getAllFiles() = dao.getAllFiles()

    fun getAllFile(sortBy:BottomSheetSort.SortBy,isAscending:Boolean) = when(sortBy){
        BottomSheetSort.SortBy.Modify->if (isAscending) dao.getFilesModifiedAsc() else dao.getFilesModifiedDesc()
        BottomSheetSort.SortBy.Created->if (isAscending) dao.getFilesCreateAsc() else dao.getFilesCreateDesc()
        else->if (isAscending) dao.getFilesNameAsc() else dao.getFilesNameDesc()
    }

    fun getAllImageSignature(context: Context):List<String>{
        val file   = File(FileUtil.getFolderSignatureImage(context))
         file.listFiles()?.run {
             return this.map { it.path }.reversed()
         }
        return ArrayList()
    }

}