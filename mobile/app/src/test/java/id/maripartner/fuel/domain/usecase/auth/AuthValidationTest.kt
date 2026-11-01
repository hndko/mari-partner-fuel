package id.maripartner.fuel.domain.usecase.auth

import id.maripartner.fuel.core.common.result.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthValidationTest {

    private lateinit var validator: ValidateAuthInputUseCase

    @Before
    fun setup() {
        validator = ValidateAuthInputUseCase()
    }

    @Test
    fun validateEmail_acceptsValidEmails() {
        assertTrue(validator.validateEmail("user@example.com").isSuccess)
        assertTrue(validator.validateEmail("driver.go@mari-partner.id").isSuccess)
        assertTrue(validator.validateEmail("test_user+fuel@domain.co.id").isSuccess)
    }

    @Test
    fun validateEmail_rejectsInvalidEmails() {
        assertFalse(validator.validateEmail("").isSuccess)
        assertFalse(validator.validateEmail("   ").isSuccess)
        assertFalse(validator.validateEmail("userexample.com").isSuccess)
        assertFalse(validator.validateEmail("user@.com").isSuccess)
        assertFalse(validator.validateEmail("user@domain").isSuccess)
    }

    @Test
    fun validatePassword_acceptsValidPasswords() {
        assertTrue(validator.validatePassword("123456").isSuccess)
        assertTrue(validator.validatePassword("SecurePass#2026").isSuccess)
    }

    @Test
    fun validatePassword_rejectsTooShortOrEmpty() {
        assertFalse(validator.validatePassword("").isSuccess)
        assertFalse(validator.validatePassword("12345").isSuccess)
    }

    @Test
    fun validatePasswordMatch_handlesMatchAndMismatch() {
        assertTrue(validator.validatePasswordMatch("secret123", "secret123").isSuccess)

        val mismatch = validator.validatePasswordMatch("secret123", "secret456")
        assertFalse(mismatch.isSuccess)
        assertEquals(
            "Konfirmasi kata sandi tidak cocok.",
            (mismatch as AppResult.Error).exception.message
        )
    }

    @Test
    fun validateDisplayName_requiresMinimumTwoCharacters() {
        assertTrue(validator.validateDisplayName("Budi Santoso").isSuccess)
        assertTrue(validator.validateDisplayName("Ed").isSuccess)

        assertFalse(validator.validateDisplayName("").isSuccess)
        assertFalse(validator.validateDisplayName("A").isSuccess)
        assertFalse(validator.validateDisplayName("   ").isSuccess)
    }
}
