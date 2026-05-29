package io.imito.woundgenius.sample.di.modules

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.repo.impl.SampleAppRepoImpl
import io.imito.woundgenius.sample.managers.SampleDateTimeManager
import io.imito.woundgenius.sample.storage.db.AssessmentRoomDatabase
import io.imito.woundgenius.sdk.internal.data.storage.shared.SharedMemory

import dagger.Module
import dagger.Provides
import io.imito.woundgenius.sample.di.scope.AppScope
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @AppScope
    fun provideAssessmentsRepo(
        assessmentDB: AssessmentRoomDatabase,
        sampleDateTimeManager: SampleDateTimeManager,
        sharedMemory: SharedMemory
    ): SampleAppRepo =
        SampleAppRepoImpl(
            assessmentDB,
            sampleDateTimeManager,
            sharedMemory
        )
}
