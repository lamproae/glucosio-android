/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.glucosio.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.glucosio.android.R;
import org.glucosio.android.db.WeightReading;
import org.glucosio.android.presenter.AddWeightPresenter;
import org.glucosio.android.tools.AnimationTools;
import org.glucosio.android.tools.FormatDateTime;
import org.glucosio.android.tools.SplitDateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddWeightActivity extends AddReadingActivity {

    private FloatingActionButton doneFAB;
    private TextView addTimeTextView;
    private TextView addDateTextView;
    private TextView readingTextView;
    private TextView unitTextView;
    private int pagerPosition;
    private Runnable fabAnimationRunnable;
    private long editId = 0;
    private boolean editing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        Bundle b = getIntent().getExtras();
        if (b != null) {
            pagerPosition = b.getInt("pager");
            editId = b.getLong("edit_id");
            editing = b.getBoolean("editing");
        }

        AddWeightPresenter presenter = new AddWeightPresenter(this);
        this.setPresenter(presenter);
        presenter.getCurrentTime();

        doneFAB = (FloatingActionButton) findViewById(R.id.done_fab);
        addTimeTextView = (TextView) findViewById(R.id.dialog_add_time);
        addDateTextView = (TextView) findViewById(R.id.dialog_add_date);
        readingTextView = (TextView) findViewById(R.id.weight_add_value);
        unitTextView = (TextView) findViewById(R.id.weight_add_unit_measurement);

        if (!"kilograms".equals(presenter.getWeightUnitMeasuerement())) {
            unitTextView.setText("lbs");
        }

        FormatDateTime formatDateTime = new FormatDateTime(getApplicationContext());
        addDateTextView.setText(formatDateTime.getCurrentDate());
        addTimeTextView.setText(formatDateTime.getCurrentTime());
        addDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddWeightActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
                dpd.setMaxDate(now);
            }
        });

        addTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                if (android.text.format.DateFormat.is24HourFormat(getApplicationContext())) {
                    TimePickerDialog tpd = TimePickerDialog.newInstance(AddWeightActivity.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    tpd.show(getFragmentManager(), "Timepickerdialog");
                } else {
                    TimePickerDialog tpd = TimePickerDialog.newInstance(AddWeightActivity.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
                    tpd.show(getFragmentManager(), "Timepickerdialog");
                }
            }
        });
        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOnAddButtonPressed();
            }
        });

        // If an id is passed, open the activity in edit mode
        if (editing) {
            FormatDateTime dateTime = new FormatDateTime(getApplicationContext());
            setTitle(R.string.title_activity_add_weight_edit);
            WeightReading readingToEdit = presenter.getWeightReadingById(editId);
            readingTextView.setText(readingToEdit.getReading() + "");
            Calendar cal = Calendar.getInstance();
            cal.setTime(readingToEdit.getCreated());
            addDateTextView.setText(dateTime.getDate(cal));
            addTimeTextView.setText(dateTime.getTime(cal));
            SplitDateTime splitDateTime = new SplitDateTime(readingToEdit.getCreated(), new SimpleDateFormat("yyyy-MM-dd"));
            presenter.setReadingDay(splitDateTime.getDay());
            presenter.setReadingHour(splitDateTime.getHour());
            presenter.setReadingMinute(splitDateTime.getMinute());
            presenter.setReadingYear(splitDateTime.getYear());
            presenter.setReadingMonth(splitDateTime.getMonth());
        }

        fabAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                AnimationTools.startCircularReveal(doneFAB);
            }
        };

        doneFAB.postDelayed(fabAnimationRunnable, 600);
    }

    private void dialogOnAddButtonPressed() {
        AddWeightPresenter presenter = (AddWeightPresenter) this.getPresenter();
        if (editing) {
            presenter.dialogOnAddButtonPressed(addTimeTextView.getText().toString(),
                    addDateTextView.getText().toString(), readingTextView.getText().toString(), editId);
        } else {
            presenter.dialogOnAddButtonPressed(addTimeTextView.getText().toString(),
                    addDateTextView.getText().toString(), readingTextView.getText().toString());

        }
    }

    public void showErrorMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.dialog_error2), Toast.LENGTH_SHORT).show();
    }

    public void finishActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        // Pass pager position to open it again later
        Bundle b = new Bundle();
        b.putInt("pager", pagerPosition);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doneFAB.removeCallbacks(fabAnimationRunnable);
    }
}
