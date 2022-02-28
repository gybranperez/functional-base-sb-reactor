package mx.com.ciecas.app;

import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import mx.com.ciecas.app.models.Comments;
import mx.com.ciecas.app.models.User;
import mx.com.ciecas.app.models.UserComments;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class BaseSpringBootReactorApplication implements CommandLineRunner{

	public static final Logger log = LoggerFactory.getLogger(BaseSpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(BaseSpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception { 
		
		ejemploBackPressureLimitRate();
		
	}
	
	public void ejemploBackPressureLimitRate() { // Automatico
		Flux
		.range(1, 10)
		.log()
		.limitRate(3)
		.subscribe();
	}
	
	public void ejemploBackPressure() { // Manual
		
		Flux
			.range(1, 10)
			.log()
			.subscribe( new Subscriber<Integer>() {
				
				private Subscription s;
				private Integer limite = 2;
				private Integer consumido = 0;
				
				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(limite);
					
				}

				@Override
				public void onNext(Integer t) {
					log.info(t.toString());
					consumido++;
					if(consumido == limite) {
						consumido = 0;
						s.request(limite);
					}
				}

				@Override
				public void onError(Throwable t) {}

				@Override
				public void onComplete() {}
				
			});
		
	}
	
	public void ejemploIntervalFromCreate() {
		Flux.create(emitter -> {
			
			Timer timer = new Timer();
			
			timer.schedule(
					new TimerTask() {
						private Integer count = 0;
						@Override
						public void run() {
							emitter.next(++count);
							if(count == 10) {
								timer.cancel();
								emitter.complete();
							}
							
							/*
							 * if(count == 5) { timer.cancel(); emitter.error( new
							 * InterruptedException("ERROR, se ha detenido el flujo en 5") ); }
							 */
							
						}
					}
					, 1000 	//delay
					, 1000 	//period
			);
		})
		.subscribe(
			next -> log.info(next.toString()),
			error -> log.error(error.getMessage()),
			() -> log.info("Terminado")
		);
	}
	
	public void ejemploInfiniteInterval() throws Exception{
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux
			.interval(Duration.ofSeconds(1))
			
			.doOnTerminate(latch::countDown) //finally
			
			.flatMap(i -> {
				return (i>=5) ? Flux.error(new InterruptedException("Solo hasta 5")) : Flux.just(i);
			})
			
			.map(i -> "Hola " + i)
			
			.retry(2)
			
			.subscribe(log::info , e -> log.error(e.getMessage()));
		
		latch.await();
	}
	
	public void ejemploDelayElements() throws Exception{
		
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		
		rango.blockLast();
	}
	
	public void ejemploInterval() throws Exception{
		
		Flux<Integer> rango = Flux.range(1, 12);
		
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		  
		rango.zipWith( retraso, (ra , re) -> ra) 
		.doOnNext(i -> log.info(i.toString())) 
		.blockLast();
		 
			//.subscribe();
	}
	
	public void ejemploZipWithRanges() throws Exception{
		
		Flux.just(1,2,3,4)
			.map(v -> v*2)
			.zipWith( 
					Flux.range(0, 4) , 
					(uno, dos) -> 
						String.format("Primer flux: %d , Segundo flux: %d", uno, dos )
			)
			.subscribe(log::info);
				
	}

	public void ejemploUserCommentsZipWith2() throws Exception{
		
		Mono<User> userMono = Mono.fromCallable( () -> new User("Gybran", "Perez") );
		
		Mono<Comments> commentsMono = Mono.fromCallable( () -> {
			Comments c = new Comments();
			c.addComment("admirable");
			c.addComment("deseable");
			c.addComment("amable");
			return c;
		});
		
		Mono<UserComments> ucMono = userMono
						.zipWith( commentsMono )
						.map(tuple -> {
							User u = tuple.getT1();
							Comments c = tuple.getT2();
							return new UserComments(u, c);
						});
						
		ucMono.subscribe( uc -> log.info(uc.toString()) );
		
	}
	
	public void ejemploUserCommentsZipWith() throws Exception{
		
		Mono<User> userMono = Mono.fromCallable( () -> new User("Gybran", "Perez") );
		
		Mono<Comments> commentsMono = Mono.fromCallable( () -> {
			Comments c = new Comments();
			c.addComment("admirable");
			c.addComment("deseable");
			c.addComment("amable");
			return c;
		});
		
		Mono<UserComments> ucMono = userMono
						.zipWith( commentsMono , ( user , comments ) -> new UserComments(user, comments));
						
		ucMono.subscribe( uc -> log.info(uc.toString()) );
		
	}
	
	public void ejemploUserCommentsFlatmap() throws Exception{
		
		Mono<User> userMono = Mono.fromCallable( () -> new User("Gybran", "Perez") );
		
		Mono<Comments> commentsMono = Mono.fromCallable( () -> {
			Comments c = new Comments();
			c.addComment("admirable");
			c.addComment("deseable");
			c.addComment("amable");
			return c;
		});
		
		userMono.flatMap(user -> 
							commentsMono.map( 
									commentsList -> 
										new UserComments(user, commentsList) 
									) 
						)
				.subscribe( uc -> log.info(uc.toString()) );
		
	}
	
	public void ejemploCollectList() throws Exception{
		List<User> list = Stream
				.of(
					new User("Juan", "Perez"),
					new User("Carlos", "Hernandez"),
					new User("Juan", "Paramo"),
					new User("Rodrigo", "Rosales"),
					new User("Carlos", "Juarez")
				)
				.collect(Collectors.toList());
		
		Flux.fromIterable(list)
			.collectList()
			.subscribe(
					lista -> lista
					.forEach(
						item -> log.info(item.toString())
					)
			);
	}
	
	public void ejemploFlatMap() throws Exception{
		
		List<User> list = Stream
				.of(
					new User("Juan", "Perez"),
					new User("Carlos", "Hernandez"),
					new User("Juan", "Paramo"),
					new User("Rodrigo", "Rosales"),
					new User("Carlos", "Juarez")
				)
				.collect(Collectors.toList());
		
		Flux.fromIterable(list)
				
				.map(u -> u.getNombre().concat(" ").concat(u.getApellido()))
				
				.flatMap(nombre -> {
					return (nombre.toLowerCase().contains("carlos")) ? Mono.just(nombre) : Mono.empty() ;
				})
				
				.map(nombre -> nombre.toUpperCase())
				
				.subscribe(
						nombre -> log.info(nombre.toString())
				);
		
	}
	
	public void ejemploIterable() throws Exception{
		
		Flux<String> nombres = Flux.just("Gybran Perez", "Juan Perez", "Rodrigo Rosales", "Mario Casas", "Alfredo Jimenez", "Juan Hernandez");
		
		
		nombres
			.map(nombre -> new User(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			
			.filter(usuario -> usuario.getNombre().equalsIgnoreCase("Juan"))
			
			.doOnNext(usuario -> {
				if(usuario == null) {
					throw new RuntimeException("Usuario nulo");
				}
				System.out.println(usuario);
			})
			
			.subscribe(
					usuario -> log.info(usuario.toString())
			);
	}

}
