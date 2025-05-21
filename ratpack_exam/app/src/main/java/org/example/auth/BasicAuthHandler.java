
package org.example.auth;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.ratpack.handling.Context;
import io.ratpack.handling.Handler;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class BasicAuthHandler implements Handler {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD_HASH_FILE = "admin_password.hash";
    private static final String DEFAULT_PASSWORD = "admin123";  // Default password for initial setup

    public BasicAuthHandler() {
        try {
            Path hashPath = Paths.get(ADMIN_PASSWORD_HASH_FILE);
            
            // If password hash file doesn't exist, create one with default password
            if (!Files.exists(hashPath)) {
                String passwordHash = BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt());
                Files.write(hashPath, passwordHash.getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize authentication", e);
        }
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String authHeader = ctx.getRequest().getHeaders().get(HttpHeaderNames.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);
            
            if (values.length == 2) {
                String username = values[0];
                String password = values[1];
                
                if (authenticate(username, password)) {
                    ctx.next();
                    return;
                }
            }
        }
        
        ctx.getResponse().status(401)
            .headers(headers -> headers.add(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"Company samsung Admin\""))
            .send("Authentication required");
    }

    private boolean authenticate(String username, String password) {
        if (!ADMIN_USERNAME.equals(username)) {
            return false;
        }
        
        try {
            String storedHash = Files.readString(Paths.get(ADMIN_PASSWORD_HASH_FILE));
            return BCrypt.checkpw(password, storedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
