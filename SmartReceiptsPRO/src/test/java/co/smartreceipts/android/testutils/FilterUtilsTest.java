package co.smartreceipts.android.testutils;

import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.filters.FilterUtils;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class FilterUtilsTest {

	// These scenario is using same time representation
	// with different timezone for all assertion (let's say 09:00)
	private final long millis = new java.util.Date().getTime();
	private final Date date = new Date(millis);

	private final TimeZone tz1 = TimeZone.getTimeZone("GMT+1");
	private final TimeZone tz2 = TimeZone.getTimeZone("GMT+2");
	private final TimeZone tz3 = TimeZone.getTimeZone("GMT+3");

	@Test
	public void isOnOrAfterTimezoneHandlingTest() {
		// combining timezone
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz2));
		assertTrue(FilterUtils.isOnOrAfter(date, tz2, date, tz3));
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz3));

		// same timezone
		assertTrue(FilterUtils.isOnOrAfter(date, tz1, date, tz1));
		assertTrue(FilterUtils.isOnOrAfter(date, tz2, date, tz2));
		assertTrue(FilterUtils.isOnOrAfter(date, tz3, date, tz3));
	}

	@Test
	public void isOnOrBeforeTimezoneHandlingTest() {
		// combining timezone
		assertTrue(FilterUtils.isOnOrBefore(date, tz2, date, tz1));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz2));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz1));

		// same timezone
		assertTrue(FilterUtils.isOnOrBefore(date, tz1, date, tz1));
		assertTrue(FilterUtils.isOnOrBefore(date, tz2, date, tz2));
		assertTrue(FilterUtils.isOnOrBefore(date, tz3, date, tz3));
	}

	@Test
	public void differentChronologyOrderTest() throws ParseException {
		// Testing the case where UTC and local representation
		// have different chronological order.
		// Correct order must be determined by UTC representation.
		// 01:00 UTC-2 -> 03:00 UTC
		// 03:00 UTC+2 -> 01:00 UTC

		Calendar cal = Calendar.getInstance();
		cal.set(0, 0, 0, 1, 0); // 01:00
		long d = cal.getTimeInMillis();
		Date am1 = new Date(d);
		Date am3 = new Date(d + (2 * 60 * 60 * 1000)); // offsetting by +2h

		TimeZone tzMinusTwo = TimeZone.getTimeZone("GMT-2");
		TimeZone tzPlusTwo = TimeZone.getTimeZone("GMT+2");

		assertTrue(FilterUtils.isOnOrAfter(am1, tzMinusTwo, am3, tzPlusTwo));
		assertTrue(FilterUtils.isOnOrBefore(am3, tzPlusTwo, am1, tzMinusTwo));
	}
}
