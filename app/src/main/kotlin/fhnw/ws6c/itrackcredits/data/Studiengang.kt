package fhnw.ws6c.itrackcredits.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Studiengang(
    @PrimaryKey val id: Int,
    @ColumnInfo(name="name") val name: String,
) {
    @Ignore
    var modulgruppen: List<Modulgruppe> = emptyList()

    constructor(id: Int, name: String, modulgruppen: List<Modulgruppe>) : this(id = id, name = name) {
        this.modulgruppen = modulgruppen
    }

}
