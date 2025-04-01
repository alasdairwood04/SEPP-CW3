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
public class MockAuthenticationServiceTest {
    private MockAuthenticationService mockAuthenticationService;
    private JSONParser parser;


    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, ParseException {
        mockAuthenticationService = new MockAuthenticationService();
        parser = new JSONParser();
    }


    /**
     * Test 1: Successful Login (correct username + password)
     * Verifies that login succeeds with valid credentials.
     */
    @Test
    public void testSuccessfulLogin() throws URISyntaxException, IOException, ParseException {

        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        // Verify successful login
        assertFalse(result.containsKey("error"), "Response should not contain error");
        assertEquals("admin1@hindeburg.ac.uk", result.get("email"), "Email should match");
        assertEquals("AdminStaff", result.get("role"), "Role should be AdminStaff");

        // Test with teacher credentials
        response = mockAuthenticationService.login("teacher1", "teacher1pass");
        result = (JSONObject) parser.parse(response);

        // Verify successful login
        assertFalse(result.containsKey("error"), "Response should not contain error");
        assertEquals("teacher1@hindeburg.ac.uk", result.get("email"), "Email should match");
        assertEquals("TeachingStaff", result.get("role"), "Role should be TeachingStaff");

        // Test with student credentials
        response = mockAuthenticationService.login("student1", "student1pass");
        result = (JSONObject) parser.parse(response);

        // Verify successful login
        assertFalse(result.containsKey("error"), "Response should not contain error");
        assertEquals("student1@hindeburg.ac.uk", result.get("email"), "Email should match");
        assertEquals("Student", result.get("role"), "Role should be Student");

    }

    /**
     * Test 2: Login with Incorrect Password (correct username but not password)
     * Verifies that login fails when username is correct but password is wrong.
     */
    @Test
    public void testLoginWithInvalidPassword() throws URISyntaxException, IOException, ParseException {
        String response = mockAuthenticationService.login("admin1", "InvalidPassword");
        JSONObject result = (JSONObject) parser.parse(response);

        assertTrue(result.containsKey("error"), "Response should not contain error");
        assertEquals("Wrong username or password", result.get("error"), "Error message should match");
    }

    /**
     * Test 3: Login with Nonexistent Username
     * Verifies that login fails when username doesn't exist in the system.
     */
    @Test
    public void testLoginWithInvalidUsername() throws URISyntaxException, IOException, ParseException {
        String response = mockAuthenticationService.login("InvalidUsername", "password");
        JSONObject result = (JSONObject) parser.parse(response);

        // Verify login fails with error
        assertTrue(result.containsKey("error"), "Response should contain error for nonexistent username");
        assertEquals("Wrong username or password", result.get("error"), "Error message should match");
    }

    /**
     * Test 4: Case Sensitivity Check
     * Verifies that username and password matching is case-sensitive.
     */
    @Test
    public void testCaseSensitivity() throws ParseException {
        // Test with uppercase username (original is lowercase)
        String response = mockAuthenticationService.login("ADMIN1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);
        assertTrue(result.containsKey("error"), "Username should be case-sensitive");

        // Test with uppercase password (original is lowercase)
        response = mockAuthenticationService.login("admin1", "ADMIN1PASS");
        result = (JSONObject) parser.parse(response);
        assertTrue(result.containsKey("error"), "Password should be case-sensitive");
    }

    /**
     * Test 5: Empty Username or Password
     * Verifies handling of empty strings for username and password.
     */
    @Test
    public void testEmptyCredentials() throws ParseException {
        // Test with empty username
        String response = mockAuthenticationService.login("", "anypassword");
        JSONObject result = (JSONObject) parser.parse(response);
        assertTrue(result.containsKey("error"), "Response should contain error for empty username");

        // Test with empty password
        response = mockAuthenticationService.login("admin1", "");
        result = (JSONObject) parser.parse(response);
        assertTrue(result.containsKey("error"), "Response should contain error for empty password");
    }

    /**
     * Test 6: Null Values Test
     * Verifies handling of null values for username and password.
     */
    @Test
    public void testNullValues() throws ParseException {
        // Test with null username
        String response = mockAuthenticationService.login(null, "anypassword");
        JSONObject result = (JSONObject) parser.parse(response);
        assertTrue(result.containsKey("error"), "Response should contain error for null username");

        // Test with null password - expect NullPointerException
        assertThrows(NullPointerException.class, () -> {
            mockAuthenticationService.login("admin1", null);
        }, "Should throw NullPointerException for null password");
    }

    /**
     * Test 7: login response structure test
     * verifies that the successful login response contains all expected fields + correct values
     * @throws ParseException
     */
    @Test
    public void testSuccessfulLoginResponseStructure() throws ParseException {
        String response = mockAuthenticationService.login("admin1", "admin1pass");
        JSONObject result = (JSONObject) parser.parse(response);

        // Check all expected fields are present
        assertTrue(result.containsKey("username"), "Response should contain username");
        assertTrue(result.containsKey("email"), "Response should contain email");
        assertTrue(result.containsKey("role"), "Response should contain role");
        assertTrue(result.containsKey("password"), "Response should contain password");

        // Check values are correct
        assertEquals("admin1", result.get("username"));
        assertEquals("admin1@hindeburg.ac.uk", result.get("email"));
        assertEquals("AdminStaff", result.get("role"));
    }

}
