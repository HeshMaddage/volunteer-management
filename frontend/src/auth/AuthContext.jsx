import { createContext, useContext, useState } from 'react';
import client from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [email, setEmail] = useState(localStorage.getItem('email'));
    const [role, setRole] = useState(localStorage.getItem('role'));

    const login = async (loginEmail, password) => {
        const response = await client.post('/auth/login', { email: loginEmail, password });
        const { token: newToken, email: respEmail, role: respRole } = response.data;

        localStorage.setItem('token', newToken);
        localStorage.setItem('email', respEmail);
        localStorage.setItem('role', respRole);

        setToken(newToken);
        setEmail(respEmail);
        setRole(respRole);
    };

    const register = async (regEmail, password, fullName) => {
        const response = await client.post('/auth/register', { email: regEmail, password, fullName });
        const { token: newToken, email: respEmail, role: respRole } = response.data;

        localStorage.setItem('token', newToken);
        localStorage.setItem('email', respEmail);
        localStorage.setItem('role', respRole);

        setToken(newToken);
        setEmail(respEmail);
        setRole(respRole);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        localStorage.removeItem('role');
        setToken(null);
        setEmail(null);
        setRole(null);
    };

    const value = {
        token,
        email,
        role,
        isAuthenticated: !!token,
        isAdmin: role === 'ADMIN',
        login,
        register,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}