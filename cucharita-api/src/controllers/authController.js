// =============================================
// controllers/authController.js - VERSION MEJORADA
// =============================================
import { supabase } from '../config/supabase.js';

// REGISTRO CON CREACIÓN MANUAL DE PERFIL
export const register = async (req, res) => {
    try {
        const { email, password, username, fullName } = req.body;

        // Validaciones básicas
        if (!email || !password) {
            return res.status(400).json({ error: 'Email y password son requeridos' });
        }

        if (password.length < 6) {
            return res.status(400).json({ error: 'El password debe tener al menos 6 caracteres' });
        }

        // 1. Registrar usuario
        const { data: authData, error: authError } = await supabase.auth.signUp({
            email,
            password,
            options: {
                data: {
                    username: username || email.split('@')[0],
                    full_name: fullName || ''
                },
                // Deshabilitar confirmación por email en desarrollo
                emailRedirectTo: undefined
            }
        });

        if (authError) throw authError;

        // 2. Crear perfil manualmente si el trigger falló
        if (authData.user) {
            try {
                // Verificar si el perfil ya existe
                const { data: existingProfile } = await supabase
                    .from('profiles')
                    .select('id')
                    .eq('id', authData.user.id)
                    .single();

                // Si no existe, crearlo
                if (!existingProfile) {
                    const { error: profileError } = await supabase
                        .from('profiles')
                        .insert([{
                            id: authData.user.id,
                            username: username || email.split('@')[0],
                            full_name: fullName || '',
                            avatar_url: ''
                        }]);

                    if (profileError) {
                        console.error('Error creating profile:', profileError);
                        // No fallar el registro por esto
                    }
                }
            } catch (profileErr) {
                console.error('Error in profile creation:', profileErr);
                // Continuar aunque falle la creación del perfil
            }
        }

        res.status(201).json({
            message: 'Usuario registrado exitosamente',
            user: authData.user,
            session: authData.session
        });

    } catch (err) {
        console.error('Registration error:', err);
        res.status(400).json({ error: err.message });
    }
};

// LOGIN
export const login = async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email y password son requeridos' });
        }

        const { data, error } = await supabase.auth.signInWithPassword({
            email,
            password
        });

        if (error) throw error;

        res.json({
            message: 'Login exitoso',
            user: data.user,
            session: data.session
        });

    } catch (err) {
        res.status(401).json({ error: err.message });
    }
};

// LOGOUT
export const logout = async (req, res) => {
    try {
        const { error } = await supabase.auth.signOut();
        if (error) throw error;

        res.json({ message: 'Logout exitoso' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// PERFIL ACTUAL
export const getCurrentUser = async (req, res) => {
    try {
        // Obtener datos adicionales del perfil
        const { data: profile, error } = await supabase
            .from('profiles')
            .select('*')
            .eq('id', req.user.id)
            .single();

        // Si no existe el perfil, crearlo ahora
        if (error && error.code === 'PGRST116') {
            const { data: newProfile, error: createError } = await supabase
                .from('profiles')
                .insert([{
                    id: req.user.id,
                    username: req.user.email.split('@')[0],
                    full_name: req.user.user_metadata?.full_name || '',
                    avatar_url: req.user.user_metadata?.avatar_url || ''
                }])
                .select()
                .single();

            if (createError) {
                console.error('Error creating profile:', createError);
                return res.json({ 
                    user: req.user,
                    profile: null,
                    warning: 'Profile not found and could not be created'
                });
            }

            return res.json({ 
                user: req.user,
                profile: newProfile
            });
        }

        if (error) throw error;

        res.json({ 
            user: req.user,
            profile: profile
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// ACTUALIZAR PERFIL
export const updateProfile = async (req, res) => {
    try {
        const { username, fullName, avatarUrl } = req.body;

        const updateData = {};
        if (username !== undefined) updateData.username = username;
        if (fullName !== undefined) updateData.full_name = fullName;
        if (avatarUrl !== undefined) updateData.avatar_url = avatarUrl;

        const { data, error } = await supabase
            .from('profiles')
            .update(updateData)
            .eq('id', req.user.id)
            .select()
            .single();

        if (error) throw error;

        res.json({
            message: 'Perfil actualizado exitosamente',
            profile: data
        });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};