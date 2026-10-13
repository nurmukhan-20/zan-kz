import React from 'react';
import { Linking, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { LANGUAGES } from '../i18n';
import { useApp, useT, useTheme } from '../store';

export default function ProfileScreen() {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const insets = useSafeAreaInsets();
  const { lang, setLang, themeMode, setThemeMode } = useApp();

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: c.bg }}
      contentContainerStyle={{ padding: 16, paddingTop: insets.top + 12, paddingBottom: 32 }}
    >
      <Text style={s.title}>{t('tabProfile')}</Text>

      {/* язык */}
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

      {/* тема */}
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

      {/* о приложении */}
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
  });