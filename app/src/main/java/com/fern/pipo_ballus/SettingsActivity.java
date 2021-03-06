/*
 * Copyright (C) 2021 Fern H. (aka Pavel Neshumov), PiPo-Ballus Android application
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR MILITARY PURPOSES. ALSO, IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR ANY PURPOSE THAT MAY LEAD TO INJURY, HUMAN, ANIMAL OR ENVIRONMENTAL DAMAGE.
 * ALSO, IT IS PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE) FOR ANY PURPOSE THAT
 * VIOLATES INTERNATIONAL HUMAN RIGHTS OR HUMAN FREEDOM.
 * BY USING THE PROJECT (OR PART OF THE PROJECT / CODE) YOU AGREE TO ALL OF THE ABOVE RULES.
 */

package com.fern.pipo_ballus;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;

import org.opencv.android.CameraBridgeViewBase;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private String[] cameraOptions;

    private ArgbEvaluator argbEvaluator;

    // Local settings
    private int cameraID;
    private int tableColorLower, tableColorUpper;
    private int ballColorLower, ballColorUpper;
    private int rotationSpeed, rotationRadius, jumpSpeed;
    private int baudRate;
    private byte suffix1, suffix2;

    // Elements
    private Spinner cameraIDSpinner;
    private Button settingsTableColor;
    private Button settingsBallColor;
    private Slider settingsRotationSpeed, settingsRotationRadius, settingsJumpSpeed;
    private EditText settingsSuffix1, settingsSuffix2;
    private EditText settingsBaudRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize elements
        cameraOptions = getResources().getStringArray(R.array.camera_options);
        argbEvaluator = new ArgbEvaluator();
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        cameraIDSpinner = findViewById(R.id.cameraIDSpinner);
        settingsTableColor = findViewById(R.id.settingsTableColor);
        settingsBallColor = findViewById(R.id.settingsBallColor);
        settingsRotationSpeed = findViewById(R.id.settingsRotationSpeed);
        settingsRotationRadius = findViewById(R.id.settingsRotationRadius);
        settingsJumpSpeed = findViewById(R.id.settingsJumpSpeed);
        settingsSuffix1 = findViewById(R.id.settingsSuffix1);
        settingsSuffix2 = findViewById(R.id.settingsSuffix2);
        settingsBaudRate = findViewById(R.id.settingsBaudRate);

        // Select home item
        bottomNavigationView.setSelectedItemId(R.id.menuSettings);

        // Add bottom menu clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menuHome) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                System.gc();
                finish();
            }
            else if (item.getItemId() == R.id.menuCamera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                System.gc();
                finish();
            }
            return false;
        });

        // Connect Restore button
        findViewById(R.id.settingsResetBtn).setOnClickListener(view -> {
            // Reset settings to default
            cameraID = CameraBridgeViewBase.CAMERA_ID_ANY;
            tableColorLower = 0xff1e3319;
            tableColorUpper = 0xff00ffd5;
            ballColorLower = 0xff7f7f7f;
            ballColorUpper = 0xffffb2b2;
            rotationSpeed = 4;
            rotationRadius = 150;
            jumpSpeed = 80;
            baudRate = 57600;
            suffix1 = (byte) 0xEE;
            suffix2 = (byte) 0xEF;

            // Update view
            updateView();
        });

        // Connect Save button
        findViewById(R.id.settingsSaveBtn).setOnClickListener(view -> saveSettings());

        // Connect camera ID spinner
        cameraIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView
                            , int position, long id) {
                        if (position == 1)
                            cameraID = CameraBridgeViewBase.CAMERA_ID_BACK;
                        else if (position == 2)
                            cameraID = CameraBridgeViewBase.CAMERA_ID_FRONT;
                        else
                            cameraID = CameraBridgeViewBase.CAMERA_ID_ANY;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                    }

                });

        // Connect table color button
        settingsTableColor.setOnClickListener(view -> {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this,
                    new HSVColor(tableColorLower), new HSVColor(tableColorUpper));
            colorPickerDialog.setColorPickerListener((lower, upper) -> {
                // Set new colors
                tableColorLower = lower.getIntColor();
                tableColorUpper = upper.getIntColor();

                // Update View
                updateView();
            });
            colorPickerDialog.show();
        });

        // Connect ball color button
        settingsBallColor.setOnClickListener(view -> {
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(this,
                    new HSVColor(ballColorLower), new HSVColor(ballColorUpper));
            colorPickerDialog.setColorPickerListener((lower, upper) -> {
                // Set new colors
                ballColorLower = lower.getIntColor();
                ballColorUpper = upper.getIntColor();

                // Update View
                updateView();
            });
            colorPickerDialog.show();
        });

        // Connect rotation speed slider
        settingsRotationSpeed.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                rotationSpeed = (int) slider.getValue();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                rotationSpeed = (int) slider.getValue();
            }
        });

        // Connect rotation radius slider
        settingsRotationRadius.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                rotationRadius = (int) slider.getValue();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                rotationRadius = (int) slider.getValue();
            }
        });

        // Connect jump speed slider
        settingsJumpSpeed.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                jumpSpeed = (int) slider.getValue();
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                jumpSpeed = (int) slider.getValue();
            }
        });

        // Connect USB serial baud rate
        settingsBaudRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    baudRate = Integer.parseInt(charSequence.toString());
                else
                    baudRate = 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Connect suffix1
        settingsSuffix1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    suffix1 = (byte) Integer.parseInt(charSequence.toString(), 16);
                else
                    suffix1 = 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Connect suffix2
        settingsSuffix2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    suffix2 = (byte) Integer.parseInt(charSequence.toString(), 16);
                else
                    suffix2 = 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Copy settings to local variables
        this.cameraID = SettingsContainer.cameraID;
        this.tableColorLower = SettingsContainer.tableColorLower;
        this.tableColorUpper = SettingsContainer.tableColorUpper;
        this.ballColorLower = SettingsContainer.ballColorLower;
        this.ballColorUpper = SettingsContainer.ballColorUpper;
        this.rotationSpeed = SettingsContainer.rotationSpeed;
        this.rotationRadius = SettingsContainer.rotationRadius;
        this.jumpSpeed = SettingsContainer.jumpSpeed;
        this.baudRate = SettingsContainer.baudRate;
        this.suffix1 = SettingsContainer.suffix1;
        this.suffix2 = SettingsContainer.suffix2;

        // Load view
        updateView();
    }

    /**
     * Updates activity elements with local settings variables
     */
    private void updateView() {
        // Camera index
        cameraIDSpinner.setAdapter(new ArrayAdapter<>(this,
                R.layout.spinner_layout, R.id.textViewSpinner, cameraOptions));
        if (cameraID == CameraBridgeViewBase.CAMERA_ID_BACK)
            cameraIDSpinner.setSelection(1);
        else if (cameraID == CameraBridgeViewBase.CAMERA_ID_FRONT)
            cameraIDSpinner.setSelection(2);
        else
            cameraIDSpinner.setSelection(0);

        // Table color
        int tableMidColor = (int) argbEvaluator.evaluate(0.5f, tableColorLower,
                tableColorUpper);
        settingsTableColor.setBackgroundColor(tableMidColor);
        settingsTableColor.setTextColor(HSVColor.getContrastColor(tableMidColor));
        String tableColorRange = String.format("#%06X", (0xFFFFFF & tableColorLower))
                + " - " + String.format("#%06X", (0xFFFFFF & tableColorUpper));
        settingsTableColor.setText(tableColorRange);

        // Ball color
        int ballMidColor = (int) argbEvaluator.evaluate(0.5f, ballColorLower,
                ballColorUpper);
        settingsBallColor.setBackgroundColor(ballMidColor);
        settingsBallColor.setTextColor(HSVColor.getContrastColor(ballMidColor));
        String ballColorRange = String.format("#%06X", (0xFFFFFF & ballColorLower))
                + " - " + String.format("#%06X", (0xFFFFFF & ballColorUpper));
        settingsBallColor.setText(ballColorRange);

        // Motion settings
        settingsRotationSpeed.setValue((float) rotationSpeed);
        settingsRotationRadius.setValue((float) rotationRadius);
        settingsJumpSpeed.setValue((float) jumpSpeed);

        // USB serial baud rate
        settingsBaudRate.setText(String.valueOf(baudRate));

        // Serial packet suffixes
        settingsSuffix1.setText(String.format("%02X", suffix1 & 0xFF));
        settingsSuffix2.setText(String.format("%02X", suffix2 & 0xFF));
    }

    /**
     * Assigns local settings to a SettingsContainer class and calls SettingsHandler.saveSettings()
     * to save the settings to a JSON file
     */
    private void saveSettings() {
        try {
            // Copy settings from local variables
            SettingsContainer.cameraID = this.cameraID;
            SettingsContainer.tableColorLower = this.tableColorLower;
            SettingsContainer.tableColorUpper = this.tableColorUpper;
            SettingsContainer.ballColorLower = this.ballColorLower;
            SettingsContainer.ballColorUpper = this.ballColorUpper;
            SettingsContainer.rotationSpeed = this.rotationSpeed;
            SettingsContainer.rotationRadius = this.rotationRadius;
            SettingsContainer.jumpSpeed = this.jumpSpeed;
            SettingsContainer.baudRate = this.baudRate;
            SettingsContainer.suffix1 = this.suffix1;
            SettingsContainer.suffix2 = this.suffix2;

            // Save settings to file
            SettingsHandler.saveSettings(HomeActivity.settingsFile, this);
            Toast.makeText(this, R.string.settings_saved,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.wrong_settings,
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Wrong settings provided!", e);
        }
    }
}