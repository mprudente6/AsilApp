package it.uniba.dib.sms23248.Amministrazione;



public class UploadedFile {
    private String fileName;
    private String fileUrl;

    public UploadedFile() {
    }

    public UploadedFile(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
