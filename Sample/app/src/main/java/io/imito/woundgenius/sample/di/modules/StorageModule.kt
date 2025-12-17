package io.imito.woundgenius.sample.di.modules


import android.content.Context
import androidx.room.Room
import io.imito.woundgenius.sample.storage.db.AssessmentRoomDatabase
import io.imito.woundgenius.sample.storage.db.AssessmentRoomDatabase.Companion.migration2_3
import io.imito.woundgenius.sample.storage.db.AssessmentRoomDatabase.Companion.migrationFromFirstToSecondVersion
import dagger.Module
import dagger.Provides
import io.imito.woundgenius.sdk.storage.shared.SharedMemory
import io.imito.woundgenius.sdk.storage.shared.SharedMemoryImpl
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
        ).addMigrations(migrationFromFirstToSecondVersion,migration2_3)
            .build()

    @Singleton
    @Provides
    fun providesSharedMemory(context: Context): SharedMemory = SharedMemoryImpl(context)
}
