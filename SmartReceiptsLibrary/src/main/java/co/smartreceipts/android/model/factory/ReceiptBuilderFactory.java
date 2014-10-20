package co.smartreceipts.android.model.factory;

import android.text.TextUtils;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.impl.DefaultReceiptImpl;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Receipt} objects
 */
public class ReceiptBuilderFactory implements BuilderFactory<Receipt> {

    private Trip _trip;
    private PaymentMethod _paymentMethod;
    private File _file;
    private String _name, _category, _comment;
    private String _extraEditText1, _extraEditText2, _extraEditText3;
    private BigDecimal _price, _tax;
    private Date _date;
    private TimeZone _timezone;
    private final int _id;
    private int _index;
    private boolean _isExpenseable, _isFullPage, _isSelected;
    private WBCurrency _currency;
    private Source _source;

    public ReceiptBuilderFactory(int id) {
        _id = id;
        _index = -1;
        _source = Source.Undefined;
        _timezone = TimeZone.getDefault();
    }

    public ReceiptBuilderFactory setTrip(Trip trip) {
        _trip = trip;
        return this;
    }

    public ReceiptBuilderFactory setPaymentMethod(PaymentMethod method) {
        _paymentMethod = method;
        return this;
    }

    public ReceiptBuilderFactory setName(String name) {
        _name = name;
        return this;
    }

    public ReceiptBuilderFactory setCategory(String category) {
        _category = category;
        return this;
    }

    public ReceiptBuilderFactory setComment(String comment) {
        _comment = comment;
        return this;
    }

    /**
     * Sets the price of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param price - the desired price as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setPrice(String price) {
        _price = tryParse(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(double price) {
        _price = new BigDecimal(price);
        return this;
    }

    /**
     * Sets the tax of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param tax - the desired tax as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setTax(String tax) {
        _tax = tryParse(tax);
        return this;
    }

    public ReceiptBuilderFactory setTax(double tax) {
        _tax = new BigDecimal(tax);
        return this;
    }

    public ReceiptBuilderFactory setFile(File file) {
        _file = file;
        return this;
    }

    public ReceiptBuilderFactory setImage(File image) {
        _file = image;
        return this;
    }

    public ReceiptBuilderFactory setPDF(File pdf) {
        _file = pdf;
        return this;
    }

    public ReceiptBuilderFactory setDate(Date date) {
        _date = date;
        return this;
    }

    public ReceiptBuilderFactory setDate(long datetime) {
        _date = new Date(datetime);
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(String timeZoneId) {
        if (timeZoneId != null) {
            _timezone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(TimeZone timeZone) {
        _timezone = timeZone;
        return this;
    }

    public ReceiptBuilderFactory setIsExpenseable(boolean isExpenseable) {
        _isExpenseable = isExpenseable;
        return this;
    }

    public ReceiptBuilderFactory setIsFullPage(boolean isFullPage) {
        _isFullPage = isFullPage;
        return this;
    }

    public ReceiptBuilderFactory setIsSelected(boolean isSelected) {
        _isSelected = isSelected;
        return this;
    }

    public ReceiptBuilderFactory setCurrency(WBCurrency currency) {
        _currency = currency;
        return this;
    }

    public ReceiptBuilderFactory setCurrency(String currencyCode) {
        _currency = WBCurrency.getInstance(currencyCode);
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText1(String extraEditText1) {
        _extraEditText1 = extraEditText1;
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText2(String extraEditText2) {
        _extraEditText2 = extraEditText2;
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText3(String extraEditText3) {
        _extraEditText3 = extraEditText3;
        return this;
    }

    public ReceiptBuilderFactory setIndex(int index) {
        _index = index;
        return this;
    }

    public ReceiptBuilderFactory setSourceAsCache() {
        _source = Source.Cache;
        return this;
    }

    public Receipt build() {
        return new DefaultReceiptImpl(_id, _index, _trip, _paymentMethod, _name, _category, _comment, _price, _tax, _currency, _date, _timezone, _isExpenseable, _isFullPage, _source, _extraEditText1, _extraEditText2, _extraEditText3);
    }

    private BigDecimal tryParse(String number) {
        if (TextUtils.isEmpty(number)) {
            return new BigDecimal(0);
        }
        try {
            return new BigDecimal(number.replace(",", "."));
        } catch (NumberFormatException e) {
            return new BigDecimal(0);
        }
    }
}
