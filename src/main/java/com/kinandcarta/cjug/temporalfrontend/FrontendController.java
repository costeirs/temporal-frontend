package com.kinandcarta.cjug.temporalfrontend;

import com.kinandcarta.cjug.temporalfrontend.models.ClaimInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class FrontendController {

  private final ClaimsService claimsService;

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("claims", claimsService.getAll());
    model.addAttribute("claimInput", new ClaimInput("", ""));
    return "home";
  }

  @PostMapping("/submit")
  public String handleNewClaim(@ModelAttribute ClaimInput claimInput, Model model) {
    // save
    claimsService.save(claimInput);
    // go home
    return home(model);
  }
}
