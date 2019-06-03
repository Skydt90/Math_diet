package com.skydt.matematiskvgttab.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.skydt.matematiskvgttab.R;
import com.skydt.matematiskvgttab.models.BodyWeighIn;
import com.skydt.matematiskvgttab.models.Day;
import com.skydt.matematiskvgttab.models.FoodWeighIn;
import com.skydt.matematiskvgttab.services.BodyWeighInService;
import com.skydt.matematiskvgttab.services.DayService;
import com.skydt.matematiskvgttab.services.FoodWeighInService;

public class RegisterWeightActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher
{
    private EditText etNewWeight;
    private RadioGroup rgMealType;
    private Button btnRegister;
    private String btnText;
    private int dietID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_weight_activity);
        extractIntent();
        loadInterface();
    }

    private void extractIntent()
    {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            btnText = bundle.getString("btnText");
            dietID = bundle.getInt("dietID");
        }
        else
        {
            btnText = "";
            dietID = 0;
        }
    }

    private void loadInterface()
    {
        etNewWeight = findViewById(R.id.etNewWeight);
        rgMealType = findViewById(R.id.rbGroup);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);
        if (!btnText.equals("Mad"))
        {
            rgMealType.setAlpha(0f);
            etNewWeight.setHint("Nuværende vægt");
        }
        etNewWeight.addTextChangedListener(this);
        btnRegister.setClickable(false);
        btnRegister.setAlpha(0.2f);
    }

    @Override
    public void onClick(View v)
    {
        DayService dayService = new DayService();
        Day day = dayService.loadDayByPrimaryKey(dayService.getCurrentDateAsString(), dietID, this);
        Intent intent = new Intent(this, MainMenuActivity.class);
        BodyWeighInService bodyWeighInService = new BodyWeighInService();
        BodyWeighIn bodyWeighIn;
        double enteredWeight;

        switch (btnText)
        {
            case "Morgenvægt":
                day.setMorningWeight(Double.parseDouble(etNewWeight.getText().toString()));
                dayService.updateMorningWeight(day, day.getMorningWeight(), this);
                enteredWeight = Double.parseDouble(etNewWeight.getText().toString());
                bodyWeighIn = new BodyWeighIn(day.getDayID(), day.getDietID(), enteredWeight);
                bodyWeighInService.createWeighIn(bodyWeighIn, this);
                break;

            case "Krop":
                enteredWeight = Double.parseDouble(etNewWeight.getText().toString());
                bodyWeighIn = new BodyWeighIn(day.getDayID(), day.getDietID(), enteredWeight);
                bodyWeighInService.createWeighIn(bodyWeighIn, this);
                dayService.updateAllowedFoodIntakeBasedOnBodyWeighIn(day, enteredWeight, this);
                break;

            case "Mad":
                FoodWeighInService foodWeighInService = new FoodWeighInService();
                enteredWeight = Double.parseDouble(etNewWeight.getText().toString());
                int selectedId = rgMealType.getCheckedRadioButtonId();
                RadioButton rbSelectedMeal = findViewById(selectedId);
                FoodWeighIn foodWeighIn = new FoodWeighIn(day.getDayID(), day.getDietID(), enteredWeight, rbSelectedMeal.getText().toString());
                foodWeighInService.createWeighIn(foodWeighIn, this);
                dayService.updateAllowedFoodIntakeBasedOnFoodWeighIn(day, foodWeighIn.getFoodWeighIn(), this);
                break;

            default:
                break;
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        btnRegister.setClickable(true);
        btnRegister.setAlpha(1);
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        if(s.toString().trim().length()==0)
        {
            btnRegister.setClickable(false);
            btnRegister.setAlpha(0.2f);
        }
    }
}
