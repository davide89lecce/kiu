package com.gambino_serra.KIU;

import android.app.DialogFragment;
import android.app.NotificationManager;
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
import com.kosalgeek.android.json.JsonConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe gestisce l'Activity relativa alle richieste di coda pendenti ricevute dall'Helper
 */
public class NotificationHelper extends AppCompatActivity implements Response.Listener<String> {

    final String TAG = this.getClass().getSimpleName();
    final private static String MY_PREFERENCES = "kiuPreferences";
    final private static String EMAIL = "email";
    private static final String LOGGED_USER = "logged_user";

    ListView lvProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //Elimina le notifiche relative alle richieste di coda ricevute dall'Helper
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(001);

        //Lettura dal database di altervista tramite Volley delle richieste di coda pendenti
        updateRichieste();

    }

    /**
     * Metodo invocato da Volley alla ricezione della risposta.
     * Si occupa di convertire il JSON ricevuto e di valorizzare la ListView e le rispettive Dialog
     */
    @Override
    public void onResponse(String response) {
        Log.d(TAG, response);
        final Bundle bundle = new Bundle();
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);
        final BindDictionary<JsonCheckRichiesta> dictionary = new BindDictionary<>();
        dictionary.addStringField(R.id.tvText, new StringExtractor<JsonCheckRichiesta>() {
            @Override
            public String getStringValue(JsonCheckRichiesta product, int position) {
                return getResources().getString(R.string.hour_queue) + " " + product.orario.toString().substring(0, 5) + "\n" + getResources().getString(R.string.kiuer2) + product.nome.toString() + "\n";

            }
        }).onClick(new ItemClickListener<JsonCheckRichiesta>() {

            @Override
            public void onClick(JsonCheckRichiesta item, int position, View view) {
                DialogFragment newFragment = new NotificationDetailsHelper();
                bundle.putString("id", productList.get(position).ID_richiesta.toString());
                bundle.putString("text", productList.get(position).nome + " " + getResources().getString(R.string.text_request_queue) + "  " + productList.get(position).orario.substring(0, 5) + " " + getResources().getString(R.string.text_request_queue2) + " " + productList.get(position).luogo);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "NotificationDetailsHelper");
            }
        });

        FunDapter<JsonCheckRichiesta> adapter = new FunDapter<>(getApplicationContext(), productList, R.layout.notification_layout_helper, dictionary);
        lvProduct.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRichieste();
    }

    /**
     * Il metodo esegue la richiesta di lettura dei dati presenti nel databse altervista relativi alle richieste di coda pendenti per l'aggiornamento della ListView
     */
    public void updateRichieste() {
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String url = "http://www.kiu.altervista.org/check_richiesta.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.err_read_google, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", prefs.getString(EMAIL, "").toString());
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        lvProduct = (ListView) findViewById(R.id.lvProduct);

    }

    /**
     * Il metodo crea il menù sull'ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.helper_notification_menu, menu);
        return true;
    }

    /**
     * Il metodo gestisce il menù sull'ActionBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent in;
        boolean check = false;
        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.chat_helper:
                in = new Intent(getApplicationContext(), ConversationsActivity.class);
                startActivity(in);
                check = true;
                break;
            case R.id.exit_helper:
                SharedPreferences.Editor editor;
                editor = prefs.edit().clear();
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                in = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(in);
                check = true;
                break;
            default:
                check = super.onOptionsItemSelected(item);
        }
        return check;
    }
}