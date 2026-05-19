package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.LoginRq;
import by.lyofchik.quiz.Model.DTO.Request.RegistrationRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class AuthController {
    AuthService authService;

    @GetMapping
    public ModelAndView root() {
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        return new ModelAndView("forward:/html/login.html");
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        return new ModelAndView("forward:/html/register.html");
    }

    @PostMapping("/api/login")
    @ResponseBody
    public Response login(@RequestBody LoginRq request, HttpSession session){
        return authService.login(request, session);
    }

    @PostMapping("/api/register")
    @ResponseBody
    public Response registration(@RequestBody RegistrationRq request){
        return authService.registration(request);
    }

    @GetMapping("/api/me")
    @ResponseBody
    public Response me(HttpSession session) {
        return authService.currentUser(session);
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public Response logout(HttpSession session) {
        session.invalidate();
        return Response.success();
    }
}
