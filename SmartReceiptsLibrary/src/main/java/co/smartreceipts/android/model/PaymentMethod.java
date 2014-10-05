package co.smartreceipts.android.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This object is responsible for encapsulating the behavior of different payment models. It should be 
 * constructed with the {@ling Builder#build()} method.
 * 
 * @author Will Baumann
 */
public class PaymentMethod implements Parcelable {
	
	private final int mId;
	private final String mMethod;
	
	private PaymentMethod(final int id, final String method) {
		mId = id;
		mMethod = method;
	}
	
	private PaymentMethod(final Parcel in) {
		mId = in.readInt();
		mMethod = in.readString();
	}
	
	/**
	 * @return - the database primary key id for this method
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * @return - the actual payment method that the user specified
	 */
	public String getMethod() {
		return mMethod;
	}
	
	@Override
	public String toString() {
		return mMethod;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mId;
		result = prime * result + ((mMethod == null) ? 0 : mMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		PaymentMethod other = (PaymentMethod) obj;
		if (mId != other.mId) {
			return false;
		}
		if (mMethod == null) {
			if (other.mMethod != null) {
				return false;
			}
		} else if (!mMethod.equals(other.mMethod)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeInt(mId);
		out.writeString(mMethod);
	}
	
	public static Parcelable.Creator<PaymentMethod> CREATOR = new Parcelable.Creator<PaymentMethod>() {

		@Override
		public PaymentMethod createFromParcel(Parcel source) {
			return new PaymentMethod(source);
		}

		@Override
		public PaymentMethod[] newArray(int size) {
			return new PaymentMethod[size];
		}

	};

	/**
	 * This static nested class should be used to construct {@link PaymentMethod} objects via the
	 * {@link Builder#build()} method
	 */
	public static final class Builder {
		
		private int _id;
		private String _method;
		
		/**
		 * Default constructor for this class
		 */
		public Builder() {
			
		}
		
		/**
		 * Defines the primary key id for this object
		 * 
		 * @param id - the id
		 * @return this {@link Builder} for method chaining
		 */
		public Builder setId(final int id) {
			_id = id;
			return this;
		}
		
		/**
		 * Defines the payment method type for this object
		 * 
		 * @param method - the payment method
		 * @return this {@link Builder} for method chaining
		 */
		public Builder setMethod(final String method) {
			_method = method;
			return this;
		}
		
		/**
		 * @return - the {@link PaymentMethod} object as set by the setter methods in this class
		 */
		public PaymentMethod build() {
			return new PaymentMethod(_id, _method);
		}
	}

}
