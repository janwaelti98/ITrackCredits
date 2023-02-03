package fhnw.ws6c.itrackcredits.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fhnw.ws6c.itrackcredits.data.GradeType
import fhnw.ws6c.itrackcredits.data.Modul
import fhnw.ws6c.itrackcredits.data.Modulgruppe
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.ui.TopBar
import fhnw.ws6c.itrackcredits.ui.SettingsDialog
import fhnw.ws6c.itrackcredits.ui.Subtitle
import fhnw.ws6c.itrackcredits.ui.Title
import fhnw.ws6c.itrackcredits.ui.theme.dark_ProjectsContainer
import fhnw.ws6c.itrackcredits.ui.theme.light_ProjectsContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulgruppeScreen(model: ITrackCreditsModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(model) },
        content = { paddingValues ->
            ModulgruppeScreenContent(
                model,
                model.currentModulegroup!!,
                paddingValues
            )
        }
    )
}

@Composable
fun ModulgruppeScreenContent(
    model: ITrackCreditsModel,
    modulgruppe: Modulgruppe,
    paddingValues: PaddingValues
) {
    with(model) {
        ConstraintLayout(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val (title, contentlist) = createRefs()

            Column(modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    //bottom.linkTo(contentlist.top)
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                modulgruppe.darkColor,
                                MaterialTheme.colorScheme.surface
                            )
                        } else {
                            listOf(
                                modulgruppe.lightColor,
                                MaterialTheme.colorScheme.surface
                            )
                        }
                    )
                )
            ) {
                val passedModules = calcPassedNrOfModulesFromModulgroup(modulgruppe)
                Spacer(modifier = Modifier.height(100.dp))
                Title(text = modulgruppe.nameMG)
                Subtitle(
                    text = calcCompletedModulgroupCredits(modulgruppe).toString() + " / " + modulgruppe.minima.toString() + " ECTS",
                    isBold = false
                )
                Subtitle(
                    text = passedModules.toString() + " / " + modulgruppe.modules.size + " Module abgeschlossen",
                    isBold = false
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyVerticalGrid(modifier = Modifier
                .constrainAs(contentlist) {
                    start.linkTo(parent.start, margin = 10.dp)
                    top.linkTo(title.bottom, margin = 10.dp)
                    end.linkTo(parent.end, margin = 10.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
                horizontalArrangement = Arrangement.spacedBy(10.dp),

                columns = GridCells.Fixed(count = 2),
                content = {
                    items(modulgruppe.modules) { item ->
                        ModulCardModulteppich(model, item)
                    }
                }
            )
        }
    }
    SettingsDialog(model)
}

@Composable
fun ModulCardModulteppich(model: ITrackCreditsModel, modul: Modul) {
    with(model) {
        Card(modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .height(165.dp),
            content = {
                Column(
                    modifier = Modifier
                        .padding(all = 15.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = modul.credits.toString() + " ECTS",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (checkIfModuleTaken(modul)) {
                            if (calcPassOfModulgroupModul(modul)) {
                                Icon(
                                    Icons.Filled.CheckCircleOutline, "pass icon",
                                    modifier = Modifier.scale(1.3f),
                                    tint = if (isDarkTheme) light_ProjectsContainer else dark_ProjectsContainer
                                )
                            } else {
                                Icon(
                                    Icons.Filled.AddCircleOutline, "fail icon",
                                    modifier = Modifier
                                        .rotate(45f)
                                        .scale(1.3f),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = modul.code, style = MaterialTheme.typography.titleLarge)
                        if (checkIfModuleTaken(modul)) {
                            val grade =
                                if (calcGradeOfModulgroupModul(modul) != -1.0) formatDoubleToString(
                                    calcGradeOfModulgroupModul(modul)
                                ) else "0.0"
                            val pass = if (calcPassOfModulgroupModul(modul)) "Pass" else "Fail"
                            Text(
                                text = if (getGradeType(modul) == GradeType.NUMBER) grade else pass,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = modul.nameModul,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        )
    }
}
