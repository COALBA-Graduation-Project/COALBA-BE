package com.project.coalba.domain.auth.service;

import com.project.coalba.domain.auth.dto.response.*;
import com.project.coalba.domain.auth.entity.*;
import com.project.coalba.domain.auth.entity.enums.*;
import com.project.coalba.domain.auth.info.*;
import com.project.coalba.domain.auth.repository.*;
import com.project.coalba.domain.auth.token.AuthTokenManager;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final AuthTokenManager tokenManager;
    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private static final String USER_ID_KEY = "userId";

    @Transactional
    public AuthResponse login(Provider provider, String token, Role role) {
        User socialUser = getSocialUser(provider, token, role), loginUser;
        Optional<User> userOptional = getSubscribedUser(socialUser.getProviderId(), role);
        boolean isNewUser = userOptional.isEmpty();
        loginUser = getLoginUser(userOptional, socialUser);

        String accessToken = tokenManager.createAccessToken(loginUser.getProviderId(), loginUser.getId());
        String refreshToken = tokenManager.createRefreshToken();
        manageRefreshToken(loginUser, refreshToken);
        return new AuthResponse(accessToken, refreshToken, isNewUser);
    }

    @Transactional
    public TokenResponse reissue(String accessToken, String refreshToken) {
        Claims claims = tokenManager.getExpiredTokenClaims(accessToken);
        if (claims == null) throw new RuntimeException("");
        String providerId = claims.getSubject();
        Long userId = claims.get(USER_ID_KEY, Long.class);

        UserRefreshToken userRefreshToken = getUserRefreshToken(userId).orElseThrow(() -> new RuntimeException(""));
        if (isValidRefreshToken(refreshToken, userRefreshToken.getToken())) {
            String newAccessToken = tokenManager.createAccessToken(providerId, userId);
            String newRefreshToken = tokenManager.createRefreshToken();
            userRefreshToken.updateToken(newRefreshToken);
            return new TokenResponse(newAccessToken, newRefreshToken);
        }
        throw new RuntimeException("");
    }

    private User getSocialUser(Provider provider, String token, Role role) {
        UserInfo userInfo = UserInfoFactory.getUserInfo(provider);
        return userInfo.getUser(token, role);
    }

    private Optional<User> getSubscribedUser(String providerId, Role role) {
        return userRepository.findByProviderIdAndRole(providerId, role);
    }

    private User getLoginUser(Optional<User> originalUserOptional, User newSocialUser) {
        if (originalUserOptional.isPresent()) {
            return originalUserOptional.get().updateSocialInfo(newSocialUser);
        } else {
            return userRepository.save(newSocialUser);
        }
    }

    private void manageRefreshToken(User loginUser, String refreshToken) {
        Optional<UserRefreshToken> userRefreshTokenOptional = getUserRefreshToken(loginUser.getId());
        if (userRefreshTokenOptional.isPresent()) {
            userRefreshTokenOptional.get().updateToken(refreshToken);
        } else {
            userRefreshTokenRepository.save(UserRefreshToken.builder()
                    .user(loginUser).token(refreshToken).build());
        }
    }

    private Optional<UserRefreshToken> getUserRefreshToken(Long userId) {
        return userRefreshTokenRepository.findById(userId);
    }

    private boolean isValidRefreshToken(String refreshToken, String dbRefreshToken) {
        return tokenManager.validate(refreshToken) && refreshToken.equals(dbRefreshToken);
    }
}
