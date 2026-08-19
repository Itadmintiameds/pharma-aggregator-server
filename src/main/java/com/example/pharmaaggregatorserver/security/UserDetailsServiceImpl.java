package com.example.pharmaaggregatorserver.security;

import com.example.pharmaaggregatorserver.entity.auth.User;
import com.example.pharmaaggregatorserver.entity.buyer.BuyerUser;

import com.example.pharmaaggregatorserver.repository.auth.UserRepository;
import com.example.pharmaaggregatorserver.repository.buyer.BuyerUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final BuyerUserRepository buyerUserRepository;

    // Seller/admin logins live in tbl_user; buyer logins live in the fully
    // separate tbl_buyer_user (see BuyerAuthService). The JWT subject is the
    // username/email either side issued it for, so on the request path we
    // don't know in advance which table to check — try seller/admin first
    // (the common case for most endpoints), then fall back to buyer.
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetails> seller = userRepository.findByUsername(username).map(UserDetailsImpl::build);
        if (seller.isPresent()) {
            return seller.get();
        }
        return buyerUserRepository.findByEmail(username)
                .map(this::buildBuyerUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    private UserDetails buildBuyerUserDetails(BuyerUser buyerUser) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_BUYER"));
        return UserDetailsImpl.builder()
                .id(buyerUser.getBuyerUserId())
                .username(buyerUser.getEmail())
                .password(buyerUser.getPasswordHash())
                .isPasswordTemporary(buyerUser.isPasswordTemporary())
                .isActive(buyerUser.isActive())
                .isAccountLocked(buyerUser.isAccountLocked())
                .authorities(authorities)
                .build();
    }
}