import React, { useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useTheme } from '../store';
import Header from '../components/Header';

const TEXT = {
  kk: {
    login: 'Кіру', register: 'Тіркелу', name: 'Атыңыз', email: 'Электрондық пошта', password: 'Құпиясөз',
    hint: 'Таңдаулылар мен тарих сақталуы үшін аккаунтқа кіріңіз.',
    noAccount: 'Аккаунт жоқ па? Тіркелу', haveAccount: 'Аккаунт бар ма? Кіру',
    fill: 'Барлық өрісті толтырыңыз', short: 'Құпиясөз кемінде 6 таңба болуы керек',
  },
  ru: {
    login: 'Войти', register: 'Регистрация', name: 'Ваше имя', email: 'Электронная почта', password: 'Пароль',
    hint: 'Войдите, чтобы сохранять избранное и историю просмотров.',
    noAccount: 'Нет аккаунта? Зарегистрироваться', haveAccount: 'Уже есть аккаунт? Войти',
    fill: 'Заполните все поля', short: 'Пароль должен быть не короче 6 символов',
  },
  en: {
    login: 'Sign in', register: 'Sign up', name: 'Your name', email: 'Email', password: 'Password',
    hint: 'Sign in to save favorites and your viewing history.',
    noAccount: 'No account? Sign up', haveAccount: 'Already have an account? Sign in',
    fill: 'Please fill in all fields', short: 'Password must be at least 6 characters',
  },
};

function toUser(data, fallback) {
  const info = (data && data.user) || data || {};
  return {
    token: data.token || data.accessToken,
    name: info.name || fallback.name || '',
    email: info.email || fallback.email,
    role: info.role || 'USER',
  };
}

export default function LoginScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const { lang, setUser } = useApp();
  const L = TEXT[lang] || TEXT.ru;

  const [mode, setMode] = useState('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const isRegister = mode === 'register';

  const submit = async () => {
    const mail = email.trim().toLowerCase();
    if (!mail || !password || (isRegister && !name.trim())) {
      setError(L.fill);
      return;
    }
    if (isRegister && password.length < 6) {
      setError(L.short);
      return;
    }
    setError('');
    setBusy(true);
    try {
      let data;
      if (isRegister) {
        data = await api.register(name.trim(), mail, password);
        if (!data || !(data.token || data.accessToken)) data = await api.login(mail, password);
      } else {
        data = await api.login(mail, password);
      }
      setUser(toUser(data, { name: name.trim(), email: mail }));
      navigation.goBack();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <Header title={isRegister ? L.register : L.login} onBack={() => navigation.goBack()} />

      <ScrollView contentContainerStyle={{ padding: 16 }} keyboardShouldPersistTaps="handled">
        <View style={s.logoBox}>
          <View style={s.logo}>
            <Ionicons name="person-outline" size={34} color={c.onPrimary} />
          </View>
          <Text style={s.hint}>{L.hint}</Text>
        </View>

        {isRegister ? (
          <Field c={c} s={s} icon="person-outline" value={name} onChangeText={setName} placeholder={L.name} />
        ) : null}
        <Field
          c={c} s={s} icon="mail-outline" value={email} onChangeText={setEmail} placeholder={L.email}
          keyboardType="email-address" autoCapitalize="none"
        />
        <Field
          c={c} s={s} icon="lock-closed-outline" value={password} onChangeText={setPassword}
          placeholder={L.password} secureTextEntry autoCapitalize="none"
        />

        {error ? (
          <View style={s.error}>
            <Ionicons name="alert-circle-outline" size={18} color={c.danger} />
            <Text style={s.errorText}>{error}</Text>
          </View>
        ) : null}

        <Pressable style={[s.button, busy && { opacity: 0.7 }]} onPress={submit} disabled={busy}>
          {busy ? <ActivityIndicator color={c.onPrimary} /> : <Text style={s.buttonText}>{isRegister ? L.register : L.login}</Text>}
        </Pressable>

        <Pressable
          style={s.switch}
          onPress={() => {
            setMode(isRegister ? 'login' : 'register');
            setError('');
          }}
        >
          <Text style={s.switchText}>{isRegister ? L.haveAccount : L.noAccount}</Text>
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

function Field({ c, s, icon, ...props }) {
  return (
    <View style={s.field}>
      <Ionicons name={icon} size={20} color={c.sub} />
      <TextInput style={s.input} placeholderTextColor={c.sub} autoCorrect={false} {...props} />
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    logoBox: { alignItems: 'center', marginTop: 8, marginBottom: 22 },
    logo: {
      width: 72, height: 72, borderRadius: 24, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center', marginBottom: 12,
    },
    hint: { color: c.sub, textAlign: 'center', lineHeight: 20, paddingHorizontal: 20 },
    field: {
      flexDirection: 'row', alignItems: 'center', gap: 10, height: 54, borderRadius: 16, marginBottom: 12,
      paddingHorizontal: 14, backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
    },
    input: { flex: 1, fontSize: 16, color: c.text },
    error: { flexDirection: 'row', gap: 8, alignItems: 'center', marginBottom: 12, paddingHorizontal: 4 },
    errorText: { flex: 1, color: c.danger, lineHeight: 19 },
    button: {
      height: 54, borderRadius: 16, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center', marginTop: 4,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    switch: { alignItems: 'center', paddingVertical: 18 },
    switchText: { color: c.primary, fontWeight: '700' },
  });