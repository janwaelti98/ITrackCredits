package fhnw.ws6c.itrackcredits.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudiengangDao {
    @Query("SELECT * FROM studiengang")
    fun getAll(): List<Studiengang>

    @Query("SELECT * FROM studiengang WHERE id LIKE (:studiengangId)")
    fun loadById(studiengangId: Int): Studiengang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(studiengang: Studiengang)

    @Update
    fun update(studiengang: Studiengang)

    @Delete
    fun delete(studiengang: Studiengang)

    @Query("DELETE FROM studiengang")
    fun deleteAll()
}