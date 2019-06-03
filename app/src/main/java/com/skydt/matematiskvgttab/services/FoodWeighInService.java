package com.skydt.matematiskvgttab.services;

import android.content.Context;
import android.util.Log;

import com.skydt.matematiskvgttab.models.Day;
import com.skydt.matematiskvgttab.models.FoodWeighIn;
import com.skydt.matematiskvgttab.repositories.FoodWeighInRepo;

import java.util.List;

public class FoodWeighInService
{
    private static final String TAG = "FoodWeighInService";
    private FoodWeighInRepo foodWeighInRepo;

    public FoodWeighInService()
    {
        foodWeighInRepo = new FoodWeighInRepo();
    }

    /*
    DB LOGIC
     */
    public void createWeighIn(FoodWeighIn foodWeighIn, Context context)
    {
        Log.d(TAG, "createWeighIn: Called");
        foodWeighIn.setFoodWeighIn(convertGramToKG(foodWeighIn.getFoodWeighIn()));
        foodWeighInRepo.createFoodWeighIn(foodWeighIn, context);
        Log.d(TAG, "createWeighIn: Finished");
    }

    public List<FoodWeighIn> readAllFoodWeighInsFromDay(Day day, Context context)
    {
        Log.d(TAG, "readAllFoodWeighInsFromDay: Called");
        List<FoodWeighIn> foodWeighIns = foodWeighInRepo.readAllFoodWeighInsFromDay(day, context);
        addFoodWeighInsToDay(day, foodWeighIns);
        return foodWeighIns;
    }

    /*
    BUSINESS LOGIC
     */
    private void addFoodWeighInsToDay(Day day, List<FoodWeighIn> foodWeighIns)
    {
        for (FoodWeighIn foodWeighIn: foodWeighIns)
        {
            day.addFoodWeighIn(foodWeighIn);
        }
    }

    private double convertGramToKG(double input)
    {
        return input /= 1000;
    }
}
