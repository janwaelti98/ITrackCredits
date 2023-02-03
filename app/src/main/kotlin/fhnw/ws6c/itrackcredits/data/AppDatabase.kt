package fhnw.ws6c.itrackcredits.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Studiengang::class, Semester::class, Modulgruppe::class, Modul::class, Grade::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studiengangDao(): StudiengangDao
    abstract fun semesterDao(): SemesterDao
    abstract fun modulgruppeDao(): ModulgruppeDao
    abstract fun modulDao(): ModulDao
    abstract fun gradeDao(): GradeDao
}