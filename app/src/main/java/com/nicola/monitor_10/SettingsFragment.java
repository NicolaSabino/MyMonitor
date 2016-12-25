package com.nicola.monitor_10;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by nicola on 11/12/16.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        Preference button = (Preference)findPreference(getString(R.string.bottone));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {


                // quando questa Preference viene cliccata faccio partire un alert
                new AlertDialog.Builder(getActivity())
                        .setTitle("Elimina Valori")
                        .setMessage("Sei sicuro di voler eliminare i dati salvati nel database?")
                        .setIcon(R.drawable.db_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                DbManager db  = new DbManager(getActivity());
                                db.deleteTables();
                                Toast.makeText(getActivity(), "valori cancellati correttamente", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });

        Preference bottone = (Preference)findPreference("bottoneCondividi");
        bottone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DbManager dbManager = new DbManager(getActivity());
                dbManager.sharing();
                return true;
            }
        });

        Preference bottone2 = (Preference)findPreference("bottoneCondividi2");
        bottone2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DbManager dbManager = new DbManager(getActivity());
                dbManager.sharing2();
                return true;
            }
        });

    }


}