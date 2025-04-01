package unit_tests;

import external.EmailService;
import external.MockEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the MockEmailService class.
 */
public class MockEmailServiceTest {
    private MockEmailService mockEmailService;


    @BeforeEach
    public void setUp() {
        mockEmailService = new MockEmailService();
    }

    /**
     * Test 1: Valid Case - Sending a Well-Formatted Email
     * Verifies that a well-formatted email with valid sender, recipient, subject, and content
     * is sent successfully and returns the success status code.
     */
    @Test
    public void testSendValidEmail() {
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
     * Test 2: Invalid Sender Email
     * Verifies that sending an email with an invalid sender email format
     * returns the appropriate error status code.
     */
    @Test
    public void testInvalidSenderEmail() {
        // Test with null sender
        int status1 = mockEmailService.sendEmail(
                null,
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status1,
                "Null sender email should return invalid sender status");

        // Test with invalid email format (no @ symbol)
        int status2 = mockEmailService.sendEmail(
                "invalid-email",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status2,
                "Invalid sender email format should return invalid sender status");

        // Test with invalid email format (no domain)
        int status3 = mockEmailService.sendEmail(
                "invalid@",
                "recipient@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_SENDER_EMAIL, status3,
                "Invalid sender email with no domain should return invalid sender status");
    }

    /**
     * Test 3: Invalid Recipient Email
     * Verifies that sending an email with an invalid recipient email format
     * returns the appropriate error status code.
     */
    @Test
    public void testInvalidRecipientEmail() {
        // Test with null recipient
        int status1 = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                null,
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status1,
                "Null recipient email should return invalid recipient status");

        // Test with invalid email format (no @ symbol)
        int status2 = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "invalid-email",
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status2,
                "Invalid recipient email format should return invalid recipient status");

        // Test with invalid email format (no domain)
        int status3 = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "invalid@",
                "Test Subject",
                "This is a test email content."
        );
        assertEquals(EmailService.STATUS_INVALID_RECIPIENT_EMAIL, status3,
                "Invalid recipient email with no domain should return invalid recipient status");
    }

    /**
     * Test 4: Edge Case - Empty Subject
     * Verifies that sending an email with an empty subject
     * is handled properly and still returns success status.
     */
    @Test
    public void testEmptySubject() {
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
     * Test 5: Edge Case - Empty Content
     * Verifies that sending an email with empty content
     * is handled properly and still returns success status.
     */
    @Test
    public void testEmptyContent() {
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
     * Test 6: Edge Case - Special Characters in Email
     * Verifies that email addresses containing special characters
     * that are still valid according to email standards are accepted.
     */
    @Test
    public void testSpecialCharactersInEmail() {
        // Email with dots, hyphens, plus signs, and underscores
        int status = mockEmailService.sendEmail(
                "first.last-name+tag_123@hindeburg.ac.uk",
                "test.user-name+filter@hindeburg.ac.uk",
                "Test Subject",
                "This is a test email content."
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Valid email addresses with special characters are accepted");
    }

    /**
     * Test 7: Edge Case - Long Subject and Content
     * Verifies that emails with very long subject lines and content
     * are handled properly and still return success status.
     */
    @Test
    public void testLongSubjectAndContent() {
        // Create a very long subject
        String longSubject = "Long Subject ".repeat(200);

        // Create very long content
        String longContent = "This is a very long email content line. ".repeat(1000);


        int status = mockEmailService.sendEmail(
                "sender@hindeburg.ac.uk",
                "recipient@hindeburg.ac.uk",
                longSubject,
                longContent
        );

        assertEquals(EmailService.STATUS_SUCCESS, status,
                "Sending an email with very long subject and content should still return success status");
    }

    /**
     * Test 8: Error Precedence - Invalid Sender takes precedence over Invalid Recipient
     * Verifies that when both sender and recipient are invalid,
     * the invalid sender error code is returned (showing error precedence).
     */
    @Test
    public void testErrorPrecedence() {
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
