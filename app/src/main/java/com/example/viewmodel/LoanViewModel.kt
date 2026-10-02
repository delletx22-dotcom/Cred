package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.model.CustomerData
import com.example.model.InputFormatters
import com.example.model.LoanCalculator
import com.example.model.SimulationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppScreen {
    REGISTRATION,
    SIMULATION
}

data class UiValidationState(
    val nameError: String? = null,
    val cpfError: String? = null,
    val phoneError: String? = null,
    val identityError: String? = null,
    val addressError: String? = null
) {
    val hasErrors: Boolean
        get() = nameError != null || cpfError != null || phoneError != null || identityError != null || addressError != null
}

class LoanViewModel : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.REGISTRATION)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _customerData = MutableStateFlow(CustomerData())
    val customerData: StateFlow<CustomerData> = _customerData.asStateFlow()

    // O valor padrão inicial deve ser R$ 1.000.
    private val _loanAmount = MutableStateFlow(1000.0)
    val loanAmount: StateFlow<Double> = _loanAmount.asStateFlow()

    private val _simulationResult = MutableStateFlow(LoanCalculator.calculate(1000.0))
    val simulationResult: StateFlow<SimulationResult> = _simulationResult.asStateFlow()

    private val _validationState = MutableStateFlow(UiValidationState())
    val validationState: StateFlow<UiValidationState> = _validationState.asStateFlow()

    // Default WhatsApp attendant number (can be configured in UI dialog)
    private val _attendantPhone = MutableStateFlow("5511999999999")
    val attendantPhone: StateFlow<String> = _attendantPhone.asStateFlow()

    fun updateName(name: String) {
        _customerData.update { it.copy(fullName = name) }
        if (_validationState.value.nameError != null && name.trim().length >= 3) {
            _validationState.update { it.copy(nameError = null) }
        }
    }

    fun updateCpf(rawCpf: String) {
        val formatted = InputFormatters.formatCpf(rawCpf)
        _customerData.update { it.copy(cpf = formatted) }
        if (_validationState.value.cpfError != null && InputFormatters.cleanDigits(formatted).length == 11) {
            _validationState.update { it.copy(cpfError = null) }
        }
    }

    fun updatePhone(rawPhone: String) {
        val formatted = InputFormatters.formatPhone(rawPhone)
        _customerData.update { it.copy(phone = formatted) }
        if (_validationState.value.phoneError != null && InputFormatters.cleanDigits(formatted).length in 10..11) {
            _validationState.update { it.copy(phoneError = null) }
        }
    }

    fun setIdentityDoc(uri: Uri?, fileName: String) {
        _customerData.update { it.copy(identityDocUri = uri, identityDocName = fileName) }
        if (uri != null) {
            _validationState.update { it.copy(identityError = null) }
        }
    }

    fun setAddressDoc(uri: Uri?, fileName: String) {
        _customerData.update { it.copy(addressDocUri = uri, addressDocName = fileName) }
        if (uri != null) {
            _validationState.update { it.copy(addressError = null) }
        }
    }

    fun setLoanAmount(amount: Double) {
        val clamped = amount.coerceIn(50.0, 5000.0)
        _loanAmount.value = clamped
        _simulationResult.value = LoanCalculator.calculate(clamped)
    }

    fun updateAttendantPhone(phone: String) {
        val clean = InputFormatters.cleanDigits(phone)
        _attendantPhone.value = clean.ifEmpty { "5511999999999" }
    }

    fun validateAndProceed(): Boolean {
        val current = _customerData.value
        val nameTrimmed = current.fullName.trim()
        val cpfDigits = InputFormatters.cleanDigits(current.cpf)
        val phoneDigits = InputFormatters.cleanDigits(current.phone)

        val nameErr = when {
            nameTrimmed.isEmpty() -> "Por favor, digite seu nome completo."
            !nameTrimmed.contains(" ") || nameTrimmed.length < 5 -> "Digite nome e sobrenome completos."
            else -> null
        }

        val cpfErr = when {
            cpfDigits.isEmpty() -> "Por favor, digite seu CPF."
            cpfDigits.length != 11 -> "CPF incompleto. Deve conter 11 dígitos."
            else -> null
        }

        val phoneErr = when {
            phoneDigits.isEmpty() -> "Por favor, digite seu telefone com DDD."
            phoneDigits.length < 10 -> "Telefone incompleto com DDD."
            else -> null
        }

        val identityErr = if (current.identityDocUri == null) {
            "Anexe a foto da sua identidade (frente e verso)."
        } else null

        val addressErr = if (current.addressDocUri == null) {
            "Anexe o seu comprovante de endereço atualizado."
        } else null

        val state = UiValidationState(
            nameError = nameErr,
            cpfError = cpfErr,
            phoneError = phoneErr,
            identityError = identityErr,
            addressError = addressErr
        )

        _validationState.value = state

        if (!state.hasErrors) {
            _currentScreen.value = AppScreen.SIMULATION
            return true
        }
        return false
    }

    fun navigateToRegistration() {
        _currentScreen.value = AppScreen.REGISTRATION
    }

    fun getWhatsAppMessage(): String {
        return LoanCalculator.buildWhatsAppMessage(
            customerName = _customerData.value.fullName,
            simulation = _simulationResult.value
        )
    }
}
