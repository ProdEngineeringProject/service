package ro.unibuc.hello.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ro.unibuc.hello.auth.PublicEndpoint;
import ro.unibuc.hello.dto.Greeting;
import ro.unibuc.hello.exception.EntityNotFoundException;
import ro.unibuc.hello.service.GreetingsService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
@PublicEndpoint
@Controller
public class GreetingsController {

    @Autowired
    private GreetingsService greetingsService;

    @GetMapping("/hello-world")
    @ResponseBody
    @Timed(value = "hello.greeting.time", description = "Time taken to return greeting")
    @Counted(value = "hello.greeting.count", description = "Times greeting was returned")
    public Greeting sayHello(@RequestParam(name="name", required=false, defaultValue="Stranger") String name) {
        return greetingsService.hello(name);
    }

    @GetMapping("/info")
    @ResponseBody
    public Greeting info(@RequestParam(name="title", required=false, defaultValue="Overview") String title) throws EntityNotFoundException {
        return greetingsService.buildGreetingFromInfo(title);
    }

    @GetMapping("/greetings")
    @ResponseBody
    public List<Greeting> getAllGreetings() {
        return greetingsService.getAllGreetings();
    }


    @PostMapping("/greetings")
    @ResponseBody
    public Greeting createGreeting(@RequestBody Greeting greeting) {
        return greetingsService.saveGreeting(greeting);
    }

    @PutMapping("/greetings/{id}")
    @ResponseBody
    public Greeting updateGreeting(@PathVariable String id, @RequestBody Greeting greeting) throws EntityNotFoundException {
        return greetingsService.updateGreeting(id, greeting);
    }

    @DeleteMapping("/greetings/{id}")
    @ResponseBody
    public void deleteGreeting(@PathVariable String id) throws EntityNotFoundException {
        greetingsService.deleteGreeting(id);
    }
}

