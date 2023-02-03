package fhnw.ws6c.itrackcredits.data

import androidx.room.*

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semester")
    fun getAll(): List<Semester>

    @Query("SELECT * FROM semester WHERE id LIKE (:id)")
    fun loadById(id: Int): Semester?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(semester: Semester)

    @Update
    fun update(semester: Semester)

    @Delete
    fun delete(semester: Semester)

    @Query("DELETE FROM semester")
    fun deleteAll()

  //  @Query("SELECT id FROM semester WHERE id LIKE (:id)")
  //  fun getGradeId(id: String): String?
}