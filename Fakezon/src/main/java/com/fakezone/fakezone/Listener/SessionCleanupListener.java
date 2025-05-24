package com.fakezone.fakezone.Listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fakezone.fakezone.ui.view.EmptyResponseErrorHandler;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinRequest;

import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;

@WebListener
@Component
public class SessionCleanupListener implements HttpSessionListener {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${api.url}") 
    String apiUrl;


    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // no-op
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        restTemplate.setErrorHandler(new EmptyResponseErrorHandler());
        HttpSession session = se.getSession();
        String token = (String) session.getAttribute("token");
        UserDTO user = (UserDTO) session.getAttribute("userDTO");
        if(isGuestToken(token)){
            String url = (apiUrl + "user/removeUnsignedUser/" + user.getUserId());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<Response<Boolean>>() {});
            session.removeAttribute("token");
            session.removeAttribute("userDTO");
        }
        else{
            Request<Integer> req = new Request<Integer>(token, user.getUserId());
            String url = apiUrl + "user/logout";
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(req),
                    new ParameterizedTypeReference<Response<Void>>() {}
                    );
            session.removeAttribute("token");
            session.removeAttribute("userDTO");
      }
    }

    private boolean isGuestToken(String token){
        String url = apiUrl + "user/isGuestToken";
        ResponseEntity<Response<Boolean>> apiResponse = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(token), new ParameterizedTypeReference<Response<Boolean>>() {});
        
        Response<Boolean> response = apiResponse.getBody();
        if(response.isSuccess()){
            return response.getData();
        }
        else{
            return true;
        }
    }

}