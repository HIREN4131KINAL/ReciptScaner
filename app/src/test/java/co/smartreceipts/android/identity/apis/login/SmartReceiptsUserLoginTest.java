package co.smartreceipts.android.identity.apis.login;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SmartReceiptsUserLoginTest {

    @Test
    public void getters() {
        final SmartReceiptsUserLogin login = new SmartReceiptsUserLogin("email", "password");
        assertEquals(login.getTypeString(), "login");
        assertEquals(login.getEmail(), "email");
        assertEquals(login.getPassword(), "password");
        assertEquals(login.getLoginType(), LoginType.LogIn);
        assertNull(login.getToken());
    }
}