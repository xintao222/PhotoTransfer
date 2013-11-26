package com.example.phototransfersampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.phototransfersdk.SimplePhotoTransfer;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.callback.KiiUserCallBack;

public class MainActivity extends Activity {

	private KiiObject object = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Kii.initialize("4cb6fa00", "c48ff2c856c603773b95434c9deeb993", Site.US);
		
//		KiiUser user = KiiUser.builderWithName("fujisan").build();
//		try {
//            user.register(userCallback, "password");
//        } catch (Exception e) {
//            showToast("Error : " + e.getLocalizedMessage());
//        }
		
		try {
            KiiUser.logIn(userCallback, "fujisan", "password");
        } catch (Exception e) {
            showToast("Error : " + e.getLocalizedMessage());
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    KiiUserCallBack userCallback = new KiiUserCallBack() {
        @Override
        public void onRegisterCompleted(int token, KiiUser user, Exception e) {
            if (e == null) {
                showToast("User registered!");
                createObject(user);
            } else {
                showToast("Error : " + e.getLocalizedMessage());
            }
        }
        
        @Override
        public void onLoginCompleted(int token, KiiUser user, Exception e) {
            if (e == null) {
                showToast("User logged-in!");
                createObject(user);
            } else {
                showToast("Error : " + e.getLocalizedMessage());
            }
        }
        
        private void createObject(KiiUser user) {
            KiiBucket bucket = user.bucket("bucket");
            object = bucket.object();
            object.set("foo", "bar");
            
            try {
            	object.save(objectCallback);
            } catch (Exception exception) {
                showToast("Error : " + exception.getLocalizedMessage());
            }
        }
    };
    
    KiiObjectCallBack objectCallback = new KiiObjectCallBack() {
    	@Override
    	public void onSaveCompleted(int token, KiiObject object, Exception exception) {
            if (exception == null) {
            	showToast("Object saved!");
            } else {
                showToast("Error : " + exception.getLocalizedMessage());
            }
    	};
    };

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
	
	public void onUploadButtonClicked(View v) {
        SimplePhotoTransfer.upload(this, object);
	}

}
