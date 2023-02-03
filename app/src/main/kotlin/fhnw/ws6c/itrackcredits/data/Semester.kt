package fhnw.ws6c.itrackcredits.data

import androidx.compose.runtime.mutableStateListOf
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class Semester(
    @PrimaryKey var id: Int,
    @ColumnInfo(name="name") var name: String,
    @ColumnInfo(name="startDate") var startDate: LocalDate = LocalDate.now(),
    @ColumnInfo(name="endDate") var endDate: LocalDate = LocalDate.now(),
    @ColumnInfo(name="completed") var completed: Boolean = false
    )
{

	@ColumnInfo(name="credits")
	var credits: Int = 0
    @Ignore //todo how to save modules and what semester they belong to?
    var modules: MutableList<Modul> = mutableStateListOf()

    constructor(id: Int, name: String, modules: List<Modul>) : this(id = id, name = name) {
        this.modules = modules.toMutableList()
    }

    fun addModule(modul: Modul) {
        modules.add(modul)
        credits += modul.credits
    }
}
