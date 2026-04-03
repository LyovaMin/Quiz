package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.LoginRq;
import by.lyofchik.quiz.Model.DTO.Request.RegistrationRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public Response login(@RequestBody LoginRq request){
        return null;
    }

    @PostMapping("/registration")
    public Response registration(@RequestBody RegistrationRq request){
        return authService.registration(request);
    }
}
