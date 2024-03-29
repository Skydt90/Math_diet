package com.skydt.weightcontrol.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skydt.weightcontrol.R;
import com.skydt.weightcontrol.models.Diet;
import com.skydt.weightcontrol.services.DayService;
import com.skydt.weightcontrol.services.DietService;
import com.skydt.weightcontrol.services.TextPopupService;

import java.util.Locale;

public class DietCreationActivity extends AppCompatActivity implements View.OnClickListener
{
    private EditText etDietName;
    private EditText etCurrentWeight;
    private EditText etDesiredWeight;
    private EditText etDays;
    private EditText etHeight;
    private ProgressDialog dialog;

    private DietService dietService;
    private DayService dayService;
    private int dietID;
    private boolean welcome;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diet_creation_activity);
        sharedPreferences = getSharedPreferences("menu_values", MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadInterface();
        checkSharedPreferences();
    }

    private void checkSharedPreferences()
    {
        welcome = sharedPreferences.getBoolean("welcome", true);
    }

    private void loadInterface()
    {
        etDietName = findViewById(R.id.etDietName);
        etCurrentWeight = findViewById(R.id.etCurrentWeight);
        etDesiredWeight = findViewById(R.id.etDesiredWeight);
        etDays = findViewById(R.id.etDays);
        etHeight = findViewById(R.id.etHeight);
        Button btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(this);
        dietService = new DietService();
        dayService = new DayService();
    }

    @Override
    public void onClick(View v)
    {
        if (dietService.isEmpty(etDietName))
        {
            dietService.setError(etDietName, "Diætens navn påkrævet");
        }
        else if (dietService.isEmpty(etCurrentWeight))
        {
            dietService.setError(etCurrentWeight, "Nuværende vægt påkrævet");
        }
        else if (dietService.isEmpty(etDesiredWeight))
        {
            dietService.setError(etDesiredWeight, "Ønskede vægt påkrævet");
        }
        else if (dietService.isEmpty(etDays))
        {
            dietService.setError(etDays, "Antal dage påkrævet");
        }
        else if (Integer.parseInt(etDays.getText().toString()) <= 0)
        {
            dietService.setError(etDays, "Skal være over 0");
        }
        else if (dietService.isEmpty(etHeight))
        {
            dietService.setError(etHeight, "Din højde mangler");
        }
        else if (dietService.tooShort(etHeight))
        {
            dietService.setError(etHeight, "Indtast gyldig højde");
        }
        else if (Double.parseDouble(etCurrentWeight.getText().toString()) <= 0)
        {
            dietService.setError(etCurrentWeight, "Må ikke være 0");
        }
        else if (Double.parseDouble(etDesiredWeight.getText().toString()) <= 0)
        {
            dietService.setError(etDesiredWeight, "Må ikke være 0");
        }
        else
        {
            double dailyWeightLoss = dietService.calculateDailyWeightLoss(Double.parseDouble(etCurrentWeight.getText().toString()),
                                                                          Double.parseDouble(etDesiredWeight.getText().toString()),
                                                                          Integer.parseInt(etDays.getText().toString()));
            showAlertDialog(dailyWeightLoss);
        }
    }

    private void showAlertDialog(double dailyWeightLoss)
    {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
        String dwl = String.format(Locale.getDefault(), "%.0f", dailyWeightLoss * 1000);
        AlertDialog alertDialog;

        if (dailyWeightLoss >= 0.150 && dailyWeightLoss < 0.250)
        {
            alertBox.setMessage("Dit daglige vægttab er beregnet til:" +
                                "\n" + dwl + " gram per dag." +
                                "\nDette kan være urealistisk/usundt." +
                                "\nØnsker du alligevel at fortsætte?").setCancelable(false)
                    .setPositiveButton(R.string.ja, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            startDietCreation();
                        }
                    })
                    .setNegativeButton(R.string.nej, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            alertDialog = alertBox.create();
            alertDialog.setTitle("Advarsel!");
        }
        else if (dailyWeightLoss >= 0.250)
        {
            alertBox.setMessage("Dit daglige vægttab er beregnet til:" +
                                "\n" + dwl + " gram per dag." +
                                "\nDette er både urealistisk og usundt." +
                                "\nJuster antallet af dage for at fortsætte.").setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            alertDialog = alertBox.create();
            alertDialog.setTitle("Desværre");
        }
        else
        {
            alertBox.setMessage("Dit daglige vægttab er beregnet til:" +
                                "\n" + dwl + " gram per dag." +
                                "\nØnsker du at oprette diæten?").setCancelable(false)
                    .setPositiveButton(R.string.ja, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            startDietCreation();
                        }
                    })
                    .setNegativeButton(R.string.nej, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
            alertDialog = alertBox.create();
            alertDialog.setTitle("Resultat:");
        }
        alertDialog.show();
    }

    public void hideKeyBoard(View view)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void startDietCreation()
    {
        CreateDiet createDiet = new CreateDiet();
        createDiet.execute();
    }

    private void saveIDInSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("menu_values", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("dietID", dietID);
        editor.apply();
    }

    private void showWelcome()
    {
        TextPopupService popupService = new TextPopupService(findViewById(android.R.id.content), this, sharedPreferences);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        double[] sizes = {displayMetrics.widthPixels * 0.85, displayMetrics.heightPixels * 0.85};

        popupService.showPopupWindow(sizes);
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (welcome)
        {
            showWelcome();
        }
    }

    private class CreateDiet extends AsyncTask<Void, Void, Void>
    {
        private static final String TAG = "CreateDiet";

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            dialog = new ProgressDialog(DietCreationActivity.this);
            dialog.setMessage("Et øjeblik...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            Log.d(TAG, "onPostExecute: Called");

            super.onPostExecute(aVoid);
            saveIDInSharedPreferences();
            Intent intent = new Intent(DietCreationActivity.this, MainMenuActivity.class);
            dialog.dismiss();
            Toast.makeText(DietCreationActivity.this, "Diæt oprettet", Toast.LENGTH_LONG).show();
            startActivity(intent);

            Log.d(TAG, "onPostExecute: finished");
            finish();
        }

        @Override
        protected Void doInBackground(Void... aVoid)
        {
            Log.d(TAG, "doInBackground: Called");
            Diet diet = new Diet(etDietName.getText().toString(),
                    Double.parseDouble(etCurrentWeight.getText().toString()),
                    Double.parseDouble(etDesiredWeight.getText().toString()),
                    Integer.parseInt(etDays.getText().toString()), Double.parseDouble(etHeight.getText().toString()));
            dietID = (int) dietService.postDiet(diet, DietCreationActivity.this);
            diet.setDietID(dietID);

            dayService.createDays(diet, dietService.createDietDates(diet), dietService.calculateDailyWeightLoss(diet), DietCreationActivity.this);
            Log.d(TAG, "doInBackground: finished");
            return null;
        }
    }
}
