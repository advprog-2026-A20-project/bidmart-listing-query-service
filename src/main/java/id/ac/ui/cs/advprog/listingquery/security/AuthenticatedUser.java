package id.ac.ui.cs.advprog.listingquery.security;

import id.ac.ui.cs.advprog.listingquery.model.Role;
import java.util.UUID;

public record AuthenticatedUser(
    UUID id,
    String email,
    Role role
) {
}
