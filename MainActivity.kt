package com.adarsh.syllabusmission

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adarsh.syllabusmission.data.AppDatabase
import com.adarsh.syllabusmission.data.ChapterData
import com.adarsh.syllabusmission.data.Repository
import com.adarsh.syllabusmission.ui.HomeScreen
import com.adarsh.syllabusmission.ui.StreakScreen
import com.adarsh.syllabusmission.ui.SubjectScreen
import com.adarsh.syllabusmission.ui.SyllabusTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app))

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today

    /** chapterId -> date completed. null = still loading from the database. */
    val completions: StateFlow<Map<String, LocalDate>?> = repo.observe()
        .map { list ->
            list.filter { it.chapterId in ChapterData.allIds }
                .associate { it.chapterId to LocalDate.ofEpochDay(it.completedOn) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Called when the app comes back to the screen, so a new calendar day is noticed. */
    fun refreshToday() {
        _today.value = LocalDate.now()
    }

    fun toggle(chapterId: String) {
        viewModelScope.launch { repo.toggle(chapterId, LocalDate.now().toEpochDay()) }
    }
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SyllabusTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshToday()
    }
}

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    val doneOrNull by vm.completions.collectAsState()
    val today by vm.today.collectAsState()
    val done = doneOrNull

    if (done == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                done = done,
                onSubject = { nav.navigate("subject/$it") },
                onStreak = { nav.navigate("streak") }
            )
        }
        composable(
            "subject/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val subject = ChapterData.find(entry.arguments?.getString("id"))
            if (subject != null) {
                SubjectScreen(
                    subject = subject,
                    done = done,
                    onToggle = vm::toggle,
                    onBack = { nav.popBackStack() }
                )
            }
        }
        composable("streak") {
            StreakScreen(done = done, today = today, onBack = { nav.popBackStack() })
        }
    }
}
