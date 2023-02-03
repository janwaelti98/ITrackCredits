package fhnw.ws6c.itrackcredits.data

import android.content.Context
import fhnw.ws6c.itrackcredits.ui.theme.*
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.StandardCharsets

object StudiengangRepository {

    lateinit var data: List<Studiengang>

    fun loadStudiengang(context: Context) {
        val allStudiengaenge = mutableListOf<Studiengang>()
        val modulgruppeniC = mutableListOf<Modulgruppe>()
        val modulgruppenIT = mutableListOf<Modulgruppe>()

        val dataiCraw = JSONObject(loadFromAsset(context, "iCompetence_Module_gruppiert.json"))
        val dataITraw = JSONObject(loadFromAsset(context, "Informatik_Module_gruppiert.json"))

        val dataiC = dataiCraw.getJSONObject("data").getJSONArray("groups")
        val dataIT = dataITraw.getJSONObject("data").getJSONArray("groups")
        for (modulIndex in 0 until dataiC.length()) {
            val modulgruppe = Modulgruppe(dataiC.getJSONObject(modulIndex))
            modulgruppe.major = 1
            if (modulgruppe.nameMG == "Hauptgruppe") continue
            val moduleDerGruppeJSONArray = dataiC.getJSONObject(modulIndex).optJSONArray("modules")
            val modulList: MutableList<Modul> = mutableListOf()
            if (moduleDerGruppeJSONArray != null) {
                for (i in 0 until moduleDerGruppeJSONArray.length()) {
                    modulList.add(Modul(modulgruppe.modulgruppeID, modulgruppe, moduleDerGruppeJSONArray.getJSONObject(i)))
                }
            }
            modulgruppe.modules = modulList
            modulgruppeniC.add(modulgruppe)
        }
        for (modulIndex in 0 until dataIT.length()) {
            val modulgruppe = Modulgruppe(dataIT.getJSONObject(modulIndex))
            modulgruppe.major = 2
            if (modulgruppe.nameMG == "Hauptgruppe") continue
            val moduleDerGruppeJSONArray = dataIT.getJSONObject(modulIndex).optJSONArray("modules")
            val modulList: MutableList<Modul> = mutableListOf()
            if (moduleDerGruppeJSONArray != null) {
                for (i in 0 until moduleDerGruppeJSONArray.length()) {
                    modulList.add(Modul(modulgruppe.modulgruppeID, modulgruppe, moduleDerGruppeJSONArray.getJSONObject(i)))
                }
            }
            modulgruppe.modules = modulList
            modulgruppenIT.add(modulgruppe)
        }

        loadColors(modulgruppeniC)
        loadColors(modulgruppenIT)

        allStudiengaenge.add(Studiengang(1,"iCompetence", modulgruppeniC))
        allStudiengaenge.add(Studiengang(2, "Informatik", modulgruppenIT))
        data = allStudiengaenge
    }

     fun loadColors(modulgruppen: List<Modulgruppe>) {
        for(modulgruppe in modulgruppen) {
            if(modulgruppe.major == 1) {
                when (modulgruppe.nameMG) {
                    "Ergänzungen" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Projekte" -> {
                        modulgruppe.lightColor = light_ProjectsContainer
                        modulgruppe.darkColor = dark_ProjectsContainer
                    }
                    "Informatik Grundlagen" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Management" -> {
                        modulgruppe.lightColor = light_ManagementContainer
                        modulgruppe.darkColor = dark_ManagementContainer
                    }
                    "Kommunikation" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "Englisch" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "GSW" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "Workshop" -> {
                        modulgruppe.lightColor = light_UIContainer
                        modulgruppe.darkColor = dark_UIContainer
                    }
                    "Software Engineering" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Design" -> {
                        modulgruppe.lightColor = light_DesignContainer
                        modulgruppe.darkColor = dark_DesignContainer
                    }
                    "Theoretische Grundlagen und Mathematik" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "User Interface Engineering" -> {
                        modulgruppe.lightColor = light_UIContainer
                        modulgruppe.darkColor = dark_UIContainer
                    }
                }
            }
            if(modulgruppe.major == 2) {
                when (modulgruppe.nameMG) {
                    "Informatik Ergänzungen" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Informatik" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Projekte" -> {
                        modulgruppe.lightColor = light_ProjectsContainer
                        modulgruppe.darkColor = dark_ProjectsContainer
                    }
                    "Betriebswirtschaft" -> {
                        modulgruppe.lightColor = light_ManagementContainer
                        modulgruppe.darkColor = dark_ManagementContainer
                    }
                    "Kommunikation" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "Englisch" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "GSW" -> {
                        modulgruppe.lightColor = light_KontextContainer
                        modulgruppe.darkColor = dark_KontextContainer
                    }
                    "Programmierung" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Software Engineering" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "ICT Systeme" -> {
                        modulgruppe.lightColor = light_DesignContainer
                        modulgruppe.darkColor = dark_DesignContainer
                    }
                    "Mathematik" -> {
                        modulgruppe.lightColor = light_HauptgruppeContainer
                        modulgruppe.darkColor = dark_HauptgruppeContainer
                    }
                    "Profilierungen" -> {
                        modulgruppe.lightColor = light_UIContainer
                        modulgruppe.darkColor = dark_UIContainer
                    }
                }
            }
        }
    }

    private fun loadFromAsset(context: Context, fileName: String): String {
        val inputStream: InputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()

        return String(buffer, StandardCharsets.UTF_8)
    }
}
