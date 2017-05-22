package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.ConversationsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kosalgeek.android.json.JsonConverter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella l'interazione dell'utente Helper con l'applicazione.
 */
public class HelperHomeActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener {

    final private static String MY_PREFERENCES = "kiuPreferences";
    private static final String ID = "IDutente";
    final String TAG = this.getClass().getSimpleName();
    TextView disponibilita;
    Switch switchDisponibilita;
    ListView lvProduct;

   // private FirebaseAuth mAuth;
    //private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_home);

      //  mAuth = FirebaseAuth.getInstance();
       // mAuthListener = new FirebaseAuth.AuthStateListener() {
        /*
            @Override
            public void onAuthStateChanged(@NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent in = new Intent(HelperHomeActivity.this, MainActivity.class);
                    startActivity(in);
                }
            }
        };
        */
        disponibilita = (TextView) findViewById(R.id.disponibilita);
        switchDisponibilita = (Switch) findViewById(R.id.switchDisponibilita);
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        //Lettura con volley dell'attributo disponibilità helper per settare lo switch presente nel layout
        String url = "http://www.davideantonio2.altervista.org/read_disponibilita_helper.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("disponibile")) {
                    switchDisponibilita.setChecked(true);
                    disponibilita.setText(getResources().getString(R.string.visible_by_kiuer));
                } else {
                    switchDisponibilita.setChecked(false);
                    disponibilita.setText(getResources().getString(R.string.invisible_by_kiuer));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(ID, "").toString());
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        //Gestione dello switch disponibilita' per settare la disponibilita' dell'helper nel DB
        switchDisponibilita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDisponibilita.isChecked()) {
                    //Aggiornamento dell'attributo disponibilita' helper del DB su "Disponibile"
                    String url = "http://www.davideantonio2.altervista.org/set_disponibilita_helper.php";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Se l'aggiornamento è andato a buon fine setto text altrimenti disabilito switch
                            if (response.equals("ok")) {
                                disponibilita.setText(getResources().getString(R.string.visible_by_kiuer));
                            } else {
                                switchDisponibilita.setChecked(false);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
                            switchDisponibilita.setChecked(false);
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("IDutente", prefs.getString(ID, "").toString());
                            params.put("disponibile", "1");
                            return params;
                        }
                    };
                    MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                } else {
                    //Aggiornamento dell'attributo disponibilità helper del DB su "Non Disponibile"
                    String url = "http://www.davideantonio2.altervista.org/set_disponibilita_helper.php";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Se aggiornamento è andato a buon fine setto text altrimenti abilito switch
                            if (response.equals("ok")) {
                                disponibilita.setText(getResources().getString(R.string.invisible_by_kiuer));
                            } else {
                                switchDisponibilita.setChecked(true);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
                            switchDisponibilita.setChecked(true);
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("IDutente", prefs.getString(ID, "").toString());
                            params.put("disponibile", "0");
                            return params;
                        }
                    };
                    MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                }
            }
        });

        //Lettura dal DB tramite Volley delle Code assegnate all'helper
        updateRichieste();

        //avvio il servizio per la ricezione delle notifiche e dei messaggi
        startService();
    }

    /**
     * Il metodo crea il menù sull'Action Bar
     *
     * @param menu
     * @return booleano
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.helper_menu, menu);
        return true;
    }


    /**
     * Il metodo Gestisce il menù sull'Action Bar
     *
     * @param item
     * @return booleano
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in;
        boolean check = false;
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.home_helper:
                in = new Intent(getApplicationContext(), HelperHomeActivity.class);
                startActivity(in);
                check = true;
                break;
            case R.id.chat_helper:
                in = new Intent(getApplicationContext(), ConversationsActivity.class);
                startActivity(in);
                check = true;
                break;
            case R.id.notification_helper:
                in = new Intent(getApplicationContext(), NotificationHelper.class);
                startActivity(in);
                check = true;
                break;
            case R.id.setting:
                in = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(in);
                check = true;
                break;
            case R.id.exit_helper:
                SharedPreferences.Editor editor;
                editor = prefs.edit().clear();
                editor.apply();
                //FirebaseAuth.getInstance().signOut();
                stopService();
                in = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(in);
                check = true;
                break;
            default:
                check = super.onOptionsItemSelected(item);
        }

        return check;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRichieste();
    }

    /**
     *  Il metodo esegue la richiesta di lettura dei dati presenti nel DB remoto relativi alle Code assegate all'helper per l'aggiornamento della List View.
     */
    public void updateRichieste() {

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.davideantonio2.altervista.org/code_helper.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("IDutente", prefs.getString(ID, "").toString());
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        lvProduct = (ListView) findViewById(R.id.lvProduct);
    }

    /**
     * Il metodo e' invocato alla risposta (dati ricevuti da database altervista) della richiesta di invio della informazioni.
     * Si occupa di convertire il JSON ricevuto e di valorizzare la ListView e le rispettive Dialog.
     *
     * @param response
     */
    @Override
    public void onResponse(String response) {

        final Bundle bundle = new Bundle(); //permette di passare i parametri tra activity
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);
        final BindDictionary<JsonCheckRichiesta> dictionary = new BindDictionary<>();

        dictionary.addStringField(R.id.tvText, new StringExtractor<JsonCheckRichiesta>() { //crea la lista con tutti i dati
            @Override
            public String getStringValue(JsonCheckRichiesta product, int position) {
                String statoCoda = "";
                if (product.stato_coda.toString().equals("0")) {
                    statoCoda = getResources().getString(R.string.queue_not_started);
                } else if (product.stato_coda.toString().equals("1")) {
                    statoCoda = getResources().getString(R.string.queue_in_progress);
                } else if (product.stato_coda.toString().equals("2")) {
                    statoCoda = getResources().getString(R.string.queue_terminated);
                }
                return getResources().getString(R.string.hour_queue) + "  " + product.orario.toString().substring(0, 5) + "\n" + getResources().getString(R.string.kiuer2) + "  " + product.nome.toString() + "\n" + getResources().getString(R.string.state) + "  " + statoCoda.toString() + "\n";
            }
        }).onClick(new ItemClickListener<JsonCheckRichiesta>() { //rende la listview cliccabile
            @Override
            public void onClick(JsonCheckRichiesta item, int position, View view) {
                String statoCoda = "";
                if (productList.get(position).stato_coda.toString().equals("0")) {
                    statoCoda = getResources().getString(R.string.queue_not_started);
                } else if (productList.get(position).stato_coda.toString().equals("1")) {
                    statoCoda = getResources().getString(R.string.queue_in_progress);
                } else if (productList.get(position).stato_coda.toString().equals("2")) {
                    statoCoda = getResources().getString(R.string.queue_terminated);
                }
                DialogFragment newFragment = new KiuerDetails(); //dialog che appare al click sulla lista
                bundle.putString("ID", productList.get(position).ID_richiesta.toString());
                bundle.putString("text", getResources().getString(R.string.queue_start_at) + " " + productList.get(position).orario.substring(0, 5) + "\n\n" + getResources().getString(R.string.place) + "  " + productList.get(position).luogo + "\n\n" + "Descrizione" + "  " + productList.get(position).descrizione + "\n\n" + getResources().getString(R.string.state) + "  " + productList.get(position).stato_coda.toString() + "\n");
                bundle.putString("nome", productList.get(position).nome);
                bundle.putString("latitudine", productList.get(position).pos_latitudine);
                bundle.putString("longitudine", productList.get(position).pos_longitudine);
                bundle.putString("stato_coda", statoCoda.toString());
                bundle.putString("orario_inizio", productList.get(position).orario_inizio.toString());
                bundle.putString("orario_fine", productList.get(position).orario_fine.toString());
                bundle.putString("ID_helper", productList.get(position).ID_helper.toString());
                bundle.putString("ID_kiuer", productList.get(position).ID_kiuer.toString());
                bundle.putInt("tariffa_oraria", productList.get(position).tariffa_oraria);
                newFragment.setArguments(bundle); //passa i parametri al fragment
                newFragment.show(getFragmentManager(), "KiuerDetails"); //avvia il fragment
            }
        });

        FunDapter<JsonCheckRichiesta> adapter = new FunDapter<>(getApplicationContext(), productList, R.layout.product_layout, dictionary);
        lvProduct.setAdapter(adapter);
    }

    /**
     * Il metodo viene invocato in caso di problemi nella ricezione della risposta.
     *
     * @param error
     */
    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getApplicationContext(), R.string.error_update_volley, Toast.LENGTH_SHORT).show();
    }

    /**
     * Il metodo avvia il servizio per la gestione delle notifiche e messaggi
     */
    public void startService() {
        startService(new Intent(this, Service_check.class));
    }

    /**
     *  Il metodo permette di mettere in Stop il servizio per la gestione delle notifiche e dei messaggi (eseguito al logout).
     */
    public void stopService() {
        stopService(new Intent(this, Service_check.class));
    }

}