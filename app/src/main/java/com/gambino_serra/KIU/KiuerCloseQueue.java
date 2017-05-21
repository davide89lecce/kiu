package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa alla chiusura della coda (lato Kiuer)
 */
public class KiuerCloseQueue extends DialogFragment {

    //private DatabaseReference mDatabase;
    TextView title;
    Context context;

    public KiuerCloseQueue() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //mDatabase = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/");
        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        context = getActivity();
        title = new TextView(context);
        title.setText(R.string.kiuer_close_kiu_title);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25F);
        builder.setCustomTitle(title);

        builder.setView(inflater.inflate(R.layout.fragment_kiuer_close_queue, null))

                //Memorizza sul DB la chiusura della coda da parte del Kiuer
                .setPositiveButton(R.string.chiudi_coda, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        final RatingBar rating = (RatingBar) getDialog().findViewById(R.id.rating_kiuer);

                        String url = "http://www.kiu.altervista.org/chiudi_coda_kiuer.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                //Se il DB risponde con close_conversation viene avviata la chiusura della conversazione
                                if (response.equals("close_conversation")) {

                                    closeChat();
                                    Log.d("volley","close_conversation");

                                }else if(response.equals("error_update_code_effettuate")){

                                    Log.d("volley","error_update_code_effettuate");

                                }else if(response.equals("error_update_coda_completata")){

                                    Log.d("volley","error_update_coda_completata");

                                }else if(response.equals("close_queue")){

                                    Log.d("volley","close_queue");
                                }
                            }
                        },((KiuerHomeActivity) getActivity())) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("rating", String.valueOf(Float.toString(rating.getRating())));
                                params.put("id_richiesta", bundle.get("id").toString());
                                params.put("email_helper", bundle.get("email_helper").toString());
                                params.put("email_kiuer", bundle.get("email_kiuer").toString());

                                return params;
                            }
                        };
                        MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

                        Toast.makeText(getActivity().getApplicationContext(), R.string.queue_closed, Toast.LENGTH_SHORT).show();

                        dialog.dismiss(); // dismette positivo o neutrale
                        ((KiuerHomeActivity) getActivity()).onResume();
                    }
                })
                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel(); // dismette con rifiuto
                    }
                });
        return builder.create();
    }

    /**
     * Il metodo chiude la conversazione con l'Helper.
     */
    private void closeChat(){
        Bundle bundle = getArguments();
        String my_uid = bundle.get("uid_helper").toString();
        String other_uid = bundle.get("uid_kiuer").toString();
       // mDatabase.child(my_uid).child(other_uid).removeValue();
       // mDatabase.child(other_uid).child(my_uid).removeValue();
       // DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("/chat/" + other_uid + my_uid);
       // mRef.removeValue();
    }


    /**
     *  Il metodo inizializza la Dialog con i dati ricevuti dal Bundle.
     */
    @Override
    public void onStart(){
        super.onStart();
        Bundle bundle = getArguments();
        TextView text = (TextView) this.getDialog().findViewById(R.id.dettagli2);
        TextView nome = (TextView) this.getDialog().findViewById(R.id.text_nome_1h);
        nome.setText(bundle.get("nome").toString());
        text.setText(getResources().getString(R.string.hour_start_queue) + "  " + bundle.get("orario_inizio_coda").toString().substring(11,16) + "\n\n" + getResources().getString(R.string.hour_end_queue) + "  " + bundle.get("orario_fine_coda").toString().substring(11,16) + "\n\n"
                + getResources().getString(R.string.time_queue)  + "  " + calcoloOre(bundle.get("orario_inizio_coda").toString(),bundle.get("orario_fine_coda").toString()) + "\n\n"
                + getResources().getString(R.string.payment) + "  " + calcoloCompenso(bundle.get("orario_inizio_coda").toString(),bundle.get("orario_fine_coda").toString(),bundle.getInt("tariffa_oraria")) + "€\n");
    }

    /**
     * Il metodo calcola il la durata della coda (ore:minuti)
     *
     * @param orarioInizio
     * @param orarioFine
     * @return  tempo durata della coda.
     */
    private String calcoloOre(String orarioInizio, String orarioFine) {
        String ore = "";
        String minuti = "";
        String tempo = "00:00";
        try {

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fmt.setLenient(false);

            // Parse delle due stringhe.
            Date d1 = fmt.parse(orarioInizio);
            Date d2 = fmt.parse(orarioFine);

            // Calcola la differenza in millisecondi.
            long millisDiff = d2.getTime() - d1.getTime();

            // Calcola giorni/ore/minuti/secondi.
            int seconds = (int) (millisDiff / 1000 % 60);
            int minutes = (int) (millisDiff / 60000 % 60);
            int hours = (int) (millisDiff / 3600000 % 24);
            int days = (int) (millisDiff / 86400000);

            if (hours >= 10)
                ore = String.valueOf(hours);
            else
                ore = "0" + String.valueOf(hours);

            if (minutes >= 10)
                minuti = String.valueOf(minutes);
            else
                minuti = "0" + String.valueOf(minutes);

            tempo = ore + ":" + minuti;

        } catch (Exception e) {
            System.err.println(e);
        }

        return tempo;
    }

    /**
     * Il metodo calcola il compenso in base alla durata della coda e la tariffa.
     *
     * @param orarioInizio
     * @param orarioFine
     * @param tariffa
     * @return compenso
     */
    private String calcoloCompenso(String orarioInizio, String orarioFine, int tariffa) {

        double compenso = 0.0;
        DecimalFormat df2 = new DecimalFormat("##.##");
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fmt.setLenient(false);

            // Parse delle due stringhe.
            Date d1 = fmt.parse(orarioInizio);
            Date d2 = fmt.parse(orarioFine);

            // Calcola la differenza in millisecondi.
            long millisDiff = d2.getTime() - d1.getTime();

            int minutes = (int) (millisDiff / 60000);

            compenso = (Double.valueOf(tariffa) / 60) * minutes;

        } catch (Exception e) {
            System.err.println(e);
        }

        return String.valueOf(df2.format(compenso));
    }
}


