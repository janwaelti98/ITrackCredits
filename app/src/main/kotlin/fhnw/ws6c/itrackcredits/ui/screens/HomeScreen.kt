package fhnw.ws6c.itrackcredits.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import fhnw.ws6c.itrackcredits.data.Semester
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import fhnw.ws6c.itrackcredits.model.Screen
import fhnw.ws6c.itrackcredits.ui.*
import fhnw.ws6c.itrackcredits.ui.theme.dark_ProjectsContainer
import fhnw.ws6c.itrackcredits.ui.theme.light_ProjectsContainer
import java.time.LocalDate

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(model: ITrackCreditsModel) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopBar(model) },
        floatingActionButton = { FAB(model, "Add new semester", snackbarHostState) },
        floatingActionButtonPosition = FabPosition.End,
        content = { HomeScreenContent(model) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreenContent(model: ITrackCreditsModel) {
    Column(modifier = Modifier.padding(10.dp, 80.dp, 10.dp, 0.dp)) {
        Summary(model)
        Divider(modifier = Modifier.padding(start = 10.dp, top = 20.dp, end = 10.dp, bottom = 0.dp))
        Spacer(Modifier.height(30.dp))
        SemesterUebersicht(model)
    }
    SettingsDialog(model)
    if (model.isAddSemesterOpen) AddSemesterDialog(model)
    if (model.isSemesterBeingDeleted) DeleteSemesterDialog(model)
}

@Composable
fun Summary(model: ITrackCreditsModel) {
    with(model) {
        Column(modifier = Modifier.padding(10.dp, 0.dp)) {
            Text(
                text = selectedMajor?.name ?: "N/A",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(0.dp, 20.dp),
            )
            var progress by remember { mutableStateOf(0f) }
            val indicatorProgress =
                if (majorHasBeenSelected) (calcPassedCredits().toFloat() / 180) else 0.0f
            val progressAnimation by animateFloatAsState(
                targetValue = progress,
                animationSpec = spring(
                    dampingRatio = 0.3f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .height(15.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                progress = progressAnimation,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.surfaceTint
            )
            LaunchedEffect(indicatorProgress) {
                progress = indicatorProgress
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardText(
                    text = if (majorHasBeenSelected) calcPassedCredits().toString() + " / 180 ECTS" else "0 / 180 ECTS",
                    isBold = false
                )
                val grade =
                    if (calcAverageGrade() != -1.0) formatDoubleToString(calcAverageGrade()) else " - "
                CardText(
                    text = if (majorHasBeenSelected) "Notendurchschnitt: $grade" else "Notendurchschnitt: N/A",
                    isBold = true
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { currentScreen = Screen.MODULTEPPICH },
                    content = {
                        CardText("Modulteppich", true)
                        Icon(Icons.Filled.ArrowRight, "Arrow right")
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SemesterUebersicht(model: ITrackCreditsModel) {
    with(model) {
        Column {
            TitleMedium("Semester")
            if (allSemesters.isEmpty()) {
                Text(
                    text = "Sie haben noch kein Semester hinzugefügt.",
                    modifier = Modifier.padding(24.dp, 0.dp)
                )
            } else {
                LazyColumn(content = {
                    items(allSemesters) { item ->
                        val dismissState = rememberDismissState(confirmStateChange = {
                            if (it == DismissValue.DismissedToStart) prepareSemesterBeingDeleted(
                                item
                            )
                            false
                        })
                        DismissSemesterCard(dismissState, item, model)
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DismissSemesterCard(dismissState: DismissState, item: Semester, model: ITrackCreditsModel) {
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        dismissThresholds = {
            androidx.compose.material.FractionalThreshold( //Redundant qualifier name needed, do not remove!
                0.4f
            )
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
            SemesterCard(model, item)
        }
    )
}

@Composable
fun SemesterCard(model: ITrackCreditsModel, item: Semester) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(0.dp, 0.dp, 0.dp, 15.dp)
        .clickable(onClick = {
            model.currentSemester = item
            model.currentScreen = Screen.SEMESTER
        }),
        content = {
            Column(
                modifier = Modifier
                    .padding(all = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.weight(0.9f)) {
                        CardTitleSmall(text = item.name)
                    }
                    if (item.completed) {
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
                val credits = if (item.completed) model.getPassedSemesterCredits(item)
                    .toString() + " / " else "0 / "
                CardText(
                    text = credits + item.credits.toString() + " ETCS",
                    false
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddSemesterDialog(model: ITrackCreditsModel) {
    var titleInput by remember { mutableStateOf("") }

    with(model) {
        fun resetAll() {
            titleInput = ""
            selectedStartDate = LocalDate.now().minusDays(1)
            selectedStartDateString = model.formatDate(LocalDate.now().minusDays(1))
            selectedEndDate = LocalDate.now()
            selectedEndDateString = model.formatDate(LocalDate.now())
            isAddSemesterOpen = false
        }
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(10.dp),
            onDismissRequest = { resetAll() },
            title = { Text(text = "Semester hinzufügen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        readOnly = false,
                        textStyle = LocalTextStyle.current,
                        label = { Text("Semester Titel") },
                        placeholder = { Text("Semester 1") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SemStartDateDropdown(model = model)
                    Spacer(modifier = Modifier.height(10.dp))
                    SemEndDateDropdown(model = model)
                    if (titleInput == "") Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Das Semester muss einen Titel haben!",
                        color = Color.Red
                    )
                    if (selectedStartDate >= selectedEndDate) Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = "Das Startdatum muss VOR dem Enddatum sein!",
                        color = Color.Red
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        addSemester(
                            titleInput,
                            selectedStartDate,
                            selectedEndDate
                        )
                        resetAll()
                    },
                    enabled = (titleInput != "") && (selectedStartDate < selectedEndDate)
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

@Composable
fun DeleteSemesterDialog(model: ITrackCreditsModel) {
    with(model) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            onDismissRequest = { resetRemoveSemester() },
            title = { Text(text = "Semester löschen") },
            text = {
                Column {
                    Text("Sind Sie sicher, dass Sie das Semester '" + semesterBeingDeleted?.name + "' löschen möchten?")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Alle im Semester gespeicherten Daten werden unwiederruflich gelöscht.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        removeSemesterBeingDeleted()
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        resetRemoveSemester()
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
