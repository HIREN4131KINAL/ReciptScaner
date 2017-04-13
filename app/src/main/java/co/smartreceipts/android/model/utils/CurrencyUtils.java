package co.smartreceipts.android.model.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CurrencyUtils {

    @NonNull
    public static List<String> getAllCurrencies() {
        final List<String> currencies = new ArrayList<>();
        currencies.addAll(getIso4217CurrencyCodes());
        currencies.addAll(getNonIso4217CurrencyCodes());
        return currencies;
    }

    /**
     * Returns a list of all ISO 4127 currencies
     * http://en.wikipedia.org/wiki/ISO_4217
     *
     * @return a List<String> containing all ISO 4217 Currencies
     */
    @NonNull
    public static List<String> getIso4217CurrencyCodes() {
        final ArrayList<String> iso4217Currencies = new ArrayList<>();
        iso4217Currencies.add("AED");
        iso4217Currencies.add("AFN");
        iso4217Currencies.add("ALL");
        iso4217Currencies.add("AMD");
        iso4217Currencies.add("ANG");
        iso4217Currencies.add("AOA");
        iso4217Currencies.add("ARS");
        iso4217Currencies.add("AUD");
        iso4217Currencies.add("AWG");
        iso4217Currencies.add("AZN");
        iso4217Currencies.add("BAM");
        iso4217Currencies.add("BBD");
        iso4217Currencies.add("BDT");
        iso4217Currencies.add("BGN");
        iso4217Currencies.add("BHD");
        iso4217Currencies.add("BIF");
        iso4217Currencies.add("BMD");
        iso4217Currencies.add("BND");
        iso4217Currencies.add("BOB");
        iso4217Currencies.add("BOV");
        iso4217Currencies.add("BRL");
        iso4217Currencies.add("BSD");
        iso4217Currencies.add("BTN");
        iso4217Currencies.add("BWP");
        iso4217Currencies.add("BYR");
        iso4217Currencies.add("BZD");
        iso4217Currencies.add("CAD");
        iso4217Currencies.add("CDF");
        iso4217Currencies.add("CHE");
        iso4217Currencies.add("CHF");
        iso4217Currencies.add("CHW");
        iso4217Currencies.add("CLF");
        iso4217Currencies.add("CLP");
        iso4217Currencies.add("CNY");
        iso4217Currencies.add("COP");
        iso4217Currencies.add("COU");
        iso4217Currencies.add("CRC");
        iso4217Currencies.add("CUC");
        iso4217Currencies.add("CUP");
        iso4217Currencies.add("CVE");
        iso4217Currencies.add("CZK");
        iso4217Currencies.add("DJF");
        iso4217Currencies.add("DKK");
        iso4217Currencies.add("DOP");
        iso4217Currencies.add("DZD");
        iso4217Currencies.add("EGP");
        iso4217Currencies.add("ERN");
        iso4217Currencies.add("ETB");
        iso4217Currencies.add("EUR");
        iso4217Currencies.add("FJD");
        iso4217Currencies.add("FKP");
        iso4217Currencies.add("GBP");
        iso4217Currencies.add("GEL");
        iso4217Currencies.add("GHS");
        iso4217Currencies.add("GIP");
        iso4217Currencies.add("GMD");
        iso4217Currencies.add("GNF");
        iso4217Currencies.add("GTQ");
        iso4217Currencies.add("GYD");
        iso4217Currencies.add("HKD");
        iso4217Currencies.add("HNL");
        iso4217Currencies.add("HRK");
        iso4217Currencies.add("HTG");
        iso4217Currencies.add("HUF");
        iso4217Currencies.add("IDR");
        iso4217Currencies.add("ILS");
        iso4217Currencies.add("INR");
        iso4217Currencies.add("IQD");
        iso4217Currencies.add("IRR");
        iso4217Currencies.add("ISK");
        iso4217Currencies.add("JMD");
        iso4217Currencies.add("JOD");
        iso4217Currencies.add("JPY");
        iso4217Currencies.add("KES");
        iso4217Currencies.add("KGS");
        iso4217Currencies.add("KHR");
        iso4217Currencies.add("KMF");
        iso4217Currencies.add("KPW");
        iso4217Currencies.add("KRW");
        iso4217Currencies.add("KWD");
        iso4217Currencies.add("KYD");
        iso4217Currencies.add("KZT");
        iso4217Currencies.add("LAK");
        iso4217Currencies.add("LBP");
        iso4217Currencies.add("LKR");
        iso4217Currencies.add("LRD");
        iso4217Currencies.add("LSL");
        iso4217Currencies.add("LTL");
        iso4217Currencies.add("LVL");
        iso4217Currencies.add("LYD");
        iso4217Currencies.add("MAD");
        iso4217Currencies.add("MDL");
        iso4217Currencies.add("MGA");
        iso4217Currencies.add("MKD");
        iso4217Currencies.add("MMK");
        iso4217Currencies.add("MNT");
        iso4217Currencies.add("MOP");
        iso4217Currencies.add("MRO");
        iso4217Currencies.add("MUR");
        iso4217Currencies.add("MVR");
        iso4217Currencies.add("MWK");
        iso4217Currencies.add("MXN");
        iso4217Currencies.add("MXV");
        iso4217Currencies.add("MYR");
        iso4217Currencies.add("MZN");
        iso4217Currencies.add("NAD");
        iso4217Currencies.add("NGN");
        iso4217Currencies.add("NIO");
        iso4217Currencies.add("NIS");
        iso4217Currencies.add("NOK");
        iso4217Currencies.add("NPR");
        iso4217Currencies.add("NZD");
        iso4217Currencies.add("OMR");
        iso4217Currencies.add("PAB");
        iso4217Currencies.add("PEN");
        iso4217Currencies.add("PGK");
        iso4217Currencies.add("PHP");
        iso4217Currencies.add("PKR");
        iso4217Currencies.add("PLN");
        iso4217Currencies.add("PYG");
        iso4217Currencies.add("QAR");
        iso4217Currencies.add("RON");
        iso4217Currencies.add("RSD");
        iso4217Currencies.add("RUB");
        iso4217Currencies.add("RWF");
        iso4217Currencies.add("SAR");
        iso4217Currencies.add("SBD");
        iso4217Currencies.add("SCR");
        iso4217Currencies.add("SDG");
        iso4217Currencies.add("SEK");
        iso4217Currencies.add("SGD");
        iso4217Currencies.add("SHP");
        iso4217Currencies.add("SLL");
        iso4217Currencies.add("SOS");
        iso4217Currencies.add("SRD");
        iso4217Currencies.add("SSP");
        iso4217Currencies.add("STD");
        iso4217Currencies.add("SYP");
        iso4217Currencies.add("SZL");
        iso4217Currencies.add("THB");
        iso4217Currencies.add("TJS");
        iso4217Currencies.add("TMT");
        iso4217Currencies.add("TND");
        iso4217Currencies.add("TOP");
        iso4217Currencies.add("TRY");
        iso4217Currencies.add("TTD");
        iso4217Currencies.add("TWD");
        iso4217Currencies.add("TZS");
        iso4217Currencies.add("UAH");
        iso4217Currencies.add("UGX");
        iso4217Currencies.add("USD");
        iso4217Currencies.add("USN");
        iso4217Currencies.add("USS");
        iso4217Currencies.add("UYI");
        iso4217Currencies.add("UYU");
        iso4217Currencies.add("UZS");
        iso4217Currencies.add("VEF");
        iso4217Currencies.add("VND");
        iso4217Currencies.add("VUV");
        iso4217Currencies.add("WST");
        iso4217Currencies.add("XAF");
        iso4217Currencies.add("XAG");
        iso4217Currencies.add("XAU");
        iso4217Currencies.add("XBA");
        iso4217Currencies.add("XBB");
        iso4217Currencies.add("XBC");
        iso4217Currencies.add("XBD");
        iso4217Currencies.add("XCD");
        iso4217Currencies.add("XDR");
        iso4217Currencies.add("XFU");
        iso4217Currencies.add("XOF");
        iso4217Currencies.add("XPD");
        iso4217Currencies.add("XPF");
        iso4217Currencies.add("XPT");
        iso4217Currencies.add("XTS");
        iso4217Currencies.add("XXX");
        iso4217Currencies.add("YER");
        iso4217Currencies.add("ZAR");
        iso4217Currencies.add("ZMW");
        iso4217Currencies.add("ZWL");
        return iso4217Currencies;
    }

    /**
     * Returns a list of non ISO 4217 Currency Codes (e.g. crypto-currencies, non-official ones, etc.)
     * Mostly ones that have been requested over time.
     *
     * @return a {@link List} of extra currency codes
     */
    @NonNull
    public static List<String> getNonIso4217CurrencyCodes() {
        final ArrayList<String> nonIso4217Currencies = new ArrayList<>();
        nonIso4217Currencies.add("BSF");
        nonIso4217Currencies.add("BTC"); // Bitcoin
        nonIso4217Currencies.add("BYN"); // New Belarus Currency
        nonIso4217Currencies.add("DOGE"); // Dogecoin
        nonIso4217Currencies.add("DRC");
        nonIso4217Currencies.add("GHS");
        nonIso4217Currencies.add("GST");
        nonIso4217Currencies.add("LTC"); // Litecoin
        nonIso4217Currencies.add("PPC"); // Peercoin
        nonIso4217Currencies.add("SJCX"); // Storjcoin
        nonIso4217Currencies.add("XCP"); // CounterParty
        nonIso4217Currencies.add("XOF");
        nonIso4217Currencies.add("ZMK");
        nonIso4217Currencies.add("ZWD");
        return nonIso4217Currencies;
    }
}
