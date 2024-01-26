package it.uniba.dib.sms23248.SpeseRichiedente;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    public static long convertDateToTimestamp(String dateString) {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            //converte in oggetto Date
            Date date = dateFormat.parse(dateString);

            // Ottiene la data in millisecondi
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();

            return -1;
        }
    }
}
