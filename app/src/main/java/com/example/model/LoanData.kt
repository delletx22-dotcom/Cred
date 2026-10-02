package com.example.model

import android.net.Uri
import java.text.NumberFormat
import java.util.Locale

data class CustomerData(
    val fullName: String = "",
    val cpf: String = "",
    val phone: String = "",
    val identityDocUri: Uri? = null,
    val identityDocName: String = "",
    val addressDocUri: Uri? = null,
    val addressDocName: String = ""
)

data class SimulationResult(
    val requestedAmount: Double,
    val interestRatePercent: Int = 50,
    val interestAmount: Double,
    val totalAmountToPay: Double,
    val paymentTerm: String,
    val formattedRequestedAmount: String,
    val formattedTotalAmount: String
)

object LoanCalculator {
    private val brazilianLocale = Locale("pt", "BR")
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(brazilianLocale)

    fun calculate(amount: Double): SimulationResult {
        // Enforce bounds R$ 50 to R$ 5.000
        val clampedAmount = amount.coerceIn(50.0, 5000.0)
        val interestRate = 0.50
        val totalAmount = clampedAmount * (1.0 + interestRate)
        val interest = clampedAmount * interestRate

        // Regra A (R$ 50 a R$ 599): Prazo: 15 dias
        // Regra B (R$ 600 a R$ 5.000): Prazo: 30 dias (1 mês)
        val term = if (clampedAmount < 600.0) {
            "15 dias"
        } else {
            "30 dias (1 mês)"
        }

        return SimulationResult(
            requestedAmount = clampedAmount,
            interestRatePercent = 50,
            interestAmount = interest,
            totalAmountToPay = totalAmount,
            paymentTerm = term,
            formattedRequestedAmount = formatCurrency(clampedAmount),
            formattedTotalAmount = formatCurrency(totalAmount)
        )
    }

    fun formatCurrency(value: Double): String {
        return currencyFormatter.format(value)
    }

    fun formatNumberOnly(value: Double): String {
        return String.format(brazilianLocale, "%,.2f", value)
    }

    fun buildWhatsAppMessage(customerName: String, simulation: SimulationResult): String {
        val formattedRequested = formatNumberOnly(simulation.requestedAmount)
        val formattedTotal = formatNumberOnly(simulation.totalAmountToPay)
        // Regra estrita: "Olá! Fiz uma simulação no app e quero finalizar meu empréstimo. Nome: [Nome do Usuário] | Valor Simulado: R$ [Valor] | Total com Juros: R$ [Valor Total] | Prazo: [Prazo]."
        return "Olá! Fiz uma simulação no app e quero finalizar meu empréstimo. Nome: ${customerName.trim()} | Valor Simulado: R$ $formattedRequested | Total com Juros: R$ $formattedTotal | Prazo: ${simulation.paymentTerm}."
    }
}

object InputFormatters {
    fun formatCpf(input: String): String {
        val digits = input.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        for (i in digits.indices) {
            sb.append(digits[i])
            if (i == 2 || i == 5) {
                sb.append('.')
            } else if (i == 8) {
                sb.append('-')
            }
        }
        return sb.toString()
    }

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        for (i in digits.indices) {
            if (i == 0) sb.append('(')
            sb.append(digits[i])
            if (i == 1) sb.append(") ")
            if (digits.length <= 10) {
                if (i == 5) sb.append('-')
            } else {
                if (i == 6) sb.append('-')
            }
        }
        return sb.toString()
    }

    fun cleanDigits(input: String): String {
        return input.filter { it.isDigit() }
    }
}
