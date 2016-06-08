package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ImageSelectorMainActivity extends Activity
{
	private final int REQUEST_CODE_SELECTION = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_selector_main);
	}

	public void startImageMultiSelection(View view)
	{
		Intent intent = new Intent(this, SelectActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SELECTION);
	}

	@Override
	protected void
	        onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_CODE_SELECTION:
			{
				if(resultCode == RESULT_OK && data != null)
				{
					ArrayList<String> selectedImageUris = data.getStringArrayListExtra(SelectActivity.KEY_SELECTED_IMAGES_PATHS);
					if(selectedImageUris !=  null && selectedImageUris.size()>0)
					{
						StringBuilder sb = new StringBuilder();
						for(String uriString : selectedImageUris)
						{
							sb.append(uriString + "\n");
						}
						((TextView) findViewById(R.id.tv_selected_uri)).setText(sb.toString());
					}
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
