package fhnw.ws6c.itrackcredits.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ModulgruppeDao {
    @Query("SELECT * FROM modulgruppe")
    fun getAll(): List<Modulgruppe>

    @Query("SELECT * FROM modulgruppe WHERE modulgruppeID LIKE (:modulgruppeID)")
    fun loadById(modulgruppeID: Int): Modulgruppe?

    @Query("SELECT * FROM modulgruppe WHERE major LIKE (:majorId)")
    fun loadByMajor(majorId: Int): List<Modulgruppe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(modulgruppe: Modulgruppe)

    @Update
    fun update(modulgruppe: Modulgruppe)

    @Delete
    fun delete(modulgruppe: Modulgruppe)

    @Query("DELETE FROM modulgruppe")
    fun deleteAll()

   // @Query("SELECT modulgruppeID FROM modulgruppe WHERE modulgruppeID LIKE (:modulgruppeID)")
  //  fun getModulgroupId(modugruppeId: String): String?

}