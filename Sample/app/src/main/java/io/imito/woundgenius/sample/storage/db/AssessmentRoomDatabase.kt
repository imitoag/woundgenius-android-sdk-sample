package io.imito.woundgenius.sample.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.imito.woundgenius.sample.data.pojo.converter.CalibrationConverter
import io.imito.woundgenius.sample.data.pojo.converter.MeasurementDataConverter
import io.imito.woundgenius.sample.data.pojo.converter.MediaModelConverter
import io.imito.woundgenius.sample.data.pojo.converter.MetadataConverter
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.pojo.converter.AnnotationConverter
import io.imito.woundgenius.sample.data.pojo.converter.ThumbnailsConverter
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel
import io.imito.woundgenius.sample.storage.db.dao.MediaDao

@Database(
    entities = [SampleAssessmentEntity::class], version = 3, exportSchema = true
)
@TypeConverters(
    MediaModelConverter::class,
    MetadataConverter::class,
    MeasurementDataConverter::class,
    CalibrationConverter::class,
    AnnotationConverter::class,
    ThumbnailsConverter::class
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

        val migration2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
            CREATE TABLE sample_assessment_entity_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT,
                patientId TEXT,
                width_cm REAL,
                datetime TEXT,
                media TEXT,
                area_cm_sq REAL,
                wound_id TEXT,
                circumference_cm REAL,
                original_image_id TEXT,
                length_cm REAL,
                depth_cm REAL,
                created_by_user_id TEXT,
                created_by TEXT,
                observationsJson TEXT,
                stomaDocumentation INTEGER
            )
        """.trimIndent())

                database.execSQL("""
            INSERT INTO sample_assessment_entity_new (
                id,
                userId,
                patientId,
                width_cm,
                datetime,
                media,
                area_cm_sq,
                wound_id,
                circumference_cm,
                original_image_id,
                length_cm,
                depth_cm,
                created_by_user_id,
                created_by,
                observationsJson,
                stomaDocumentation
            )
            SELECT
                id,
                userId,
                patientId,
                width_cm,
                datetime,
                media,
                area_cm_sq,
                wound_id,
                circumference_cm,
                original_image_id,
                length_cm,
                depth_cm,
                created_by_user_id,
                created_by,
                observationsJson,
                isStoma
            FROM sample_assessment_entity
        """.trimIndent())


                database.execSQL("DROP TABLE sample_assessment_entity")


                database.execSQL("ALTER TABLE sample_assessment_entity_new RENAME TO sample_assessment_entity")
            }
        }
    }
}
