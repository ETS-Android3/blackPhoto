package com.urrecliner.blackphoto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.blackphoto.Vars.SPAN_COUNT;
import static com.urrecliner.blackphoto.Vars.eventFolderAdapter;
import static com.urrecliner.blackphoto.Vars.eventMP4Folder;
import static com.urrecliner.blackphoto.Vars.selectedJpgFolder;
import static com.urrecliner.blackphoto.Vars.jpgFullFolder;
import static com.urrecliner.blackphoto.Vars.eventFolders;
import static com.urrecliner.blackphoto.Vars.mContext;
import static com.urrecliner.blackphoto.Vars.mActivity;
import static com.urrecliner.blackphoto.Vars.eventFolderView;
import static com.urrecliner.blackphoto.Vars.spanWidth;
import static com.urrecliner.blackphoto.Vars.utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        mActivity = this;
        askPermission();
        File[] fullFileList = jpgFullFolder.listFiles(file -> (file.getPath().contains("V2")));
        if (fullFileList == null) {
            Toast.makeText(this,"No event Jpg Folders",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Arrays.sort(fullFileList);
        eventFolders = new ArrayList<>();
        eventFolders.addAll(Arrays.asList(fullFileList));
        utils = new Utils();
        utils.log("blackPhoto", "Start--");
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        spanWidth = (size.x / SPAN_COUNT) * 90 / 100;
//        Log.w("spanWith","x is "+size.x+", spanWidth="+spanWidth);
        eventFolderView = findViewById(R.id.eventView);
        eventFolderAdapter = new EventFolderAdapter();
        eventFolderView.setAdapter(eventFolderAdapter);
        utils.readyPackageFolder(selectedJpgFolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.erase) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Old Events");
            builder.setMessage("Delete Old Event Files?");
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            File[] mp4Files = eventMP4Folder.listFiles(file -> (file.getPath().contains("mp4")));
                            if (mp4Files != null) {
                                for (File mp4: mp4Files) {
                                    mp4.delete();
                                }
                                Toast.makeText(getApplicationContext(), mp4Files.length+" event mp4 deleted ", Toast.LENGTH_SHORT).show();
                            }
                            File[] jpgFolders = jpgFullFolder.listFiles(file -> (file.getPath().contains("V2")));
                            if (jpgFolders != null) {
                                for (File fJpg: jpgFolders) {
                                    EventFolderAdapter.deleteRecursive(fJpg);
                                    Toast.makeText(getApplicationContext(), fJpg.getName()+" deleted ", Toast.LENGTH_SHORT).show();
                                }
                            }
                            File[] jpgFiles = selectedJpgFolder.listFiles();
                            if (jpgFiles != null) {
                                for (File fJpg: jpgFiles) {
                                    fJpg.delete();
                                }
                                Toast.makeText(getApplicationContext(), jpgFiles.length+" selected Jpgs deleted ", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                    });
            builder.setNegativeButton("No, not Now",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
        }
        return false;
    }


    // ↓ ↓ ↓ P E R M I S S I O N   RELATED /////// ↓ ↓ ↓ ↓  BEST CASE 20/09/27 with no lambda
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    String [] permissions;

    private void askPermission() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;//This array contain
        } catch (Exception e) {
            Log.e("Permission", "Not done", e);
        }

        permissionsToRequest = findUnAskedPermissions();
        if (permissionsToRequest.size() != 0) {
            requestPermissions((String[]) permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions() {
        ArrayList <String> result = new ArrayList<>();
        for (String perm : permissions) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (Object perms : permissionsToRequest) {
                if (hasPermission((String) perms)) {
                    permissionsRejected.add((String) perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
        }
    }

    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                (dialog, which) -> MainActivity.this.requestPermissions(permissionsRejected.toArray(
                        new String[0]), ALL_PERMISSIONS_RESULT));
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}