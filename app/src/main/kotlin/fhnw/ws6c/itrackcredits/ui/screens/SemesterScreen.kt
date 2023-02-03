package fhnw.ws6c.itrackcredits.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import fhnw.ws6c.itrackcredits.data.GradeType
import fhnw.ws6c.itrackcredits.data.Modul
import fhnw.ws6c.itrackcredits.data.Semester
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.model.Screen
import fhnw.ws6c.itrackcredits.ui.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random
import androidx.compose.material3.RadioButton as RadioButton1

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterScreen(model: ITrackCreditsModel) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopBar(model) },
        floatingActionButton = { FAB(model, "Add module to semester", snackbarHostState) },
        floatingActionButtonPosition = FabPosition.End,
        content = {
            SemesterScreenContent(
                model,
                model.currentSemester!!,
                snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}

@Composable
fun SemesterScreenContent(
    model: ITrackCreditsModel,
    curSemester: Semester,
    snackbarHostState: SnackbarHostState,
) {
    Column(modifier = Modifier.padding(10.dp, 80.dp, 10.dp, 0.dp)) {
        SemesterHeading(model, curSemester, snackbarHostState)
        Divider(modifier = Modifier.padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 0.dp))
        Spacer(modifier = Modifier.height(30.dp))
        SemesterModules(model, curSemester)
    }
    if (model.isAddModuleOpen) AddModuleDialog(model)
    if (model.isModuleBeingDeleted) DeleteModuleDialog(model)
    if (model.isEditSemesterOpen) EditSemesterDialog(model)
    SettingsDialog(model)
}

@Composable
fun SemesterHeading(
    model: ITrackCreditsModel,
    curSemester: Semester,
    snackbarHostState: SnackbarHostState,
) {
    with(model) {
        val recompose = model.triggerRecompose
        val scope = rememberCoroutineScope()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 0.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                Title(text = curSemester.name)
            }
            IconButton(
                modifier = Modifier.padding(top = 12.dp),
                onClick = {
                    isEditSemesterOpen = true
                    selectedStartDate = curSemester.startDate
                    selectedStartDateString = formatDate(curSemester.startDate)
                    selectedEndDate = curSemester.endDate
                    selectedEndDateString = formatDate(curSemester.endDate)
                },
                enabled = !curSemester.completed
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit semester",
                    tint = if (curSemester.completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                )
            }
        }

        val formattedStartDate = formatDate(curSemester.startDate)
        val formattedEndDate = formatDate(curSemester.endDate)
        Subtitle(text = "$formattedStartDate - $formattedEndDate", false)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Semester abgeschlossen")
            Switch(
                onCheckedChange = {
                    scope.launch {
                        completeSemester(curSemester, it)

                        if (curSemester.completed) {
                            snackbarHostState.showSnackbar(
                                "Semester wurde abgeschlossen und kann nicht mehr bearbeitet werden",
                                "OK",
                                duration = SnackbarDuration.Short
                            )
                        } else {
                            snackbarHostState.showSnackbar(
                                "Semester kann bearbeitet werden",
                                "OK",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                checked = curSemester.completed
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SemesterModules(model: ITrackCreditsModel, curSemester: Semester) {
    val recompose = model.triggerRecompose
    with(model) {
        Column {
            TitleMedium(text = "Module")
            if (curSemester.modules.isEmpty()) {
                Text(
                    text = "Sie haben noch keine Module dem Semester hinzugefügt.",
                    modifier = Modifier.padding(24.dp, 0.dp)
                )
            } else {
                LazyColumn {
                    itemsIndexed(curSemester.modules) { _, item ->
                        val dismissState = rememberDismissState {
                            if (it == DismissValue.DismissedToStart && !curSemester.completed)
                                prepareModuleBeingDeleted(item)
                            false
                        }
                        DismissModuleCard(dismissState, item, model)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSemesterDialog(model: ITrackCreditsModel) {
    var titleInput by remember { mutableStateOf(model.currentSemester!!.name) }

    with(model) {
        fun resetAll() {
            titleInput = model.currentSemester!!.name
            selectedStartDate = LocalDate.now()
            selectedStartDateString = model.formatDate(LocalDate.now())
            selectedEndDate = LocalDate.now()
            selectedEndDateString = model.formatDate(LocalDate.now())
            isEditSemesterOpen = false
        }
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            onDismissRequest = { resetAll() },
            title = { Text(text = "Semester bearbeiten") },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = true,
                        readOnly = false,
                        textStyle = LocalTextStyle.current,
                        label = { Text("Semester Titel") },
                        placeholder = { Text("Semester 1") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SemStartDateDropdown(model)
                    Spacer(modifier = Modifier.height(10.dp))
                    SemEndDateDropdown(model)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editSemester(
                            model.currentSemester!!.id,
                            titleInput,
                            selectedStartDate,
                            selectedEndDate
                        )
                        resetAll()
                    },
                    enabled = titleInput != "" && (selectedStartDate < selectedEndDate)
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { resetAll() }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun ModulCard(model: ITrackCreditsModel, modul: Modul) {
    with(model) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 15.dp)
            .clickable(onClick = {
                currentModule = modul
                currentScreen = Screen.MODUL
            }),
            content = {
                Column(modifier = Modifier.padding(all = 24.dp)) {
                    CardTitleSmall(text = modul.code + " - " + modul.nameModul)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardText(text = modul.credits.toString() + " ECTS", false)
                        when (modul.gradeType) {
                            GradeType.NUMBER -> {
                                val grade =
                                    if (calcModuleGrade(modul) != -1.0) formatDoubleToString(
                                        calcModuleGrade(modul)
                                    ) else " - "
                                CardText(
                                    text = "Note: $grade",
                                    isBold = true
                                )
                            }
                            else -> {
                                CardText(
                                    text = if (modul.allPartialGrades.isEmpty()) {
                                        "Bestanden: -"
                                    } else if (model.calcModulePass(modul)) {
                                        "Bestanden: Pass"
                                    } else {
                                        "Bestanden: Fail"
                                    },
                                    isBold = true
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissModuleCard(dismissState: DismissState, item: Modul, model: ITrackCreditsModel) {

    SwipeToDismiss(
        state = dismissState,
        directions = if (!model.currentSemester!!.completed) setOf(DismissDirection.EndToStart) else setOf(),
        dismissThresholds = {
            androidx.compose.material.FractionalThreshold(
                0.4f
            ) //Redundant qualifier name needed, do not remove!
        },
        background = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.error
                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.secondaryContainer
                    DismissValue.Default -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
            val icon = Icons.Default.Delete
            val scale by animateFloatAsState(targetValue = if (dismissState.targetValue == DismissValue.Default) 0.8f else 1.2f)
            val alignment = Alignment.CenterEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 15.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(color),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = "Icon",
                    modifier = Modifier
                        .scale(scale)
                        .padding(end = 15.dp)
                )
            }
        },
        dismissContent = {
            ModulCard(model, item)
        }
    )
}

@Composable
fun DeleteModuleDialog(model: ITrackCreditsModel) {
    with(model) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            onDismissRequest = { resetRemoveModule() },
            title = { Text(text = "Modul löschen") },
            text = {
                Column {
                    Text("Sind Sie sicher, dass Sie das Modul '" + moduleBeingDeleted?.nameModul + "' löschen möchten?")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Alle im Modul gespeicherten Daten werden unwiederruflich gelöscht.")
                }
            },
            confirmButton = {
                Button(onClick = { removeModuleBeingDeleted() }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { resetRemoveModule() }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddModuleDialog(model: ITrackCreditsModel) {
    var isCustomModule by remember { mutableStateOf(false) }
    var customModuleName by remember { mutableStateOf("") }
    var customModuleCredits by remember { mutableStateOf(0) }
    var isPassFailModule by remember { mutableStateOf(false) }

    with(model) {
        fun resetAll() {
            isCustomModule = false
            isCustomModule = false
            customModuleName = ""
            customModuleCredits = 0
            isPassFailModule = false
            selectedModulegroup = null
            selectedModulegroupName = ""
            selectedModule = null
            selectedModuleName = ""
            isAddModuleOpen = false
        }
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(10.dp),
            onDismissRequest = { resetAll() },
            title = { Text(text = "Modul hinzufügen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Eigenes Modul erstellen",
                            modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                        )
                        Switch(
                            checked = isCustomModule,
                            onCheckedChange = { isCustomModule = !isCustomModule })
                    }
                    DropdownModulgroup(model)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isCustomModule) {
                        TextField(
                            value = customModuleName,
                            onValueChange = { customModuleName = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = LocalTextStyle.current,
                            label = { Text("Modulname") },
                            placeholder = { Text("eingeben") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = customModuleCredits.toString(),
                            onValueChange = {
                                customModuleCredits = try {
                                    it
                                        .replace(".", "")
                                        .replace(",", "")
                                        .toInt()
                                } catch (e: NumberFormatException) {
                                    0
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Credits") },
                            placeholder = { Text("eingeben") }
                        )
                    } else {
                        DropdownModul(model)
                    }
                    Text(
                        text = "Benotung",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    //radio buttons
                    val radioOptions = listOf("Note", "Pass/Fail")
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
                    Column {
                        radioOptions.forEach { text ->
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onOptionSelected(text)
                                    }
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                RadioButton1(
                                    modifier = Modifier.size(30.dp),
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onOptionSelected(text)
                                        isPassFailModule = text == "Pass/Fail"
                                    }
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val gradeType =
                            if (isPassFailModule) GradeType.PASS_FAIL else GradeType.NUMBER
                        if (isCustomModule) {
                            val values = """
                                        {
                                        "id":      ${model.moduleID},
                                        "name":    "$customModuleName",
                                        "code":   "$customModuleName",
                                        "credits": "$customModuleCredits",
                                        "hs": "true",
                                        "fs": "true",
                                        "msp": "NONE",
                                        "requirements": []
                                        }
                                """.trimIndent()
                            addModule(
                                currentSemester!!,
                                selectedModulegroup!!,
                                values,
                                gradeType
                            )
                        } else {
                            addModuleToSemester(currentSemester!!, selectedModulegroup!!, selectedModule!!, gradeType)
                        }
                        resetAll()
                    },
                    enabled = (selectedModulegroup != null) && ((!isCustomModule && (selectedModule != null)) || (isCustomModule && (customModuleName != "") && (customModuleCredits != null)))
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { resetAll() }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownModulgroup(model: ITrackCreditsModel) {
    val textFieldSize = remember { mutableStateOf(Size.Zero) }

    with(model) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            TextField(
                value = selectedModulegroupName,
                onValueChange = {
                    selectedModulegroupName = it
                    selectedModule = null
                    selectedModuleName = ""
                },
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        textFieldSize.value = coordinates.size.toSize()
                    }
                    .fillMaxWidth(),
                readOnly = true,
                textStyle = LocalTextStyle.current,
                label = { Text("Modulgruppe") },
                placeholder = { Text("auswählen") },
                trailingIcon = {
                    Icon(
                        imageVector = if (modulegroupDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = "Arrow down icon",
                    )
                },
                singleLine = true,
            )
            DropdownMenu(
                expanded = modulegroupDropdownExpanded,
                onDismissRequest = { modulegroupDropdownExpanded = false },
                offset = DpOffset(0.dp, (-20).dp),
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
                    .requiredSizeIn(maxHeight = 290.dp)
            ) {
                selectedMajor!!.modulgruppen.forEach {
                    DropdownMenuItem(
                        text = { Text(it.nameMG) },
                        onClick = {
                            selectedModulegroup = it
                            selectedModulegroupName = it.nameMG
                            modulegroupDropdownExpanded = false
                        },
                        enabled = true
                    )
                }
            }
            // Used to make whole Textfield clickable
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable {
                    modulegroupDropdownExpanded = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownModul(model: ITrackCreditsModel) {
    var modulDropdownExpanded by remember { mutableStateOf(false) }
    val textFieldSize = remember { mutableStateOf(Size.Zero) }

    with(model) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            TextField(
                value = selectedModuleName,
                onValueChange = { selectedModuleName = it },
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        textFieldSize.value = coordinates.size.toSize()
                    }
                    .fillMaxWidth(),
                readOnly = true,
                textStyle = LocalTextStyle.current,
                label = { Text("Modul") },
                placeholder = { Text("auswählen") },
                enabled = selectedModulegroup != null,
                trailingIcon = {
                    Icon(
                        imageVector = if (modulDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = "Arrow down icon",
                    )
                },
                singleLine = true,
            )
            DropdownMenu(
                expanded = modulDropdownExpanded,
                onDismissRequest = { modulDropdownExpanded = false },
                offset = DpOffset(0.dp, 0.dp),
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
                    .requiredSizeIn(maxHeight = 200.dp)
            ) {
                selectedModulegroup?.modules?.forEach {
                    //only show in dropdown if module has not been passed already
                    if (!(checkIfModuleTaken(it) && calcPassOfModulgroupModul(it))) {
                        DropdownMenuItem(
                            text = { Text(it.nameModul) },
                            onClick = {
                                selectedModule = it
                                selectedModuleName = it.nameModul
                                modulDropdownExpanded = false
                            },
                            enabled = true
                        )
                    }
                }
            }
            // Used to make whole Textfield clickable
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable {
                    modulDropdownExpanded = true
                }
            )
        }
    }
}
