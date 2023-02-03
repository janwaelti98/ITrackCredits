package fhnw.ws6c.itrackcredits.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Grade(
    @PrimaryKey var id: Int,
    @ColumnInfo(name="gradeType") var gradeType: GradeType,
    @ColumnInfo(name="name") var name: String,
    @ColumnInfo(name="weight") var weight: Int,
    @ColumnInfo(name="gradeNumber") var gradeNumber: Double,
    @ColumnInfo(name="gradePass") var gradePass: Boolean,
    @ColumnInfo(name="gradeBonus") var gradeBonus: Double,
    @ColumnInfo(name="modulID") var modulID: Int,
)

enum class GradeType(val gradeTypeName: String) {
    PASS_FAIL(gradeTypeName = "Pass/Fail"),
    NUMBER(gradeTypeName = "Note"),
    BONUS(gradeTypeName = "Bonus")
}
