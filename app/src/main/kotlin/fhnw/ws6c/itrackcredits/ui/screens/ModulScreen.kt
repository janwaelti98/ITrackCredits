package fhnw.ws6c.itrackcredits.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import fhnw.ws6c.itrackcredits.data.Grade
import fhnw.ws6c.itrackcredits.data.GradeType
import fhnw.ws6c.itrackcredits.data.Modul
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.ui.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulScreen(model: ITrackCreditsModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { TopBar(model) },
        floatingActionButton = { FAB(model, "Add new grade", snackbarHostState) },
        floatingActionButtonPosition = FabPosition.End,
        content = { ModulScreenContent(model, model.currentModule!!, snackbarHostState) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}

@Composable
fun ModulScreenContent(
    model: ITrackCreditsModel,
    modul: Modul,
    snackbarHostState: SnackbarHostState
) {
    Column(modifier = Modifier.padding(10.dp, 80.dp, 10.dp, 0.dp)) {
        ModulHeading(model, modul)
        Divider(modifier = Modifier.padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 0.dp))
        Spacer(modifier = Modifier.height(30.dp))
        ModulGrades(model, modul, snackbarHostState)
    }
    if (model.isGradeDialogOpen) GradeDialog(model)
    if (model.isGradeBeingDeleted) DeleteGradeDialog(model)
    SettingsDialog(model)
}

@Composable
fun ModulHeading(model: ITrackCreditsModel, modul: Modul) {
    with(modul) {
        val recompose = model.triggerRecompose
        Title(text = "$code - $nameModul")
        BodyMedium(text = modul.modulgruppe.nameMG, isBold = false)
        val mspString = when (gradeType) {
            GradeType.NUMBER -> {
                when (msp) {
                    "WRITTEN"   -> "EN & MSP (schriftlich)"
                    "ORAL"      -> "EN & MSP (mündlich)"
                    else        -> "EN"
                }
            }
            else -> {
                when (msp) {
                    "WRITTEN"   -> "Testat & MSP (schriftlich)"
                    "ORAL"      -> "Testat & MSP (mündlich)"
                    else        -> "Testat"
                }
            }
        }
        BodyMedium(text = mspString, isBold = false)
        BodyMedium(text = "$credits ECTS", isBold = false)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModulGrades(model: ITrackCreditsModel, modul: Modul, snackbarHostState: SnackbarHostState) {
    with(model) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleMedium(text = "Noten")
                when (modul.gradeType) {
                    GradeType.NUMBER -> {
                        val grade =
                            if (model.calcModuleGrade(modul) != -1.0) model.formatDoubleToString(
                                model.calcModuleGrade(modul)
                            ) else " - "
                        BodyMedium(
                            text = "Abschlussnote: $grade",
                            isBold = true
                        )
                    }
                    else -> {
                        BodyMedium(
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
            if (modul.allPartialGrades.isEmpty()) {
                Text(
                    text = "Sie haben dem Modul noch keine Noten hinzugefügt.",
                    modifier = Modifier.padding(24.dp, 0.dp)
                )
            } else {
                LazyColumn(content = {
                    itemsIndexed(modul.allPartialGrades) { _, item ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToStart && !model.currentSemester?.completed!!)
                                    prepareGradeBeingDeleted(item)
                                false
                            }
                        )
                        DismissGradeCard(dismissState, item, model, snackbarHostState)
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissGradeCard(
    dismissState: DismissState,
    item: Grade,
    model: ITrackCreditsModel,
    snackbarHostState: SnackbarHostState
) {
    SwipeToDismiss(
        state = dismissState,
        directions = if (!model.currentSemester!!.completed) setOf(DismissDirection.EndToStart) else setOf(),
        dismissThresholds = { androidx.compose.material.FractionalThreshold(0.4f) }, //Redundant qualifier name needed, do not remove!
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
            GradeCard(model, item, snackbarHostState)
        }
    )
}

@Composable
fun GradeCard(model: ITrackCreditsModel, grade: Grade, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    with(grade) {
        val recompose = model.triggerRecompose
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 15.dp)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = if (model.currentSemester!!.completed) null else rememberRipple(
                        true
                    ),
                    onClick = {
                        scope.launch {
                            if (!model.currentSemester?.completed!!) {
                                model.isGradeDialogOpen = true
                                model.isEditOfGrade = true
                                model.currentGrade = grade
                                model.gradeTitle = grade.name
                                model.selectedGradeNumber = grade.gradeNumber
                                model.selectedGradePass = grade.gradePass
                                model.selectedGradeBonus = grade.gradeBonus
                                model.selectedGradeType = grade.gradeType
                                model.selectedWeight = grade.weight
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Note bearbeiten nicht möglich, da das Semester abgeschlossen ist!",
                                    "OK",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                ),
            content = {
                Column(modifier = Modifier.padding(all = 15.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardText(text = name, false)
                        val gradeToShow = when (gradeType) {
                            GradeType.PASS_FAIL -> if (gradePass) "Pass" else "Fail"
                            GradeType.NUMBER -> gradeNumber
                            GradeType.BONUS -> gradeBonus
                        }
                        Text(
                            fontWeight = FontWeight.ExtraBold,
                            text = gradeToShow.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    val gradeWeight =
                        if (gradeType == GradeType.NUMBER) "$weight%" else gradeType.gradeTypeName
                    CardTextSmall(text = "Gewichtung: $gradeWeight")
                }
            }
        )
    }
}

@Composable
fun DeleteGradeDialog(model: ITrackCreditsModel) {
    with(model) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            onDismissRequest = { resetRemoveGrade() },
            title = { Text(text = "Note löschen") },
            text = {
                Column {
                    Text("Sind Sie sicher, dass Sie die Note '" + gradeBeingDeleted?.name + "' löschen möchten?")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Diese wird unwiederruflich gelöscht.")
                }
            },
            confirmButton = {
                Button(onClick = { removeGradeBeingDeleted() }) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { resetRemoveGrade() }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GradeDialog(model: ITrackCreditsModel) {
    fun resetAll() {
        model.gradeTitle = ""
        model.selectedGradeNumber = 0.0
        model.selectedGradePass = false
        model.selectedGradeBonus = 0.0
        model.selectedGradeType = GradeType.NUMBER
        model.selectedWeight = 100
        model.isGradeDialogOpen = false
        model.isEditOfGrade = false
    }

    with(model) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(10.dp),
            onDismissRequest = { resetAll() },
            title = { Text(text = if (isEditOfGrade) "Note bearbeiten" else "Note hinzufügen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = gradeTitle,
                        onValueChange = { gradeTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        readOnly = false,
                        textStyle = LocalTextStyle.current,
                        label = { Text("Notentitel") },
                        placeholder = { Text("Prüfung 1") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeightDropdown(model)
                        when (selectedGradeType) {
                            GradeType.PASS_FAIL -> {
                                Column(
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Bestanden",
                                        modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                                    )
                                    Switch(
                                        checked = selectedGradePass,
                                        onCheckedChange = {
                                            selectedGradePass = !selectedGradePass
                                        })
                                }
                            }
                            else -> {
                                TextField(
                                    value = if (selectedGradeType == GradeType.NUMBER) {
                                        selectedGradeNumber.toString()
                                    } else selectedGradeBonus.toString(),
                                    onValueChange = {
                                        if (selectedGradeType == GradeType.NUMBER) {
                                            selectedGradeNumber = try {
                                                it.toDouble()
                                            } catch (e: NumberFormatException) {
                                                0.0
                                            }
                                        } else {
                                            selectedGradeBonus = try {
                                                it.toDouble()
                                            } catch (e: NumberFormatException) {
                                                0.0
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = true,
                                    readOnly = false,
                                    textStyle = LocalTextStyle.current,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text(if (selectedGradeType == GradeType.NUMBER) "Note" else "Bonus") },
                                    placeholder = { Text("0.0") },
                                    singleLine = true
                                )
                            }
                        }
                    }
                    if (selectedGradeType == GradeType.NUMBER) {
                        TextField(
                            value = selectedWeight.toString(),
                            onValueChange = {
                                selectedWeight = try {
                                    it.toInt()
                                } catch (e: NumberFormatException) {
                                    0
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            enabled = true,
                            readOnly = false,
                            textStyle = LocalTextStyle.current,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Gewichtung in %") },
                            placeholder = { Text("25") },
                            singleLine = true
                        )
                    }

                    if (gradeTitle == "") Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Die Note muss einen Titel haben!",
                        color = Color.Red
                    )
                    if (selectedGradeType == GradeType.NUMBER && (selectedWeight > 100 || selectedWeight < 0)) Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Die Gewichtung muss zwischen 0 und 100 sein!",
                        color = Color.Red
                    )
                    if (selectedGradeType == GradeType.NUMBER && (selectedGradeNumber > 6.0 || selectedGradeNumber <= 0.0)) Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Die Note muss grösser 0.0 und kleiner oder gleich 6.0 sein!",
                        color = Color.Red
                    )
                    if (selectedGradeType == GradeType.BONUS && (selectedGradeBonus > 6.0 || selectedGradeBonus <= 0.0)) Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Der Bonus muss grösser 0.0 und kleiner oder gleich 6.0 sein!",
                        color = Color.Red
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isEditOfGrade) {
                            addGrade(
                                currentModule!!,
                                selectedGradeType,
                                gradeTitle,
                                selectedWeight,
                                selectedGradeNumber,
                                selectedGradePass,
                                selectedGradeBonus
                            )
                        } else {
                            updateGrade(
                                currentGrade!!,
                                selectedGradeType,
                                gradeTitle,
                                selectedWeight,
                                selectedGradeNumber,
                                selectedGradePass,
                                selectedGradeBonus
                            )
                        }
                        resetAll()
                    },
                    enabled = model.isGradeInputValid()
                ) {
                    Text(if (isEditOfGrade) "Speichern" else "Hinzufügen")
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
fun WeightDropdown(model: ITrackCreditsModel) {
    var weightDropdownExpanded by remember { mutableStateOf(false) }
    val curWeight = if (model.isEditOfGrade) {
        model.currentGrade!!.gradeType.gradeTypeName
    } else GradeType.NUMBER.gradeTypeName
    var weight by remember { mutableStateOf(curWeight) }
    val textFieldSize = remember { mutableStateOf(Size.Zero) }

    with(model) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(56.dp)
                .padding(start = 0.dp, top = 0.dp, end = 10.dp, bottom = 0.dp)
        ) {
            TextField(
                value = weight,
                onValueChange = { weight = it },
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        textFieldSize.value = coordinates.size.toSize()
                    }
                    .fillMaxWidth(),
                readOnly = true,
                textStyle = LocalTextStyle.current,
                label = { Text("Notentyp") },
                placeholder = { Text("auswählen") },
                trailingIcon = {
                    Icon(
                        imageVector = if (weightDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = "Arrow down icon"
                    )
                },
                singleLine = true,
            )
            DropdownMenu(
                expanded = weightDropdownExpanded,
                onDismissRequest = { weightDropdownExpanded = false },
                offset = DpOffset(0.dp, (-20).dp),
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.value.width.toDp() })
            ) {
                DropdownMenuItem(
                    text = { Text(GradeType.PASS_FAIL.gradeTypeName) },
                    onClick = {
                        selectedGradeType = GradeType.PASS_FAIL
                        weight = GradeType.PASS_FAIL.gradeTypeName
                        weightDropdownExpanded = false
                    },
                    enabled = true
                )
                DropdownMenuItem(
                    text = { Text(GradeType.BONUS.gradeTypeName) },
                    onClick = {
                        selectedGradeType = GradeType.BONUS
                        weight = GradeType.BONUS.gradeTypeName
                        weightDropdownExpanded = false
                    },
                    enabled = true
                )
                DropdownMenuItem(
                    text = { Text(GradeType.NUMBER.gradeTypeName) },
                    onClick = {
                        selectedGradeType = GradeType.NUMBER
                        weight = GradeType.NUMBER.gradeTypeName
                        weightDropdownExpanded = false
                    },
                    enabled = true
                )
            }
            // Used to make whole Textfield clickable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = {
                        weightDropdownExpanded = true
                    })
            )
        }
    }
}
