package com.skydt.weightcontrol.models;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BodyWeighIn
{
    private static final String TAG = "BodyWeighIn";
    private int bodyWeighInID;
    private Date dayID;
    private int dietID;
    private double bodyWeight;

    /*
    Constructors
     */
    public BodyWeighIn()
    {
    }

    public BodyWeighIn(int bodyWeighInID, Date dayID, int dietID, double bodyWeight)
    {
        this.bodyWeighInID = bodyWeighInID;
        this.dayID = dayID;
        this.dietID = dietID;
        this.bodyWeight = bodyWeight;
    }

    public BodyWeighIn(Date dayID, int dietID, double bodyWeight)
    {
        this.dayID = dayID;
        this.dietID = dietID;
        this.bodyWeight = bodyWeight;
    }


    /*
    Getters
     */
    public int getDietID() { return dietID; }
    public String getSQLDate()
    {
        DateFormat correctFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return correctFormat.format(this.dayID);
    }
    public double getBodyWeight()
    {
        return bodyWeight;
    }
    public int getBodyWeighInID() { return bodyWeighInID; }

    /*
    Setters
     */
    public void setBodyWeight(double bodyWeight)
    {
        this.bodyWeight = bodyWeight;
    }
    public void setDayID(Date dayID) { this.dayID = dayID; }
    public void setDayIDByString(String date)
    {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try
        {
            this.dayID = formatter.parse(date);
        }
        catch (ParseException pe)
        {
            Log.e(TAG, "setDayIDByString: " + pe);
        }
    }
    public void setDietID(int dietID) { this.dietID = dietID; }
    public void setBodyWeighInID(int bodyWeighInID)
    {
        this.bodyWeighInID = bodyWeighInID;
    }


    @Override
    public String toString()
    {
        return bodyWeight + " kg";
    }
}


