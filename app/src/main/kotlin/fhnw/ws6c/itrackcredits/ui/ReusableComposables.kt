package fhnw.ws6c.itrackcredits.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.model.Screen

import fhnw.ws6c.itrackcredits.ui.theme.shapesITC
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(model: ITrackCreditsModel) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                model.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (model.currentScreen != Screen.HOME) {
                IconButton(onClick = {
                    model.currentScreen = model.currentScreen.prevScreen
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                model.isSettingsOpen = true
                model.updatePrefs()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun FAB(
    model: ITrackCreditsModel,
    contentDescription: String,
    snackbarHostState: SnackbarHostState,
) {
    with(model) {
        val scope = rememberCoroutineScope()
        val recompose = model.triggerRecompose
        val disableFAB =
            model.currentSemester?.completed == true && (currentScreen == Screen.SEMESTER || currentScreen == Screen.MODUL)

        FloatingActionButton(
            onClick = {
                scope.launch {
                    when (currentScreen) {
                        Screen.HOME -> isAddSemesterOpen = true

                        Screen.SEMESTER -> {
                            if (model.currentSemester?.completed == false) {
                                isAddModuleOpen = true
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Modul hinzufügen nicht möglich, da das Semester abgeschlossen ist!",
                                    "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }

                        else -> {
                            if (model.currentSemester?.completed == false) {
                                isGradeDialogOpen = true
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Note hinzufügen nicht möglich, da das Semester abgeschlossen ist!",
                                    "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }
            },
            interactionSource = if (disableFAB) NoRippleInteractionSource() else MutableInteractionSource(),
            elevation = if (disableFAB) {
                FloatingActionButtonDefaults.elevation(0.dp)
            } else {
                FloatingActionButtonDefaults.elevation(8.dp)
            },
            shape = shapesITC.medium,
            containerColor = if (disableFAB) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (disableFAB) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = contentDescription)
        }
    }
}

// to be used for all screen titles
@Composable
fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
    )
}

@Composable
fun TitleMedium(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(24.dp, 10.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

// to be used for all text directly underneath screen titles
@Composable
fun Subtitle(text: String, isBold: Boolean) {
    Text(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(10.dp, 0.dp, 10.dp, 0.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun BodyMedium(text: String, isBold: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)
    )
}

@Composable
fun CardTitleSmall(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
    )
}

// to be used for text in cards
@Composable
fun CardText(text: String, isBold: Boolean) {
    Text(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
fun CardTextSmall(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(model: ITrackCreditsModel) {
    var expanded by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf("auswählen") }
    val textFieldSize = remember { mutableStateOf(Size.Zero) }

    with(model) {
        if (isSettingsOpen) {
            AlertDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                onDismissRequest = { if (majorHasBeenSelected) {
                    isSettingsOpen = false
                    updatePrefs()
                }},
                title = { Text(text = "Einstellungen") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (!majorHasBeenSelected) {
                            Text(
                                "Es muss ein Studiengang ausgewählt werden.",
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                TextField(
                                    value = selection,
                                    onValueChange = { selection = it },
                                    modifier = Modifier
                                        .onGloballyPositioned { coordinates ->
                                            textFieldSize.value = coordinates.size.toSize()
                                        }
                                        .fillMaxWidth(),
                                    enabled = true,
                                    readOnly = true,
                                    textStyle = LocalTextStyle.current,
                                    label = { Text("Studiengang") },
                                    placeholder = { Text("auswählen") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.School,
                                            contentDescription = "Major icon"
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Arrow down icon"
                                        )
                                    },
                                    singleLine = true,
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("iCompetence") },
                                        onClick = {
                                            selection = "iCompetence"
                                            expanded = false
                                        },
                                        enabled = true
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Informatik") },
                                        onClick = {
                                            selection = "Informatik"
                                            expanded = false
                                        },
                                        enabled = true
                                    )
                                }
                                // Used to make whole Textfield clickable
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        expanded = true
                                    }
                                )
                            }
                        } else if(selectedMajor != null){
                            Text("Gewählter Studiengang: " + selectedMajor!!.name)
                        } else {
                            Text("Daten werden geladen...")
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Dark mode",
                                modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                            )
                            Switch(checked = isDarkTheme,
                                onCheckedChange = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!majorHasBeenSelected) {
                        Button(
                            onClick = {
                                setStudiengang(selection)
                                isSettingsOpen = false
                                updatePrefs()
                            },
                            enabled = selection != "auswählen"
                        ) {
                            Text("Speichern")
                        }
                    }
                },
                dismissButton = {
                    if (majorHasBeenSelected) {
                        OutlinedButton(
                            onClick = {
                                if (majorHasBeenSelected) {
                                    isSettingsOpen = false
                                    updatePrefs()
                                }
                            }
                        ) {
                            Text("Schliessen")
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemStartDateDropdown(model: ITrackCreditsModel) {
    with(model) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            TextField(
                value = selectedStartDateString,
                onValueChange = { selectedStartDateString = it },
                modifier = Modifier
                    .fillMaxSize(),
                enabled = true,
                readOnly = true,
                textStyle = LocalTextStyle.current,
                label = { Text("Startdatum") },
                placeholder = { Text(formatDate(LocalDate.now())) },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        "Calendar icon",
                    )
                }
            )
            // Used to make whole Textfield clickable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = { showStartDatepicker = true })
            )
        }
        if (showStartDatepicker) {
            DatePicker(
                model,
                initSelDate = Date.from(
                    selectedStartDate
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                ),
                onDateSelected = {
                    selectedStartDate = it.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    selectedStartDateString = formatDate(selectedStartDate)

                },
                onDismissRequest = { showStartDatepicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemEndDateDropdown(model: ITrackCreditsModel) {
    with(model) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            TextField(
                value = selectedEndDateString,
                onValueChange = { selectedEndDateString = it },
                modifier = Modifier
                    .fillMaxSize(),
                enabled = true,
                readOnly = true,
                textStyle = LocalTextStyle.current,
                label = { Text("Enddatum") },
                placeholder = { Text(formatDate(LocalDate.now())) },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        "Calendar icon",
                    )
                }
            )
            // Used to make whole Textfield clickable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = { showEndDatepicker = true })
            )
            if (showEndDatepicker) {
                DatePicker(
                    model,
                    initSelDate = Date.from(
                        selectedEndDate
                            .atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    ),
                    onDateSelected = {
                        selectedEndDate = it.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        selectedEndDateString = formatDate(selectedEndDate)

                    },
                    onDismissRequest = { showEndDatepicker = false }
                )
            }
        }
    }
}
