package com.kinandcarta.cjug.temporalfrontend;

import com.kinandcarta.cjug.temporalfrontend.models.Claim;
import com.kinandcarta.cjug.temporalfrontend.models.ClaimInput;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClaimsService {

  public List<Claim> getAll() {
    return List.of(
        new Claim("1", "Stephen", "asdf", "CREATED"),
        new Claim("2", "Stephen", "asdf", "APPROVED ")
    );
  }

  public void save(ClaimInput input) {
    log.info("howdy");
  }
}
