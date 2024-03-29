package com.skydt.weightcontrol.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.skydt.weightcontrol.database.AppDatabase;
import com.skydt.weightcontrol.database.DBContract;
import com.skydt.weightcontrol.models.BodyWeighIn;
import com.skydt.weightcontrol.models.Day;

import java.util.ArrayList;
import java.util.List;

public class BodyWeighInRepo
{
    private static final String TAG = "BodyWeighInRepo";
    private AppDatabase appDatabase;
    private SQLiteDatabase database;
    private Cursor cursor;

    public void createBodyWeighIn(BodyWeighIn bodyWeighIn, Context context)
    {
        Log.d(TAG, "createBodyWeighIn: called");
        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(DBContract.BodyWeightEntries.DAY_ID, bodyWeighIn.getSQLDate());
            values.put(DBContract.FoodWeightEntries.DIET_ID, bodyWeighIn.getDietID());
            values.put(DBContract.BodyWeightEntries.WEIGHT, bodyWeighIn.getBodyWeight());

            database.insertOrThrow(DBContract.BodyWeightEntries.TABLE_NAME, null, values);
        }
        catch (SQLiteException sqle)
        {
            Log.e(TAG, "createBodyWeighIn: SQLite Exception Thrown!", sqle);
        }
        catch (SQLException sqle)
        {
            Log.e(TAG, "createBodyWeighIn: SQL Exception Thrown!", sqle);
        }
        finally
        {
            database.close();
            Log.d(TAG, "createBodyWeighIn: Finished");
        }
    }

    public List<BodyWeighIn> readAllBodyWeighInsFromDay(Day day, Context context)
    {
        Log.d(TAG, "readAllBodyWeighInsFromDay: Called");
        List<BodyWeighIn> bodyWeighIns = new ArrayList<>();

        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getReadableDatabase();

            String query = "SELECT * FROM " + DBContract.BodyWeightEntries.TABLE_NAME + " WHERE "
                                            + DBContract.BodyWeightEntries.DAY_ID + " = " + "\"" + day.getSqlDate() + "\""
                                            + " AND "
                                            + day.getDietID() + " = " + DBContract.BodyWeightEntries.DIET_ID
                                            + " ORDER BY " + DBContract.BodyWeightEntries.BODY_ID + " DESC";
            cursor = database.rawQuery(query, null, null);

            if (cursor != null)
            {
                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    BodyWeighIn bodyWeighIn = new BodyWeighIn(cursor.getInt(0), day.getDayID(), day.getDietID(), cursor.getDouble(3) );
                    bodyWeighIns.add(bodyWeighIn);
                    cursor.moveToNext();
                }
            }
        }
        catch (SQLiteException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromDay SQLite exception thrown: " + sqle);
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromDay SQL exception thrown: " + sqle);
        }
        finally
        {
            database.close();
            cursor.close();
            Log.d(TAG, "readAllBodyWeighInsFromDay: Finished");
        }
        return bodyWeighIns;
    }

    public List<BodyWeighIn> readAllBodyWeighInsFromDiet(Day day, Context context)
    {
        Log.d(TAG, "readAllBodyWeighInsFromDiet: Called");
        List<BodyWeighIn> bodyWeighIns = new ArrayList<>();

        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getReadableDatabase();

            String query = "SELECT * FROM " + DBContract.BodyWeightEntries.TABLE_NAME + " WHERE "
                    + DBContract.BodyWeightEntries.DIET_ID + " = " + day.getDietID();
            cursor = database.rawQuery(query, null, null);

            if (cursor != null)
            {
                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    BodyWeighIn bodyWeighIn = new BodyWeighIn();
                    bodyWeighIn.setBodyWeighInID(cursor.getInt(0));
                    bodyWeighIn.setDayIDByString(cursor.getString(1));
                    bodyWeighIn.setDietID(cursor.getInt(2));
                    bodyWeighIn.setBodyWeight(cursor.getDouble(3));
                    bodyWeighIns.add(bodyWeighIn);
                    cursor.moveToNext();
                }
            }
        }
        catch (SQLiteException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromDiet SQLite exception thrown: " + sqle);
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromDiet SQL exception thrown: " + sqle);
        }
        finally
        {
            database.close();
            cursor.close();
            Log.d(TAG, "readAllBodyWeighInsFromDiet: Finished");
        }
        return bodyWeighIns;
    }

    public List<BodyWeighIn> readLastBodyWeighInFromCompletedDaysInDiet(List<Day> completedDays, Context context)
    {
        Log.d(TAG, "readLastBodyWeighInFromCompletedDaysInDiet: Called");
        List<BodyWeighIn> bodyWeighIns = new ArrayList<>();

        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getReadableDatabase();

            for (Day day : completedDays)
            {
                String query = "SELECT * FROM " + DBContract.BodyWeightEntries.TABLE_NAME
                        + " WHERE " + DBContract.BodyWeightEntries.BODY_ID + " = "
                        + "(SELECT max(" + DBContract.BodyWeightEntries.BODY_ID + ")"
                        + " FROM " + DBContract.BodyWeightEntries.TABLE_NAME
                        + " WHERE " + DBContract.BodyWeightEntries.DIET_ID + " = " + day.getDietID()
                        + " AND " + DBContract.BodyWeightEntries.DAY_ID + " = " + "\"" + day.getSqlDate() + "\");";
                cursor = database.rawQuery(query, null, null);

                if (cursor != null && cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    bodyWeighIns.add(new BodyWeighIn(cursor.getInt(0), day.getDayID(), day.getDietID(), cursor.getDouble(3)));
                }
            }
        }
        catch (SQLiteException sqle)
        {
            Log.d(TAG, "readLastBodyWeighInFromCompletedDaysInDiet SQLite exception thrown: " + sqle);
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "readLastBodyWeighInFromCompletedDaysInDiet SQL exception thrown: " + sqle);
        }
        finally
        {
            database.close();
            if (cursor != null)
            {
                cursor.close();
            }
            Log.d(TAG, "readLastBodyWeighInFromCompletedDaysInDiet: Finished");
        }
        return bodyWeighIns;
    }

    public BodyWeighIn readLastBodyWeighInFromDay(Day day, Context context)
    {
        Log.d(TAG, "readLastBodyWeighInFromDay: Called");
        BodyWeighIn bodyWeighIn = new BodyWeighIn();

        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getReadableDatabase();

                String query = "SELECT " + DBContract.BodyWeightEntries.BODY_ID + ", " + DBContract.BodyWeightEntries.WEIGHT
                        + " FROM " + DBContract.BodyWeightEntries.TABLE_NAME
                        + " WHERE " + DBContract.BodyWeightEntries.BODY_ID + " = "
                        + "(SELECT max(" + DBContract.BodyWeightEntries.BODY_ID + ")"
                        + " FROM " + DBContract.BodyWeightEntries.TABLE_NAME
                        + " WHERE " + DBContract.BodyWeightEntries.DIET_ID + " = " + day.getDietID()
                        + " AND " + DBContract.BodyWeightEntries.DAY_ID + " = " + "\"" + day.getSqlDate() + "\");";
                cursor = database.rawQuery(query, null, null);

                if (cursor != null && cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    bodyWeighIn.setBodyWeighInID(cursor.getInt(0));
                    bodyWeighIn.setDayID(day.getDayID());
                    bodyWeighIn.setDietID(day.getDietID());
                    bodyWeighIn.setBodyWeight(cursor.getDouble(1));
                    return bodyWeighIn;
                }
        }
        catch (SQLiteException sqle)
        {
            Log.d(TAG, "readLastBodyWeighInFromDay SQLite exception thrown: " + sqle);
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "readLastBodyWeighInFromDay SQL exception thrown: " + sqle);
        }
        finally
        {
            database.close();
            cursor.close();
            Log.d(TAG, "readLastBodyWeighInFromDay: Finished");
        }
        return bodyWeighIn;
    }

    public List<Day> readAllBodyWeighInsFromCompletedDaysInDiet(List<Day> completedDays, Context context)
    {
        Log.d(TAG, "readAllBodyWeighInsFromCompletedDaysInDiet: Called");

        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getReadableDatabase();

            for (Day day : completedDays)
            {
                String query = "SELECT * FROM " + DBContract.BodyWeightEntries.TABLE_NAME
                        + " WHERE " + DBContract.BodyWeightEntries.DAY_ID + " ="
                        + " DATE('" + day.getSqlDate() + "') AND " + DBContract.BodyWeightEntries.DIET_ID
                        + " = " + day.getDietID();

                cursor = database.rawQuery(query, null, null);

                if (cursor != null && cursor.getCount() > 0)
                {
                    cursor.moveToFirst();
                    do
                    {
                        BodyWeighIn bodyWeighIn = new BodyWeighIn();
                        bodyWeighIn.setBodyWeighInID(cursor.getInt(0));
                        bodyWeighIn.setDayIDByString(cursor.getString(1));
                        bodyWeighIn.setDietID(cursor.getInt(2));
                        bodyWeighIn.setBodyWeight(cursor.getDouble(3));
                        day.getBodyWeighIns().add(bodyWeighIn);
                    } while (cursor.moveToNext());
                }
            }
        }
        catch (SQLiteException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromCompletedDaysInDiet SQLite exception thrown: " + sqle);
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "readAllBodyWeighInsFromCompletedDaysInDiet SQL exception thrown: " + sqle);
        }
        finally
        {
            database.close();
            if (cursor != null)
            {
                cursor.close();
            }
            Log.d(TAG, "readAllBodyWeighInsFromCompletedDaysInDiet: Finished");
        }
        return completedDays;
    }

    public void deleteBodyWeighInByID(int weighInID, Context context)
    {
        appDatabase = AppDatabase.getInstance(context);
        try
        {
            database = appDatabase.getWritableDatabase();

            String selection = DBContract.BodyWeightEntries.BODY_ID + " = ?";
            String[] selectionArgs = {(Integer.toString(weighInID))};
            int deleteRows = database.delete(DBContract.BodyWeightEntries.TABLE_NAME, selection, selectionArgs);

            Log.d(TAG, "deleteBodyWeighInByID: finished with: " + deleteRows + " deleted rows");
        }
        catch(SQLiteException sqle)
        {
            Log.d(TAG, "deleteBodyWeighInByID SQLiteException thrown: " + sqle );
        }
        catch (SQLException sqle)
        {
            Log.d(TAG, "deleteBodyWeighInByID SQLException thrown: " + sqle);
        }
        finally
        {
            database.close();
        }
    }
}
