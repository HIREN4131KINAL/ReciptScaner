package co.smartreceipts.android.ocr.apis.model;

import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.utils.TestUtils;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class OcrResponseTest {

    private static final String JSON = "{\n" +
            "  \"location\": {\n" +
            "    \"country\": \"NZ\",\n" +
            "    \"state\": \"Auckland\",\n" +
            "    \"city\": \"Auckland\"\n" +
            "  },\n" +
            "  \"totalAmount\": {\n" +
            "    \"data\": 23.3,\n" +
            "    \"confidenceLevel\": 0.7216666666666667\n" +
            "  },\n" +
            "  \"taxAmount\": {\n" +
            "    \"data\": 3.04,\n" +
            "    \"confidenceLevel\": 0.7216666666666667\n" +
            "  },\n" +
            "  \"currency\": {\n" +
            "    \"data\": \"NZD\",\n" +
            "    \"confidenceLevel\": 0.7216666666666667\n" +
            "  },\n" +
            "  \"date\": {\n" +
            "    \"data\": \"2016-12-22T12:00:00.000Z\",\n" +
            "    \"confidenceLevel\": 0.7216666666666667\n" +
            "  },\n" +
            "  \"merchant\": {\n" +
            "    \"name\": \"Adina Apartment Hotel Auckland Britomart\",\n" +
            "    \"address\": \"2 Tapora St, Auckland, 1010, New Zealand\",\n" +
            "    \"confidenceLevel\": 0.7216666666666667\n" +
            "  },\n" +
            "  \"confidenceLevel\": 0.7216666666666667,\n" +
            "  \"error\": \"string\"\n" +
            "}";

    // Class under test
    OcrResponse response;

    @Before
    public void setUp() {
        response = new GsonBuilder().create().fromJson(JSON, OcrResponse.class);
        assertNotNull(response);
    }

    @Test
    public void totalAmount() {
        final OcrResponseField<Double> totalAmount = response.getTotalAmount();
        assertNotNull(totalAmount);
        assertNotNull(totalAmount.getData());
        assertNotNull(totalAmount.getConfidenceLevel());
        assertEquals(23.3, totalAmount.getData(), TestUtils.EPSILON);
        assertEquals(0.7216666666666667, totalAmount.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void taxAmount() {
        final OcrResponseField<Double> taxAmount = response.getTaxAmount();
        assertNotNull(taxAmount);
        assertNotNull(taxAmount.getData());
        assertNotNull(taxAmount.getConfidenceLevel());
        assertEquals(3.04, taxAmount.getData(), TestUtils.EPSILON);
        assertEquals(0.7216666666666667, taxAmount.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void currency() {
        final OcrResponseField<String> currency = response.getCurrency();
        assertNotNull(currency);
        assertNotNull(currency.getData());
        assertNotNull(currency.getConfidenceLevel());
        assertEquals("NZD", currency.getData());
        assertEquals(0.7216666666666667, currency.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void date() {
        final OcrResponseField<String> date = response.getDate();
        assertNotNull(date);
        assertNotNull(date.getData());
        assertNotNull(date.getConfidenceLevel());
        assertEquals("2016-12-22T12:00:00.000Z", date.getData());
        assertEquals(0.7216666666666667, date.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void merchant() {
        final OcrMerchantField merchant = response.getMerchant();
        assertNotNull(merchant);
        assertNotNull(merchant.getName());
        assertNotNull(merchant.getAddress());
        assertNotNull(merchant.getConfidenceLevel());
        assertEquals("Adina Apartment Hotel Auckland Britomart", merchant.getName());
        assertEquals("2 Tapora St, Auckland, 1010, New Zealand", merchant.getAddress());
        assertEquals(0.7216666666666667, merchant.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void confidenceLevel() {
        assertNotNull(response.getConfidenceLevel());
        assertEquals(0.7216666666666667, response.getConfidenceLevel(), TestUtils.EPSILON);
    }

    @Test
    public void error() {
        assertEquals("string", response.getError());
    }

}