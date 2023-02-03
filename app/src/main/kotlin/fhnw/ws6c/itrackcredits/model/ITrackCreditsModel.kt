package fhnw.ws6c.itrackcredits.model

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fhnw.ws6c.itrackcredits.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ITrackCreditsModel(private val repo: StudiengangRepository, db: AppDatabase, context: Context) {
    var title                                       = "ITrackCredits"
    var selectedMajor           : Studiengang?      by mutableStateOf(null)

    private val modelScope      = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val studiengangDao  = db.studiengangDao()
    private val modulgruppenDao = db.modulgruppeDao()
    private val semesterDao     = db.semesterDao()
    private val modulDao        = db.modulDao()
    private val gradeDao        = db.gradeDao()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("iTrackCreditsApp", Context.MODE_PRIVATE)
    var majorHasBeenSelected                        by mutableStateOf(false)
    var isDarkTheme                                 by mutableStateOf(false)
    private var majorID                             by mutableStateOf(0)
    private var semesterID                          by mutableStateOf(1)
    var moduleID                                    by mutableStateOf(1)
    private var gradeID                             by mutableStateOf(1)
    var isSettingsOpen                              by mutableStateOf(!majorHasBeenSelected)

    fun loadPrefs() {
        //load existing prefs (or set default values if first usage of app)
        majorHasBeenSelected = sharedPreferences.getBoolean("majorHasBeenSelected", false)
        isDarkTheme          = sharedPreferences.getBoolean("isDarkTheme", false)
        majorID              = sharedPreferences.getInt("majorID", 0)
        semesterID           = sharedPreferences.getInt("semesterID", 1)
        moduleID             = sharedPreferences.getInt("moduleID", 1)
        gradeID              = sharedPreferences.getInt("gradeID", 1)
        isSettingsOpen       = sharedPreferences.getBoolean("settingsOpen", true)
    }

    fun updatePrefs() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("majorHasBeenSelected", majorHasBeenSelected)
        editor.putBoolean("isDarkTheme", isDarkTheme)
        editor.putInt("majorID", majorID)
        editor.putInt("semesterID", semesterID)
        editor.putInt("moduleID", moduleID)
        editor.putInt("gradeID", gradeID)
        editor.putBoolean("settingsOpen", isSettingsOpen)
        editor.apply()
    }

    var allSemesters                                =  mutableStateListOf<Semester>()

    //Deletion of something
    var isSemesterBeingDeleted                      by mutableStateOf(false)
    var semesterBeingDeleted        : Semester?     = null
    var semesterToRemoveFromDB      : Semester?     = null

    var isModuleBeingDeleted                        by mutableStateOf(false)
    var moduleBeingDeleted          : Modul?        by mutableStateOf(null)
    var moduleToRemoveFromDB        : Modul?        by mutableStateOf(null)

    var isGradeBeingDeleted                         by mutableStateOf(false)
    var gradeBeingDeleted           : Grade?        by mutableStateOf(null)
    var gradeToRemoveFromDB         : Grade?        by mutableStateOf(null)

    //add/editSemester Dialog
    var isAddSemesterOpen                           by mutableStateOf(false)
    var isEditSemesterOpen                          by mutableStateOf(false)
    var selectedStartDate                           by mutableStateOf(LocalDate.now().minusDays(1))
    var selectedStartDateString                     by mutableStateOf(formatDate(LocalDate.now().minusDays(1)))
    var showStartDatepicker                         by mutableStateOf(false)
    var selectedEndDate                             by mutableStateOf(LocalDate.now())
    var selectedEndDateString                       by mutableStateOf(formatDate(LocalDate.now()))
    var showEndDatepicker                           by mutableStateOf(false)

    //addModule Dialog
    var isAddModuleOpen                             by mutableStateOf(false)
    var selectedModulegroup         : Modulgruppe?  by mutableStateOf(null)
    var selectedModulegroupName     : String        by mutableStateOf("")
    var selectedModule              : Modul?        by mutableStateOf(null)
    var selectedModuleName          : String        by mutableStateOf("")
    var modulegroupDropdownExpanded                 by mutableStateOf(false)

    //add/editGrade Dialog
    var isGradeDialogOpen                           by mutableStateOf(false)
    var isEditOfGrade                               by mutableStateOf(false)
    var gradeTitle                                  by mutableStateOf("")
    var selectedGradeNumber                         by mutableStateOf(0.0)
    var selectedGradePass                           by mutableStateOf(false)
    var selectedGradeBonus                          by mutableStateOf(0.0)
    var selectedGradeType                           by mutableStateOf(GradeType.NUMBER)
    var selectedWeight                              by mutableStateOf(100)

    //current
    var currentScreen                               by mutableStateOf(Screen.HOME)
    var currentSemester             : Semester?     by mutableStateOf(null)
    var currentModulegroup          : Modulgruppe?  by mutableStateOf(null)
    var currentModule               : Modul?        by mutableStateOf(null)
    var currentGrade                : Grade?        by mutableStateOf(null)

    // used to trigger a specific recompose of UI elements
    var triggerRecompose by mutableStateOf(false)

    fun loadDataFromDB() {
        modelScope.launch {
           selectedMajor = studiengangDao.loadById(majorID)
           selectedMajor?.let { major ->
               modulgruppenDao.loadByMajor(selectedMajor!!.id).forEach {
                   for (module in modulDao.loadByModulgruppe(it.modulgruppeID)){
                       for (grade in gradeDao.loadByModulID(module.idModul)){
                           module.allPartialGrades = module.allPartialGrades.plus(grade)
                       }
                       module.modulgruppe = it
                       if(module.semesterId == 0 || module.nameModul == module.code) it.modules = it.modules.plus(module)
                   }
                   major.modulgruppen = major.modulgruppen.plus(it)
               }
               repo.loadColors(major.modulgruppen)
           }
            semesterDao.getAll().forEach { sem ->
                modulDao.getAll().forEach { module ->
                    if(module.semesterId == sem.id && sem.modules.find {
                            it.idModul == module.idModul
                        } == null ) {
                        for (grade in gradeDao.loadByModulID(module.idModul)){
                            module.allPartialGrades = module.allPartialGrades.plus(grade)
                        }
                        module.modulgruppe = modulgruppenDao.loadById(module.modulgruppeID)!!
                        sem.modules.add(module)
                    }
                }
                allSemesters.add(sem)
            }
        }
    }

    fun setStudiengang(name: String) {
        selectedMajor = if (name == "iCompetence") repo.data[0] else repo.data[1]
        majorHasBeenSelected = true
        majorID = if(name == "iCompetence") 1 else 2

        persistMajorToDB(selectedMajor!!)
        this.updatePrefs()
    }

    private fun persistMajorToDB(studiengang: Studiengang) {
        modelScope.launch {
            studiengangDao.insert(studiengang)
            for (mg in studiengang.modulgruppen){
                modulgruppenDao.insert(mg)
                for (mod in mg.modules){
                    modulDao.insert(mod)
                    for (grade in mod.allPartialGrades){
                        gradeDao.insert(grade)
                    }
                }
            }
        }
    }

    fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return date.format(formatter)
    }

    fun formatDoubleToString(double: Double): String {
        val format = DecimalFormat("#.##")
        return format.format(double)
    }

    fun addSemester(name: String, startDate: LocalDate, endDate: LocalDate) {
        val newSemester = Semester(semesterID, name, startDate, endDate)
        allSemesters.add(newSemester)
        semesterID++
        modelScope.launch {
            semesterDao.insert(newSemester)
        }
        this.updatePrefs()
    }

    fun editSemester(id: Int, name: String, startDate: LocalDate, endDate: LocalDate) {
        val oldSemester = allSemesters.first { s -> s.id == id }
        oldSemester.name = name
        oldSemester.startDate = startDate
        oldSemester.endDate = endDate

        modelScope.launch {
            semesterDao.update(oldSemester)
        }
        triggerRecompose = !triggerRecompose
    }

    fun completeSemester(semester: Semester, completed: Boolean) {
        semester.completed = completed
        modelScope.launch {
            semesterDao.update(semester)
        }
        triggerRecompose = !triggerRecompose
    }

    fun prepareSemesterBeingDeleted(semester: Semester) {
        semesterBeingDeleted = semester
        semesterToRemoveFromDB = semester
        isSemesterBeingDeleted = true
    }

    fun prepareModuleBeingDeleted(module: Modul) {
        moduleBeingDeleted = module
        moduleToRemoveFromDB = module
        isModuleBeingDeleted = true
    }

    fun prepareGradeBeingDeleted(grade: Grade) {
        gradeBeingDeleted = grade
        gradeToRemoveFromDB = grade
        isGradeBeingDeleted = true
    }

    fun removeSemesterBeingDeleted() {
            if (semesterBeingDeleted != null) {
                allSemesters.remove(semesterBeingDeleted)
                modelScope.launch {
                    semesterDao.delete(semesterToRemoveFromDB!!)
                    semesterToRemoveFromDB!!.modules.forEach { curModule ->
                        modulDao.delete(curModule)
                        curModule.allPartialGrades.forEach { curGrade ->
                            gradeDao.delete(curGrade)
                        }
                    }
                    semesterToRemoveFromDB = null
                }
                isSemesterBeingDeleted = false
                semesterBeingDeleted = null
            }
    }

    fun removeModuleBeingDeleted() {
        if (moduleBeingDeleted != null) {
            currentSemester!!.credits -= moduleBeingDeleted!!.credits
            currentSemester!!.modules.remove(moduleBeingDeleted)

            //If moduleBeingDeleted is custom module
            if(moduleBeingDeleted!!.nameModul == moduleBeingDeleted!!.code) {
                //remove from selected Studiengang
                var mgIndex = 0
                selectedMajor!!.modulgruppen.forEachIndexed{ index, it ->
                    if(it.modulgruppeID == moduleBeingDeleted!!.modulgruppeID) mgIndex = index
                }
                selectedMajor!!.modulgruppen[mgIndex].modules = selectedMajor!!.modulgruppen[mgIndex].modules.minus(moduleBeingDeleted!!)
            }

            modelScope.launch {
                semesterDao.update(currentSemester!!)
                modulDao.delete(moduleToRemoveFromDB!!)
                moduleToRemoveFromDB!!.allPartialGrades.forEach {
                    gradeDao.delete(it)
                }
                moduleToRemoveFromDB = null
            }
            isModuleBeingDeleted = false
            moduleBeingDeleted = null
        }
    }

    fun removeGradeBeingDeleted() {
        if(gradeBeingDeleted != null) {
            currentModule!!.allPartialGrades = currentModule!!.allPartialGrades.minus(gradeBeingDeleted!!)

            modelScope.launch {
                gradeDao.delete(gradeToRemoveFromDB!!)
                gradeToRemoveFromDB = null
            }
            isGradeBeingDeleted = false
            gradeBeingDeleted = null
        }
    }

    fun resetRemoveSemester() {
            isSemesterBeingDeleted = false
            semesterBeingDeleted = null
            semesterToRemoveFromDB = null
    }

    fun resetRemoveModule() {
        isModuleBeingDeleted = false
        moduleBeingDeleted = null
        moduleToRemoveFromDB = null
    }

    fun resetRemoveGrade() {
        isGradeBeingDeleted = false
        gradeBeingDeleted = null
        gradeToRemoveFromDB = null
    }

    fun addModuleToSemester(curSem: Semester, selModulgroup: Modulgruppe, module: Modul, gradeType: GradeType) {
        val values = """
                        {
                        "id":      ${moduleID},
                        "name":    "${module.nameModul}",
                        "code":   "${module.code}",
                        "credits": "${module.credits}",
                        "hs": "${module.hs}",
                        "fs": "${module.fs}",
                        "msp": "${module.msp}",
                        "requirements": []
                        }
                """.trimIndent()
        val newModule = Modul(selModulgroup.modulgruppeID, selModulgroup, values)
        newModule.semesterId = curSem.id
        newModule.gradeType = gradeType
        curSem.addModule(newModule)
        moduleID++

        modelScope.launch {
            modulDao.insert(newModule)
            semesterDao.update(curSem)
        }
        this.updatePrefs()
    }

    //Custom module
    fun addModule(curSem: Semester, selModulgroup: Modulgruppe, values: String, gradeType: GradeType) {
        val newModule = Modul(selModulgroup.modulgruppeID, selModulgroup, values)
        newModule.semesterId = curSem.id
        newModule.gradeType = gradeType
        curSem.addModule(newModule)
        moduleID++

        //add to selected studiengang
        var mgIndex = 0
        selectedMajor!!.modulgruppen.forEachIndexed{ index, it ->
            if(it.modulgruppeID == selModulgroup.modulgruppeID) mgIndex = index
        }
        selectedMajor!!.modulgruppen[mgIndex].modules = selectedMajor!!.modulgruppen[mgIndex].modules.plus(newModule)

        modelScope.launch {
            modulDao.insert(newModule)
            semesterDao.update(curSem)
        }
        this.updatePrefs()
    }

    fun addGrade(
        curModule: Modul,
        gradeType: GradeType,
        name: String,
        weight: Int,
        gradeNumber: Double,
        gradePass: Boolean,
        gradeBonus: Double
    ) {
        val newGrade = Grade(
            gradeID,
            gradeType,
            name,
            weight,
            gradeNumber,
            gradePass,
            gradeBonus,
            curModule.idModul
        )
        curModule.allPartialGrades = curModule.allPartialGrades.plus(newGrade)
        gradeID++

        modelScope.launch {
            gradeDao.insert(newGrade)
        }
        this.updatePrefs()
    }

    fun updateGrade(
        curGrade: Grade,
        gradeType: GradeType,
        name: String,
        weight: Int,
        gradeNumber: Double,
        gradePass: Boolean,
        gradeBonus: Double
    ) {
        curGrade.gradeType = gradeType
        curGrade.name = name
        curGrade.weight = weight
        curGrade.gradeNumber = gradeNumber
        curGrade.gradePass = gradePass
        curGrade.gradeBonus = gradeBonus

        modelScope.launch {
            gradeDao.update(curGrade)
        }
        triggerRecompose = !triggerRecompose
    }

    //###################### Calculation functions ######################

    //returns the number of passed credits in a semester
    fun getPassedSemesterCredits(semester: Semester): Int {
        var sum = 0
        semester.modules.forEach{
            if(calcModulePass(it)) sum += it.credits
        }
        return sum
    }

    //returns false if no grades present, or if module grade is <4 or if a fail is present
    fun calcModulePass(module: Modul): Boolean {
        if(module.allPartialGrades.isEmpty()) return false
        var fail = false
        val grade = calcModuleGrade(module)
        if(grade < 4 && grade >= 0 ) fail = true
        module.allPartialGrades.forEach {
            if (it.gradeType == GradeType.PASS_FAIL && !it.gradePass) fail = true
        }
        return !fail
    }

    //calculates a module's overall grade
    fun calcModuleGrade(module: Modul): Double {
        var sumOfGrades = 0.0
        var sumOfPercentages = 0.0
        var nrOfGrades = 0
        var bonus = 0.0
        if(module.allPartialGrades.isEmpty()) return -1.0
        module.allPartialGrades.forEach {
            if (it.gradeType == GradeType.NUMBER && it.weight != 0) {
                sumOfGrades += it.gradeNumber * (it.weight * 0.01)
                sumOfPercentages += it.weight * 0.01
                nrOfGrades++
            } else if (it.gradeType == GradeType.BONUS) bonus += it.gradeBonus
        }
        return if(sumOfPercentages == 0.0) bonus else (sumOfGrades / sumOfPercentages) + bonus
    }

    //find all completed semesters
    fun getCompletedSemesters(): List<Semester> {
        val semesters = allSemesters
        var completedSemesters: List<Semester> = emptyList()
        semesters.forEach {
            if (it.completed) completedSemesters = completedSemesters.plus(it)
        }
        return completedSemesters
    }

    //calculates an overall modulgroup's credits (e.g. Hauptgruppe)
    fun calcOverallCompletedModulgroupCredits(modulgroups: List<Modulgruppe>): Int {
        val completedSemesters = getCompletedSemesters()
        val size = modulgroups.size
        var i = 0
        var sum = 0
        while (i < size) {
            completedSemesters.forEach { semester ->
                semester.modules.forEach { mod ->
                    //only count the ones that are passed
                    if(mod.modulgruppeID == modulgroups[i].modulgruppeID && calcModulePass(mod)) {
                        sum += mod.credits
                    }
                }
            }
            i++
        }
        return sum
    }

    //calculates a modulgroups completed credits
    fun calcCompletedModulgroupCredits(modulgruppe: Modulgruppe): Int {
        val completedSemesters = getCompletedSemesters()
        var sumOfCredits = 0
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                //only count the ones that are passed
                if(module.modulgruppeID == modulgruppe.modulgruppeID && calcModulePass(module)) {
                    sumOfCredits += module.credits
                }
            }
        }
        return sumOfCredits
    }

    //for Modulteppich
    fun checkIfModuleTaken(curModule: Modul): Boolean {
        val completedSemesters = getCompletedSemesters()
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                //return true if module found in completed semesters
                if (module.code == curModule.code) return true
            }
        }
        return false
    }

    fun calcPassOfModulgroupModul(curModule: Modul): Boolean {
        val completedSemesters = getCompletedSemesters()
        var lastFoundModule: Modul? = null
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                if(module.code == curModule.code) {
                    lastFoundModule = module
                }
            }
        }
        return if(lastFoundModule != null) calcModulePass(lastFoundModule!!) else false

    }

    fun calcPassedNrOfModulesFromModulgroup(modulgruppe: Modulgruppe): Int {
        val completedSemesters = getCompletedSemesters()
        var sumOfModules = 0
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                //only count the ones that are passed
                if(module.modulgruppeID == modulgruppe.modulgruppeID && calcModulePass(module)) {
                    sumOfModules++
                }
            }
        }
        return sumOfModules
    }

    fun calcGradeOfModulgroupModul(curModule: Modul): Double {
        val completedSemesters = getCompletedSemesters()
        var lastFoundModule: Modul? = null
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                if(module.code == curModule.code) {
                    lastFoundModule = module
                }
            }
        }
        return if(lastFoundModule != null) calcModuleGrade(lastFoundModule!!) else 0.0
    }

    fun getGradeType(curModule: Modul): GradeType {
        val completedSemesters = getCompletedSemesters()
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                if (module.code == curModule.code) {
                    return module.gradeType
                }
            }
        }
        return GradeType.NUMBER
    }

    //calculates the overall average grade of the whole studium
    fun calcAverageGrade(): Double {
        val completedSemesters = getCompletedSemesters()
        var sumOfGrades = 0.0
        var sumOfPassedModules = 0
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                //only count the ones that are passed and have an actual number grade
                val modulePassed = calcModulePass(module)
                if (module.gradeType == GradeType.NUMBER && modulePassed) {
                    sumOfGrades += calcModuleGrade(module)
                    sumOfPassedModules++
                }
            }
        }
        return if(sumOfPassedModules == 0) -1.0 else (sumOfGrades / sumOfPassedModules)
    }

    //calculates all passed credits in the studium
    fun calcPassedCredits(): Int {
        val completedSemesters = getCompletedSemesters()
        var sum = 0
        completedSemesters.forEach { semester ->
            semester.modules.forEach { module ->
                if(calcModulePass(module)) sum += module.credits
            }
        }
        return sum
    }

    fun isGradeInputValid(): Boolean {
        return when (selectedGradeType) {
            GradeType.NUMBER -> gradeTitle != "" && 1.0 <= selectedGradeNumber && selectedGradeNumber <= 6.0
            GradeType.BONUS -> gradeTitle != "" && 0.0 <= selectedGradeBonus && selectedGradeBonus <= 6.0
            GradeType.PASS_FAIL -> gradeTitle != ""
        }
    }
}
