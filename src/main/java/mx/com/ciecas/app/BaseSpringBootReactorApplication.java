package mx.com.ciecas.app;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import mx.com.ciecas.app.models.User;
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
		
		ejemploCollectList();
		
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
			
			.subscribe(
					usuario -> log.info(usuario.toString())
			);
	}

}
