package com.starlive.shopping.domain.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starlive.shopping.domain.user.UserEntity;
import com.starlive.shopping.domain.user.UserEntity.Platform;
import com.starlive.shopping.domain.user.UserEntity.UserType;
import com.starlive.shopping.domain.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String platform = request.getClientRegistration().getClientName();
        OAuth2AccessToken accessTokenInfo = request.getAccessToken();

        try { // 사용자 정보 출력
            System.out.println(new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        String socialId = null;

        if (platform.equalsIgnoreCase("kakao")) {
            socialId = processKakaoUser(oAuth2User.getAttributes());
        } else if (platform.equalsIgnoreCase("naver")) {
            socialId = processNaverUser(oAuth2User.getAttributes());
        } else if (platform.equalsIgnoreCase("google")) {
            socialId = processGoogleUser(oAuth2User.getAttributes());
        }

        return new CustomOAuth2User(socialId);
    }

    private String processKakaoUser(Map<String, Object> attributes) {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String socialId = attributes.get("id").toString();
        String name = properties.get("nickname").toString();

        saveOrUpdateUser(socialId, name, Platform.KAKAO);

        return socialId;
    }

    private String processNaverUser(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String socialId = response.get("id").toString();
        String name = response.get("nickname").toString();

        saveOrUpdateUser(socialId, name, Platform.NAVER);

        return socialId;
    }

    private String processGoogleUser(Map<String, Object> attributes) {
        // 응답 데이터가 attributes에 바로 포함됨
        String socialId = attributes.get("sub").toString(); // Google의 고유 ID
        String name = attributes.get("name").toString();

        saveOrUpdateUser(socialId, name, Platform.GOOGLE);

        return socialId;
    }

    private UserEntity saveOrUpdateUser(String socialId, String name, Platform platform) {
        Optional<UserEntity> existingUser = userRepository.findBySocialId(socialId);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        UserEntity newUser = new UserEntity();
        newUser.setSocialId(socialId);
        newUser.setName(name);
        newUser.setUserType(UserType.CUSTOMER);
        newUser.setPlatform(platform);

        return userRepository.save(newUser);
    }
}
