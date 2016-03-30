/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hackbridge.viral;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class InputActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        final EditText inputIP = (EditText) findViewById(R.id.ipText);
        final EditText inputPort = (EditText) findViewById(R.id.portTest);
        final Button acceptButton = (Button) findViewById(R.id.acceptButton);

        // add a listener for the button
        acceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // puts in the values from the text boxes into an intent
                Intent intent = new Intent("com.hackbridge.viral.MAIN_ACTIVITY");

                intent.putExtra("ip", inputIP.getText().toString());
                intent.putExtra("port", inputPort.getText().toString());

                // starts up the main
                startActivity(intent);
            }
        });

    }
}
