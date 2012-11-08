package no.rodentcorp;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;

public class MainActivity extends Activity {

    private SurfaceView surface_view;
    private Camera mCamera;
    SurfaceHolder.Callback sh_ob = null;
    SurfaceHolder surface_holder = null;
    SurfaceHolder.Callback sh_callback = null;
    Camera.Parameters p;
    Button button;
    boolean lightToggle;
    boolean screenLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);      //Locks the screen orientation to portrait mode
        surface_view = new SurfaceView(getApplicationContext());
        addContentView(surface_view, new LayoutParams(1, 1));                   //Adds the view to the total, but makes it 1x1 pixel(hack for some brands such as Samsung)
        if (surface_holder == null) {
            surface_holder = surface_view.getHolder();
        }
        sh_callback = my_callback();
        surface_holder.addCallback(sh_callback);
        button = (Button) findViewById(R.id.button_onoff);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (lightToggle && !screenLock) {                               //Turn flash off
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    lightToggle = false;
                } else if (!lightToggle && !screenLock) {                       //Turn flash on
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    lightToggle = true;
                } else if (screenLock) {                                        //Informs the user that the screen is locked
                    Toast toast = Toast.makeText(getApplicationContext(), "The screen is locked. Click and hold to unlock the screen.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        button.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (screenLock) {                                               //Unlocks the screen(actually just the on/off button)
                    screenLock = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Screen unlocked.", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (!screenLock) {                                       //Locks the screen
                    screenLock = true;
                    Toast toast = Toast.makeText(getApplicationContext(), "The screen has been locked. Click and hold again to unlock screen.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
            }
        });
    }

    SurfaceHolder.Callback my_callback() {
        SurfaceHolder.Callback ob1 = new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {                //Releases the camera when the app is not in use(onPause, onDestroy etc.)
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {                  //Gets the camera when the app is to be (re)initialized(onResume, onCreate etc.)
                mCamera = Camera.open();

                try {
                    mCamera.setPreviewDisplay(holder);                          //Attaches a preview view to the camera. This is neccesary for some specific models(Samsung among others)
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,//Initializes the flash. In this app, this method is called straight after surfaceCreated due
                    int height) {                                                   //to the screen orientation lock. Default state is off and not locked.
                p = mCamera.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
                mCamera.startPreview();
                lightToggle = false;
                screenLock = false;
            }
        };
        return ob1;
    }
}