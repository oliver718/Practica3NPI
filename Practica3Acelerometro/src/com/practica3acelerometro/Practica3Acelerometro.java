package com.practica3acelerometro;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
//import android.widget.Toast;
import android.media.AudioManager;

public class Practica3Acelerometro extends Activity implements SensorEventListener {
	SoundManager sound;
	int tada;
	private long last_update = 0, miliInicial = 0;  //last_movement = 0;
    private float curX = 0, curY = 0, curZ = 0;
    private int igualX = 1, igualY = 2, igualZ = 3;//al inicio serán distintos
    private boolean sonarIgual = false, primeraVez = true, controlAccidente = true;
    	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Creamos una instancia de SoundManager
     	sound = new SoundManager(getApplicationContext());
     	// Set volume rocker mode to media volume
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		// Lee los sonidos que figuran en res/raw
         tada = sound.load(R.raw.tada);
         //sound.play(tada);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);        
        if (sensors.size() > 0) {
        	sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        } 
    }
    
    @Override
    protected void onStop() {
    	SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);    	
        sm.unregisterListener(this);
        super.onStop();
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
        	long iniSeg;
        	long current_time = event.timestamp;
        	long miliActual = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
            
            curX = event.values[0];
            curY = event.values[1];
            curZ = event.values[2];
            
            if (primeraVez) {
                last_update = current_time;
                miliInicial = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
                primeraVez = false;
            }
            
            long seg = (miliActual - miliInicial)/1000; //contador de segundos
            long miliseg = (miliActual - miliInicial); //contador de milisegundos
            long time_difference = current_time - last_update;
            if (time_difference > 0) {
                last_update = current_time;
                igualX = (int)curX;
                igualY = (int)curY;
                igualZ = (int)curZ;
            }
          
            ((TextView) findViewById(R.id.txtAccX)).setText(getResources().getString(R.string.valX) + curX);
            ((TextView) findViewById(R.id.txtAccY)).setText(getResources().getString(R.string.valY) + curY);
            ((TextView) findViewById(R.id.txtAccZ)).setText(getResources().getString(R.string.valZ) + curZ); 
            
            
            if(igualX == igualY && igualX == igualZ && sonarIgual){//cuando la gravedad actue de igual manera en los tres ejes sonará
            	sound.play(tada);
            	sonarIgual = false;
            	((EditText)findViewById(R.id.textoAyuda)).setText("");
            }
            
            setBtnIgual();
            
        }
		
	}    
	
	private void setBtnIgual(){
		Button igualGrav = (Button) findViewById(R.id.btnIgual);
        igualGrav.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sonarIgual = true;
				((EditText)findViewById(R.id.textoAyuda)).setText(getResources().getString(R.string.TextoAyuda));
			}
		});
	}
	/*public void esperarYCerrar(int milisegundos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //acciones que se ejecutan tras los milisegundos
                //finalizarApp();
            }
        }, milisegundos);
    }*/
    
}