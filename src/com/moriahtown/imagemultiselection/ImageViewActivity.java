package com.moriahtown.imagemultiselection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageViewActivity extends Activity {
	
	ImageView iv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        
        Bundle bun = getIntent().getExtras();
        String filePath = bun.getString("filePath");
        
        iv = (ImageView) findViewById(R.id.iv_edited_image);
        
        Drawable drawable = Drawable.createFromPath(filePath); 
        iv.setImageDrawable(drawable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch(requestCode) {
    		
    	}
    }
}
