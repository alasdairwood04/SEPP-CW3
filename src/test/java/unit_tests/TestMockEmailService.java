package unit_tests;

import external.EmailService;
import external.MockEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the MockEmailService class.
 */
public class TestMockEmailService {

    private MockEmailService mockEmailService;

    @BeforeEach
    public void setUp() {
        mockEmailService = new MockEmailService();
    }

    /**
     * Tests that sending an email with valid parameters returns success status.
     */
    @Test
    public void testSendEmailWithValidParameters() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending a valid email should return success status");
    }

    /**
     * Tests that sending an email with a null sender returns invalid sender status.
     */
    @Test
    public void testSendEmailWithNullSender() {
        int status = mockEmailService.sendEmail(
                null,
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status,
                "Null sender email should return invalid sender status");
    }

    /**
     * Tests that sending an email with an invalid sender format (no @ symbol) returns invalid sender status.
     */
    @Test
    public void testSendEmailWithInvalidSenderNoAtSymbol() {
        int status = mockEmailService.sendEmail(
                "invalid-email",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status,
                "Invalid sender email format should return invalid sender status");
    }

    /**
     * Tests that sending an email with an invalid sender format (no domain) returns invalid sender status.
     */
    @Test
    public void testSendEmailWithInvalidSenderNoDomain() {
        int status = mockEmailService.sendEmail(
                "invalid@",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status,
                "Invalid sender email with no domain should return invalid sender status");
    }

    /**
     * Tests that sending an email with a null recipient returns invalid recipient status.
     */
    @Test
    public void testSendEmailWithNullRecipient() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                null,
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status,
                "Null recipient email should return invalid recipient status");
    }

    /**
     * Tests that sending an email with an invalid recipient format (no @ symbol) returns invalid recipient status.
     */
    @Test
    public void testSendEmailWithInvalidRecipientNoAtSymbol() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "invalid-email",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status,
                "Invalid recipient email format should return invalid recipient status");
    }

    /**
     * Tests that sending an email with an invalid recipient format (no domain) returns invalid recipient status.
     */
    @Test
    public void testSendEmailWithInvalidRecipientNoDomain() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "invalid@",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status,
                "Invalid recipient email with no domain should return invalid recipient status");
    }

    /**
     * Tests that sending an email with an empty subject is handled properly and returns success status.
     */
    @Test
    public void testSendEmailWithEmptySubject() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                "",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending an email with empty subject should still return success status");
    }

    /**
     * Tests that sending an email with empty content is handled properly and returns success status.
     */
    @Test
    public void testSendEmailWithEmptyContent() {
        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                ""
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending an email with empty content should still return success status");
    }

    /**
     * Tests that email addresses containing special characters that are still valid
     * according to email standards are accepted.
     */
    @Test
    public void testSendEmailWithSpecialCharactersInEmail() {
        int status = mockEmailService.sendEmail(
                "first.last-name+tag_123@hindeburg.ac.uk",
                "test.user-name+filter@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Valid email addresses with special characters should be accepted");
    }

    /**
     * Tests that emails with very long subject lines are handled properly and return success status.
     */
    @Test
    public void testSendEmailWithLongSubject() {

        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                "Long Subject ".repeat(200),
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending an email with very long subject should still return success status");
    }

    /**
     * Tests that emails with very long content are handled properly and return success status.
     */
    @Test
    public void testSendEmailWithLongContent() {

        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a very long email content line. ".repeat(1000)
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending an email with very long content should still return success status");
    }

    /**
     * Tests that when both sender and recipient are invalid,
     * the invalid sender error code is returned (showing error precedence).
     */
    @Test
    public void testSendEmailWithBothInvalidSenderAndRecipient() {
        int status = mockEmailService.sendEmail(
                "invalid-sender",
                "invalid-recipient",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status,
                "Invalid sender error should take precedence over invalid recipient");
    }
}