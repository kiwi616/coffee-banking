package de.fruity.coffeeapp.tools;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.Objects;

import de.fruity.coffeeapp.R;
import de.fruity.coffeeapp.database.SqlAccessAPI;
import de.fruity.coffeeapp.database.SqlDatabaseContentProvider;
import de.fruity.coffeeapp.database.SqliteDatabase;
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

    public static int roundAndConvert(float value) {
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

        multiRenderer.setChartTitle(context.getText(R.string.current_ballance).toString());
        multiRenderer.setXTitle(context.getText(R.string.time).toString());
        multiRenderer.setXLabelsPadding(4.0f);
        multiRenderer.setYTitle("Value in euro");
        multiRenderer.setYLabelsPadding(10.0f);
        multiRenderer.setLabelsTextSize(20.0f);
        multiRenderer.setMargins(new int[]{25, 50, 25, 25});
        multiRenderer.setShowLabels(true);
        multiRenderer.setShowLegend(true);
        multiRenderer.setLegendTextSize(40);
        multiRenderer.setFitLegend(true);

        TimeSeries ts_coffee = getDataset(context.getContentResolver(),
                context.getText(R.string.coffee) + " (" + HelperMethods.roundTwoDecimals(SqlAccessAPI.getCoffeeValueFromPerson(context.getContentResolver(), person_id)) + "€)"
                ,"coffee", person_id);
        TimeSeries ts_candy = getDataset(context.getContentResolver(),
                context.getText(R.string.candy) + " (" + HelperMethods.roundTwoDecimals(SqlAccessAPI.getCandyValueFromPerson(context.getContentResolver(), person_id)) + "€)"
                ,"candy", person_id);
        TimeSeries ts_beer = getDataset(context.getContentResolver(),
                context.getText(R.string.beer) + " (" + HelperMethods.roundTwoDecimals(SqlAccessAPI.getBeerValueFromPerson(context.getContentResolver(), person_id)) + "€)"
                , "beer", person_id);
        TimeSeries ts_can = getDataset(context.getContentResolver(),
                context.getText(R.string.can) + " (" + HelperMethods.roundTwoDecimals(SqlAccessAPI.getCanValueFromPerson(context.getContentResolver(), person_id)) + "€)"
                , "can", person_id);
        TimeSeries ts_misc = getDataset(context.getContentResolver(),
                context.getText(R.string.misc) + " (" + HelperMethods.roundTwoDecimals(SqlAccessAPI.getMiscValueFromPerson(context.getContentResolver(), person_id)) + "€)"
                , "misc", person_id);

        // Adding Visits Series to the dataset
        dataset.addSeries(ts_coffee);
        dataset.addSeries(ts_candy);
        dataset.addSeries(ts_beer);
        dataset.addSeries(ts_can);
        dataset.addSeries(ts_misc);

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
//            singleRenderer.setChartValuesTextSize(30);
//            singleRenderer.setDisplayChartValues(true);
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

    static private TimeSeries getDataset(ContentResolver cr, final CharSequence legend_title, String kind, long person_id) {
        TimeSeries ts_candy = new TimeSeries(legend_title.toString());
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

        float value = SqlAccessAPI.getValueFromPersonById(context.getContentResolver(), pk, database_ident);
        String packname = context.getPackageName();
        int resourceId = context.getResources().getIdentifier(gettext, "string", packname);
        sb.append(context.getText(resourceId));

        sb.append(' ');
        sb.append(HelperMethods.roundTwoDecimals(value));
        sb.append(" €");

        new CustomToast(context, sb.toString(), 1500);
    }

    public static boolean isPersonalNumberValid(String number) {
        try {
            int persno = Integer.parseInt(number);
            return isPersonalNumberValid(persno);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isNameSurnameTupleInvalid(String s) {
        return s.split(" ").length < 2;
    }


    private static boolean isPersonalNumberValid(int number) {
        return (number / 99) > 1;
    }

    @SuppressLint("SetTextI18n")
    public static Dialog createNewUser(final Context context, Integer personalnumber, Integer rfid) {
        // custom dialog
        final int rfid_intern = rfid != null ? rfid : 0;

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_person);

        // set the custom dialog components - text, image and button
        final TextInputLayout floatingUsernameLabel = dialog.findViewById(R.id.til_newperson_username);
        final TextInputLayout floatingPersnoLabel = dialog.findViewById(R.id.til_newperson_personalnumber);
        final Button btnSave = dialog.findViewById(R.id.btn_newperson_apply);
        final EditText et_name = dialog.findViewById(R.id.et_newperson_username);
        final EditText et_persno = dialog.findViewById(R.id.et_newperson_personalnumber);
        final EditText et_rfid = dialog.findViewById(R.id.et_newperson_rfid);

        HelperMethods.setupFloatingLabelErrorUsername(context, floatingUsernameLabel);
        HelperMethods.setupFloatingLabelErrorPersonalNumber(context, floatingPersnoLabel, -1);

        if (personalnumber != null)
            et_persno.setText(personalnumber.toString());
        if (rfid != null)
            et_rfid.setText(rfid.toString());

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!HelperMethods.isPersonalNumberValid(et_persno.getText().toString())) {
                    new CustomToast(context, context.getText(R.string.no_personalnumber_number).toString(), 2000);
                    return;
                }
                if (HelperMethods.isNameSurnameTupleInvalid(et_name.getText().toString())) {
                    new CustomToast(context, context.getText(R.string.error_invalid_username).toString(), 2000);
                    return;
                }

                try {
                    SqlAccessAPI.createUser(context.getContentResolver(), et_name.getText().toString(), rfid_intern, Integer.parseInt(et_persno.getText().toString()));
                } catch (SQLiteConstraintException ex) {
                    new CustomToast(context,
                            context.getText(R.string.personalnumber_in_use).toString(), 2000);
                    return;
                }

                new CustomToast(context,
                        context.getText(R.string.user_created).toString(), 2000);
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

    public static void setupFloatingLabelErrorUsername(final Context c, final TextInputLayout floatingUsernameLabel) {
        Objects.requireNonNull(floatingUsernameLabel.getEditText()).addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (HelperMethods.isNameSurnameTupleInvalid(s.toString())) {
                    floatingUsernameLabel.setError(c.getString(R.string.error_invalid_username));
                    floatingUsernameLabel.setErrorEnabled(true);
                } else {
                    floatingUsernameLabel.setErrorEnabled(false);
                }
            }
        });
    }

    public static void setupFloatingLabelErrorPersonalNumber(final Context c, final TextInputLayout floatingUsernameLabel, final Integer pk_user) {
        Objects.requireNonNull(floatingUsernameLabel.getEditText()).addTextChangedListener(new TextWatcher() {
            // ...
            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean valid = HelperMethods.isPersonalNumberValid(s.toString());

                if (valid) {
                    Cursor cursor = c.getContentResolver().query(
                            SqlDatabaseContentProvider.CONTENT_URI,
                            null,
                            SqliteDatabase.COLUMN_PERSONAL_NUMBER + "= ? AND " +
                                    SqliteDatabase.COLUMN_ID + "!= ?",
                            new String[]{s.toString(), pk_user.toString()}, null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            valid = false;
                        }
                        cursor.close();
                    }
                }

                if (!valid)
                    floatingUsernameLabel.setError(c.getString(R.string.no_personalnumber_number));
                floatingUsernameLabel.setErrorEnabled(!valid);
            }
        });
    }
}