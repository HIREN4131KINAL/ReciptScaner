package co.smartreceipts.android.identity.apis.login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SmartReceiptsUserSignUpTest {

    @Test
    public void getters() {
        final SmartReceiptsUserSignUp login = new SmartReceiptsUserSignUp("email", "password");
        assertNull(login.getTypeString());
        assertEquals(login.getEmail(), "email");
        assertEquals(login.getPassword(), "password");
        assertEquals(login.getLoginType(), LoginType.SignUp);
        assertNull(login.getToken());
    }

}