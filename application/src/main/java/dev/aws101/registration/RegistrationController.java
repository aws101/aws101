package dev.aws101.registration;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import dev.aws101.person.Person;
import dev.aws101.person.PersonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrationController {

  private final RegistrationService registrationService;

  private final PersonRepository personRepository;

  public RegistrationController(
    RegistrationService registrationService,
    PersonRepository personRepository
  ) {
    this.registrationService = registrationService;
    this.personRepository = personRepository;
  }

  @GetMapping
  public String getRegisterView(Model model) {
    model.addAttribute("registrationPageActiveClass", "active");
    model.addAttribute("registration", new Registration());

    return "register";
  }

  @PostMapping
  public String registerUser(@Valid Registration registration,
                             BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("registration", registration);

      return "register";
    }

    // TODO: Move this also to Bean Validation like Password
    if (!registrationService.isValidInvitationCode(registration.getInvitationCode())) {
      model.addAttribute("registration", registration);
      model.addAttribute("message", "Invalid invitation code. Please check it again or contact the person who invited you.");
      model.addAttribute("messageType", "danger");

      return "register";
    }

    try {
      UserType user = registrationService.registerUser(registration);
      Person person = new Person();
      person.setName(user.getUsername());
      for (AttributeType attribute : user.getAttributes()) {
        if (attribute.getName().equals("email")) {
          person.setEmail(attribute.getValue());
        }
      }
      personRepository.save(person);

      redirectAttributes.addFlashAttribute("message", "You successfully registered for the Todo App. " +
        "Go check your E-Mail inbox for further instructions.");
      redirectAttributes.addFlashAttribute("messageType", "success");
    } catch (InvalidParameterException | UsernameExistsException awsCognitoIdentityProviderException) {
      model.addAttribute("registration", registration);
      redirectAttributes.addFlashAttribute("message", awsCognitoIdentityProviderException.getMessage());
      redirectAttributes.addFlashAttribute("messageType", "danger");

      return "register";
    }

    return "redirect:/register";
  }
}
