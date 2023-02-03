package fhnw.ws6c.itrackcredits.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import fhnw.ws6c.itrackcredits.data.Modulgruppe
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.model.Screen
import fhnw.ws6c.itrackcredits.ui.*
import fhnw.ws6c.itrackcredits.ui.theme.dark_ProjectsContainer
import fhnw.ws6c.itrackcredits.ui.theme.light_ProjectsContainer

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulteppichScreen(model: ITrackCreditsModel) {
    Scaffold(
        topBar = { TopBar(model) },
        content = { ModulteppichScreenContent(model) }
    )
}

@Composable
fun ModulteppichScreenContent(model: ITrackCreditsModel) {
    Column(modifier = Modifier.padding(10.dp, 80.dp, 10.dp, 0.dp)) {
        Title(text = "Modulteppich")
        if (model.majorHasBeenSelected) ModulgruppenList(model)
    }
    SettingsDialog(model)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModulgruppenList(model: ITrackCreditsModel) {
    with(model) {
        val groupedModulgruppen = selectedMajor!!.modulgruppen
            .sortedBy { m -> m.lightColor.value }
            .groupBy { it.parent }
        val scrollState = rememberLazyListState()
        LazyColumn(
            state = scrollState,
            modifier = Modifier.padding(top = 10.dp),
            content = {
                groupedModulgruppen.forEach { (id, allModulgruppe) ->
                    stickyHeader {
                        if (id != null) {
                            val groupTitle =
                                if (id == "1") "Hauptgruppe" else if (id == "10") "Kontext" else "Projekte"
                            val maxCredits =
                                if (id == "1") {
                                    " / 111 ECTS"
                                } else if (id == "10") {
                                    if (selectedMajor!!.name == "Informatik") {
                                        " / 22 ECTS"
                                    } else " / 18 ECTS"
                                } else {
                                    " / 42 ECTS"
                                }
                            val passedCredits =
                                calcOverallCompletedModulgroupCredits(allModulgruppe)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                TitleMedium(text = groupTitle)
                                Text(
                                    text = passedCredits.toString() + maxCredits,
                                    modifier = Modifier.padding(24.dp, 10.dp),
                                )
                            }
                        }
                    }
                    items(allModulgruppe) { modulgruppe ->
                        ModulgruppeCard(model, modulgruppe)
                    }
                }
            }
        )
    }
}

@Composable
fun ModulgruppeCard(model: ITrackCreditsModel, modulgruppe: Modulgruppe) {
    with(model) {
        val completedCredits = calcCompletedModulgroupCredits(modulgruppe)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 15.dp)
                .clickable(onClick = {
                    currentModulegroup = modulgruppe
                    currentScreen = Screen.MODULGRUPPE
                }),
            content = {
                Column(
                    modifier = Modifier
                        .padding(all = 24.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.weight(0.9f)) {
                            CardTitleSmall(text = modulgruppe.nameMG)
                        }
                        if (completedCredits >= modulgruppe.minima) {
                            Box(
                                Modifier.weight(0.1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircleOutline, "pass icon",
                                    modifier = Modifier.scale(1.3f),
                                    tint = if (model.isDarkTheme) light_ProjectsContainer else dark_ProjectsContainer
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardText(
                            text = completedCredits.toString() + " / " + modulgruppe.minima + " ECTS",
                            false
                        )
                    }
                }
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) modulgruppe.darkColor else modulgruppe.lightColor
            )
        )
    }
}
