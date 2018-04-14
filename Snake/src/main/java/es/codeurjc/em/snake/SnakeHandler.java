package es.codeurjc.em.snake;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SnakeHandler extends TextWebSocketHandler {

	private static final String SNAKE_ATT = "snake";



	private SnakeGame snakeGame = new SnakeGame();
	private Lock l=new ReentrantLock();
	private Lock lmuro=new ReentrantLock();
	  Executor executor = Executors.newFixedThreadPool(20);
	 
	

	
	
	
	String user_name;
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		
	}

	
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		
		
		
		try {
			int id=snakeGame.snakeIds.getAndIncrement();
			String payload = message.getPayload();
			
			JSONObject json = new JSONObject(payload);
			
			String tipo=json.getString("type");
			
			switch (tipo){
			
			case "user":
				
				System.out.println("User invocado");
				 
		    	Runnable tarea=()->{
		    		try {
		    			l.lock();
					       String msg; //Tendrá el mensaje de confirmación o no de que la sala existe o no
				             String nombre = json.getString("user");
				             System.out.println(nombre);
				            Snake s = new Snake(id, session, nombre);
				             s.setHiloBlock(Thread.currentThread());
				             System.out.println("Nombre de usuario "+nombre);
				             Sala sal;
				             session.getAttributes().put(SNAKE_ATT, s);
				             if(json.getString("ComandoSala").equals("Crear")){
				              
				              //Si no existe la sala la crea
				              if(!snakeGame.comprobarSala(json.getString("Sala"))){
				              
				            	  if(snakeGame.getNumSalas() == 0){
				            		  snakeGame.startTimer();
				            	  }
				               int idSala = snakeGame.salasIds.getAndIncrement();
				               String nom = json.getString("Sala");
				               System.out.println(nom);
				               
				              sal = new Sala(id, nom,s);
				               sal.AñadirJugador(s);
				               

				               
				               s.setSala(sal);
				               System.out.println("Nombre de sala "+nom);
				               
				               snakeGame.addSala(sal);
				               snakeGame.addSnake(s);
				              
				               //snakeGame.lock();
				               msg="{\"type\": \"Okcrear\",\"data\":\"Ok\"}";
				               s.sendMessage(msg);
				               //snakeGame.unlock();
				              
				             }else{
				              
				              //snakeGame.lock();
				              msg="{\"type\": \"Okcrear\",\"data\":\"NotOk\"}";
				              s.sendMessage(msg);
				              //snakeGame.unlock();
				              l.unlock();
				              return;
				              }
				             //si el comando es unir:
				             }else{
				              
				              //si existe la sala (Se tiene que devolver la sala de la lista de salas)
				           if(snakeGame.comprobarSala(json.getString("Sala"))){
				            
				            sal=snakeGame.getSala(json.getString("Sala"));
				             
				            
				           boolean comprobar=  sal.AñadirJugador(s);
				           snakeGame.addSnake(s);
				            
				            //Espero a que termine de añadir el jugador, de lo contrario la siguiente instruccion no se sabe que valor tomaria
				      
				             
				            //Comprueba si hay 4 jugadores comienza el juego
				            
				            int aux3 = sal.contador.availablePermits();
				         if(aux3 == 0){ 
				        	 //snakeGame.lock();
				        	 msg="{\"type\": \"empezar\"}";
				        	 sal.getCreador().sendMessage(msg);
				        	 //snakeGame.unlock();
				        	 sal.partida_empezada=true;
				        	 
				         }
				         if(aux3>=2 && (!sal.partida_empezada)){
				        	 //snakeGame.lock();
				        	 msg="{\"type\": \"iniciar\"}";
			    			   System.out.println("----------------->"+msg);  
			    			   sal.getCreador().sendMessage(msg);
			    			 //snakeGame.unlock();
				         }
				             
				         
				            if(comprobar){ //true si se ha añadido el jugador
				            s.setSala(sal);
				            //snakeGame.lock();
				            msg="{\"type\": \"Okunir\",\"data\":\"Ok\"}";
				            s.sendMessage(msg);
				            //snakeGame.unlock();
				            
				              }
				              else{
				            	  //session.getAttributes().remove(SNAKE_ATT, s);
				               //snakeGame.lock();
				                msg="{\"type\": \"Okunir\",\"data\":\"NotOk\"}";
				               s.sendMessage(msg);
				               //snakeGame.unlock();
				               l.unlock();
				               return;
				              }
				           }//Sala no existe
				           else{
				            	//session.getAttributes().remove(SNAKE_ATT, s);
				               //snakeGame.lock();
				               msg="{\"type\": \"Okunir\",\"data\":\"NotOk\"}";
				               s.sendMessage(msg);
				               //snakeGame.unlock();
				               l.unlock();
				               return;
				              }}
				            
				            
				             
				             	 
				             StringBuilder sb = new StringBuilder();
				             for (Snake snake : sal.getLista().values()) {   
				              sb.append(String.format("{\"id\": %d, \"color\": \"%s\",\"nombre\":\"%s\"}", snake.getId(), snake.getHexColor(),nombre));
				              sb.append(',');
				             }
				             sb.deleteCharAt(sb.length()-1);
				             String msg2 = String.format("{\"type\": \"join\",\"data\":[%s]}", sb.toString());
				             //snakeGame.lock();
				             snakeGame.broadcast(msg2, s.getSala());
				             //snakeGame.unlock();
				             snakeGame.pintar();
				             l.unlock();
				            // snakeGame.pintar();
				      
				      } catch (Exception e) {
				       // TODO Auto-generated catch block
				       e.printStackTrace();
				      }
		    		};
			     executor.execute(tarea);
			     break;
				
			case "direction":
				Snake sn = (Snake) session.getAttributes().get(SNAKE_ATT);
				String aux=json.getString("direction");
				System.out.println("------------------------------------------>"+aux);
				Direction d = Direction.valueOf(aux.toUpperCase());
				sn.setDirection(d);
				return;
			
			
			case "ping":
			return;
				    
			case "cancelar":
				   System.out.println("-------------------------------\n---------------------\n cancelar");
				   Snake sss = (Snake) session.getAttributes().get(SNAKE_ATT);
				  Thread añadir=sss.getHiloBlock();
				  añadir.interrupt();
				   String ms="{\"type\": \"cancelar\",\"info\": \"Espera cancelada\"}";
				   //snakeGame.lock();
				   sss.sendMessage(ms);
				   //snakeGame.unlock();
				   break;

	    	
			case "Init":
				System.out.println("recibido Init");
				Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
				s.getSala().partida_empezada=true;
 			   	//snakeGame.startTimer();
 			   	break;
 			
			case "Muro":
				
				
				//System.err.println("MUROOOOOOOOOOOOOOO");
				
				
				Snake snake = (Snake) session.getAttributes().get(SNAKE_ATT);
				
				
					String cadena=snakeGame.Mejores();
					String msg2 = String.format("{\"type\": \"muro\",\"data\":\"%s\"}",cadena);
					
					snake.sendMessage(msg2);
					
						
						
					
					
					
					/*
				}else{
						
						//Sala sal = (Sala) session.getAttributes().get("sala");
						System.out.println("quedan partidas en juego");
						 //snakeGame.lock();
			        	 String msn="{\"type\": \"partidasEnJuego\"}";
			        	 snake.sendMessage(msn);
			        	 System.out.println("pulsado muro = "+snake.getSala().getPulsadoMuro());
			        	  if(snake.getSala().getPulsadoMuro() == false){
			        		  System.out.println("pulsado muro = "+snake.getSala().getPulsadoMuro());
			        		  snake.getSala().muro();
			        		  snakeGame.DecSalas();
			        	  }
			        	 //snakeGame.unlock();
			        	  	
						//enviar mensaje para mostrar pantallas de espera
					}*/
				 
				break;
				
			case "finPartida":
				
				/*for (Snake snk : snakeGame.getSnakes()){
					String msg = String.format("{\"type\": \"leave\", \"id\": %d,\"nombre\":\"%s\"}", snk.getId(),snk.getName());
					//snakeGame.lock();
					//snakeGame.broadcast(msg, snk.getSala());
					//snakeGame.unlock();
					snakeGame.removeSnake(snk);
				}*/
				
				}	

			System.out.println(payload);

			
			} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status)  {

		System.out.println("Connection closed. Session " + session.getId());

		Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
		
		
		if(s != null){
			String msg = String.format("{\"type\": \"leave\", \"id\": %d,\"nombre\":\"%s\"}", s.getId(),s.getName());
			System.out.println("-------------------------------->"+s.getId());
			//snakeGame.lock();
			try{
		    snakeGame.broadcast(msg, s.getSala());
		    snakeGame.removeSnake(s);
		    snakeGame.pintar();
			}catch(Exception e){
				
			}
		   //snakeGame.unlock();
		    
		
			
		    
		    
			//snakeGame.pintar();
			
		    
		}
		
	}

}
