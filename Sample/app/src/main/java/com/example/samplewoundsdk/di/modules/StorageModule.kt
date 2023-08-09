package com.example.samplewoundsdk.di.modules


import android.content.Context
import androidx.room.Room
import com.example.samplewoundsdk.storage.db.AssessmentRoomDatabase
import com.example.woundsdk.storage.shared.SharedMemory
import com.example.woundsdk.storage.shared.SharedMemoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class StorageModule {

    @Singleton
    @Provides
    fun providesAssessmentRoomDatabase(context: Context): AssessmentRoomDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AssessmentRoomDatabase::class.java,
            AssessmentRoomDatabase.DATABASE_NAME
        )
//            .addMigrations(migrationFromFirstToSecondVersion)
            .build()

    @Singleton
    @Provides
    fun providesSharedMemory(): SharedMemory = SharedMemoryImpl()
}
