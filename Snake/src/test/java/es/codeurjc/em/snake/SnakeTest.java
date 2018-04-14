package es.codeurjc.em.snake;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.*;


public class SnakeTest {
  AtomicInteger contador=new AtomicInteger(0);
  
  
  
  
 @BeforeClass
 public static void startServer(){
  Application.main(new String[]{ "--server.port=8080" });
 }
  
 @Test
 public void testConnection() throws Exception {
	 System.out.println("---------------------------------------TEST_CONEXION--------------------------------------");
  WebSocketClient wsc = new WebSocketClient();
  wsc.connect("ws://127.0.0.1:8080/snake");
        wsc.disconnect();  
 }

 @Test
 public void testJoin() throws Exception {
	 System.out.println("---------------------------------------TEST_JOIN--------------------------------------");
	 
  contador.set(0);
  CyclicBarrier c=new CyclicBarrier(5);
    Executor executor = Executors.newFixedThreadPool(4);
  
  AtomicReferenceArray<String> firstMsg = new AtomicReferenceArray<String>(4);
  
  
  Runnable tarea=()->{

int id=Character.getNumericValue(Thread.currentThread().getName().charAt(Thread.currentThread().getName().length()-1)-1);
   
   
WebSocketClient wsc = new WebSocketClient();
   
wsc.onMessage((session, msg) -> {
 
 System.out.println("TestMessage: "+msg);
if(msg.contains("update")){
 firstMsg.compareAndSet(id,null, msg);
}
});

   try{
   wsc.connect("ws://127.0.0.1:8080/snake");
   
          
   System.out.println("Connected");
   
   String msg;
   System.out.println("Nombre "+Thread.currentThread().getName());
   if(contador.getAndIncrement()==0){
       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"1\"}", "Creador");
          
         }else{
       
         msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Unir\",\"Sala\":\"1\"}", "Union");
         }
   wsc.sendMessage(msg);
   
   Thread.sleep(1500);
    String ms = String.format("{\"type\": \"finPartida\"}");
    wsc.sendMessage(ms);
   wsc.disconnect(); 
   c.await();
   
   }catch(Exception e){
    e.printStackTrace();
   }
  
  };
  
  
  
  
  
  
  executor.execute(tarea);
 Thread.sleep(500);
  executor.execute(tarea);
  //Thread.sleep(1000);
  executor.execute(tarea);
 //Thread.sleep(1000);
  executor.execute(tarea);
  
  
  Thread.sleep(2500);
  c.await();
  for(int h=0;h<4;h++){
   
   String msg=firstMsg.get(h);
   assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("update"));
  }
  
 }
 
 @Test
 public void testIniciar() throws Exception {
 System.out.println("---------------------------------------TEST_INICIAR--------------------------------------");
 contador.set(0);
  CyclicBarrier c=new CyclicBarrier(3);
     Executor executor = Executors.newFixedThreadPool(2);
     
     AtomicReferenceArray<String> firstMsg = new AtomicReferenceArray<String>(3);
     
     
     Runnable tarea=()->{

   int id=Character.getNumericValue(Thread.currentThread().getName().charAt(Thread.currentThread().getName().length()-1)-1);
      
      
   WebSocketClient wsc = new WebSocketClient();
      
   wsc.onMessage((session, msg) -> {
    System.out.println("TestMessage: "+msg);
   
    if(msg.contains("iniciar")){
     
     firstMsg.compareAndSet(2,null, msg);
     
     try {
   wsc.sendMessage("{\"type\":\"Init\"}");
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
    }
    if(msg.contains("update")){
     
     firstMsg.compareAndSet(id,null, msg);
     
    }

  
   });

      try{
      wsc.connect("ws://127.0.0.1:8080/snake");
      
             
             System.out.println("Connected");
      
      String msg;
      System.out.println("Nombre "+Thread.currentThread().getName());
      if(contador.getAndIncrement()==0){
          msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"1\"}", "Creador");
            

            }else{
          
            msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Unir\",\"Sala\":\"1\"}", "Union");
            }
      wsc.sendMessage(msg);
      
      Thread.sleep(5000);
      String ms = String.format("{\"type\": \"finPartida\"}");
      wsc.sendMessage(ms);
      wsc.disconnect(); 
      c.await();
      }catch(Exception e){
       e.printStackTrace();
      }
     
     };
     
     
     
     
     
     
     executor.execute(tarea);
     Thread.sleep(500);
     executor.execute(tarea);
    
     
     
     Thread.sleep(7000);
     c.await();
     
      
      String msg=firstMsg.get(0);
      assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("update"));
      msg=firstMsg.get(1);
      assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("update"));
      msg=firstMsg.get(2);
      assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("iniciar"));
  
  
  
  
  
  
 
}
 @Test
 public void testFin() throws Exception{
	 
	 System.out.println("---------------------------------------TEST_FIN--------------------------------------");
	contador.set(0);
	 
	 CyclicBarrier c=new CyclicBarrier(3);
	  Executor executor = Executors.newFixedThreadPool(2);
	  
	  AtomicReferenceArray<String> firstMsg = new AtomicReferenceArray<String>(3);
	  
	  
	  Runnable tareaCreador=()->{

	int id=Character.getNumericValue(Thread.currentThread().getName().charAt(Thread.currentThread().getName().length()-1)-1);
	   
	   
	WebSocketClient wsc = new WebSocketClient();
	   
	wsc.onMessage((session, msg) -> {
	 
	 System.out.println("TestMessage: "+msg);
	if(msg.contains("leave")){
	 firstMsg.compareAndSet(2,null, msg);
	}
	if(msg.contains("update")){
		 firstMsg.compareAndSet(id,null, msg);
		}
	
	
	if(msg.contains("iniciar")){

	     try {
	   wsc.sendMessage("{\"type\":\"Init\"}");
	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	}
	
	});

	   try{
	   wsc.connect("ws://127.0.0.1:8080/snake");
	   
	          
	   System.out.println("Connected");
	   
	   String msg;
	   System.out.println("Nombre "+Thread.currentThread().getName());
	   
	       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"1\"}", "Creador");
	         
	        
	   wsc.sendMessage(msg);
	   
	   Thread.sleep(6500);
	   String ms = String.format("{\"type\": \"finPartida\"}");
	    wsc.sendMessage(ms);
	   wsc.disconnect(); 
	   c.await();
	   }catch(Exception e){
	    e.printStackTrace();
	   }
	  
	  };
	  
	  Runnable tareaUnir=()->{

			int id=Character.getNumericValue(Thread.currentThread().getName().charAt(Thread.currentThread().getName().length()-1)-1);
			   
			   
			WebSocketClient wsc = new WebSocketClient();
			   
			wsc.onMessage((session, msg) -> {
			 
			 System.out.println("TestMessage: "+msg);
			if(msg.contains("update")){
			 firstMsg.compareAndSet(id,null, msg);
			}
			});

			   try{
			   wsc.connect("ws://127.0.0.1:8080/snake");
			   
			          
			   System.out.println("Connected");
			   
			   String msg;
			   System.out.println("Nombre "+Thread.currentThread().getName());
			  
			       
			         msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Unir\",\"Sala\":\"1\"}", "Union");
			         
			   wsc.sendMessage(msg);
			   
			   Thread.sleep(4000);
			   String ms = String.format("{\"type\": \"finPartida\"}");
			    wsc.sendMessage(ms);
			   wsc.disconnect(); 
			   c.await();
			   }catch(Exception e){
			    e.printStackTrace();
			   }
			  
			  };
	  
	  
	  
	  
	  executor.execute(tareaCreador);
	  Thread.sleep(500);
	  executor.execute(tareaUnir);
	

	  
	  
	  Thread.sleep(8000);
	  c.await();
	  String msg=firstMsg.get(0);
	   assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("update"));
	   msg=firstMsg.get(1);
	   assertTrue("The first message should contain 'update', but it is "+msg, msg.contains("update"));
	   msg=firstMsg.get(2);
	   assertTrue("The first message should contain 'leave', but it is "+msg, msg.contains("leave"));
	 
	 
	 
	 
	 
	 
	 
	 
 }
 
 @Test
 public void testEspera() throws Exception{
	 
	contador.set(0);
	  CyclicBarrier c=new CyclicBarrier(6);
	    Executor executor = Executors.newFixedThreadPool(5);
	  
	  AtomicReferenceArray<String> firstMsg = new AtomicReferenceArray<String>(7);
	  
	 
	  Runnable tarea=()->{

	int id=Character.getNumericValue(Thread.currentThread().getName().charAt(Thread.currentThread().getName().length()-1)-1);
	   
	 WebSocketClient wsc = new WebSocketClient();
	
	   
	wsc.onMessage((session, msg) -> {
	 
	 System.out.println("TestMessage: "+msg);
	if(msg.contains("join")){
	 firstMsg.compareAndSet(id,null, msg);
	}
	if(msg.contains("espera")){
		 firstMsg.compareAndSet(5,null, msg);
		}
	if(msg.contains("leave")){
		firstMsg.compareAndSet(6,null, msg);
	}
	});

	   try{
	   wsc.connect("ws://127.0.0.1:8080/snake");
	   
	          
	   System.out.println("Connected");
	   
	   String msg;
	   System.out.println("Nombre "+Thread.currentThread().getName());
	   if(contador.getAndIncrement()==0){
	       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"1\"}", "Creador");
	          
	          wsc.sendMessage(msg);
	  	      Thread.sleep(1500);
	  	  	
  	    	
	  	    wsc.disconnect(); 
	  	    c.await();
	         }else{
	       
	         msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Unir\",\"Sala\":\"1\"}", "Union");
	         wsc.sendMessage(msg);
	         Thread.sleep(3000);
	         
	
		   
	 	   wsc.disconnect(); 
	 	   c.await();
	 	   	
	         }
	   
	   
	   
	   
	   }catch(Exception e){
	    e.printStackTrace();
	   }
	  
	  };
	  
	  
	  
	  
	  
	  
	  executor.execute(tarea);
	 Thread.sleep(500);
	  executor.execute(tarea);
	  //Thread.sleep(1000);
	  executor.execute(tarea);
	 //Thread.sleep(1000);
	  executor.execute(tarea);
	  
	  executor.execute(tarea);
	  
	  Thread.sleep(4000);
	  c.await();
	 
	  for(int h=0;h<4;h++){
	   
	   String msg=firstMsg.get(h);
	   assertTrue("The first message should contain 'join', but it is "+msg, msg.contains("join"));
	  }
	  String msg=firstMsg.get(5);
	 
	  assertTrue("The first message should contain 'espera', but it is "+msg, msg.contains("espera"));
	 
	   msg=firstMsg.get(6);
		 
	  assertTrue("The first message should contain 'leave', but it is "+msg, msg.contains("leave"));
	 
	 
 }
@Test
 public void testCarga() throws InterruptedException, BrokenBarrierException{
	 
	 String nombres[]={"0","1","2","3","4","5","6","7","8","9"};
	 contador.set(0);
	  CyclicBarrier c=new CyclicBarrier(11);
	    Executor executor = Executors.newFixedThreadPool(10);
	  
	  AtomicReferenceArray<String> firstMsg = new AtomicReferenceArray<String>(10);
	  
	  
	  
  for(String s:nombres){
		  
		  
	  Runnable tarea=()->{

			int id=Character.getNumericValue(s.charAt(0));
			   
			   
			WebSocketClient wsc = new WebSocketClient();
			   
			wsc.onMessage((session, msg) -> {
			 
			 System.out.println("TestMessage: "+msg);
			if(msg.contains("Okcrear")){
			 firstMsg.compareAndSet(id,null, msg);
			}
			});

			   try{
			   wsc.connect("ws://127.0.0.1:8080/snake");
			   
			          
			   System.out.println("Connected");
			   
			   String msg;
			   System.out.println("Nombre "+Thread.currentThread().getName());
			  
			       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"%s\"}", "Creador"+s,s);
			          System.out.println(msg);
			       
			   wsc.sendMessage(msg);
			   
			   Thread.sleep(2000);
			  
			   
			   wsc.disconnect(); 
			   c.await();
			   
			   }catch(Exception e){
			    e.printStackTrace();
			   }
			  
			  };
		  executor.execute(tarea);
		  
	  }
	  Thread.sleep(3000);
	  c.await();
	  for(int h=0;h<10;h++){
		   
		   String msg=firstMsg.get(h);
		   System.out.println(msg);
		   assertTrue("The first message should contain 'Okcrear', but it is "+msg, msg.contains("Okcrear"));
		  }
	  
	  
	  
	AtomicReferenceArray<String> secondMsg = new AtomicReferenceArray<String>(10);
	  
	  AtomicReferenceArray<String> puntuaciones = new AtomicReferenceArray<String>(10);
	 
	  Executor executor2 = Executors.newFixedThreadPool(10);
	  CyclicBarrier barrera=new CyclicBarrier(11);
	String nombresCreador[]={"0","5"};
	  for(String s:nombresCreador){
		  
		  
		  
		  Runnable tareaCreador=()->{

				int id=Character.getNumericValue(s.charAt(0));
				   
				   
				WebSocketClient wsc = new WebSocketClient();
				   
				wsc.onMessage((session, msg) -> {
				 
				 System.out.println("TestMessage: "+msg);
				if(msg.contains("join")){
				 secondMsg.compareAndSet(id,null, msg);
				}
				 if(msg.contains("muro")){
					 puntuaciones.compareAndSet(id,null, msg);
				 }
				});

				   try{
				   wsc.connect("ws://127.0.0.1:8080/snake");
				   
				          
				   System.out.println("Connected");
				   
				   String msg;
				   System.out.println("Nombre "+Thread.currentThread().getName());
				  String sala;
				  
				  if(s.equals("0")){
					  sala="SalaA";
				  }else{
					  sala="SalaB";
				  }
				   
				       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Crear\",\"Sala\":\"%s\"}", "Creador"+s,sala);
				          System.out.println(msg);
				       
				   wsc.sendMessage(msg);
				   
				   Thread.sleep(10000);
				  
				   	  msg="{\"type\": \"Muro\"}";
			          System.out.println(msg);
			          wsc.sendMessage(msg);
			          Thread.sleep(1000);
			          
				   wsc.disconnect(); 
				  barrera.await();
				   
				   }catch(Exception e){
				    e.printStackTrace();
				   }
				  
				  };
			  executor2.execute(tareaCreador);
		  
		  
		  
	  }
	  Thread.sleep(500);
	  
	  String nombresUnir[]={"1","2","3","4","6","7","8","9"};
	  
	  for(String s:nombresUnir){
		  
		  
		  
		  Runnable tareaUnion=()->{

				int id=Character.getNumericValue(s.charAt(0));
				   
				   
				WebSocketClient wsc = new WebSocketClient();
				   
				wsc.onMessage((session, msg) -> {
				 
				 System.out.println("TestMessage: "+msg);
				 if(msg.contains("join")){
					 secondMsg.compareAndSet(id,null, msg);
					 }
				 if(msg.contains("espera")){
					 secondMsg.compareAndSet(id,null, msg);
				 }
				 if(msg.contains("muro")){
					 puntuaciones.compareAndSet(id,null, msg);
				 }
				});

				   try{
				   wsc.connect("ws://127.0.0.1:8080/snake");
				   
				          
				   System.out.println("Connected");
				   
				   String msg;
				   System.out.println("Nombre "+Thread.currentThread().getName());
				  String sala;
				  
				  if(s.equals("1") || s.equals("2") || s.equals("3") || s.equals("4")){
					  sala="SalaA";
				  }else{
					  
					  sala="SalaB";
				  }
				   
				       msg=String.format("{\"type\": \"user\", \"user\": \"%s\", \"ComandoSala\":\"Unir\",\"Sala\":\"%s\"}", "Union"+s,sala);
				          System.out.println(msg);
				       
				   wsc.sendMessage(msg);
				   
				   Thread.sleep(10000);
				  
				   msg="{\"type\": \"Muro\"}";
			          System.out.println(msg);
			          wsc.sendMessage(msg);
			          Thread.sleep(1000);
				   
				   wsc.disconnect(); 
				   barrera.await();
				   
				   }catch(Exception e){
				    e.printStackTrace();
				   }
				  
				  };
			  executor2.execute(tareaUnion);
  
	 
 }
	  Thread.sleep(14000);
	  barrera.await();
	  
	  for(int h=0;h<10;h++){
		   
		   String msg=secondMsg.get(h);
		   //System.out.println(msg);
		   assertTrue("The first message should contain 'join'or'espera', but it is "+msg, msg.contains("join")||msg.contains("espera"));
		  }
	  
	  System.out.println("-------------------PUNTUACIONES---------------------");
	  
	  System.err.println(puntuaciones.get(0));

		   
		   String msg=puntuaciones.get(0);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(1);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(2);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(3);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(4);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(5);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(6);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(7);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(8);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
		    msg=puntuaciones.get(9);
		   assertTrue("The first message should contain 'muro', but it is "+msg, msg.contains("muro"));
}
}