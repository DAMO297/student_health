package org.example.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具 - 用于生成BCrypt密码
 * 运行此类可以生成密码的BCrypt hash
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 为密码 "111111" 生成BCrypt hash
        String password = "111111";
        String hash = encoder.encode(password);

        System.out.println("原始密码: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println();
        System.out.println("SQL语句:");
        System.out.println("UPDATE t_user SET password_hash = '" + hash + "' WHERE username = 'root';");
    }
}
