package com.skydt.weightcontrol.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.skydt.weightcontrol.R;
import com.skydt.weightcontrol.models.Day;
import com.skydt.weightcontrol.models.Diet;
import com.skydt.weightcontrol.services.BodyWeighInService;
import com.skydt.weightcontrol.services.ChartService;
import com.skydt.weightcontrol.services.DayService;
import com.skydt.weightcontrol.services.DietService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button btnGoToDay;
    private Button btnBodyWeighIn;
    private Button btnFood;
    private TextView tvCurrentDiet;
    private TextView tvDate;
    private TextView tvGoalWeight;
    private TextView tvMorningWeight;
    private TextView tvAllowedFood;
    private TextView tvBMI;
    private LineChart lineChart;

    private Intent intent;
    private int dietID;
    private Day day;
    private Diet diet;
    private DietService dietService;
    private DayService dayService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadInterface();
        setupInterfaceBasedOnSharedPreferences();
    }

    private void loadInterface()
    {
        Button btnGoToDiet = findViewById(R.id.btnGoToDiet);
        btnGoToDiet.setOnClickListener(this);
        btnGoToDay = findViewById(R.id.btnGoToDay);
        btnGoToDay.setOnClickListener(this);
        btnBodyWeighIn = findViewById(R.id.btnBodyWeighIn);
        btnBodyWeighIn.setOnClickListener(this);
        btnFood = findViewById(R.id.btnFood);
        btnFood.setOnClickListener(this);

        tvCurrentDiet = findViewById(R.id.tvCurrentDiet);
        tvDate = findViewById(R.id.tvDate);
        tvGoalWeight = findViewById(R.id.tvGoalWeight);
        tvMorningWeight = findViewById(R.id.tvMorningWeight);
        tvAllowedFood = findViewById(R.id.tvAllowedFood);
        tvBMI = findViewById(R.id.tvBMI);

        lineChart = findViewById(R.id.linechart);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void setupInterfaceBasedOnSharedPreferences()
    {
        checkSharedPreferences();
        if (dietID == 0)
        {
            intent = new Intent(this, DietCreationActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            populateInterface();
        }
    }

    private void checkSharedPreferences()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("menu_values", MODE_PRIVATE);
        dietID = sharedPreferences.getInt("dietID", 0);
    }

    private void populateInterface()
    {
        dayService = new DayService();
        dietService = new DietService();
        BodyWeighInService bodyWeighInService = new BodyWeighInService();
        String currentDate = dayService.getCurrentDateAsString();

        diet = dietService.loadDietByID(dietID, this);
        diet.setDays(dayService.loadAllCompletedDaysFromDiet((diet.getDietID()), currentDate, this));
        diet.setDays(bodyWeighInService.readAllBodyWeighInsFromCompletedDaysInDiet(diet.getDays(), this));
        day = dayService.loadDayByPrimaryKey(currentDate, dietID, this);

        tvCurrentDiet.setText(diet.getDietName());

        if (day.getDayID() != null)
        {
            tvDate.setText(day.getDateAsDanishDisplayText());
            tvGoalWeight.setText(String.format(Locale.getDefault(), "%.1f", day.getGoalWeight()));
            tvGoalWeight.append(" kg");
            tvMorningWeight.setText(String.format(Locale.getDefault(), "%.1f", day.getMorningWeight()));
            tvMorningWeight.append(" kg");
            tvAllowedFood.setText(String.format(Locale.getDefault(), "%.0f", dayService.convertRemainingFoodToGram(day.getAllowedFoodIntake())));
            tvAllowedFood.append(" g");
            tvBMI.setText(String.format(Locale.getDefault(), "%.1f", dietService.calculateBMI(bodyWeighInService.readLastBodyWeighInFromDay(day, this).getBodyWeight(), diet.getHeight())));
            setAllowedFoodColor();
        }
        else
        {
            dietComplete();
        }
        if (day.getMorningWeight() == 0 && day.getDayID() != null)
        {
            hideBtnAndAdjustText();
        }
        if (dietService.getDietProgress(diet.getDays()) == 0)
        {
            dayZero();
        }
        setChartData();
    }

    private void resetColorAndBtnText()
    {
        tvMorningWeight.setTextColor(Color.GRAY);
        tvAllowedFood.setTextColor(Color.GRAY);
        tvBMI.setTextColor(Color.GRAY);
        tvDate.setTextColor(Color.GRAY);
        tvGoalWeight.setTextColor(Color.GRAY);
        btnBodyWeighIn.setText(R.string.krop);

        btnFood.setClickable(true);
        btnFood.setAlpha(1);
        btnGoToDay.setClickable(true);
        btnGoToDay.setAlpha(1);
        btnBodyWeighIn.setClickable(true);
        btnBodyWeighIn.setAlpha(1);
    }

    private void hideBtnAndAdjustText()
    {
        tvMorningWeight.setTextColor(Color.RED);
        tvMorningWeight.setText(R.string.morgenvægt_fejl);
        tvAllowedFood.setTextColor(Color.RED);
        tvAllowedFood.setText(R.string.morgenvægt_fejl);
        tvBMI.setText(R.string.morgenvægt_fejl);
        tvBMI.setTextColor(Color.RED);
        btnBodyWeighIn.setText(R.string.morgenvægt);
        btnFood.setClickable(false);
        btnFood.setAlpha(0.2f);
    }

    private void dietComplete()
    {
        tvDate.setTextColor((Color.parseColor("#53C557")));
        tvDate.setText(R.string.done);
        tvDate.setClickable(false);
        tvDate.getPaint().setUnderlineText(false);
        tvGoalWeight.setText(String.format(Locale.getDefault(), "%.1f", diet.getDesiredWeight()));
        tvGoalWeight.append(" kg");
        tvMorningWeight.setTextColor((Color.parseColor("#53C557")));
        tvMorningWeight.setText(R.string.done);
        tvAllowedFood.setTextColor((Color.parseColor("#53C557")));
        tvAllowedFood.setText(R.string.done);
        tvBMI.setText(String.format(Locale.getDefault(), "%.1f", dietService.calculateBMI(diet.getDesiredWeight(), diet.getHeight())));

        btnGoToDay.setClickable(false);
        btnGoToDay.setAlpha(0.2f);
        btnBodyWeighIn.setClickable(false);
        btnBodyWeighIn.setAlpha(0.2f);
        btnFood.setClickable(false);
        btnFood.setAlpha(0.2f);
    }

    private void setAllowedFoodColor()
    {
        double allowedFood = day.getAllowedFoodIntake();

        if (allowedFood > 0)
        {
            tvAllowedFood.setTextColor(Color.parseColor("#53C557"));
        }
        else if ((int)allowedFood <= 0)
        {
            tvAllowedFood.setTextColor(Color.RED);
        }
    }

    private void setChartData()
    {
        ChartService chartService = new ChartService();
        List<ILineDataSet> iLineDataSets = new ArrayList<>();   // Contains multiple LineDataSets
        iLineDataSets.add(chartService.getDietWeightRange(dayService.loadAllDaysFromDiet(diet.getDietID(), this)));
        iLineDataSets.add(chartService.getCompletedDaysWeightEntryData(diet)); // Add weight plots to chart
        chartService.setLineChartStyle(lineChart); // style chart
        LineData lineData = new LineData(iLineDataSets); // molds the data
        lineChart.setData(lineData);    // display the data in graph
    }

    private void dayZero()
    {
        tvDate.setText(R.string.dag01);
        tvDate.setTextColor(Color.parseColor("#53C557"));
        tvGoalWeight.setText(R.string.dag02);
        tvGoalWeight.setTextColor(Color.parseColor("#53C557"));
        tvMorningWeight.setText(R.string.error);
        tvAllowedFood.setText(R.string.error);
        tvBMI.setText(R.string.error);
        btnBodyWeighIn.setClickable(false);
        btnBodyWeighIn.setAlpha(0.2f);
    }

    @Override
    public void onClick(View v)
    {
        Button button = (Button) v;
        int buttonID = button.getId();

        switch (buttonID)
        {
            case R.id.btnGoToDiet:
                intent = new Intent(MainMenuActivity.this, DietActivity.class);
                intent.putExtra("dietID", diet.getDietID());
                startActivity(intent);
                break;

            case R.id.btnGoToDay:
                intent = new Intent(MainMenuActivity.this, DayActivity.class);
                intent.putExtra("dietID", diet.getDietID());
                intent.putExtra("dayID", day.getSqlDate());
                startActivity(intent);
                break;

            case R.id.btnBodyWeighIn:
                intent = new Intent(this, RegisterWeightActivity.class);
                intent.putExtra("btnText", btnBodyWeighIn.getText().toString());
                intent.putExtra("dietID", dietID);
                startActivity(intent);
                break;

            case R.id.btnFood:
                intent = new Intent(this, RegisterWeightActivity.class);
                intent.putExtra("btnText", btnFood.getText().toString());
                intent.putExtra("dietID", dietID);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.select_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.mnuCreateDiet:
                intent = new Intent(this, DietCreationActivity.class);
                startActivity(intent);
                break;

            case R.id.mnuSwitchDiet:
                intent = new Intent(this, DietSelectionActivity.class);
                startActivity(intent);
                break;

            case R.id.mnuGuide:
                intent = new Intent(this, GuideActivity.class);
                startActivity(intent);
                break;

            case R.id.mnuInformation:
                intent = new Intent(this, InformationActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onRestart()
    {
        resetColorAndBtnText();
        setupInterfaceBasedOnSharedPreferences();
        super.onRestart();
    }
}
