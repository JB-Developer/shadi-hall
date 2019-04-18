package org.by9steps.shadihall.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import org.by9steps.shadihall.AppController;
import org.by9steps.shadihall.activities.CashCollectionActivity;

import java.util.Calendar;

public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        populateSetDate(yy, mm+1, dd);
    }
    public void populateSetDate(int year, int month, int day) {
        if (AppController.date.equals("CashBook")){
            String d = String.valueOf(day), m = String.valueOf(month);
            if (month<10){
                m = "0"+String.valueOf(month);
            }
            if (day<10){
                d = "0"+String.valueOf(day);
            }
            CashCollectionActivity.date.setText(year+"-"+m+"-"+d);
        }else if (AppController.fDate1.equals("Date1")){
            String d = String.valueOf(day), m = String.valueOf(month);
            if (month<10){
                m = "0"+String.valueOf(month);
            }
            if (day<10){
                d = "0"+String.valueOf(day);
            }
                CashBookFragment.date1.setText(String.valueOf(year+"-"+ m+"-"+d));
                AppController.fDate1 = "";
        }else if (AppController.fDate2.equals("Date2")){
            String d = String.valueOf(day), m = String.valueOf(month);
            if (month<10){
                m = "0"+String.valueOf(month);
            }
            if (day<10){
                d = "0"+String.valueOf(day);
            }
            CashBookFragment.date2.setText(String.valueOf(year+"-" +m+"-"+d));
            AppController.fDate2 = "";
        }else {
            ReportsFragment.date_picker.setText(year+"-"+month+"-"+day);
        }

    }

}
