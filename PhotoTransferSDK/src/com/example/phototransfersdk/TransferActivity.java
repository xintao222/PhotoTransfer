package com.example.phototransfersdk;

import java.io.File;
import java.io.FileOutputStream;

import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.resumabletransfer.KiiRTransfer;
import com.kii.cloud.storage.resumabletransfer.KiiRTransferCallback;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class TransferActivity extends Activity {
	
	private String objectUri = null;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "TransferActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transfer);
		
        objectUri = getIntent().getStringExtra("object_uri");

	    Intent intent = new Intent();
	    intent.setType("image/*");
	    intent.setAction(Intent.ACTION_GET_CONTENT);
	    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
	        startActivityForResult(intent, PICK_IMAGE);
	    } else {
	        startActivityForResult(
	                Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transfer, menu);
		return true;
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            showToast("picking file success!");
            
            Uri selectedFileUri = data.getData();
            String filePath = getFilePathByUri(selectedFileUri);
            Log.v(TAG, "Picture Path : " + filePath);
            if (filePath == null) {
                showToast("Please select an image that exists locally.");
                return;
            }
            
            uploadFile(filePath);
        } else {
            showToast("picking file failed!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private String getFilePathByUri(Uri selectedFileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Workaround of retrieving file image through ContentResolver
            // for Android4.2 or later
            String filePath = null;
            FileOutputStream fos = null;
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), selectedFileUri);

                String cacheDir = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + File.separator + "tutorialapp";
                File createDir = new File(cacheDir);
                if (!createDir.exists()) {
                    createDir.mkdir();
                }
                filePath = cacheDir + File.separator + "upload.jpg";
                File file = new File(filePath);

                fos = new FileOutputStream(file);
                bmp.compress(CompressFormat.JPEG, 95, fos);
                fos.flush();
                fos.getFD().sync();
            } catch (Exception e) {
                filePath = null;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        // Nothing to do
                    }
                }
            }
            return filePath;
        } else {
            String[] filePathColumn = { MediaStore.MediaColumns.DATA };
            Cursor cursor = getContentResolver().query(selectedFileUri,
                    filePathColumn, null, null, null);

            if (cursor == null)
                return null;
            try {
                if (!cursor.moveToFirst())
                    return null;
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                if (columnIndex < 0) {
                    return null;
                }
                String picturePath = cursor.getString(columnIndex);
                return picturePath;
            } finally {
                cursor.close();
            }
        }
    }
    
    private void uploadFile(String path) {
        KiiObject object = KiiObject.createByUri(Uri.parse(objectUri));
        File f = new File(path);
        Log.v(TAG, "file can read : " + f.canRead());
        
        KiiUploader uploader = object.uploader(this, f);
        uploader.transferAsync(new KiiRTransferCallback() {
        	
        	private NotificationManager notifyMgr = null;
        	private Notification.Builder notifyBuilder = null;
        	
            @Override
            public void onStart(KiiRTransfer operator) {
            	notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            	notifyBuilder = new Notification.Builder(TransferActivity.this);
            	notifyBuilder.setContentTitle("Picture Upload");
            	notifyBuilder.setContentText("Upload in progress");
            	notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
            }

            @Override
            public void onProgress(KiiRTransfer operator,
                    long completedInBytes, long totalSizeinBytes) {
                int parcentage = getProgressParcentage(completedInBytes, totalSizeinBytes);
                
                notifyBuilder.setProgress(100, parcentage, false);
                // Displays the progress bar for the first time.
                notifyMgr.notify(0, notifyBuilder.build());
            }

            @Override
            public void onTransferCompleted(KiiRTransfer operator, Exception e) {
                if (e != null) {
                    showToast("Error in file upload :"
                            + e.getLocalizedMessage());
                }
                
                notifyBuilder.setContentText("Upload complete");
                notifyBuilder.setProgress(0,0,false);
                notifyMgr.notify(R.string.app_name, notifyBuilder.build());
                
                showToast("Upload complete!");
            }
            
        });
    }

    private int getProgressParcentage(long completedInBytes,
            long totalSizeinBytes) {
        return (int) ((completedInBytes * 100.0f) / totalSizeinBytes);
    }

}
