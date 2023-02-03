package fhnw.ws6c.itrackcredits.data

import androidx.room.*
import org.json.JSONObject

@Entity
data class Modul(
    @PrimaryKey val idModul: Int,
    @ColumnInfo(name="nameModul") val nameModul: String,
    @ColumnInfo(name="code") val code: String,
    @ColumnInfo(name="credits") val credits: Int,
    @ColumnInfo(name="hs") val hs: Boolean = false,
    @ColumnInfo(name="fs") val fs: Boolean = true,
    @ColumnInfo(name="msp") val msp: String = "wasistdas?",
    @ColumnInfo(name="modulgruppeID") var modulgruppeID: Int,
    @ColumnInfo(name="gradeType") var gradeType: GradeType = GradeType.NUMBER
) {

    @Ignore
    lateinit var modulgruppe: Modulgruppe
    @Ignore var allPartialGrades: List<Grade> = emptyList()
    @ColumnInfo(name="semesterId")
    var semesterId: Int = 0

    constructor(id: Int, nameModul: String, code: String, allPartialGrades: List<Grade>, modulgruppe: Modulgruppe) :
            this(idModul = id, nameModul = nameModul, code = code, credits = 3, hs = false, fs = true, msp = "", modulgruppeID = 0, gradeType = GradeType.NUMBER) {
        this.allPartialGrades = allPartialGrades.toMutableList()
        this.modulgruppe = modulgruppe
    }

    constructor(modulgruppeID: Int, modulgruppe: Modulgruppe, json: JSONObject) : this(json = json) {
        this.modulgruppe = modulgruppe
        this.modulgruppeID = modulgruppeID
    }

    constructor(modulgruppeID: Int, modulgruppe: Modulgruppe, json: String) : this(json = JSONObject(json)) {
        this.modulgruppe = modulgruppe
        this.modulgruppeID = modulgruppeID
    }



    constructor(json: JSONObject) : this(
        idModul = json.getString("id").toInt(),
        nameModul = json.getString("name"),
        code = json.getString("code"),
        credits = json.getInt("credits"),
        hs = json.getBoolean("hs"),
        fs = json.getBoolean("fs"),
        msp = json.getString("msp"),
        modulgruppeID = 0, //is updated later
        gradeType = GradeType.NUMBER
    )
}
