package wb.android.fkexstorage;

public class SDCardStateException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public final String state;
	
	SDCardStateException(final String state) {
		super();
		this.state = state;
	}
	
	@Override
	public final String toString() {
		return "The current state of the SD card is not valid: " + state;
	}

}
