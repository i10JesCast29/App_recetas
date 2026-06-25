package com.ferhatozcelik.jetpackcomposetemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 1. MODELO DE DATOS Y VIEWMODEL
data class Receta(val id: Int, val nombre: String, val descripcion: String)

class RecetaViewModel : ViewModel() {
    val recetas = listOf(
        Receta(1, "Enchiladas Suizas", "Tortillas rellenas de pollo bañadas en salsa verde cremosa, coronadas con queso gratinado."),
        Receta(2, "Tacos al Pastor", "Carne de cerdo adobada, servida con piña, cilantro y cebolla fresca en tortilla de maíz."),
        Receta(3, "Chiles en Nogada", "Chile poblano relleno de picadillo de carne y frutas, bañado en salsa de nuez de castilla y granada.")
    )

    private val _recetaSeleccionada = MutableStateFlow<Receta?>(null)
    val recetaSeleccionada: StateFlow<Receta?> = _recetaSeleccionada.asStateFlow()

    fun seleccionarReceta(id: Int) {
        _recetaSeleccionada.value = recetas.find { it.id == id }
    }
}

// 2. ACTIVIDAD PRINCIPAL
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavegacion()
                }
            }
        }
    }
}

// 3. NAVEGACIÓN (NAVHOST)
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val viewModel: RecetaViewModel = viewModel()

    NavHost(navController = navController, startDestination = "Home") {
        composable("Home") {
            ListaRecetasScreen(
                viewModel = viewModel,
                onRecetaClick = { recetaId ->
                    viewModel.seleccionarReceta(recetaId)
                    navController.navigate("Detail/$recetaId")
                }
            )
        }
        composable(
            route = "Detail/{recetaId}",
            arguments = listOf(navArgument("recetaId") { type = NavType.IntType })
        ) {
            DetalleRecetaScreen(
                viewModel = viewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

// 4. PANTALLAS (LAYOUTS)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRecetasScreen(viewModel: RecetaViewModel, onRecetaClick: (Int) -> Unit) {
    val recetas = viewModel.recetas

    Scaffold(
        topBar = { TopAppBar(title = { Text("App de Recetas") }) }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(recetas) { receta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onRecetaClick(receta.id) }
                ) {
                    Text(
                        text = receta.nombre,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(viewModel: RecetaViewModel, onVolver: () -> Unit) {
    val receta by viewModel.recetaSeleccionada.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de la Receta") }) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            
            Button(onClick = onVolver, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("← Volver")
            }
            
            receta?.let {
                Text(text = it.nombre, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it.descripcion, style = MaterialTheme.typography.bodyLarge)
            } ?: run {
                Text("Receta no encontrada.")
            }
        }
    }
}
