package com.remi.pdfscanner.db


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class], version = 1)
abstract class FileDatabase : RoomDatabase(){
    abstract  fun fileDao(): FileDao
}