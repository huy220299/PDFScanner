package com.remi.pdfscanner.db


import androidx.room.*
import com.remi.pdfscanner.ui.main.BottomSheetSort
import com.remi.pdfscanner.util.Constant.FILE_TABLE

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile(noteEntity: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFile1(noteEntity: FileEntity) : Long


    @Insert
    fun duplicateFile(noteEntity: FileEntity)

    @Update
    fun updateFile(noteEntity: FileEntity)

    @Delete
    fun deleteFile(noteEntity: FileEntity)

    @Query("SELECT * FROM $FILE_TABLE ORDER BY time_created DESC")
    fun getAllFiles(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE WHERE fileId LIKE :id")
    fun getFile(id: Int): FileEntity

    @Query("SELECT * FROM $FILE_TABLE WHERE file_name LIKE '%' || :name || '%' ORDER BY time_created DESC")
    fun getFileByName(name: String): List<FileEntity>

    @Query("DELETE FROM $FILE_TABLE WHERE file_name LIKE '%FakeFile'")
    fun deleteFakeFile()

    @Query("SELECT * FROM $FILE_TABLE ORDER BY time_created DESC")
    fun getFilesCreateDesc(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE ORDER BY time_created ASC")
    fun getFilesCreateAsc(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE ORDER BY time_modified DESC")
    fun getFilesModifiedDesc(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE ORDER BY time_modified ASC")
    fun getFilesModifiedAsc(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE ORDER BY file_name DESC")
    fun getFilesNameDesc(): MutableList<FileEntity>

    @Query("SELECT * FROM $FILE_TABLE ORDER BY file_name ASC")
    fun getFilesNameAsc(): MutableList<FileEntity>
}