package com.example.samplewoundsdk.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.samplewoundsdk.storage.db.dao.MediaDao

@Database(
    entities = [SampleAssessmentEntity::class], version = 2, exportSchema = true
)
@TypeConverters(
    SampleAssessmentEntity.Converter::class,
    MediaModel.Converter::class,
    MediaModel.Metadata.Converter::class,
    MediaModel.Metadata.MeasurementData.Converter::class,
    MediaModel.Metadata.MeasurementData.Calibration.Converter::class,
    MediaModel.Metadata.MeasurementData.Annotation.Converter::class
)
abstract class AssessmentRoomDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao

    companion object {
        const val DATABASE_NAME = "sample_assessment_database"
        val migrationFromFirstToSecondVersion: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE sample_assessment_entity "
                            + " ADD COLUMN isStoma INTEGER"
                )
            }
        }
    }
}
