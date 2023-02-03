package fhnw.ws6c.itrackcredits.model

enum class Screen(val title: String, val prevScreen: Screen) {
    HOME("Übersicht", HOME),
    SEMESTER("Semester", HOME),
    MODULTEPPICH("Modulteppich", HOME),
    MODUL("Modul", SEMESTER),
    MODULGRUPPE("Modulgruppe", MODULTEPPICH)
}