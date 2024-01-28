package it.uniba.dib.sms23248.Amministrazione;




public class VideoModel {
    private String videoUrl;

    String name;



    public VideoModel(String videoUrl,String name) {

        this.videoUrl = videoUrl;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getVideoUrl() {
        return videoUrl;
    }


}
