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
import co.smartreceipts.android.model.DistanceRow;
import co.smartreceipts.tests.utils.DistanceUtils;
import co.smartreceipts.tests.utils.TripUtils;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DistanceRowTest {

	private DistanceRow mDistanceRowA, mDistanceRowB;

	@Before
	public void setUp() throws Exception {
		mDistanceRowA = getDistanceRowBuilderA().build();
		mDistanceRowB = getDistanceRowBuilderB().build();
	}

	private DistanceRow.Builder getDistanceRowBuilderA() {
		final DistanceRow.Builder builder = new DistanceRow.Builder(DistanceUtils.Constants.ID);
		builder.setTrip(TripUtils.newDefaultTripRowInstance());
		builder.setComment(DistanceUtils.Constants.COMMENT);
		builder.setDate(DistanceUtils.Constants.DATE);
		builder.setDistance(DistanceUtils.Constants.DISTANCE);
		builder.setLocation(DistanceUtils.Constants.LOCATION);
		builder.setRate(DistanceUtils.Constants.RATE);
		builder.setTimezone(DistanceUtils.Constants.TIMEZONE);
		return builder;
	}

	private DistanceRow.Builder getDistanceRowBuilderB() {
		final DistanceRow.Builder builder = new DistanceRow.Builder(DistanceUtils.Constants.ID);
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
		DistanceUtils.assertFieldEquality(mDistanceRowA, mDistanceRowB);
		assertEquals(mDistanceRowA, mDistanceRowB);
		assertEquals(mDistanceRowA, mDistanceRowA);
		assertNotSame(mDistanceRowA, null);
		assertNotSame(mDistanceRowA, getDistanceRowBuilderA().setComment("bad").build());
		assertNotSame(mDistanceRowA, getDistanceRowBuilderA().setLocation("bad").build());
		assertNotSame(mDistanceRowA, getDistanceRowBuilderA().setDistance(-1).build());
		assertNotSame(mDistanceRowA, getDistanceRowBuilderA().setRate(-1).build());
		assertNotSame(mDistanceRowA, getDistanceRowBuilderA().setDate(0).build());
	}

	@Test
	public void hashCodeTest() {
		assertEquals(mDistanceRowA.hashCode(), mDistanceRowB.hashCode());
		assertEquals(mDistanceRowA.hashCode(), mDistanceRowA.hashCode());
		assertNotSame(mDistanceRowA.hashCode(), getDistanceRowBuilderA().setComment("bad").build().hashCode());
		assertNotSame(mDistanceRowA.hashCode(), getDistanceRowBuilderA().setLocation("bad").build().hashCode());
		assertNotSame(mDistanceRowA.hashCode(), getDistanceRowBuilderA().setDistance(-1).build().hashCode());
		assertNotSame(mDistanceRowA.hashCode(), getDistanceRowBuilderA().setRate(-1).build().hashCode());
		assertNotSame(mDistanceRowA.hashCode(), getDistanceRowBuilderA().setDate(0).build().hashCode());
	}

	@Test
	public void testGetComment() {
		assertNotNull(mDistanceRowA.getComment());
		assertEquals(mDistanceRowA.getComment(), mDistanceRowB.getComment());
		assertEquals(DistanceUtils.Constants.COMMENT, mDistanceRowA.getComment());
	}

	@Test
	public void testGetDate() {
		assertNotNull(mDistanceRowA.getDate());
		assertEquals(mDistanceRowA.getDate(), mDistanceRowB.getDate());
		assertEquals(DistanceUtils.Constants.DATE, mDistanceRowA.getDate());
	}

	@Test
	public void testGetDistance() {
		assertNotNull(mDistanceRowA.getDistance());
		assertEquals(mDistanceRowA.getDistance(), mDistanceRowB.getDistance());
		assertEquals(DistanceUtils.Constants.DISTANCE, mDistanceRowA.getDistance());
	}

	@Test
	public void testGetLocation() {
		assertNotNull(mDistanceRowA.getLocation());
		assertEquals(mDistanceRowA.getLocation(), mDistanceRowB.getLocation());
		assertEquals(DistanceUtils.Constants.LOCATION, mDistanceRowA.getLocation());
	}

	@Test
	public void testGetRate() {
		assertNotNull(mDistanceRowA.getRate());
		assertEquals(mDistanceRowA.getRate(), mDistanceRowB.getRate());
		assertEquals(DistanceUtils.Constants.RATE, mDistanceRowA.getRate());
	}

	@Test
	public void testGetTimezone() {
		assertNotNull(mDistanceRowA.getTimezone());
		assertEquals(mDistanceRowA.getTimezone(), mDistanceRowB.getTimezone());
		assertEquals(DistanceUtils.Constants.TIMEZONE, mDistanceRowA.getTimezone());
		assertEquals(DistanceUtils.Constants.TIMEZONE_CODE, mDistanceRowA.getTimezoneCode());
	}

	@Test
	public void testGetTrip() {
		assertNotNull(mDistanceRowA.getTrip());
		assertEquals(mDistanceRowA.getTrip(), mDistanceRowB.getTrip());
		assertEquals(TripUtils.newDefaultTripRowInstance(), mDistanceRowA.getTrip());
	}

	@Test
	public void parcelTest() {
		final Parcel parcelA = Parcel.obtain();
		mDistanceRowA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);

		final DistanceRow parcelDistanceRowA = DistanceRow.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelDistanceRowA);
		assertEquals(mDistanceRowA, parcelDistanceRowA);
		DistanceUtils.assertFieldEquality(parcelDistanceRowA, mDistanceRowA);
	}

}
