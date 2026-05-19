package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.LoginRq;
import by.lyofchik.quiz.Model.DTO.Request.RegistrationRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "/api/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Response updateMe(@RequestParam(required = false) String username,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) MultipartFile avatar,
                             HttpSession session) {
        return authService.updateCurrentUser(username, password, avatar, session);
    }

    @GetMapping("/api/users")
    @ResponseBody
    public Response users(HttpSession session) {
        return authService.users(session);
    }

    @PutMapping(value = "/api/users/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Response updateUser(@PathVariable Integer id,
                               @RequestParam(required = false) String username,
                               @RequestParam(required = false) String password,
                               @RequestParam(required = false) MultipartFile avatar,
                               HttpSession session) {
        return authService.updateUser(id, username, password, avatar, session);
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public Response logout(HttpSession session) {
        session.invalidate();
        return Response.success();
    }
}
