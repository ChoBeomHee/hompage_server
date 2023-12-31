package com.project.devlabshompage.config.oauth;

import com.project.devlabshompage.Repository.UserRepository;
import com.project.devlabshompage.config.auth.PrincipalDetails;
import com.project.devlabshompage.config.oauth.provider.GoogleUserInfo;
import com.project.devlabshompage.config.oauth.provider.NaverUserInfo;
import com.project.devlabshompage.config.oauth.provider.OAuth2UserInfo;
import com.project.devlabshompage.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

// 후처리 함수
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = null;
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")){
            oAuth2UserInfo = new NaverUserInfo((Map<String, Object>) oAuth2User.getAttributes().get("response"));
        }
        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String username = provider+"_"+oAuth2UserInfo.getProviderId();
        String password = "devlabs";
        String email = oAuth2UserInfo.getEmail();
        String role = "ROLE_USER";

        User userEntity = userRepository.findByUsername(username);

        if(userEntity == null){
            System.out.println("최초의 oauth 로그인");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        }else{
            System.out.println("회원가입 되어있는 아이디");
        }

        return new PrincipalDetails(userEntity);
        // 구글 로그인 버튼 클릭 -> 구글 로그인 창 -> 로그인 완료 -> code 리턴(oauth 클라이언트 라이브러리가 해줌) -> 엑세스토큰 요청
        // userReq 정보 -> 회원프로필 받기(loadUser) -> 구글로부터 회원프로필 받기
        // load user 함수의 역할은 구글로부터 회원프로필을 받는 역할
    }
    // 유저 네임 : google_sub
    // 패스워드 : 암호화 후 우리마음
    // 이메일 : 그대로
    // 이 정보들로 회원가입 진행
    // ROLE = USER
}
