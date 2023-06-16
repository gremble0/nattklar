package com.example.nattklar.view.globescreen

import android.view.KeyEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.nattklar.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nattklar.viewmodel.GlobeViewModel
import kotlinx.coroutines.launch

/**
 * This composable consists of a [TextField] and a [Button] layed out in a [Row]. It processes user
 * input by forwarding data to the [globeViewModel] which then calls functions from model/ before
 * updating the view accordingly.
 */
@Composable
fun SearchBar(globeViewModel: GlobeViewModel) {
    // TODO: Make clicks outside textfield change focus + stop loading data when clicking map with textfield selected
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLocation by remember { mutableStateOf("") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
            .padding(4.dp)
            // This hides the keyboard when user clicks outside it, but it only applies to
            // composables in this row, so not particularly useful. Not gonna do more with it for
            // now tho since we're reorganizing the layout of these composables soon anyways.
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        TextField(
            value = selectedLocation,
            label = { Text("Søk etter sted i Norge") },
            singleLine = true,
            onValueChange = {
                selectedLocation = if (it.length < 30) it else selectedLocation
            },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier
                .weight(1f)
                .onKeyEvent {
                    it.key.keyCode
                    // Loads data when user presses <enter> on keyboard, requires boolean return value.
                    // Neither it.key.keyCode nor it.nativeKeyEvent.keyCode is set when clicking keyboard
                    // in emulator, but it works when hitting keys on computer. Unsure how this works
                    // on an actual device.
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        coroutineScope.launch {
                            globeViewModel.loadDataFromSearch(selectedLocation)
                        }
                    }
                    true
                },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.surface
            )
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    globeViewModel.loadDataFromSearch(selectedLocation)
                }
                focusManager.clearFocus()
            },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .weight(0.22f)
                .fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mag),
                contentDescription = "Søk på sted",
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}