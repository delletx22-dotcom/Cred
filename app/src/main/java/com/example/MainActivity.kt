package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SimulationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoanAppRoot()
                }
            }
        }
    }
}

@Composable
fun LoanAppRoot(
    viewModel: LoanViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val customerData by viewModel.customerData.collectAsStateWithLifecycle()
    val loanAmount by viewModel.loanAmount.collectAsStateWithLifecycle()
    val simulationResult by viewModel.simulationResult.collectAsStateWithLifecycle()
    val validationState by viewModel.validationState.collectAsStateWithLifecycle()
    val attendantPhone by viewModel.attendantPhone.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == AppScreen.SIMULATION) {
                (slideInHorizontally { width -> width } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn())
                    .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.REGISTRATION -> {
                RegistrationScreen(
                    customerData = customerData,
                    validationState = validationState,
                    attendantPhone = attendantPhone,
                    onNameChange = { viewModel.updateName(it) },
                    onCpfChange = { viewModel.updateCpf(it) },
                    onPhoneChange = { viewModel.updatePhone(it) },
                    onIdentityDocChange = { uri, name -> viewModel.setIdentityDoc(uri, name) },
                    onAddressDocChange = { uri, name -> viewModel.setAddressDoc(uri, name) },
                    onAdvanceClick = { viewModel.validateAndProceed() },
                    onUpdateAttendantPhone = { viewModel.updateAttendantPhone(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppScreen.SIMULATION -> {
                SimulationScreen(
                    customerData = customerData,
                    loanAmount = loanAmount,
                    simulationResult = simulationResult,
                    attendantPhone = attendantPhone,
                    onAmountChange = { viewModel.setLoanAmount(it) },
                    onBackToRegistration = { viewModel.navigateToRegistration() },
                    onUpdateAttendantPhone = { viewModel.updateAttendantPhone(it) },
                    whatsAppMessage = viewModel.getWhatsAppMessage(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
