package fhnw.ws6c.itrackcredits.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fhnw.ws6c.itrackcredits.data.*
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

internal class ITrackCreditsModelTest {

    private var model: ITrackCreditsModel? = null
    private val startDate1 = LocalDate.of(2019, 9, 19)
    private val endDate1 = LocalDate.of(2020, 1, 20)
    private val name1 = "Semester 1"

    @Before
    fun init() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        StudiengangRepository.loadStudiengang(appContext)
        val converterInstance = Converters()
        val db = Room.inMemoryDatabaseBuilder(
            appContext,
            AppDatabase::class.java
        ).addTypeConverter(converterInstance)
            .fallbackToDestructiveMigration()
            .build()
        model = ITrackCreditsModel(StudiengangRepository, db, appContext)
    }

    @Test
    fun testSetStudiengang() {
        //given
        val selected = "iCompetence"

        //when
        model!!.setStudiengang(selected)

        //then
        with(model!!) {
            assertEquals(true, majorHasBeenSelected)
            assertEquals("iCompetence", selectedMajor?.name ?: "")
            assertEquals(12, selectedMajor!!.modulgruppen.size)
        }
    }

    @Test
    fun testFormatDate() {
        //given
        val date = LocalDate.of(2022, 11, 15)

        //when
        val formattedDate = model!!.formatDate(date)

        //then
        assertEquals("15.11.2022", formattedDate)
    }

    @Test
    fun testFormatDoubleToString() {
        //given
        val double = 2.3456789
        val double2 = 2.3

        //when
        val formattedDouble = model!!.formatDoubleToString(double)
        val formatterDouble2 = model!!.formatDoubleToString(double2)
        //then
        assertEquals("2.35", formattedDouble)
        assertEquals("2.3", formatterDouble2)
    }

    @Test
    fun testAddSemester() {
        //given
        assertEquals(0, model!!.allSemesters.size)

        //when
        model!!.addSemester(name1, startDate1, endDate1)

        //then
        with(model!!.allSemesters[0]) {
            assertEquals(1, model!!.allSemesters.size)
            assertEquals(1, id)
            assertEquals("Semester 1", name)
            assertEquals(LocalDate.of(2019, 9, 19), startDate)
            assertEquals(LocalDate.of(2020, 1, 20), endDate)
            assertEquals(0, credits)
            assertEquals(0, modules.size)
            assertEquals(false, completed)
        }
    }

    @Test
    fun testEditSemester() {
        //given
        val newName = "New Semester 1"
        val newStartDate = LocalDate.of(2019, 10, 19)
        val newEndDate = LocalDate.of(2020, 2, 20)

        //when
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.editSemester(1, newName, newStartDate, newEndDate)

        //then
        with(model!!.allSemesters[0]) {
            assertEquals(1, model!!.allSemesters.size)
            assertEquals(1, id)
            assertEquals("New Semester 1", name)
            assertEquals(LocalDate.of(2019, 10, 19), startDate)
            assertEquals(LocalDate.of(2020, 2, 20), endDate)
            assertEquals(0, credits)
            assertEquals(0, modules.size)
            assertEquals(false, completed)
        }
    }

    @Test
    fun testAddModuleToSemester() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val gradeType = GradeType.NUMBER

        //when
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, gradeType)

        //then
        with(model!!.allSemesters[0].modules) {
            assertEquals(1, size)
            assertEquals(1, get(0).idModul)
            assertEquals("Programmieren in C++", get(0).nameModul)
            assertEquals("prcpp", get(0).code)
            assertEquals(3, get(0).credits)
            assertEquals(true, get(0).hs)
            assertEquals(true, get(0).fs)
            assertEquals("NONE", get(0).msp)
            //assertEquals(1, get(0).requirements.size)
            assertEquals(GradeType.NUMBER, get(0).gradeType)
            assertEquals(emptyList<Grade>(), get(0).allPartialGrades)
        }
    }

    @Test
    fun testAddModule() {
        //given
        model!!.setStudiengang("iCompetence")
        val module = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val values = """
                        {
                        "id":      ${module.idModul},
                        "name":    "${module.nameModul}",
                        "code":   "${module.code}",
                        "credits": "${module.credits}",
                        "hs": "${module.hs}",
                        "fs": "${module.fs}",
                        "msp": "${module.msp}",
                        "requirements": []
                        }
                """.trimIndent()
        val selectedModulgroup = model!!.selectedMajor!!.modulgruppen[0]
        val gradeType = GradeType.NUMBER

        model!!.addSemester(name1, startDate1, endDate1)

        //when
        model!!.addModule(model!!.allSemesters[0], selectedModulgroup, values, gradeType)

        //then
        with(model!!.allSemesters[0].modules) {
            assertEquals(1, size)
            assertEquals(6008035, get(0).idModul)
            assertEquals("Programmieren in C++", get(0).nameModul)
            assertEquals("prcpp", get(0).code)
            assertEquals(3, get(0).credits)
            assertEquals(true, get(0).hs)
            assertEquals(true, get(0).fs)
            assertEquals("NONE", get(0).msp)
            //assertEquals(1, get(0).requirements.size)
            assertEquals(GradeType.NUMBER, get(0).gradeType)
            assertEquals(emptyList<Grade>(), get(0).allPartialGrades)
        }
    }

    @Test
    fun testAddGrade() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val gradeType = GradeType.NUMBER

        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, gradeType)
        val selectedModule = model!!.allSemesters[0].modules[0]

        //when
        model!!.addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
        model!!.addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 0, 0.0, true, 0.0 )
        model!!.addGrade(selectedModule, GradeType.BONUS, "Bonus", 0, 0.0, false, 0.2 )

        //then
        with(selectedModule) {
            assertEquals(3, allPartialGrades.size)

            assertEquals("Note", allPartialGrades[0].name)
            assertEquals(GradeType.NUMBER, allPartialGrades[0].gradeType)
            assertEquals(10, allPartialGrades[0].weight)
            assertEquals(5.0, allPartialGrades[0].gradeNumber)
            assertEquals(false, allPartialGrades[0].gradePass)
            assertEquals(0.0, allPartialGrades[0].gradeBonus)

            assertEquals("Pass", allPartialGrades[1].name)
            assertEquals(GradeType.PASS_FAIL, allPartialGrades[1].gradeType)
            assertEquals(0, allPartialGrades[1].weight)
            assertEquals(0.0, allPartialGrades[1].gradeNumber)
            assertEquals(true, allPartialGrades[1].gradePass)
            assertEquals(0.0, allPartialGrades[1].gradeBonus)

            assertEquals("Bonus", allPartialGrades[2].name)
            assertEquals(GradeType.BONUS, allPartialGrades[2].gradeType)
            assertEquals(0, allPartialGrades[2].weight)
            assertEquals(0.0, allPartialGrades[2].gradeNumber)
            assertEquals(false, allPartialGrades[2].gradePass)
            assertEquals(0.2, allPartialGrades[2].gradeBonus)
        }
    }

    @Test
    fun updateGrade() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
        val selectedModule = model!!.allSemesters[0].modules[0]
        model!!.addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )

        //when
        model!!.updateGrade(selectedModule.allPartialGrades[0], GradeType.PASS_FAIL, "New Name", 20, 6.0, true, 1.0)

        //then
        with(selectedModule.allPartialGrades[0]) {
            assertEquals("New Name", name)
            assertEquals(GradeType.PASS_FAIL, gradeType)
            assertEquals(20, weight)
            assertEquals(6.0, gradeNumber)
            assertEquals(true, gradePass)
            assertEquals(1.0, gradeBonus)
        }
    }

    @Test
    fun testCalcModulePass() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[1].modules[0]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[1], modul2, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
            addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 10, 0.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.PASS_FAIL, "Pass", 10, 4.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )

            //when
            val modulPass = calcModulePass(allSemesters[0].modules[0])
            val modul2Pass = calcModulePass(allSemesters[0].modules[1])

            //then
            assertEquals(false, modulPass)
            assertEquals(true, modul2Pass)
        }
    }

    @Test
    fun testCalcModuleGradeNoGradesPresent() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[1].modules[0]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[1], modul2, GradeType.PASS_FAIL)

            //when
            val modulGrade = calcModuleGrade(allSemesters[0].modules[0])
            val modul2Grade = calcModuleGrade(allSemesters[0].modules[1])

            //then
            assertEquals(-1.0, modulGrade)
            assertEquals(-1.0, modul2Grade)
        }
    }

    @Test
    fun testCalcModuleGradeWithGradesPresent() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[1].modules[0]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[1], modul2, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.75, false, 0.0 )
            addGrade(selectedModule, GradeType.NUMBER, "Note", 20, 5.0, false, 0.0 )
            addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 10, 0.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.PASS_FAIL, "Pass", 10, 4.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )

            //when
            val modulGrade = calcModuleGrade(allSemesters[0].modules[0])
            val modul2Grade = calcModuleGrade(allSemesters[0].modules[1])

            //then
            assertEquals(5.25, modulGrade)
            assertEquals(4.0, modul2Grade)
        }
    }

    @Test
    fun testGetCompletedSemesters() {
        //given
        model!!.setStudiengang("iCompetence")
        model!!.addSemester(name1, startDate1, endDate1)

        //when
        val completedSemesters = model!!.getCompletedSemesters()
        model!!.allSemesters[0].completed = true
        val completedSemesters2 = model!!.getCompletedSemesters()

        //then
        assertEquals(emptyList<Semester>(), completedSemesters)
        assertEquals(1, completedSemesters2.size)
        assertEquals(model!!.allSemesters[0], completedSemesters2[0])
    }

    @Test
    fun testCalcOverallCompletedModulgroupCredits() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val modul2 = model!!.selectedMajor!!.modulgruppen[1].modules[0]
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[1], modul2, GradeType.NUMBER)
        val selectedModule = model!!.allSemesters[0].modules[0]
        val selectedModule2 = model!!.allSemesters[0].modules[1]
        model!!.addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
        model!!.addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )
        //when
        val completedCredits1 = model!!.calcOverallCompletedModulgroupCredits(model!!.selectedMajor!!.modulgruppen)
        model!!.allSemesters[0].completed = true
        val completedCredits2 = model!!.calcOverallCompletedModulgroupCredits(model!!.selectedMajor!!.modulgruppen)

        //then
        assertEquals(0, completedCredits1)
        assertEquals(9, completedCredits2)
    }

    @Test
    fun testCalcCompletedModulgroupCredits() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val modul2 = model!!.selectedMajor!!.modulgruppen[0].modules[1]
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
        val selectedModule = model!!.allSemesters[0].modules[0]
        val selectedModule2 = model!!.allSemesters[0].modules[1]
        model!!.addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
        model!!.addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )

        //when
        val completedCredits1 = model!!.calcCompletedModulgroupCredits(model!!.selectedMajor!!.modulgruppen[0])
        model!!.allSemesters[0].completed = true
        val completedCredits2 = model!!.calcCompletedModulgroupCredits(model!!.selectedMajor!!.modulgruppen[0])

        //then
        assertEquals(0, completedCredits1)
        assertEquals(6, completedCredits2)
    }

    @Test
    fun testCheckIfModuleTaken() {
        //given
        model!!.setStudiengang("iCompetence")
        val modul = model!!.selectedMajor!!.modulgruppen[0].modules[0]
        val modul2 = model!!.selectedMajor!!.modulgruppen[0].modules[1]
        model!!.addSemester(name1, startDate1, endDate1)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
        model!!.addModuleToSemester(model!!.allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)

        //when
        model!!.allSemesters[0].completed = true
        val mod1taken = model!!.checkIfModuleTaken(modul)
        val mod2taken = model!!.checkIfModuleTaken(modul2)
        val mod3taken = model!!.checkIfModuleTaken(model!!.selectedMajor!!.modulgruppen[0].modules[2]
        )
        //then
        assertEquals(true, mod1taken)
        assertEquals(true, mod2taken)
        assertEquals(false, mod3taken)
    }

    @Test
    fun testCalcPassOfModulgroupModul() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
            addGrade(selectedModule, GradeType.NUMBER, "Note", 20, 4.0, false, 0.0 )
            addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 10, 0.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.PASS_FAIL, "Pass", 10, 4.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, true, 0.0 )
            allSemesters[0].completed = true
            //when
            val modul1pass = calcPassOfModulgroupModul(modul)
            val modul2pass = calcPassOfModulgroupModul(modul2)

            //then
            assertEquals(false, modul1pass)
            assertEquals(true, modul2pass)
        }
    }

    @Test
    fun testCalcPassedNrOfModulesFromModulgroup() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.0, false, 0.0 )
            addGrade(selectedModule, GradeType.NUMBER, "Note", 20, 4.0, false, 0.0 )
            addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 10, 0.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.PASS_FAIL, "Pass", 10, 4.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, true, 0.0 )
            allSemesters[0].completed = true

            //when
            val passedNrOfModules = calcPassedNrOfModulesFromModulgroup(selectedMajor!!.modulgruppen[0])
            val passedNrOfModules2 = calcPassedNrOfModulesFromModulgroup(selectedMajor!!.modulgruppen[1])
            val passedNrOfModules3 = calcPassedNrOfModulesFromModulgroup(selectedMajor!!.modulgruppen[2])

            //then
            assertEquals(2, passedNrOfModules)
            assertEquals(0, passedNrOfModules2)
            assertEquals(0, passedNrOfModules3)
        }
    }

    @Test
    fun testCalcGradeOfModulgroupModul() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 5.75, false, 0.0 )
            addGrade(selectedModule, GradeType.NUMBER, "Note", 20, 5.0, false, 0.0 )
            addGrade(selectedModule, GradeType.PASS_FAIL, "Pass", 10, 0.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.PASS_FAIL, "Pass", 10, 4.0, true, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 4.0, true, 0.0 )
            allSemesters[0].completed = true
            //when
            val modul1grade = calcGradeOfModulgroupModul(modul)
            val modul2grades = calcGradeOfModulgroupModul(modul2)

            //then
            assertEquals(5.25, modul1grade)
            assertEquals(4.0, modul2grades)
        }
    }

    @Test
    fun testGetGradeType() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            addSemester(name1, startDate1, endDate1)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.PASS_FAIL)
            allSemesters[0].completed = true
            //when
            val gradetyoe1 = getGradeType(modul)
            val gradetype2 = getGradeType(modul2)

            //then
            assertEquals(GradeType.NUMBER, gradetyoe1)
            assertEquals(GradeType.PASS_FAIL, gradetype2)
        }
    }

    @Test
    fun testCalcAverageGrade() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            val modul3 = selectedMajor!!.modulgruppen[0].modules[2]
            val modul4 = selectedMajor!!.modulgruppen[1].modules[0]
            addSemester(name1, startDate1, endDate1)
            addSemester("name2", LocalDate.of(2022,1,1), LocalDate.now())
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul3, GradeType.PASS_FAIL)
            addModuleToSemester(allSemesters[1], model!!.selectedMajor!!.modulgruppen[1], modul4, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            val selectedModule3 = allSemesters[0].modules[2]
            val selectedModule4 = allSemesters[1].modules[0]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 5.0, true, 0.0 )
            addGrade(selectedModule3, GradeType.NUMBER, "Note", 10, 6.0, true, 0.0 )
            addGrade(selectedModule4, GradeType.NUMBER, "Note", 10, 5.25, true, 0.0 )
            allSemesters[0].completed = true

            //when
            val avg = calcAverageGrade()
            //then
            assertEquals(4.5, avg)
            //given
            allSemesters[1].completed = true
            //when
            val avg1 = calcAverageGrade()
            //then
            assertEquals(4.75, avg1)
        }
    }

    @Test
    fun testCalcPassedCredits() {
        with(model!!) {
            //given
            setStudiengang("iCompetence")
            val modul = selectedMajor!!.modulgruppen[0].modules[0]
            val modul2 = selectedMajor!!.modulgruppen[0].modules[1]
            val modul3 = selectedMajor!!.modulgruppen[0].modules[2]
            val modul4 = selectedMajor!!.modulgruppen[1].modules[0]
            addSemester(name1, startDate1, endDate1)
            addSemester("name2", LocalDate.of(2022,1,1), LocalDate.now())
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul2, GradeType.NUMBER)
            addModuleToSemester(allSemesters[0], model!!.selectedMajor!!.modulgruppen[0], modul3, GradeType.PASS_FAIL)
            addModuleToSemester(allSemesters[1], model!!.selectedMajor!!.modulgruppen[1], modul4, GradeType.NUMBER)
            val selectedModule = allSemesters[0].modules[0]
            val selectedModule2 = allSemesters[0].modules[1]
            val selectedModule3 = allSemesters[0].modules[2]
            val selectedModule4 = allSemesters[1].modules[0]
            addGrade(selectedModule, GradeType.NUMBER, "Note", 10, 4.0, false, 0.0 )
            addGrade(selectedModule2, GradeType.NUMBER, "Note", 10, 5.0, true, 0.0 )
            addGrade(selectedModule3, GradeType.NUMBER, "Note", 10, 6.0, true, 0.0 )
            addGrade(selectedModule4, GradeType.NUMBER, "Note", 10, 5.25, true, 0.0 )
            allSemesters[0].completed = true

            //when
            val passedCredits = calcPassedCredits()
            //then
            assertEquals(9, passedCredits)
            //when
            allSemesters[1].completed = true
            val passedCredits2 = calcPassedCredits()
            //then
            assertEquals(15, passedCredits2)
        }
    }

    @Test
    fun testIsGradeInputValidNumber() {
        //given
        model!!.selectedGradeType = GradeType.NUMBER
        model!!.selectedGradeNumber = 5.0
        model!!.selectedGradePass = false
        model!!.selectedGradeBonus = 0.0
        model!!.gradeTitle = "Note"

        //when
        val valid = model!!.isGradeInputValid()

        //then
        assertEquals(true, valid)
    }

    @Test
    fun testIsGradeInputValidPassFail() {
        //given
        model!!.selectedGradeType = GradeType.PASS_FAIL
        model!!.selectedGradeNumber = 5.0
        model!!.selectedGradePass = false
        model!!.selectedGradeBonus = 1.0
        model!!.gradeTitle = "Note"

        //when
        val valid = model!!.isGradeInputValid()

        //then
        assertEquals(true, valid)
    }

    @Test
    fun testIsGradeInputValidBonus() {
        //given
        model!!.selectedGradeType = GradeType.BONUS
        model!!.selectedGradeNumber = 5.0
        model!!.selectedGradePass = false
        model!!.selectedGradeBonus = 1.0
        model!!.gradeTitle = "Note"

        //when
        val valid = model!!.isGradeInputValid()

        //then
        assertEquals(true, valid)
    }
}