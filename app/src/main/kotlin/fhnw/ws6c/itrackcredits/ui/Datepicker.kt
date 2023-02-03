package fhnw.ws6c.itrackcredits.ui

import android.text.format.DateFormat
import android.view.ContextThemeWrapper
import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fhnw.ws6c.R
import fhnw.ws6c.itrackcredits.model.ITrackCreditsModel
import java.util.*


/**
 * A Jetpack Compose compatible Date Picker.
 * @author Arnau Mora, Joao Gavazzi
 * @param minDate The minimum date allowed to be picked.
 * @param maxDate The maximum date allowed to be picked.
 * @param onDateSelected Will get called when a date gets picked.
 * @param onDismissRequest Will get called when the user requests to close the dialog.
 */
@Composable
fun DatePicker(
    model: ITrackCreditsModel,
    initSelDate: Date,
    minDate: Long? = null,
    maxDate: Long? = null,
    onDateSelected: (Date) -> Unit,
    onDismissRequest: () -> Unit
) {
    val selDate = remember { mutableStateOf(initSelDate) }

    // Sets the language of the date picker to german
    Locale.setDefault(Locale.GERMAN);

    Dialog(onDismissRequest = { onDismissRequest() }, properties = DialogProperties()) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Column(
                Modifier
                    .defaultMinSize(minHeight = 72.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = "Datum auswählen",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.size(15.dp))

                Text(
                    text = DateFormat.format("d. MMMM yyyy", selDate.value).toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

                CustomCalendarView(
                    model,
                    selDate.value,
                    minDate,
                    maxDate,
                    onDateSelected = {
                        selDate.value = it
                    }
                )

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(24.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(
                        text = "Abbrechen",
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        val newDate = selDate.value
                        onDateSelected(
                            // This makes sure date is not out of range
                            Date(
                                maxOf(
                                    minOf(maxDate ?: Long.MAX_VALUE, newDate.time),
                                    minDate ?: Long.MIN_VALUE
                                )
                            )
                        )
                        onDismissRequest()
                    }
                ) {
                    Text(
                        text = "OK",
                    )
                }
            }
        }
    }
}

/**
 * Used at [DatePicker] to create the calendar picker.
 * @author Arnau Mora, Joao Gavazzi
 * @param minDate The minimum date allowed to be picked.
 * @param maxDate The maximum date allowed to be picked.
 * @param onDateSelected Will get called when a date is selected.
 */
@Composable
private fun CustomCalendarView(
    model: ITrackCreditsModel,
    selDate: Date,
    minDate: Long? = null,
    maxDate: Long? = null,
    onDateSelected: (Date) -> Unit
) {
    // Adds view to Compose
    AndroidView(
        modifier = Modifier.wrapContentSize(),
        factory = { context ->
            if(model.isDarkTheme) {
                CalendarView(ContextThemeWrapper(context, R.style.CalenderViewDarkTheme)).apply {
                    date = selDate.time
                }
            } else {
                CalendarView(ContextThemeWrapper(context, R.style.CalenderViewLightTheme)).apply {
                    date = selDate.time
                }
            }
        },
        update = { view ->
            if (minDate != null)
                view.minDate = minDate
            if (maxDate != null)
                view.maxDate = maxDate

            view.setOnDateChangeListener { _, year, month, dayOfMonth ->
                onDateSelected(
                    Calendar
                        .getInstance()
                        .apply {
                            set(year, month, dayOfMonth)
                        }
                        .time
                )
            }
        }
    )
}