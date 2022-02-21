package mx.com.ciecas.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import mx.com.ciecas.app.models.User;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class BaseSpringBootReactorApplication implements CommandLineRunner{

	public static final Logger log = LoggerFactory.getLogger(BaseSpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(BaseSpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploIterable();
		
	}
	
	public void ejemploFlatMap() throws Exception{
		
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
