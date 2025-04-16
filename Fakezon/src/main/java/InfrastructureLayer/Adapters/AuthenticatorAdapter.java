package InfrastructureLayer.Adapters;

import DomainLayer.Interfaces.IAuthenticator;

public class AuthenticatorAdapter implements IAuthenticator {
    @Override
    public boolean register(String email, String password) {
        // Stub: Add real registration logic here
        return true;
    }

    @Override
    public boolean login(String email, String password) {
        // Stub: Add real login logic here
        return true;
    }

    @Override
    public void logout(String email) {
        // Stub: Add real logout logic here
    }
}
