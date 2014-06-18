package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.TaxItem;
import co.smartreceipts.tests.utils.TestUtils;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class TaxItemTest {
	
	@Test
	public void constructionTest() {
		final float floatPercent = 20.2f;
		final String stringPercent = "20.20";
		final BigDecimal bigFloatPercent = new BigDecimal(floatPercent);
		final BigDecimal bigStringPercent = new BigDecimal(stringPercent);
		final TaxItem t1 = new TaxItem(floatPercent, true);
		final TaxItem t2 = new TaxItem(stringPercent, true);
		final TaxItem t3 = new TaxItem(bigFloatPercent, true);
		final TaxItem t4 = new TaxItem(bigStringPercent, true);
		final TaxItem t5 = new TaxItem(floatPercent, false);
		final TaxItem t6 = new TaxItem(stringPercent, false);
		final TaxItem t7 = new TaxItem(bigFloatPercent, false);
		final TaxItem t8 = new TaxItem(bigStringPercent, false);
		assertEquals(t2.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t2.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t3.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t3.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t4.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t4.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t5.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t5.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t6.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t6.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t7.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t7.getPercentAsString(), t1.getPercentAsString());
		assertEquals(t8.getPercent().doubleValue(), t1.getPercent().doubleValue(), TestUtils.EPSILON);
		assertEquals(t8.getPercentAsString(), t1.getPercentAsString());
	}
	
	
	@Test
	public void setPriceTest() {
		/* Post-Tax Formula
		 x = total, y = price, t = x - y, t = y * p
		 x = y + y * p
		 x = y * (1 + p)
		 y = x / (1 + p)
		 t = x - y
		 t = x - x / (1 + p)
		 t = x ( 1 - 1 / (1 + p) )
		 */
		final String price = "100.00";
		final float percent1 = 20.25f;
		final float percent2 = 50.43f;
		final TaxItem preTax1 = new TaxItem(percent1, true);
		final TaxItem postTax1 = new TaxItem(percent1, false);
		final TaxItem preTax2 = new TaxItem(percent2, true);
		final TaxItem postTax2 = new TaxItem(percent2, false);
		preTax1.setPrice(price);
		postTax1.setPrice(price);
		preTax2.setPrice(price);
		postTax2.setPrice(price);
		assertEquals(preTax1.getTax().doubleValue(), 20.25d, TestUtils.EPSILON);
		assertEquals(preTax1.toString(), "20.25");
		assertEquals(postTax1.getTax().doubleValue(), 16.84d, TestUtils.EPSILON);
		assertEquals(postTax1.toString(), "16.84");
		assertEquals(preTax2.getTax().doubleValue(), 50.43d, TestUtils.EPSILON);
		assertEquals(preTax2.toString(), "50.43");
		assertEquals(postTax2.getTax().doubleValue(), 33.52d, TestUtils.EPSILON);
		assertEquals(postTax2.toString(), "33.52");
	}
	
	
}
