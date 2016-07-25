package co.smartreceipts.android.imports;

import java.util.Arrays;
import java.util.List;

public class RequestCodes {

    public static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
    public static final int ADD_PHOTO_CAMERA_REQUEST = 2;
    public static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    public static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;
    public static final int IMPORT_GALLERY_IMAGE = 5;
    public static final int IMPORT_GALLERY_PDF = 6;

    public static final List<Integer> PHOTO_REQUESTS = Arrays.asList(NEW_RECEIPT_CAMERA_REQUEST, ADD_PHOTO_CAMERA_REQUEST,
            NATIVE_NEW_RECEIPT_CAMERA_REQUEST, NATIVE_ADD_PHOTO_CAMERA_REQUEST, IMPORT_GALLERY_IMAGE);
    public static final List<Integer> PDF_REQUESTS = Arrays.asList(IMPORT_GALLERY_PDF);

    private RequestCodes() { }
}
