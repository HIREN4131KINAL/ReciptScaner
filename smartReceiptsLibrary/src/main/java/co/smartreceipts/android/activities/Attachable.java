package co.smartreceipts.android.activities;

import co.smartreceipts.android.model.Attachment;

public interface Attachable {
	
	/**
	 * Returns the attachment that is generated via the main activity
	 * @return
	 */
	Attachment getAttachment();
	
	/**
	 * Stores the main attachment details for later
	 */
	void setAttachment(Attachment attachment);

}
