package unit_tests;

import external.MockAuthenticationService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MockAuthenticationService class.
 */
public class TestMockAuthenticationService {

    private MockAuthenticationService mockAuthenticationService;
    private JSONParser parser;


    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, ParseException {
        mockAuthenticationService = new MockAuthenticationService();
        parser = new JSONParser();
    }

    /**
     * Tests successful login with admin credentials.
     */
    @Test
    public void testLoginWithValidAdminCredentials() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertEquals("AdminStaff", result.get("role"), "Role should be AdminStaff for admin login");
    }

    /**
     * Tests successful login with teacher credentials.
     */
    @Test
    public void testLoginWithValidTeacherCredentials() throws ParseException {
        String response = mockAuthenticationService.login("teacher1", "teacher1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertEquals("TeachingStaff", result.get("role"), "Role should be TeachingStaff for teacher login");
    }

    /**
     * Tests successful login with student credentials.
     */
    @Test
    public void testLoginWithValidStudentCredentials() throws ParseException {
        String response = mockAuthenticationService.login("student1", "student1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertEquals("Student", result.get("role"), "Role should be Student for student login");
    }

    /**
     * Tests that login with incorrect password returns error message.
     */
    @Test
    public void testLoginWithIncorrectPassword() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "wrongpassword");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should contain error for incorrect password");
    }

    /**
     * Tests that login with non-existent username returns error message.
     */
    @Test
    public void testLoginWithNonexistentUsername() throws ParseException {
        String response = mockAuthenticationService.login("nonexistent", "anypassword");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should contain error for non-existent username");
    }

    /**
     * Tests that username is case-sensitive.
     */
    @Test
    public void testLoginWithCaseSensitiveUsername() throws ParseException {
        String response = mockAuthenticationService.login("ADMIN1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Username should be case-sensitive");
    }

    /**
     * Tests that password is case-sensitive.
     */
    @Test
    public void testLoginWithCaseSensitivePassword() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "ADMIN1PASS");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Password should be case-sensitive");
    }

    /**
     * Tests that login with empty username returns error message.
     */
    @Test
    public void testLoginWithEmptyUsername() throws ParseException {
        String response = mockAuthenticationService.login("", "anypassword");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should contain error for empty username");
    }

    /**
     * Tests that login with empty password returns error message.
     */
    @Test
    public void testLoginWithEmptyPassword() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should contain error for empty password");
    }

    /**
     * Tests that login with null username returns error message.
     */
    @Test
    public void testLoginWithNullUsername() throws ParseException {
        String response = mockAuthenticationService.login(null, "anypassword");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should contain error for null username");
    }

    /**
     * Tests that login with null password throws NullPointerException.
     */
    @Test
    public void testLoginWithNullPassword() {
        assertThrows(NullPointerException.class, () -> {
            mockAuthenticationService.login("admin1", null);
        }, "Should throw NullPointerException for null password");
    }

    /**
     * Tests that successful login response contains username field.
     */
    @Test
    public void testLoginResponseContainsUsername() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("username"), "Response should contain username");
    }

    /**
     * Tests that successful login response contains email field.
     */
    @Test
    public void testLoginResponseContainsEmail() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("email"), "Response should contain email");
    }

    /**
     * Tests that successful login response contains role field.
     */
    @Test
    public void testLoginResponseContainsRole() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("role"), "Response should contain role");
    }

    /**
     * Tests that successful login response contains password field.
     */
    @Test
    public void testLoginResponseContainsPassword() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("password"), "Response should contain password");
    }

    /**
     * Tests that login response is valid JSON for successful login.
     */
    @Test
    public void testLoginResponseIsValidJSONForSuccessfulLogin() {
        String response = mockAuthenticationService.login("admin1", "admin1pass");

        assertDoesNotThrow(() -> {
            parser.parse(response);
        }, "Response should be valid JSON for successful login");
    }

    /**
     * Tests that login response is valid JSON for failed login.
     */
    @Test
    public void testLoginResponseIsValidJSONForFailedLogin() {
        String response = mockAuthenticationService.login("nonexistent", "anypassword");

        assertDoesNotThrow(() -> {
            parser.parse(response);
        }, "Response should be valid JSON for failed login");
    }
}