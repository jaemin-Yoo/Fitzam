package com.jaemin.fitzam.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.R
import com.jaemin.fitzam.ui.common.FitzamBrandTopAppBar
import com.jaemin.fitzam.ui.common.FitzamCalendar
import com.jaemin.fitzam.ui.common.FitzamCalendarCellList
import com.jaemin.fitzam.ui.common.FitzamFloatingActionButton
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddWorkout: () -> Unit,
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            FitzamBrandTopAppBar(
                logoRes = R.drawable.fitzam_logo
            )
        },
        floatingActionButton = {
            FitzamFloatingActionButton(
                icon = ImageVector.vectorResource(R.drawable.ic_plus),
                onClick = onAddWorkout,
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            FitzamCalendar(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.padding(
                    vertical = 8.dp,
                    horizontal = 16.dp,
                ),
                dateContent = { date ->
                    FitzamCalendarCellList(
                        cellDate = date,
                        itemList = emptyList(),
                    )
                }
            )
            Card(
                modifier = Modifier.padding(
                    vertical = 16.dp,
                    horizontal = 16.dp,
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 (${selectedDate.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREAN)})",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    FitzamTheme {
        HomeScreen(
            onAddWorkout = {},
        )
    }
}