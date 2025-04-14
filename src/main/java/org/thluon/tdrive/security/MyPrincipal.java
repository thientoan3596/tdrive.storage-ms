package org.thluon.tdrive.security;

import java.util.UUID;

public record MyPrincipal(UUID id, String name, String role) {}
