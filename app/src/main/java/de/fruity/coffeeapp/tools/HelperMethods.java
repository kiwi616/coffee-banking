package de.fruity.coffeeapp.tools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.adminmode.AdminmodeActivity;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.ui_elements.CustomToast;

public class HelperMethods {

    private static final int[] color_array = {Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.GRAY};

    public static String roundTwoDecimals(float d) {
        Float tmp = (float) Math.round(d * 100) / 100;
        return String.format(Locale.getDefault(), "%.2f", tmp);
    }

    public static BigDecimal roundTwoDecimals(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

	public static int roundAndConvert(float value)
    {
        String rounded = roundTwoDecimals(value);
        float value_f = 0;
        try {
            value_f = NumberFormat.getNumberInstance(Locale.getDefault()).parse(rounded).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        value_f = value_f * 100;

        return (int) value_f;
    }

    static public GraphicalView createLineChart(Context context, long person_id) {
        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        multiRenderer.setChartTitle("Values over time");
        multiRenderer.setXTitle("Time");
        multiRenderer.setXLabelsPadding(4.0f);
        multiRenderer.setYTitle("Value in euro");
        multiRenderer.setYLabelsPadding(10.0f);
        multiRenderer.setMargins(new int[]{25, 50, 25, 25});
        multiRenderer.setShowLabels(true);
        multiRenderer.setShowLegend(true);
        multiRenderer.setFitLegend(true);

        TimeSeries ts_coffee = getDataset(context.getContentResolver(), "coffee", person_id);
        TimeSeries ts_candy = getDataset(context.getContentResolver(), "candy", person_id);
        TimeSeries ts_beer = getDataset(context.getContentResolver(), "beer", person_id);
        TimeSeries ts_can = getDataset(context.getContentResolver(), "can", person_id);

        // Adding Visits Series to the dataset
        dataset.addSeries(ts_coffee);
        dataset.addSeries(ts_candy);
        dataset.addSeries(ts_beer);
        dataset.addSeries(ts_can);

        double cur_x_max = dataset.getSeriesAt(0).getMaxX();
        double cur_x_min = dataset.getSeriesAt(0).getMinX();
        double cur_y_max = dataset.getSeriesAt(0).getMaxY();
        double cur_y_min = dataset.getSeriesAt(0).getMinY();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            // Creating XYSeriesRenderer to customize visitsSeries
            XYSeriesRenderer singleRenderer = new XYSeriesRenderer();
            singleRenderer.setColor(color_array[i]);
            singleRenderer.setPointStyle(PointStyle.CIRCLE);
            singleRenderer.setFillPoints(true);
            singleRenderer.setLineWidth(3);
            singleRenderer.setChartValuesTextSize(20);
            singleRenderer.setDisplayChartValues(true);
            singleRenderer.setAnnotationsColor(Color.YELLOW);
            singleRenderer.setAnnotationsTextAlign(Paint.Align.CENTER);
            singleRenderer.setAnnotationsTextSize(20); //current bug annotations with time charts

            multiRenderer.addSeriesRenderer(singleRenderer);

            if (dataset.getSeriesAt(i).getMinX() < cur_x_min)
                cur_x_min = dataset.getSeriesAt(i).getMinX();
            if (dataset.getSeriesAt(i).getMinY() < cur_y_min)
                cur_y_min = dataset.getSeriesAt(i).getMinY();
            if (dataset.getSeriesAt(i).getMaxX() > cur_x_max)
                cur_x_max = dataset.getSeriesAt(i).getMaxX();
            if (dataset.getSeriesAt(i).getMaxY() > cur_y_max)
                cur_y_max = dataset.getSeriesAt(i).getMaxY();
        }

        multiRenderer.setXAxisMin(cur_x_min - ((cur_x_min / 100) * 0.001)); // values are date time double so they are very huge
        multiRenderer.setXAxisMax(cur_x_max + ((cur_x_max / 100) * 0.001)); // -> 1 %% is enough
        if (cur_y_min == 0)
            cur_y_min = -1.5;
        multiRenderer.setYAxisMin(cur_y_min - ((cur_y_min / 100) * 20));
        multiRenderer.setYAxisMax(cur_y_max + ((cur_y_max / 100) * 20));
        // Creating a Time Chart

        return ChartFactory.getTimeChartView(context, dataset, multiRenderer, "hh:mm dd-MMM");
    }

    static private TimeSeries getDataset(ContentResolver cr, String kind, long person_id) {
        TimeSeries ts_candy = new TimeSeries(kind);
        double summ_candy_val = 0.0d;
        Map<Date, BigDecimal> data_candy = SqlAccessAPI.getDateValueTupel(cr, person_id, kind);

        for (Map.Entry<Date, BigDecimal> value : data_candy.entrySet()) {
            summ_candy_val += value.getValue().doubleValue();
            ts_candy.add(value.getKey(), summ_candy_val);
        }

        return ts_candy;
    }



    public static void billanceToast(Context context, int pk, String database_ident) {
        String gettext = "comma_new_" + database_ident + "_billance";

        StringBuilder sb = new StringBuilder();
        String name = SqlAccessAPI.getName(context.getContentResolver(), pk);
        sb.append(context.getText(R.string.hello));
        sb.append(' ');
        sb.append(name);
        sb.append(' ');

        float value = SqlAccessAPI.getValueFromPersonById(context.getContentResolver(), pk, database_ident);
        String packname = context.getPackageName();
        int resourceId= context.getResources().getIdentifier(gettext, "string", packname);
        sb.append(context.getText(resourceId));

        sb.append(' ');
        sb.append(HelperMethods.roundTwoDecimals(value));
        sb.append(" €");

        new CustomToast(context, sb.toString(), 1500);
    }

    public static boolean isPersonalnumberValid(String number) {
        try {
            int persno = Integer.parseInt(number);
            return isPersonalnumberValid(persno);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isPersonalnumberValid(int number) {
        if ((number / 99) > 1)
            return true;

        return false;
    }

    @SuppressLint("SetTextI18n")
    public static Dialog createNewUser(final Context context, Integer personalnumber, Integer rfid) {
        // custom dialog
        final int rfid_intern = rfid != null ?  rfid : 0;

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_new_person);
        dialog.setTitle(R.string.save_hint_enter_name);

        // set the custom dialog components - text, image and button
        final Button cancelButton = (Button) dialog.findViewById(R.id.newperson_dialog_btn_cancel);
        final Button btnSave = (Button) dialog.findViewById(R.id.newperson_dialog_btn_save);
        final EditText et = (EditText) dialog.findViewById(R.id.newperson_dialog_et_name);
        final EditText et_personalnumber = (EditText) dialog.findViewById(R.id.newperson_dialog_et_personalnumber);

        if (personalnumber != null)
            et_personalnumber.setText(personalnumber.toString());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int personalnumber;

                try {
                    personalnumber = Integer.parseInt(et_personalnumber.getText().toString());
                } catch (NumberFormatException ex) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }
                if (!isPersonalnumberValid(personalnumber)) { //more than 2 digit
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                try {
                    SqlAccessAPI.createUser(context.getContentResolver(), et.getText().toString(), rfid_intern, personalnumber);
                    if (SqlAccessAPI.isAdmin(context.getContentResolver(), rfid_intern))
                        createAdminCode(context);
                } catch (SQLiteConstraintException ex) {
                    new CustomToast(context,
                            context.getText(R.string.personalnumber_in_use).toString(), Toast.LENGTH_LONG);
                    return;
                }

                new CustomToast(context,
                        context.getText(R.string.user_created).toString(), Toast.LENGTH_LONG);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private static void createAdminCode(final Context context) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_new_admincode);
        dialog.setTitle(R.string.enter_admin_code);
        dialog.setCancelable(false);

        // set the custom dialog components - text, image and button
        final Button btnSave = (Button) dialog.findViewById(R.id.newperson_dialog_btn_save);
        final EditText et = (EditText) dialog.findViewById(R.id.et_admincode);
        final EditText et_reentered = (EditText) dialog.findViewById(R.id.et_admincode_reenter);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int first_et_value;
                int second_et_value;

                try {
                    first_et_value = Integer.parseInt(et.getText().toString());
                    second_et_value = Integer.parseInt(et_reentered.getText().toString());
                } catch (NumberFormatException ex) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                if(first_et_value != second_et_value)
                {
                    new CustomToast(context,
                            context.getText(R.string.two_field_dont_match).toString(), 2000);
                    return;
                }

                if (String.valueOf(et.getText().toString()).length() != 4) {
                    new CustomToast(context,
                            context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }

                AdminmodeActivity.saveAdminCode(context, second_et_value);
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AdminmodeActivity.saveAdminCode(context, 4711);
                new CustomToast(context,
                        context.getText(R.string.admincode_set_to_default).toString(), 5000);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}