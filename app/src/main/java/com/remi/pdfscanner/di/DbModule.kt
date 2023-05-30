package com.remi.pdfscanner.di

import android.content.Context
import androidx.room.Room
import com.remi.pdfscanner.db.FileDatabase
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.util.Constant.FILE_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provide(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, FileDatabase::class.java, FILE_DATABASE)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDao(db: FileDatabase) = db.fileDao()

    @Provides
    fun provideEntity() = FileEntity()


}