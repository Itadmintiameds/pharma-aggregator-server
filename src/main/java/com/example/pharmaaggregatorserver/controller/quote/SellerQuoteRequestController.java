package com.example.pharmaaggregatorserver.controller.quote;

import com.example.pharmaaggregatorserver.dto.quote.QuoteRequestResponseDTO;
import com.example.pharmaaggregatorserver.dto.quote.SellerQuoteResponseDTO;
import com.example.pharmaaggregatorserver.entity.seller.Seller;
import com.example.pharmaaggregatorserver.exception.UnauthorizedException;
import com.example.pharmaaggregatorserver.repository.seller.SellerRepository;
import com.example.pharmaaggregatorserver.response.ApiResponse;
import com.example.pharmaaggregatorserver.security.UserDetailsImpl;
import com.example.pharmaaggregatorserver.service.quote.QuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seller/quote-requests")
@RequiredArgsConstructor
public class SellerQuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final SellerRepository sellerRepository;

    // Mirrors SellerOrderController: the caller's own sellerId is always
    // resolved from the authenticated JWT principal, never trusted from the
    // path/body.
    private String resolveAuthenticatedSellerId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
            throw new UnauthorizedException("Unauthorized access");
        }
        boolean isSeller = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_SELLER"));
        if (!isSeller) {
            throw new UnauthorizedException("Unauthorized access");
        }
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("No seller profile found for this account"));
        return seller.getSellerId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuoteRequestResponseDTO>>> list(Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        List<QuoteRequestResponseDTO> response = quoteRequestService.listForSeller(sellerId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.toString(), "Quote requests fetched successfully", response, (long) response.size()));
    }

    @PatchMapping("/{quoteRequestId}/respond")
    public ResponseEntity<ApiResponse<QuoteRequestResponseDTO>> respond(
            @PathVariable Long quoteRequestId,
            @Valid @RequestBody SellerQuoteResponseDTO request,
            Authentication authentication) {
        String sellerId = resolveAuthenticatedSellerId(authentication);
        QuoteRequestResponseDTO response = quoteRequestService.respond(quoteRequestId, sellerId, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.toString(), "Response sent to buyer", response));
    }
}
