package system_tests;

import controller.AuthenticatedUserController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.Guest;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;

import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class LogoutSystemTests extends TUITest {

    private View view;

    @Test
    public void loginThenLogout() throws URISyntaxException, IOException, ParseException {
        SharedContext context = new SharedContext(view);
        loginAsAdminStaff(context);
        AuthenticatedUserController authController = new AuthenticatedUserController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());
        startOutputCapture();
        authController.logout();
        assertOutputContains("Logged out!");
        assertInstanceOf(Guest.class, context.currentUser);
    }

    @Test
    public void logoutAsGuest() throws URISyntaxException, IOException, ParseException {
        SharedContext context = new SharedContext(view);
        AuthenticatedUserController authController = new AuthenticatedUserController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());
        startOutputCapture();
        authController.logout();
        assertInstanceOf(Guest.class, context.currentUser);
    }
}
