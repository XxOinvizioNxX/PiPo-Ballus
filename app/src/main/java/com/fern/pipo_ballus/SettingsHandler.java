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

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * This class provides reading / saving of settings (the SettingsContainer class) to a JSON file
 */
public class SettingsHandler {
    private final static String TAG = SettingsHandler.class.getName();
    
    private final File settingsFile;
    private final Activity activity;

    private boolean retryFlag = false;

    /**
     * This class reads and saves application settings to a JSON file
     * @param settingsFile JSON file for settings
     * @param activity Activity for toast and finish() function
     */
    public SettingsHandler(File settingsFile,
                           Activity activity) {
        this.settingsFile = settingsFile;
        this.activity = activity;
    }

    /**
     * Reads settings from JSON file
     */
    public void readSettings() {
        // Log settings file location
        Log.i(TAG, "Reading settings from " + settingsFile.getAbsolutePath());

        if (!settingsFile.exists())
            saveSettings(settingsFile, activity);
        try {
            // Read file to JSONObject
            FileReader fileReader = new FileReader(settingsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            // Parse json object to SettingsContainer variables
            SettingsContainer.cameraID = jsonObject.getInt("camera_id");
            SettingsContainer.tableColorLower = jsonObject.getInt("table_color_lower");
            SettingsContainer.tableColorUpper = jsonObject.getInt("table_color_upper");
            SettingsContainer.ballColorLower = jsonObject.getInt("ball_color_lower");
            SettingsContainer.ballColorUpper = jsonObject.getInt("ball_color_upper");
            SettingsContainer.rotationSpeed = jsonObject.getInt("rotation_speed");
            SettingsContainer.rotationRadius = jsonObject.getInt("rotation_radius");
            SettingsContainer.jumpSpeed = jsonObject.getInt("jump_speed");
            SettingsContainer.baudRate = jsonObject.getInt("baud_rate");
            SettingsContainer.suffix1 = (byte)(jsonObject.getInt("suffix_1"));
            SettingsContainer.suffix2 = (byte)(jsonObject.getInt("suffix_2"));

        } catch (Exception e) {
            // Show error message
            Toast.makeText(activity, R.string.error_parsing_settings,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing settings!", e);

            // Remove file and try again
            if (!retryFlag) {
                if (settingsFile.delete()) {
                    Log.w(TAG, "Retrying to read settings");
                    readSettings();
                    retryFlag = true;
                }
            }
            // Exit application
            else {
                activity.finish();
                System.gc();
                System.exit(0);
            }
        }
    }

    /**
     * Saves settings to JSON file
     */
    public static void saveSettings(File settingsFile,
                                    Activity activity) {
        try {
            // Create new JSONObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("camera_id", SettingsContainer.cameraID);
            jsonObject.put("table_color_lower", SettingsContainer.tableColorLower);
            jsonObject.put("table_color_upper", SettingsContainer.tableColorUpper);
            jsonObject.put("ball_color_lower", SettingsContainer.ballColorLower);
            jsonObject.put("ball_color_upper", SettingsContainer.ballColorUpper);
            jsonObject.put("rotation_speed", SettingsContainer.rotationSpeed);
            jsonObject.put("rotation_radius", SettingsContainer.rotationRadius);
            jsonObject.put("jump_speed", SettingsContainer.jumpSpeed);
            jsonObject.put("baud_rate", SettingsContainer.baudRate);
            jsonObject.put("suffix_1", SettingsContainer.suffix1 & 0xFF);
            jsonObject.put("suffix_2", SettingsContainer.suffix2 & 0xFF);

            // Write JSONObject to file
            FileWriter fileWriter = new FileWriter(settingsFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();

        } catch (Exception e) {
            // Show error message
            Toast.makeText(activity, R.string.error_saving_settings,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving settings!", e);

            // Exit application
            activity.finish();
            // System.exit(0);
        }
    }
}
