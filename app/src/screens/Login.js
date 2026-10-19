import React, { useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { LANGUAGES } from '../i18n';
import { useApp, useT, useTheme } from '../store';
import { FadeIn, PopIn, PressScale, SlideTabs } from '../components/Motion';

// Надписи этого экрана на трёх языках
const TEXT = {
  kk: {
    login: 'Кіру', register: 'Тіркелу', name: 'Атыңыз', email: 'Электрондық пошта', password: 'Құпиясөз',
    hint: 'Жалғастыру үшін кіріңіз немесе тіркеліңіз.',
    noAccount: 'Аккаунт жоқ па? Тіркелу', haveAccount: 'Аккаунт бар ма? Кіру',
    fill: 'Барлық өрісті толтырыңыз', short: 'Құпиясөз кемінде 6 таңба болуы керек',
  },
  ru: {
    login: 'Войти', register: 'Регистрация', name: 'Ваше имя', email: 'Электронная почта', password: 'Пароль',
    hint: 'Войдите или зарегистрируйтесь, чтобы продолжить.',
    noAccount: 'Нет аккаунта? Зарегистрироваться', haveAccount: 'Уже есть аккаунт? Войти',
    fill: 'Заполните все поля', short: 'Пароль должен быть не короче 6 символов',
  },
  en: {
    login: 'Sign in', register: 'Sign up', name: 'Your name', email: 'Email', password: 'Password',
    hint: 'Sign in or create an account to continue.',
    noAccount: 'No account? Sign up', haveAccount: 'Already have an account? Sign in',
    fill: 'Please fill in all fields', short: 'Password must be at least 6 characters',
  },
};

// Сервер может вернуть данные пользователя прямо в ответе или внутри поля user
function toUser(data, fallback) {
  const info = (data && data.user) || data || {};
  return {
    token: data.token || data.accessToken,
    name: info.name || fallback.name || '',
    email: info.email || fallback.email,
    role: info.role || 'USER',
  };
}

// Первый экран приложения: вход или регистрация + выбор языка.
// Пока пользователь не вошёл, остальные экраны не показываются (см. App.js).
export default function LoginScreen() {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const insets = useSafeAreaInsets();
  const { lang, setLang, setUser } = useApp();
  const L = TEXT[lang] || TEXT.ru;

  const [mode, setMode] = useState('login'); // login | register
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
        // если сервер после регистрации не дал ключ — сразу входим
        if (!data || !(data.token || data.accessToken)) data = await api.login(mail, password);
      } else {
        data = await api.login(mail, password);
      }
      // после этого App.js сам покажет главный экран
      setUser(toUser(data, { name: name.trim(), email: mail }));
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  const switchMode = (next) => {
    setMode(next);
    setError('');
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView
        contentContainerStyle={{ flexGrow: 1, padding: 20, paddingTop: insets.top + 16, paddingBottom: insets.bottom + 24 }}
        keyboardShouldPersistTaps="handled"
      >
        {/* выбор языка — сразу на первом экране */}
        <View style={s.langRow}>
          {LANGUAGES.map((item) => {
            const active = lang === item.id;
            return (
              <Pressable key={item.id} onPress={() => setLang(item.id)} style={[s.lang, active && s.langActive]}>
                <Text style={[s.langText, active && s.langTextActive]}>{item.title}</Text>
              </Pressable>
            );
          })}
        </View>

        {/* логотип и название */}
        <View style={s.logoBox}>
          <PopIn style={s.logo}>
            <Ionicons name="library-outline" size={38} color={c.onPrimary} />
          </PopIn>
          <FadeIn delay={150}>
            <Text style={s.appName}>{t('appName')}</Text>
          </FadeIn>
          <FadeIn delay={250}>
            <Text style={s.subtitle}>{t('appSubtitle')}</Text>
          </FadeIn>
        </View>

        {/* два варианта: Вход | Регистрация */}
        <FadeIn delay={300}>
          <SlideTabs
            c={c}
            style={{ marginBottom: 14 }}
            value={mode}
            onChange={switchMode}
            items={[
              { id: 'login', title: L.login },
              { id: 'register', title: L.register },
            ]}
          />
        </FadeIn>

        <Text style={s.hint}>{L.hint}</Text>

        {isRegister ? (
          <FadeIn>
            <Field c={c} s={s} icon="person-outline" value={name} onChangeText={setName} placeholder={L.name} />
          </FadeIn>
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

        <PressScale style={[s.button, busy && { opacity: 0.7 }]} onPress={submit} disabled={busy}>
          {busy ? <ActivityIndicator color={c.onPrimary} /> : <Text style={s.buttonText}>{isRegister ? L.register : L.login}</Text>}
        </PressScale>

        <Pressable style={s.switch} onPress={() => switchMode(isRegister ? 'login' : 'register')}>
          <Text style={s.switchText}>{isRegister ? L.haveAccount : L.noAccount}</Text>
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

// Одно поле ввода со значком
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
    langRow: { flexDirection: 'row', justifyContent: 'center', gap: 8 },
    lang: {
      paddingHorizontal: 14, height: 38, borderRadius: 19, justifyContent: 'center',
      backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
    },
    langActive: { backgroundColor: c.primary, borderColor: c.primary },
    langText: { color: c.text, fontWeight: '700' },
    langTextActive: { color: c.onPrimary },
    logoBox: { alignItems: 'center', marginTop: 36, marginBottom: 28 },
    logo: {
      width: 84, height: 84, borderRadius: 26, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center', marginBottom: 14,
    },
    appName: { fontSize: 28, fontWeight: '800', color: c.text },
    subtitle: { fontSize: 14, color: c.sub, marginTop: 4, textAlign: 'center' },
    tabs: {
      flexDirection: 'row', padding: 4, borderRadius: 16, backgroundColor: c.surface,
      borderWidth: 1, borderColor: c.border, marginBottom: 14,
    },
    tab: { flex: 1, height: 44, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
    tabActive: { backgroundColor: c.primary },
    tabText: { color: c.sub, fontWeight: '800', fontSize: 15 },
    tabTextActive: { color: c.onPrimary },
    hint: { color: c.sub, textAlign: 'center', lineHeight: 20, marginBottom: 16 },
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