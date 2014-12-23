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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.media.AudioManager;

public class Practica3Acelerometro extends Activity implements SensorEventListener {
	SoundManager sound;
	int tada, alerta;
	private long last_update = 0, miliInicial = 0, iniSeg = 0, segMov = 0;  //last_movement = 0;
    private float curX = 0, curY = 0, curZ = 0;
    private float antCurX = 0, antCurY = 0, antCurZ = 0;
    private int igualX = 1, igualY = 2, igualZ = 3;//al inicio serán distintos
    private boolean sonarIgual = false, primeraVez = true, controlAccidente = false;
    private float mayZ = 0, menZ = 0, mayX = 0, menX = 0, mayY = 0, menY = 0;
    private float max = 19, min = -19;
    	
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
         alerta = sound.load(R.raw.alerta);
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
            long time_difference = current_time - last_update;
            if (time_difference > 0) {
                last_update = current_time;
                igualX = (int)curX;
                igualY = (int)curY;
                igualZ = (int)curZ;
                
                if(curZ > mayZ)
                	mayZ = curZ;
                else if(curZ < menZ)
                	menZ = curZ;
                if(curX > mayX)
                	mayX = curX;
                else if(curX < menX)
                	menX = curX;
                if(curY > mayY)
                	mayY = curY;
                else if(curY < menY)
                	menY = curY;
            }
            
            if(controlAccidente)
            	controlAccidenteActivo(seg);
            if(sonarIgual)
            	sonarIgualActivo();
          
            ((TextView) findViewById(R.id.txtAccX)).setText(getResources().getString(R.string.valX) + curX);
            ((TextView) findViewById(R.id.txtAccY)).setText(getResources().getString(R.string.valY) + curY);
            ((TextView) findViewById(R.id.txtAccZ)).setText(getResources().getString(R.string.valZ) + curZ); 
            ((TextView) findViewById(R.id.txtmayZ)).setText(getResources().getString(R.string.mayZ) + mayZ);
            ((TextView) findViewById(R.id.txtmenZ)).setText(getResources().getString(R.string.menZ) + menZ);
            ((TextView) findViewById(R.id.txtmayX)).setText(getResources().getString(R.string.mayX) + mayX);
            ((TextView) findViewById(R.id.txtmenX)).setText(getResources().getString(R.string.menX) + menX);
            ((TextView) findViewById(R.id.txtmayY)).setText(getResources().getString(R.string.mayY) + mayY);
            ((TextView) findViewById(R.id.txtmenY)).setText(getResources().getString(R.string.menY) + menY);
            
            setBtnIgual();
            setBtnAccidente();
            
        }
		
	}
	
	private void sonarIgualActivo(){
		if(igualX == igualY && igualX == igualZ){//cuando la gravedad actue de igual manera en los tres ejes sonará
        	sound.play(tada);
        	sonarIgual = false;
        	((EditText)findViewById(R.id.textoAyuda)).setText("");
        }
	}
	
	private void controlAccidenteActivo(long seg){
		if((curX > max && curY > max) || (curX > max && curZ > max) || (curY > max && curZ > max) ||
            	(curX < min && curY < min) || (curX < min && curZ < min) || (curY < min && curZ < min)){
            	iniSeg = seg;
            }

            else if(iniSeg > 0 && (seg - iniSeg) > 5){
            	sound.play(alerta);
            	controlAccidente = false;
            	((EditText)findViewById(R.id.textoAyuda)).setText("");
            }
            else if(iniSeg > 0){
            	if((curX + 1) < antCurX || (curX - 1) > antCurX || (curY + 1) < antCurY || (curY - 1) > antCurY || 
            		(curZ + 1) < antCurZ || (curZ - 1) > antCurZ){
            		iniSeg = seg;
            		if(segMov == 0)
            			segMov = seg;
            		else if((seg - segMov) > 10){
            			antCurX = antCurY = antCurZ = iniSeg = segMov = 0;
            			mayZ = menZ = mayX = menX = mayY = menY = 0;
            		}
            	}
            }
            
            antCurX = curX;
            antCurY = curY;
            antCurZ = curZ;
	}
	
	private void setBtnIgual(){
		Button igualGrav = (Button) findViewById(R.id.btnIgual);
        igualGrav.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sonarIgual = true;
				((EditText)findViewById(R.id.textoAyuda)).setText(getResources().getString(R.string.TextoAyudaGravedad));
			}
		});
	}
	
	private void setBtnAccidente(){
		Button igualGrav = (Button) findViewById(R.id.btnAccidente);
        igualGrav.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controlAccidente = true;
				antCurX = antCurY = antCurZ = iniSeg = segMov = 0;
    			mayZ = menZ = mayX = menX = mayY = menY = 0;
				((EditText)findViewById(R.id.textoAyuda)).setText(getResources().getString(R.string.TextoAyudaAccidente));
			}
		});
	}
    
}