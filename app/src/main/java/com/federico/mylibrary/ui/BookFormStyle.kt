package com.federico.mylibrary.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val bookFieldModifier = Modifier
    .fillMaxWidth()
    .padding(bottom = 6.dp)
    .heightIn(min = 48.dp) // campo più compatto

val bookFieldTextStyle = TextStyle(fontSize = 14.sp)