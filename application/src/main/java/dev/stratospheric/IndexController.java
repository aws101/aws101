package dev.stratospheric;

import dev.stratospheric.person.Person;
import dev.stratospheric.person.PersonRepository;
import dev.stratospheric.todo.Todo;
import dev.stratospheric.todo.TodoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class IndexController {

  private final PersonRepository personRepository;

  private final TodoRepository todoRepository;

  public IndexController(
    PersonRepository personRepository,
    TodoRepository todoRepository
  ) {
    this.personRepository = personRepository;
    this.todoRepository = todoRepository;
  }

  @GetMapping
  public String getIndex(
    Model model,
    Principal principal
  ) {
    model.addAttribute("indexPageActiveClass", "active");
    model.addAttribute("collaborators", List.of());

    Person person = personRepository.findByName("Admin").orElse(null);
    if (principal != null) {
      person = personRepository.findByName(principal.getName()).orElse(null);
      model.addAttribute("collaborators", personRepository.findByNameNot(principal.getName()));
    }

    Iterable<Todo> todoList = todoRepository.findAllByOwner(person);
    model.addAttribute("todos", todoList);

    return "index";
  }

}
