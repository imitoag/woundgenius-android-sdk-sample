package com.example.samplewoundsdk.di.modules

import com.example.samplewoundsdk.data.repo.SampleAppAssessmentsRepo
import com.example.samplewoundsdk.data.repo.impl.SampleAppAssessmentsRepoImpl
import com.example.samplewoundsdk.managers.SampleDateTimeManager
import com.example.samplewoundsdk.storage.db.AssessmentRoomDatabase
import com.example.woundsdk.storage.shared.SharedMemory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideAssessmentsRepo(
        assessmentDB: AssessmentRoomDatabase,
        sampleDateTimeManager: SampleDateTimeManager,
        sharedMemory: SharedMemory
    ): SampleAppAssessmentsRepo =
        SampleAppAssessmentsRepoImpl(
            assessmentDB,
            sampleDateTimeManager,
            sharedMemory
        )

}
