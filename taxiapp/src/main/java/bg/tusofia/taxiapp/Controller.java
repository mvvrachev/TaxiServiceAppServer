package bg.tusofia.taxiapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/hi")
    public String hi() {
        return "hi";
    }

    @GetMapping("/hey")
    public String hey() {
        return "hey";
    }
}
