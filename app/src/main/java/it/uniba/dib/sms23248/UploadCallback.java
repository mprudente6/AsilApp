package it.uniba.dib.sms23248;

public interface UploadCallback {
    void onUploadSuccess(String fileName, String downloadUrl);

    void onUploadFailure(String fileName, Exception exception);
}
