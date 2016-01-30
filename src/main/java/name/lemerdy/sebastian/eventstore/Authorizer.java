package name.lemerdy.sebastian.eventstore;

import lombok.Value;

import java.util.Optional;

@Value
class Authorizer {

    static final String PASSWORD_SYSTEM_PROPERTY_KEY = "password";

    String name;
    String password;

    private final Optional<String> referencePassword;

    Authorizer(String... nameAndPassword) {
        this.name = nameAndPassword[0];
        this.password = nameAndPassword[1];
        this.referencePassword = Optional.ofNullable(System.getProperty(PASSWORD_SYSTEM_PROPERTY_KEY));
    }

    boolean isAuthorized() {
        return hasReferencePassword() && "user".equals(name) && referencePassword.get().equals(password);
    }

    private boolean hasReferencePassword() {
        return referencePassword.isPresent();
    }

}
