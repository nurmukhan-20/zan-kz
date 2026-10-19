import React from 'react';
import { Linking, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { LANGUAGES } from '../i18n';
import { useApp, useT, useTheme } from '../store';
import { FadeIn } from '../components/Motion';

const ACCOUNT = {
  kk: { title: 'Аккаунт', guest: 'Таңдаулылар мен тарихты сақтау үшін кіріңіз', login: 'Кіру / Тіркелу', history: 'Қаралған баптар', logout: 'Шығу', admin: 'Әкімші' },
  ru: { title: 'Аккаунт', guest: 'Войдите, чтобы сохранять избранное и историю', login: 'Войти / Регистрация', history: 'История просмотров', logout: 'Выйти', admin: 'Администратор' },
  en: { title: 'Account', guest: 'Sign in to save favorites and history', login: 'Sign in / Sign up', history: 'Viewing history', logout: 'Sign out', admin: 'Administrator' },
};

export default function ProfileScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const insets = useSafeAreaInsets();
  const { lang, setLang, themeMode, setThemeMode, user, setUser } = useApp();
  const A = ACCOUNT[lang] || ACCOUNT.ru;

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: c.bg }}
      contentContainerStyle={{ padding: 16, paddingTop: insets.top + 12, paddingBottom: 32 }}
    >
      <Text style={s.title}>{t('tabProfile')}</Text>

      {/* аккаунт */}
      <FadeIn index={0}>
      <Text style={s.section}>{A.title}</Text>
      {user ? (
        <View style={s.card}>
          <View style={s.userRow}>
            <View style={s.avatar}>
              <Text style={s.avatarText}>{(user.name || user.email || '?').charAt(0).toUpperCase()}</Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={s.cardTitle}>{user.name || user.email}</Text>
              <Text style={s.cardText}>{user.email}</Text>
              {user.role === 'ADMIN' ? <Text style={s.role}>{A.admin}</Text> : null}
            </View>
          </View>
          <Pressable style={s.menuRow} onPress={() => navigation.navigate('History')}>
            <Ionicons name="time-outline" size={20} color={c.primary} />
            <Text style={s.menuText}>{A.history}</Text>
            <Ionicons name="chevron-forward" size={18} color={c.sub} />
          </Pressable>
          <Pressable style={s.menuRow} onPress={() => setUser(null)}>
            <Ionicons name="log-out-outline" size={20} color={c.danger} />
            <Text style={[s.menuText, { color: c.danger }]}>{A.logout}</Text>
          </Pressable>
        </View>
      ) : (
        <View style={s.card}>
          <Text style={s.cardText}>{A.guest}</Text>
          <Pressable style={s.loginButton} onPress={() => navigation.navigate('Login')}>
            <Ionicons name="log-in-outline" size={20} color={c.onPrimary} />
            <Text style={s.loginText}>{A.login}</Text>
          </Pressable>
        </View>
      )}

      </FadeIn>

      {/* язык */}
      <FadeIn index={1}>
      <Text style={s.section}>{t('language')}</Text>
      <View style={s.row}>
        {LANGUAGES.map((item) => {
          const active = lang === item.id;
          return (
            <Pressable
              key={item.id}
              onPress={() => setLang(item.id)}
              style={[s.choice, active && s.choiceActive]}
            >
              <Text style={[s.choiceText, active && s.choiceTextActive]}>{item.title}</Text>
            </Pressable>
          );
        })}
      </View>

      </FadeIn>

      {/* тема */}
      <FadeIn index={2}>
      <Text style={s.section}>{t('theme')}</Text>
      <View style={s.row}>
        {[
          { id: 'light', title: t('themeLight'), icon: 'sunny-outline' },
          { id: 'dark', title: t('themeDark'), icon: 'moon-outline' },
        ].map((mode) => {
          const active = themeMode === mode.id;
          return (
            <Pressable
              key={mode.id}
              onPress={() => setThemeMode(mode.id)}
              style={[s.choice, s.themeChoice, active && s.choiceActive]}
            >
              <Ionicons name={mode.icon} size={22} color={active ? c.onPrimary : c.primary} />
              <Text style={[s.choiceText, { marginTop: 6 }, active && s.choiceTextActive]}>{mode.title}</Text>
            </Pressable>
          );
        })}
      </View>

      </FadeIn>

      {/* о приложении */}
      <FadeIn index={3}>
      <Text style={s.section}>{t('about')}</Text>
      <View style={s.card}>
        <Text style={s.cardTitle}>{t('appName')}</Text>
        <Text style={s.cardText}>{t('aboutText')}</Text>
        <Text style={[s.cardText, { marginTop: 10 }]}>{t('disclaimer')}</Text>
        <Pressable style={s.link} onPress={() => Linking.openURL('https://adilet.zan.kz')}>
          <Ionicons name="open-outline" size={18} color={c.primary} />
          <Text style={s.linkText}>{t('source')}: adilet.zan.kz</Text>
        </Pressable>
        <Text style={[s.cardText, { marginTop: 10 }]}>{t('version')} 1.0.0</Text>
      </View>
      </FadeIn>
    </ScrollView>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    title: { fontSize: 26, fontWeight: '800', color: c.text, marginBottom: 8 },
    section: { fontSize: 17, fontWeight: '800', color: c.text, marginTop: 18, marginBottom: 10 },
    row: { flexDirection: 'row', gap: 10 },
    choice: {
      flex: 1, height: 48, borderRadius: 16, alignItems: 'center', justifyContent: 'center',
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    themeChoice: { height: 76 },
    choiceActive: { backgroundColor: c.primary, borderColor: c.primary },
    choiceText: { color: c.text, fontWeight: '700' },
    choiceTextActive: { color: c.onPrimary },
    card: { padding: 16, borderRadius: 18, backgroundColor: c.card, borderWidth: 1, borderColor: c.border },
    cardTitle: { fontSize: 17, fontWeight: '800', color: c.text, marginBottom: 6 },
    cardText: { color: c.sub, lineHeight: 20 },
    link: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 12 },
    linkText: { color: c.primary, fontWeight: '700' },
    userRow: { flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 6 },
    avatar: {
      width: 52, height: 52, borderRadius: 26, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
    avatarText: { color: c.onPrimary, fontSize: 22, fontWeight: '800' },
    role: { color: c.primary, fontWeight: '700', fontSize: 12, marginTop: 2 },
    menuRow: {
      flexDirection: 'row', alignItems: 'center', gap: 10, paddingTop: 14, marginTop: 10,
      borderTopWidth: 1, borderTopColor: c.border,
    },
    menuText: { flex: 1, color: c.text, fontWeight: '700', fontSize: 15 },
    loginButton: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 48,
      borderRadius: 16, backgroundColor: c.primary, marginTop: 12,
    },
    loginText: { color: c.onPrimary, fontWeight: '800', fontSize: 15 },
  });