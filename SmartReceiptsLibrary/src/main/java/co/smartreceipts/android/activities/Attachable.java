package co.smartreceipts.android.activities;

import co.smartreceipts.android.model.Attachment;

public interface Attachable {
	
	/**
	 * Returns the attachment that is generated via the main activity
	 * @return
	 */
	public Attachment getAttachment();
	
	/**
	 * Stores the main attachment details for later
	 */
	public void setAttachment(Attachment attachment);

}
