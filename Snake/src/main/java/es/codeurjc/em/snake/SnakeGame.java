package es.codeurjc.em.snake;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Collection.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
public class SnakeGame {

	private final static long TICK_DELAY = 100;

	private  ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<>();
	
	private  ConcurrentHashMap<Integer, Snake> snakesMuro = new ConcurrentHashMap<>();
	
	ConcurrentHashMap<Integer, Sala> salas = new ConcurrentHashMap<>();
	
	private AtomicInteger numSnakes = new AtomicInteger();
	
	private volatile AtomicInteger numSalas = new AtomicInteger();

	private ScheduledExecutorService scheduler;
	 public AtomicInteger snakeIds = new AtomicInteger(0);
	public  AtomicInteger salasIds = new AtomicInteger(0);
	private Lock l=new ReentrantLock();
	
	
	
	//Este cerrojo controla que no se elimine ninguna snake mientras se está accediendo a una lista que la contenga aunque sea al haber cerrado la conexión(afterconnectionclose)
	 Lock snakeLock=new ReentrantLock();
	
	public void addSnake(Snake snake) {
		snakes.put(snake.getId(), snake);
		snakesMuro.put(snake.getId(), snake);
		numSnakes.getAndIncrement();
	}
	
	public boolean addSala(Sala sala){
		salas.put(sala.getId(), sala);
		numSalas.getAndIncrement();
		
		
		return true;
	}
	
	//comprobar si la sala existe true si existe false si no
	public boolean comprobarSala(String sala){
		
		for(Sala sal : salas.values()) {
		    String key = sal.getName();
		   if(sala.equals(key)){
			   return true;
		   }
		
		}
		
		return false;
	}
	
	public ConcurrentHashMap<Integer, Snake> getSnakes() {
		return snakes;
	}
	public ConcurrentHashMap<Integer, Snake> getSnakesMuro() {
		return snakesMuro;
	}

	public void removeSnake(Snake snake) throws Exception {

		Sala sal=snake.getSala();
		numSnakes.getAndDecrement();
		snakeIds.getAndDecrement();
		//snakeLock.lock();
		sal.EliminarJugador(snake);
		//snakeLock.unlock();
		if(sal.getLista().size()<=1){
			sal.getLista().clear();
			numSnakes.getAndDecrement();
			removeSala(sal);
		}
		
	}
	
	void removeSala(Sala sala) throws Exception{
		
//int count =numSalas.decrementAndGet();
		if(numSnakes.get()<=0){
		
		   resetServer();
		}
		else{
			sala.getLista().clear();
			salas.remove(sala.getId());
		}
	}

	private void tick() {
		
		
		
		
		
		
		for(Sala sal : salas.values()){
		try {
			
			if(sal.partida_empezada==true){
				
				if((sal.getComida()== null)&&(sal.getContadorComida()<=3)){
					generarComida(sal);
					sal.setContadorComida(sal.getContadorComida()+1);
					
				}
				//comprobar!!!
				if(sal.getContadorComida() > 3 || sal.logSerp()){
					//l.lock();
					String mg = String.format("{\"type\": \"fin\"}");
					broadcast(mg, sal);
					sal.partida_empezada=false;
					//l.unlock();
				}
				else{
					
					if(!sal.getLista().isEmpty()){
					for (Snake snake : sal.getLista().values()) {
					    snake.update(sal.getLista().values());
					   }

					   StringBuilder sb = new StringBuilder();
					   
					   for (Snake snake : sal.getLista().values()) {
						//if(snake!=null)
					    sb.append(getLocationsJson(snake));
					    sb.append(',');
					   }
					  
					   sb.deleteCharAt(sb.length()-1);
					   
					   //l.lock();
					   String msg = String.format("{\"type\": \"update\", \"data\" : [%s]}", sb.toString());
					   broadcast(msg,sal);
					   //l.unlock();
				   }
				}
				
			}
				  } catch (Throwable ex) {
				   System.err.println("Exception processing tick()");
				   ex.printStackTrace(System.err);
				  }
			}
				
	}

	private String getLocationsJson(Snake snake) {

		synchronized (snake) {

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("{\"x\": %d, \"y\": %d}", snake.getHead().x, snake.getHead().y));
			for (Location location : snake.getTail()) {
				sb.append(",");
				sb.append(String.format("{\"x\": %d, \"y\": %d}", location.x, location.y));
			}

			return String.format("{\"id\":%d,\"body\":[%s]}", snake.getId(), sb.toString());
		}
	}
	
	
	public Sala getSala(String nombre){
		  Sala s=null;
		 
		  for(ConcurrentHashMap.Entry<Integer, Sala> entry : salas.entrySet()) {
		      String key = entry.getValue().getName();
		     if(nombre.equals(key)){
		      s=entry.getValue();
		      return s; 
		     }
		  
		 }
		  return s;
		}
	

	public synchronized void broadcast(String message, Sala sala) throws Exception {
			
		
		for (Snake snake : sala.getLista().values()) {
			try {
				//System.out.println("Sending message " + message + " to " + snake.getId());
				
				snake.sendMessage(message);

			} catch (Throwable ex) {
				System.err.println("Execption sending message to snake " + snake.getId());
				ex.printStackTrace(System.err);
				removeSnake(snake);
			}
		}
		
	}

	public void startTimer() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopTimer() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}
	public void generarComida(Sala sal) throws Exception{
		  
		  Location l=SnakeUtils.getRandomLocation();
		  Comida com=new Comida(l);
		  sal.setComida(com);
		  
		  String msg = String.format("{\"type\": \"comida\", \"x\": %d,\"y\":\"%d\"}", l.x,l.y);
		  broadcast(msg,sal);
		  
		 }
	
	public  String Mejores() {
		
		ArrayList <Snake> ordenado = new ArrayList<Snake>(snakesMuro.values());
		//ArrayList <Snake> sol = new ArrayList<Snake>();
		Comparator<Snake> comp = new Comparator<Snake>(){
			@Override
			public int compare(Snake s1, Snake s2) {
				return new Integer(s2.getPuntuacion()).compareTo(new Integer(s1.getPuntuacion()));				
			}
			
		};
		Collections.sort(ordenado,comp);

		String sol="";
		
		for(int i=0;i<ordenado.size();i++){
			
			if(i==9){
				break;
			}
			sol+=" Nombre: "+ordenado.get(i).getName()+ " Puntuación: "+ordenado.get(i).getPuntuacion();
			
		}
		
		
		return sol;	
		
	}
	public int getNumSalas(){
		return this.numSalas.get();
	}
	public void DecSalas(){
		this.numSalas.getAndDecrement();
	}
	public void lock(){
		this.l.lock();
	}
	public void unlock(){
		this.l.unlock();
	}
	
	public void pintar(){
		System.out.println("---------------------Serpientes del juego------------------------------------");
		for(Snake s : snakes.values()){
			System.out.println(s.getName());
		}
		
		System.out.println("-------------------Salas del juego ---------------------------------------");
		for(Sala sal : salas.values()){
			System.out.println(sal.getName());
		}
		
		System.out.println("-------------------Serpientes de cada sala-----------------------------");
		for (Sala sala: salas.values()){
			System.out.println("-------------Serpientes de la sala: "+sala.getName()+"------------------");
			for(Snake ss : sala.getLista().values()){
				System.out.println(ss.getName());
			}
		}
	}
	public void resetServer(){
		
			numSalas.set(0);
			snakeIds.set(0);
			salasIds.set(0);
			numSnakes.set(0);
			salas.clear();
			snakes.clear();
			stopTimer();
		
		
	}
	
	
}
