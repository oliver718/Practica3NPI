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
	private long last_update = 0, miliInicial = 0, iniSeg = 0, segMov = 0;
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
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //bloquear la orientación de la pantalla (para que no gire)
        // Creamos una instancia de SoundManager
     	sound = new SoundManager(getApplicationContext());
     	// Set volume rocker mode to media volume
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		// Carga los sonidos que figuran en res/raw
         tada = sound.load(R.raw.tada);
         alerta = sound.load(R.raw.alerta);
    }
    
    @Override
    protected void onResume() {
        super.onResume(); //registro del listener
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);  //se utilizará el acelerómetro como sensor      
        if (sensors.size() > 0) {
        	//indicar tasa de lectura de datos: 
        	//“SensorManager.SENSOR_DELAY_GAME” que es la velocidad mínima para que el acelerómetro pueda usarse
        	sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME); 
        } 
    }
    
    @Override
    protected void onStop() { //anular el registro del listener
    	SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);    	
        sm.unregisterListener(this);
        super.onStop();
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {} //es llamado cuando la precisión del sensor ha cambiado 

	@Override
	public void onSensorChanged(SensorEvent event) { //es llamado cuando los valores del sensor han cambiado
        synchronized (this) { //sincronizar, para evitar problemas de concurrencia
        	long current_time = event.timestamp;
        	long miliActual = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
            
        	//obtener los valores de los ejes del acelerometro
            curX = event.values[0];
            curY = event.values[1];
            curZ = event.values[2];
            
            if (primeraVez) { //ajustar tiempos la primera vez que entra
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
                
                //actualizar variables de mayor y menor valor obtenido de los ejes
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
          
            //mostrar por pantalla los valores del acelerómetro
            ((TextView) findViewById(R.id.txtAccX)).setText(getResources().getString(R.string.valX) + curX);
            ((TextView) findViewById(R.id.txtAccY)).setText(getResources().getString(R.string.valY) + curY);
            ((TextView) findViewById(R.id.txtAccZ)).setText(getResources().getString(R.string.valZ) + curZ); 
            ((TextView) findViewById(R.id.txtmayZ)).setText(getResources().getString(R.string.mayZ) + mayZ);
            ((TextView) findViewById(R.id.txtmenZ)).setText(getResources().getString(R.string.menZ) + menZ);
            ((TextView) findViewById(R.id.txtmayX)).setText(getResources().getString(R.string.mayX) + mayX);
            ((TextView) findViewById(R.id.txtmenX)).setText(getResources().getString(R.string.menX) + menX);
            ((TextView) findViewById(R.id.txtmayY)).setText(getResources().getString(R.string.mayY) + mayY);
            ((TextView) findViewById(R.id.txtmenY)).setText(getResources().getString(R.string.menY) + menY);
            
            
            //controlar si se pulsa alguno de los botones
            setBtnIgual();
            setBtnAccidente();
            
        }
		
	}
	
	//Método que reproduce un sonido si los tres ejes del acelerómetro soportan una fuerza de la gravedad similar
	private void sonarIgualActivo(){
		if(igualX == igualY && igualX == igualZ){//cuando la gravedad actue de igual manera en los tres ejes sonará
        	sound.play(tada);
        	sonarIgual = false;
        	((EditText)findViewById(R.id.textoAyuda)).setText("");
        }
	}
	
	//Método para controlar el control de accidente, entra como parametro el tiempo actual en segundos
	private void controlAccidenteActivo(long seg){
		//si se produce un movimiento muy fuerte
		if((curX > max && curY > max) || (curX > max && curZ > max) || (curY > max && curZ > max) ||
            	(curX < min && curY < min) || (curX < min && curZ < min) || (curY < min && curZ < min)){
            	iniSeg = seg;
            }
			//si despues de producirse el movimiento fuerte han pasado 5 segundos sin que el movil se mueva sonará el pitido
            else if(iniSeg > 0 && (seg - iniSeg) > 5){
            	sound.play(alerta);
            	controlAccidente = false;
            	((EditText)findViewById(R.id.textoAyuda)).setText("");
            }
            else if(iniSeg > 0){//controlar si el movil se mueve despues del movimiento brusco
            	//si siguen produciendose movimientos fuertes se reiniciara el tiempo de espera
            	if((curX + 1) < antCurX || (curX - 1) > antCurX || (curY + 1) < antCurY || (curY - 1) > antCurY || 
            		(curZ + 1) < antCurZ || (curZ - 1) > antCurZ){
            		iniSeg = seg;
            		if(segMov == 0)
            			segMov = seg;
            		//si pasado 10 segundos del movimiento fuerte el movil se mueve suavemente, no sonará el pitido y se reinicia todo
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
	
	//Método que controla que se pulse el botón de "Buscar equilibrio"
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
	
	//Método que controla que se pulse el botón de "Control de accidente"
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