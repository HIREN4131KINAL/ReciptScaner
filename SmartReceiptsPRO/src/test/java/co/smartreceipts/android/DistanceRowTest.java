package co.smartreceipts.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.tests.utils.DistanceUtils;
import co.smartreceipts.tests.utils.TripUtils;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DistanceRowTest {

	private Distance mDistanceA, mDistanceB;

	@Before
	public void setUp() throws Exception {
		mDistanceA = getDistanceRowBuilderA().build();
		mDistanceB = getDistanceRowBuilderB().build();
	}

	private Distance.Builder getDistanceRowBuilderA() {
		final Distance.Builder builder = new Distance.Builder(DistanceUtils.Constants.ID);
		builder.setTrip(TripUtils.newDefaultTripRowInstance());
		builder.setComment(DistanceUtils.Constants.COMMENT);
		builder.setDate(DistanceUtils.Constants.DATE);
		builder.setDistance(DistanceUtils.Constants.DISTANCE);
		builder.setLocation(DistanceUtils.Constants.LOCATION);
		builder.setRate(DistanceUtils.Constants.RATE);
		builder.setTimezone(DistanceUtils.Constants.TIMEZONE);
		return builder;
	}

	private Distance.Builder getDistanceRowBuilderB() {
		final Distance.Builder builder = new Distance.Builder(DistanceUtils.Constants.ID);
		builder.setTrip(TripUtils.newDefaultTripRowInstance());
		builder.setComment(DistanceUtils.Constants.COMMENT);
		builder.setDate(DistanceUtils.Constants.DATE_MILLIS);
		builder.setDistance(DistanceUtils.Constants.DISTANCE_DOUBLE);
		builder.setLocation(DistanceUtils.Constants.LOCATION);
		builder.setRate(DistanceUtils.Constants.RATE_DOUBLE);
		builder.setTimezone(DistanceUtils.Constants.TIMEZONE_CODE);
		return builder;
	}

	@Test
	public void equalityTest() {
		DistanceUtils.assertFieldEquality(mDistanceA, mDistanceB);
		assertEquals(mDistanceA, mDistanceB);
		assertEquals(mDistanceA, mDistanceA);
		assertNotSame(mDistanceA, null);
		assertNotSame(mDistanceA, getDistanceRowBuilderA().setComment("bad").build());
		assertNotSame(mDistanceA, getDistanceRowBuilderA().setLocation("bad").build());
		assertNotSame(mDistanceA, getDistanceRowBuilderA().setDistance(-1).build());
		assertNotSame(mDistanceA, getDistanceRowBuilderA().setRate(-1).build());
		assertNotSame(mDistanceA, getDistanceRowBuilderA().setDate(0).build());
	}

	@Test
	public void hashCodeTest() {
		assertEquals(mDistanceA.hashCode(), mDistanceB.hashCode());
		assertEquals(mDistanceA.hashCode(), mDistanceA.hashCode());
		assertNotSame(mDistanceA.hashCode(), getDistanceRowBuilderA().setComment("bad").build().hashCode());
		assertNotSame(mDistanceA.hashCode(), getDistanceRowBuilderA().setLocation("bad").build().hashCode());
		assertNotSame(mDistanceA.hashCode(), getDistanceRowBuilderA().setDistance(-1).build().hashCode());
		assertNotSame(mDistanceA.hashCode(), getDistanceRowBuilderA().setRate(-1).build().hashCode());
		assertNotSame(mDistanceA.hashCode(), getDistanceRowBuilderA().setDate(0).build().hashCode());
	}

	@Test
	public void testGetComment() {
		assertNotNull(mDistanceA.getComment());
		assertEquals(mDistanceA.getComment(), mDistanceB.getComment());
		assertEquals(DistanceUtils.Constants.COMMENT, mDistanceA.getComment());
	}

	@Test
	public void testGetDate() {
		assertNotNull(mDistanceA.getDate());
		assertEquals(mDistanceA.getDate(), mDistanceB.getDate());
		assertEquals(DistanceUtils.Constants.DATE, mDistanceA.getDate());
	}

	@Test
	public void testGetDistance() {
		assertNotNull(mDistanceA.getDistance());
		assertEquals(mDistanceA.getDistance(), mDistanceB.getDistance());
		assertEquals(DistanceUtils.Constants.DISTANCE, mDistanceA.getDistance());
	}

	@Test
	public void testGetLocation() {
		assertNotNull(mDistanceA.getLocation());
		assertEquals(mDistanceA.getLocation(), mDistanceB.getLocation());
		assertEquals(DistanceUtils.Constants.LOCATION, mDistanceA.getLocation());
	}

	@Test
	public void testGetRate() {
		assertNotNull(mDistanceA.getRate());
		assertEquals(mDistanceA.getRate(), mDistanceB.getRate());
		assertEquals(DistanceUtils.Constants.RATE, mDistanceA.getRate());
	}

	@Test
	public void testGetTimezone() {
		assertNotNull(mDistanceA.getTimezone());
		assertEquals(mDistanceA.getTimezone(), mDistanceB.getTimezone());
		assertEquals(DistanceUtils.Constants.TIMEZONE, mDistanceA.getTimezone());
		assertEquals(DistanceUtils.Constants.TIMEZONE_CODE, mDistanceA.getTimezoneCode());
	}

	@Test
	public void testGetTrip() {
		assertNotNull(mDistanceA.getTrip());
		assertEquals(mDistanceA.getTrip(), mDistanceB.getTrip());
		assertEquals(TripUtils.newDefaultTripRowInstance(), mDistanceA.getTrip());
	}

	@Test
	public void parcelTest() {
		final Parcel parcelA = Parcel.obtain();
		mDistanceA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);

		final Distance parcelDistanceA = Distance.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelDistanceA);
		assertEquals(mDistanceA, parcelDistanceA);
		DistanceUtils.assertFieldEquality(parcelDistanceA, mDistanceA);
	}

}
