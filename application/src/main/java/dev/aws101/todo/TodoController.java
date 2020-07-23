package dev.aws101.todo;

import dev.aws101.registration.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/todo")
public class TodoController {

  private final TodoRepository todoRepository;

  @Autowired
  public TodoController(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  @GetMapping("/add")
  public String showAddView(Model model) {
    model.addAttribute("todo", new Todo());

    return "todo/add";
  }

  @PostMapping
  public String add(
    @Valid Todo todo,
    BindingResult result,
    Model model
  ) {
    if (result.hasErrors()) {
      model.addAttribute("message", "Your new todo couldn't be saved.");

      return "todo/add";
    }

    todoRepository.save(todo);

    model.addAttribute("message", "Your new todo has been be saved.");

    return "redirect:/";
  }

  @GetMapping("/edit/{id}")
  public String showUpdateView(
    @PathVariable("id") long id,
    Model model
  ) {
    Todo todo = todoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid todo id:" + id));

    model.addAttribute("todo", todo);

    return "todo/update";
  }

  @PutMapping("/{id}")
  public String update(
    @PathVariable("id") long id,
    @Valid Todo todo,
    BindingResult result,
    Model model,
    RedirectAttributes redirectAttributes
  ) {
    if (result.hasErrors()) {
      model.addAttribute("message", "Your todo couldn't be saved.");

      return "todo/update";
    }

    todoRepository.save(todo);

    redirectAttributes.addFlashAttribute("message", "Your todo has been be saved.");

    return "redirect:/";
  }

  @DeleteMapping("/{id}")
  public String delete(
    @PathVariable("id") long id,
    RedirectAttributes redirectAttributes
  ) {
    Todo todo = todoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid todo id:" + id));
    todoRepository.delete(todo);

    redirectAttributes.addFlashAttribute("message", "Your todo has been be deleted.");

    return "redirect:/";
  }
}