package fhnw.ws6c.itrackcredits.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ModulDao {
    @Query("SELECT * FROM modul")
    fun getAll(): List<Modul>

    @Query("SELECT * FROM modul WHERE idModul LIKE (:idModul)")
    fun loadById(idModul: Int): Modul?

    @Query("SELECT * FROM modul WHERE modulgruppeID LIKE (:modulgruppeID)")
    fun loadByModulgruppe(modulgruppeID: Int): List<Modul>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(modul: Modul)

    @Update
    fun update(modul: Modul)

    @Delete
    fun delete(modul: Modul)

    @Query("DELETE FROM modul")
    fun deleteAll()

   // @Query("SELECT id FROM modul WHERE id LIKE (:id)")
   // fun getModulId(id: String): String?

}