package wb.receiptslibrary.model;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;

import wb.receiptslibrary.model.TripRow.Builder;
import wb.receiptslibrary.persistence.DatabaseHelper;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ReceiptRow implements Parcelable{

	private static final String EMPTY_PRICE = "0.00";
	
	private final int mId;
	private File mImg;
	private String mName, mCategory, mComment, mPrice, mTax;
	private String mExtraEditText1, mExtraEditText2, mExtraEditText3;
	private Date mDate;
	private boolean mIsExpensable, mIsFullPage;
	private WBCurrency mCurrency;
	private DecimalFormat mDecimalFormat;
	private SourceEnum mSource;
	
	private ReceiptRow(int id) {
		mId = id;
	}
	
	private ReceiptRow(Parcel in) {
		mId = in.readInt();
		mName = in.readString();
		mCategory = in.readString();
		mComment = in.readString();
		mPrice = in.readString();
		mTax = in.readString();
		mImg = new File(in.readString());
		mDate = new Date(in.readLong());
		mCurrency = WBCurrency.getInstance(in.readString());
		mIsExpensable = (in.readByte() != 0);
		mIsFullPage = (in.readByte() != 0);
		mExtraEditText1 = in.readString();
		mExtraEditText2 = in.readString();
		mExtraEditText3 = in.readString();
		mSource = SourceEnum.Parcel;
	}
	
	public int getId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public File getImage() {
		return mImg;
	}
	
	public String getImagePath() {
		if (hasImage()) {
			return getImage().getAbsolutePath();
		}
		else {
			return "";
		}
	}
	
	public boolean hasImage() {
		return (getImage() != null && getImage().exists());
	}
	
	public String getSource() {
		return mSource.name();
	}

	public String getCategory() {
		return mCategory;
	}

	public String getComment() {
		return mComment;
	}

	public String getPrice() {
		return mPrice;
	}
	
	public float getPriceAsFloat() {
		try {
			return Float.valueOf(getPrice());
		}
		catch(NumberFormatException e) {
			return 0f;
		}
	}
	
	public String getDecimalFormattedPrice() { 
		return getDecimalFormat().format(getPriceAsFloat());
	}
	
	public String getCurrencyFormattedPrice() {
		if (getCurrency() != null) {
			return getCurrency().format(getPrice());
		}
		else {
			return getDecimalFormattedPrice();
		}
	}
	
	public boolean isPriceEmpty() {
		return TextUtils.isEmpty(getPrice());
	}

	public String getTax() {
		return mTax;
	}
	
	public float getTaxAsFloat() {
		try {
			return Float.valueOf(getTax());
		}
		catch(NumberFormatException e) {
			return 0f;
		}
	}
	
	public String getDecimalFormattedTax() { 
		return getDecimalFormat().format(getTaxAsFloat());
	}
	
	public String getCurrencyFormattedTax() {
		if (getCurrency() != null) {
			return getCurrency().format(getTax());
		}
		else {
			return getDecimalFormattedTax();
		}
	}

	public String getExtraEditText1() {
		return mExtraEditText1;
	}

	public String getExtraEditText2() {
		return mExtraEditText2;
	}

	public String getExtraEditText3() {
		return mExtraEditText3;
	}

	public Date getDate() {
		return mDate;
	}
	
	public String getFormattedDate(Context context) {
		return android.text.format.DateFormat.getDateFormat(context).format(getDate());
	}

	public boolean isExpensable() {
		return mIsExpensable;
	}

	public boolean isFullPage() {
		return mIsFullPage;
	}

	public WBCurrency getCurrency() {
		return mCurrency;
	}
	
	public String getCurrencyCode() {
		return getCurrency().getCurrencyCode();
	}

	void setName(String name) {
		mName = name;
	}
	
	void setCategory(String category) {
		mCategory = category;
	}
	
	void setComment(String comment) {
		mComment = comment;
	}
	
	void setDate(Date date) {
		mDate = date;
	}
	
	public void setImage(File img) {
		mImg = img;
	}
	
	void setCurrency(WBCurrency currency) {
		mCurrency = currency;
	}
	
	void setPrice(String price) {
		if (TextUtils.isEmpty(price))
			mPrice = EMPTY_PRICE;
		else
			mPrice = price;
	}
	
	void setTax(String tax) {
		if (TextUtils.isEmpty(tax))
			mTax = EMPTY_PRICE;
		else
			mTax = tax;
	}
	
	void setIsExpenseable(boolean isExpenseable) {
		mIsExpensable = isExpenseable;
	}
	
	void setIsFullPage(boolean isFullPage) {
		mIsFullPage = isFullPage; 
	}
	
	void setSource(SourceEnum source) {
		mSource = source;
	}
	
	public boolean hasExtraEditText1() {
		return (mExtraEditText1 != null);
	}
	
	void setExtraEditText1(String extraEditText1) {
		if (!TextUtils.isEmpty(extraEditText1)) 
			mExtraEditText1 = extraEditText1.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extraEditText1;
		else 
			mExtraEditText1 = null;
	}
	
	public boolean hasExtraEditText2() {
		return (mExtraEditText2 != null);
	}
	
	void setExtraEditText2(String extraEditText2) {
		if (!TextUtils.isEmpty(extraEditText2)) 
			mExtraEditText2 = extraEditText2.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extraEditText2;
		else 
			mExtraEditText2 = null;
	}
	
	public boolean hasExtraEditText3() {
		return (mExtraEditText3 != null);
	}
	
	void setExtraEditText3(String extraEditText3) {
		if (!TextUtils.isEmpty(extraEditText3)) 
			mExtraEditText3 = extraEditText3.equalsIgnoreCase(DatabaseHelper.NO_DATA) ? null : extraEditText3;
		else 
			mExtraEditText3 = null;
	}
	
	private DecimalFormat getDecimalFormat() {
		if (mDecimalFormat == null) {
			mDecimalFormat = new DecimalFormat();
			mDecimalFormat.setMaximumFractionDigits(2);
			mDecimalFormat.setMinimumFractionDigits(2);
			mDecimalFormat.setGroupingUsed(false);
		}
		return mDecimalFormat;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "::\n" + "["
											   + "source => " + getSource() + "; \n"
											   + "id => " + getId() + "; \n"
											   + "name => " + getName() + "; \n"
											   + "category =>" + getCategory() + "; \n"
											   + "comment =>" + getComment() + "; \n"
											   + "price =>" + getPrice() + "; \n"
											   + "tax =>" + getTax() + "; \n"
											   + "imgPath =>" + getImagePath() + "; \n"
											   + "currency =>" + getCurrencyCode() + "; \n"
											   + "date =>" + getDate().toGMTString() + "; \n"
											   + "isExpensable =>" + isExpensable() + "; \n"
											   + "isFullPage =>" + isFullPage() + "; \n"
											   + "extraEditText1 =>" + getExtraEditText1() + "; \n"
											   + "extraEditText2 =>" + getExtraEditText2() + "; \n"
											   + "extraEditText3 =>" + getExtraEditText3() + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReceiptRow other = (ReceiptRow) obj;
		if (mId != other.mId)
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getId());
		dest.writeString(getName());
		dest.writeString(getCategory());
		dest.writeString(getComment());
		dest.writeString(getPrice());
		dest.writeString(getTax());
		dest.writeString(getImagePath());
		dest.writeLong(getDate().getTime());
		dest.writeString(getCurrencyCode());
		dest.writeByte((byte) (isExpensable() ? 1 : 0));
		dest.writeByte((byte) (isFullPage() ? 1 : 0));
		dest.writeString(getExtraEditText1());
		dest.writeString(getExtraEditText2());
		dest.writeString(getExtraEditText3());
	}
	
	public static final class Builder {
		
		public File _img;
		public String _name, _category, _comment, _price, _tax;
		public String _extraEditText1, _extraEditText2, _extraEditText3;
		public Date _date;
		public int _id;
		public boolean _isExpenseable, _isFullPage;
		public WBCurrency _currency;
		public SourceEnum _source;
		
		public Builder(int id) {
			_id = id;
			_source = SourceEnum.Undefined;
		}
		
		public Builder setName(String name) {
			_name = name;
			return this;
		}
		
		public Builder setCategory(String category) {
			_category = category;
			return this;
		}
		
		public Builder setComment(String comment) {
			_comment = comment;
			return this;
		}
		
		public Builder setPrice(String price) {
			_price = price;
			return this;
		}
		
		public Builder setTax(String tax) {
			_tax = tax;
			return this;
		}
		
		public Builder setImage(File image) {
			_img = image;
			return this;
		}
		
		public Builder setDate(Date date) {
			_date = date;
			return this;
		}
		
		public Builder setDate(long datetime) {
			_date = new Date(datetime);
			return this;
		}
		
		public Builder setIsExpenseable(boolean isExpenseable) {
			_isExpenseable = isExpenseable;
			return this;
		}
		
		public Builder setIsFullPage(boolean isFullPage) {
			_isFullPage = isFullPage; 
			return this;
		}
		
		public Builder setCurrency(WBCurrency currency) {
			_currency = currency;
			return this;
		}
		
		public Builder setCurrency(String currencyCode) {
			_currency = WBCurrency.getInstance(currencyCode);
			return this;
		}
		
		public Builder setExtraEditText1(String extraEditText1) {
			_extraEditText1 = extraEditText1;
			return this;
		}
		
		public Builder setExtraEditText2(String extraEditText2) {
			_extraEditText2 = extraEditText2;
			return this;
		}
		
		public Builder setExtraEditText3(String extraEditText3) {
			_extraEditText3 = extraEditText3;
			return this;
		}
		
		//TODO: Use this method
		public Builder setSourceAsCache() {
			_source = SourceEnum.Cache;
			return this;
		}
		
		public ReceiptRow build() {
			ReceiptRow receipt = new ReceiptRow(_id);
			receipt.setName(_name);
			receipt.setCategory(_category);
			receipt.setComment(_comment);
			receipt.setPrice(_price);
			receipt.setTax(_tax);
			receipt.setImage(_img);
			receipt.setExtraEditText1(_extraEditText1);
			receipt.setExtraEditText2(_extraEditText2);
			receipt.setExtraEditText3(_extraEditText3);
			receipt.setDate(_date);
			receipt.setIsExpenseable(_isExpenseable);
			receipt.setIsFullPage(_isFullPage);
			receipt.setCurrency(_currency);
			receipt.setSource(_source);
			return receipt;
		}
	}
	
	public static Parcelable.Creator<ReceiptRow> CREATOR = new Parcelable.Creator<ReceiptRow>() {

		@Override
		public ReceiptRow createFromParcel(Parcel source) {
			return new ReceiptRow(source);
		}

		@Override
		public ReceiptRow[] newArray(int size) {
			return new ReceiptRow[size];
		}
		
	};
	
}