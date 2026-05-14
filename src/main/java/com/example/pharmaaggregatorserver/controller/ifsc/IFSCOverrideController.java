package com.example.pharmaaggregatorserver.controller.ifsc;


import com.example.pharmaaggregatorserver.service.ifsc.IFSCOverrideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ifsc")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class IFSCOverrideController {

    private final IFSCOverrideService ifscOverrideService;

    @GetMapping("/{ifscCode}")
    public ResponseEntity<?> getIFSCDetails(@PathVariable String ifscCode) {
        Map<String, Object> override = ifscOverrideService.getOverrideByIFSC(ifscCode);

        if (override == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(override);
    }
}
