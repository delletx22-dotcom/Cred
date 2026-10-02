package com.example

import com.example.model.InputFormatters
import com.example.model.LoanCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testRuleA_under600() {
    val result = LoanCalculator.calculate(200.0)
    assertEquals(200.0, result.requestedAmount, 0.001)
    assertEquals(100.0, result.interestAmount, 0.001)
    assertEquals(300.0, result.totalAmountToPay, 0.001)
    assertEquals("15 dias", result.paymentTerm)
  }

  @Test
  fun testRuleB_overOrEqual600() {
    val result = LoanCalculator.calculate(1000.0)
    assertEquals(1000.0, result.requestedAmount, 0.001)
    assertEquals(500.0, result.interestAmount, 0.001)
    assertEquals(1500.0, result.totalAmountToPay, 0.001)
    assertEquals("30 dias (1 mês)", result.paymentTerm)
  }

  @Test
  fun testWhatsAppMessageFormat() {
    val simulation = LoanCalculator.calculate(1000.0)
    val message = LoanCalculator.buildWhatsAppMessage("João da Silva", simulation)
    // "Olá! Fiz uma simulação no app e quero finalizar meu empréstimo. Nome: [Nome do Usuário] | Valor Simulado: R$ [Valor] | Total com Juros: R$ [Valor Total] | Prazo: [Prazo]."
    assertTrue(message.startsWith("Olá! Fiz uma simulação no app e quero finalizar meu empréstimo."))
    assertTrue(message.contains("Nome: João da Silva"))
    assertTrue(message.contains("Prazo: 30 dias (1 mês)"))
  }

  @Test
  fun testInputFormatters() {
    val cpfFormatted = InputFormatters.formatCpf("12345678901")
    assertEquals("123.456.789-01", cpfFormatted)

    val phoneFormatted = InputFormatters.formatPhone("11987654321")
    assertEquals("(11) 98765-4321", phoneFormatted)
  }
}
