import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useT, useTheme } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// Меню ИИ-инструментов: чат, моя ситуация, документы, тест, сравнение
const TOOLS = [
  { screen: 'Assistant', icon: 'chatbubbles-outline', title: 'aiTitle', hint: 'aiHint' },
  { screen: 'AiSituation', icon: 'compass-outline', title: 'sitTitle', hint: 'sitHint' },
  { screen: 'AiDocument', icon: 'document-text-outline', title: 'docTitle', hint: 'docHint' },
  { screen: 'AiQuiz', icon: 'school-outline', title: 'quizTitle', hint: 'quizHint' },
  { screen: 'AiCompare', icon: 'git-compare-outline', title: 'cmpTitle', hint: 'cmpHint' },
];

export default function AiHubScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={t('aiToolsTitle')} subtitle={t('aiToolsSubtitle')} onBack={() => navigation.goBack()} />
      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 32 }}>
        {TOOLS.map((tool, i) => (
          <FadeIn key={tool.screen} index={i}>
            <PressScale style={s.card} onPress={() => navigation.navigate(tool.screen)}>
              <View style={s.icon}>
                <Ionicons name={tool.icon} size={26} color={c.primary} />
              </View>
              <View style={{ flex: 1, marginHorizontal: 12 }}>
                <Text style={s.title}>{t(tool.title)}</Text>
                <Text style={s.hint}>{t(tool.hint)}</Text>
              </View>
              <Ionicons name="chevron-forward" size={22} color={c.sub} />
            </PressScale>
          </FadeIn>
        ))}
      </ScrollView>
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    card: {
      flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 20, marginBottom: 12,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    icon: {
      width: 52, height: 52, borderRadius: 16, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    title: { fontSize: 17, fontWeight: '800', color: c.text },
    hint: { fontSize: 13, color: c.sub, marginTop: 3, lineHeight: 18 },
  });