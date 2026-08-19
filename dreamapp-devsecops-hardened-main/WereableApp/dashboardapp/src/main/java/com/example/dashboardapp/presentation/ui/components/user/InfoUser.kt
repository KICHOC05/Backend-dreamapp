package com.example.dashboardapp.presentation.ui.components.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.domain.model.user.Sex
import com.example.dashboardapp.domain.utils.formatNameWithPlus

@Composable
fun InfoUser(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        InfoRow(label = "Nombre:", value = formatNameWithPlus(user.name))
        InfoRow(label = "Sexo:", value = if (user.sex == Sex.MEN) "Hombre" else "Mujer")
        InfoRow(label = "Edad:", value = "${user.age} años")
        InfoRow(label = "Altura:", value = "${user.height} cm")
        InfoRow(label = "Peso:", value = "${user.weight} Kg")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}