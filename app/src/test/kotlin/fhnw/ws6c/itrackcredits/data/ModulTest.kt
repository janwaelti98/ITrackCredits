package fhnw.ws6c.itrackcredits.data

import junit.framework.Assert.assertEquals
import org.json.JSONObject
import org.junit.Test

internal class ModulTest {
    private val moduleAsString = """
            {
             "id": "123",
             "name": "Objektoriertierte Programmierung I",
             "code": "oopI1",
             "credits": 3,
             "hs": true,
             "fs": false,
             "msp": "WRITTEN",
             "requirements": [
                {
                "id": "234"
                }
             ]
    }
    """.trimIndent()

    private val modulegruppeAsString = """
        {
         "id": "13",
        "name": "Design",
        "minima": 9,
        "parent": {
          "id": "1"
        },
        "modules": [
          {
            "id": "9046834",
            "name": "Designtheorie und -prozesse",
            "code": "dtpC",
            "credits": 3,
            "hs": true,
            "fs": false,
            "msp": "NONE",
            "requirements": []
          },
		  {
            "id": "9046845",
            "name": "User Interface und Interaction Design",
            "code": "uidC",
            "credits": 3,
            "hs": false,
            "fs": true,
            "msp": "NONE",
            "requirements": [
				{
				"id": "9046834"
				}
			]
          },
		  {
            "id": "9116316",
            "name": "Advanced Experience Design",
            "code": "adxd",
            "credits": 3,
            "hs": true,
            "fs": false,
            "msp": "NONE",
            "requirements": [
				{
				"id": "9046834"
				},
				{
				"id": "9046845"
				}
			]
          },
		  {
            "id": "9018825",
            "name": "Informations-Visualisierung",
            "code": "ivis",
            "credits": 3,
            "hs": true,
            "fs": true,
            "msp": "NONE",
            "requirements": [
				{
				"id": "9046834"
				},
				{
				"id": "9046845"
				},
				{
				"id": "9118392"
				},
				{
				"id": "6007991"
				}
			]
          },
		  {
            "id": "9018824",
            "name": "Media Computing",
            "code": "meco",
            "credits": 3,
            "hs": true,
            "fs": true,
            "msp": "NONE",
            "requirements": [
				{
				"id": "9046834"
				},
				{
				"id": "9046845"
				},
				{
				"id": "6007991"
				}
			]
          }
        ]
    }
    """.trimIndent()

    @Test
    fun testConstructor(){
        //given
        val modulegroupAsJSON = JSONObject(modulegruppeAsString)
        val moduleAsJSON = JSONObject(moduleAsString)

        //when
        val modulgruppe = Modulgruppe(modulegroupAsJSON)
        val modul = Modul(1, modulgruppe, moduleAsJSON)

        //then
        with(modul) {
            assertEquals(123, idModul)
            assertEquals("Objektoriertierte Programmierung I", nameModul)
            assertEquals("oopI1", code)
            assertEquals(3, credits)
            assertEquals(true, hs)
            assertEquals(false, fs)
            assertEquals("WRITTEN", msp)
            assertEquals(GradeType.NUMBER, gradeType)
            assertEquals(emptyList<Grade>(),  allPartialGrades)
            assertEquals("Design", modul.modulgruppe.nameMG)
        }
    }
}