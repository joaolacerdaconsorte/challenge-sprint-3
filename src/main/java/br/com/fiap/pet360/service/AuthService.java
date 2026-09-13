package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.AuthRequest;
import br.com.fiap.pet360.dto.AuthResponse;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.model.Role;
import br.com.fiap.pet360.model.Usuario;
import br.com.fiap.pet360.repository.UsuarioRepository;
import br.com.fiap.pet360.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(AuthRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username já cadastrado: " + request.username());
        }
        Usuario u = new Usuario(
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );
        usuarioRepository.save(u);
        return buildResponseFor(u);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        Usuario u = usuarioRepository.findByUsername(request.username()).orElseThrow();
        return buildResponseFor(u);
    }

    private AuthResponse buildResponseFor(Usuario u) {
        UserDetails ud = new User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
        String token = jwtService.generateToken(ud);
        return new AuthResponse(token, u.getUsername(), u.getRole().name(), jwtService.getExpirationMs());
    }
}
