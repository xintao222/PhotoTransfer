package com.example.phototransfersdk;

import android.content.Context;
import android.content.Intent;

import com.kii.cloud.storage.KiiObject;


public class SimplePhotoTransfer {
	
	public static void upload(Context context, KiiObject object) {
        Intent i = new Intent(context, TransferActivity.class);
        i.putExtra("object_uri", object.toUri().toString());
        context.startActivity(i);
	}
	
	public static void download() {
		
	}
	
}
