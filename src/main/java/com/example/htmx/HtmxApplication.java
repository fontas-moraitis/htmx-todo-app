package com.example.htmx;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@SpringBootApplication
public class HtmxApplication {

	public static void main(String[] args) {
		SpringApplication.run(HtmxApplication.class, args);
	}

}

@Component
class Initializer {
	private final TodoRepository repository;

	Initializer(TodoRepository repository) {
		this.repository = repository;
	}

	@EventListener(ApplicationReadyEvent.class)
	void reset(){
		this.repository.deleteAll();
		Stream.of(
				"Example todo",
						"Ten thousand in advance. Ten thousand? We could almost buy our own ship for that! But who's going to fly it, kid! You? You bet I could. I'm not such a bad pilot myself! We don't have to sit here and listen. We haven't that much with us.")
				.forEach(t -> this.repository.save(new Todo(null, t)));
	}
}

@RequestMapping("/")
@Controller
class TodoController {
	private final TodoRepository repository;

	TodoController(TodoRepository repository) {
		this.repository = repository;
	}

	@GetMapping()
	String todos(Model model){
		model.addAttribute("todos", this.repository.findAll());
		return "todos";
	}

	@ResponseBody
	@DeleteMapping("/{id}")
	String delete(@PathVariable Integer id) {
		System.out.println("delete " + id);
		repository.deleteById(id);
		return "";
	}

	@PostMapping
	HtmxResponse add(@RequestParam("newItem") String title, Model model) {
		this.repository.save(new Todo(null, title));
		model.addAttribute("todos", this.repository.findAll());
		return new HtmxResponse()
				.addTemplate("todos::todo-list")
				.addTemplate("todos::todo-form");
	}
}

interface TodoRepository extends CrudRepository<Todo, Integer> {}

record Todo(@Id Integer id, String title) {}
