package it.uniba.dib.sms23248.SpeseRichiedente;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemSpese {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String nome;
    private String tipo;
    private Double prezzo;
    private String idProdotto;
    private String data;

    public ItemSpese() {

    }

    public ItemSpese(String data, String idProdotto, String nome, Double prezzo, String tipo) {
        this.data = data;
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.prezzo = prezzo;
        this.tipo = tipo;
    }

    public String getData() {
        return data;
    }

    public String getIdProdotto() {
        return idProdotto;
    }

    public String getNome() {
        return nome;
    }

    public Double getPrezzo() {
        return prezzo;
    }

    public String getTipo() {
        return tipo;
    }

    public static String getCurrentDate() {

        long currentDateMillis = System.currentTimeMillis();
        return dateFormat.format(new Date(currentDateMillis));
    }
}