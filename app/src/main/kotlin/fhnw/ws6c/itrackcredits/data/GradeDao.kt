package fhnw.ws6c.itrackcredits.data

import androidx.room.*

@Dao
interface GradeDao {
    @Query("SELECT * FROM grade")
    fun getAll(): List<Grade>

    @Query("SELECT * FROM grade WHERE id LIKE (:id)")
    fun loadById(id: String): Grade?

    @Query("SELECT * FROM grade WHERE modulID LIKE (:modulID)")
    fun loadByModulID(modulID: Int): List<Grade>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(grade: Grade)

    @Update
    fun update(grade: Grade)

    @Delete
    fun delete(grade: Grade)

    @Query("DELETE FROM grade")
    fun deleteAll()

 //   @Query("SELECT id FROM grade WHERE id LIKE (:id)")    mit der id die id holen ???
 //   fun getGradeId(id: String): String?
}