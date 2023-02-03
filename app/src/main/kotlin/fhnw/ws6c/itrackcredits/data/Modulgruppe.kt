package fhnw.ws6c.itrackcredits.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import fhnw.ws6c.itrackcredits.ui.theme.md_theme_dark_primary
import fhnw.ws6c.itrackcredits.ui.theme.md_theme_light_primary
import org.json.JSONArray
import org.json.JSONObject

@Entity
data class Modulgruppe(
    @PrimaryKey val modulgruppeID: Int,
    @ColumnInfo(name="nameMG") val nameMG: String,
    @ColumnInfo(name="minima") val minima: Int,
    @ColumnInfo(name="parent") val parent: String?,
) {

    @ColumnInfo(name="major")
    var major: Int = 0
    @Ignore val modulesArray: JSONArray = JSONArray()
    @Ignore var modules: List<Modul> = emptyList()
    //Theme
    @Ignore var lightColor  = md_theme_light_primary
    @Ignore var darkColor   = md_theme_dark_primary
    
    constructor(id: Int, nameMG: String, minima: Int, parent: String?, modules: List<Modul>) : this(modulgruppeID = id, nameMG = nameMG, minima = minima, parent = parent) {
        this.modules = modules
    }


    // constructor(jsonString: String) : this(JSONObject(jsonString))
    constructor(json: JSONObject) : this(
       modulgruppeID  = json.getString("id").toInt(),
        nameMG = json.getString("name"),
        minima = json.getInt("minima"),
        parent = if(json.get("parent").equals(null)) null else json.getJSONObject("parent").getString("id"),
        )
}
