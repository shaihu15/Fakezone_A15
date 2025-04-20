package InfrastructureLayer.Adapters;

import java.util.HashMap;

import DomainLayer.Interfaces.IAuthenticator;

public class AuthenticatorAdapter implements IAuthenticator {
    HashMap<String, Integer> userToken = new HashMap<>();

    @Override
    public String register(int userId) {
        // Stub: Add real registration logic here
        return "true";
    }

    @Override
    
    public String login(int userId)  {
        // Stub: Add real login logic here
        return "true";
    }

    @Override
    public void logout(int userId) {
        // Stub: Add real logout logic here
    }

    @Override
    public boolean isValid(String sessionToken) {
        return true;
    }
}
