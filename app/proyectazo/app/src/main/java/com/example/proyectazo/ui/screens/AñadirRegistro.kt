package com.example.proyectazo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.AñadirRegistroViewModel
import com.example.proyectazo.ui.viewmodel.GuardarEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AñadirRegistroScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val userId = remember { SessionManager(context).getUserId() }
    val viewModel: AñadirRegistroViewModel = viewModel(
        factory = AñadirRegistroViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navegar atrás cuando se guarda con éxito
    LaunchedEffect(uiState.guardarEstado) {
        when (val estado = uiState.guardarEstado) {
            is GuardarEstado.Exito -> {
                snackbarHostState.showSnackbar("Registro guardado correctamente")
                onBack()
            }
            is GuardarEstado.Error -> {
                snackbarHostState.showSnackbar(estado.mensaje)
                viewModel.resetGuardarEstado()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmartFitTopBar(titulo = "Añadir registro", onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Fecha ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Introduce el día",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoDia(
                        valor = uiState.dia,
                        placeholder = "DD",
                        maxLength = 2,
                        modifier = Modifier.weight(1f),
                        onValorChange = viewModel::onDiaChange
                    )
                    CampoDia(
                        valor = uiState.mes,
                        placeholder = "MM",
                        maxLength = 2,
                        modifier = Modifier.weight(1f),
                        onValorChange = viewModel::onMesChange
                    )
                    CampoDia(
                        valor = uiState.anio,
                        placeholder = "YYYY",
                        maxLength = 4,
                        modifier = Modifier.weight(2f),
                        onValorChange = viewModel::onAnioChange
                    )
                }
                if (uiState.fechaError != null) {
                    Text(
                        text = uiState.fechaError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Slider peso ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indica tu peso actual",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.pesoKg.toInt()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = uiState.pesoKg,
                    onValueChange = viewModel::onPesoChange,
                    valueRange = 30f..200f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Medidas corporales ───────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Medidas actuales",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                CampoMedida(
                    label = "Brazo",
                    valor = uiState.brazoCm,
                    onValorChange = viewModel::onBrazoChange,
                    onLimpiar = { viewModel.onBrazoChange("") }
                )
                CampoMedida(
                    label = "Cintura",
                    valor = uiState.cinturaCm,
                    onValorChange = viewModel::onCinturaChange,
                    onLimpiar = { viewModel.onCinturaChange("") }
                )
                CampoMedida(
                    label = "Pecho",
                    valor = uiState.pechoCm,
                    onValorChange = viewModel::onPechoChange,
                    onLimpiar = { viewModel.onPechoChange("") }
                )
                CampoMedida(
                    label = "Pierna",
                    valor = uiState.piernaCm,
                    onValorChange = viewModel::onPiernaChange,
                    onLimpiar = { viewModel.onPiernaChange("") }
                )
            }

            // ── Slider altura ────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indica tu altura (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.alturaCm.toInt()} cm",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = uiState.alturaCm,
                    onValueChange = viewModel::onAlturaChange,
                    valueRange = 100f..250f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Botón guardar ────────────────────────────────────────
            Button(
                onClick = { viewModel.guardar(userId) },
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Guardar registro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Campo de fecha individual ────────────────────────────────────────────────
@Composable
private fun CampoDia(
    valor: String,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier,
    onValorChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= maxLength && it.all { c -> c.isDigit() }) onValorChange(it) },
        placeholder = {
            Text(
                text = placeholder,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ── Campo de medida con botón limpiar ────────────────────────────────────────
@Composable
private fun CampoMedida(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { new ->
            // Solo números y un punto decimal
            if (new.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?\$"))) onValorChange(new)
        },
        label = { Text(label) },
        placeholder = { Text("Introduce el tamaño en cm") },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = onLimpiar) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Limpiar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}