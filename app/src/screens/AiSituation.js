import React, { useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// Поиск по жизненной ситуации: человек описывает ситуацию, ИИ подбирает статьи и план действий
export default function AiSituationScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { user, lang } = useApp();

  const [text, setText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null); // { summary, steps, articles: [{ article, why }], note }

  const examples = [t('sitEx1'), t('sitEx2'), t('sitEx3')];

  const analyze = async (value) => {
    const situation = (value ?? text).trim();
    if (!situation) { setError(t('sitNeed')); return; }
    if (loading) return;
    setText(situation);
    setError('');
    setLoading(true);
    setResult(null);
    try {
      setResult(await api.aiSituation(user.token, situation, lang));
    } catch (e) {
      setError(e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <Header title={t('sitTitle')} subtitle={t('sitSubtitle')} onBack={() => navigation.goBack()} />

      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
        <TextInput
          style={s.input}
          value={text}
          onChangeText={setText}
          placeholder={t('sitPlaceholder')}
          placeholderTextColor={c.sub}
          multiline
          maxLength={1500}
        />
        <Text style={s.privacy}>{t('aiPrivacy')}</Text>

        {!result && !loading ? (
          <View style={{ marginBottom: 8 }}>
            {examples.map((ex, i) => (
              <FadeIn key={ex} index={i}>
                <PressScale style={s.example} onPress={() => analyze(ex)}>
                  <Ionicons name="bulb-outline" size={16} color={c.primary} />
                  <Text style={s.exampleText}>{ex}</Text>
                </PressScale>
              </FadeIn>
            ))}
          </View>
        ) : null}

        {error ? <Text style={s.error}>{error}</Text> : null}

        <Pressable style={[s.button, loading && { opacity: 0.6 }]} onPress={() => analyze()} disabled={loading}>
          {loading ? <ActivityIndicator color={c.onPrimary} /> : <Ionicons name="compass-outline" size={20} color={c.onPrimary} />}
          <Text style={s.buttonText}>{loading ? t('sitLoading') : t('sitButton')}</Text>
        </Pressable>

        {result ? (
          <FadeIn>
            {/* 1. что происходит */}
            <View style={s.card}>
              <View style={s.cardHead}>
                <Ionicons name="information-circle-outline" size={20} color={c.primary} />
                <Text style={s.cardTitle}>{t('sitSummary')}</Text>
              </View>
              <Text style={s.body}>{result.summary}</Text>
            </View>

            {/* 2. что делать */}
            {result.steps && result.steps.length ? (
              <View style={s.card}>
                <View style={s.cardHead}>
                  <Ionicons name="list-outline" size={20} color={c.primary} />
                  <Text style={s.cardTitle}>{t('sitSteps')}</Text>
                </View>
                {result.steps.map((step, i) => (
                  <View key={i} style={s.step}>
                    <View style={s.stepNum}><Text style={s.stepNumText}>{i + 1}</Text></View>
                    <Text style={[s.body, { flex: 1 }]}>{step}</Text>
                  </View>
                ))}
              </View>
            ) : null}

            {/* 3. статьи */}
            <View style={s.card}>
              <View style={s.cardHead}>
                <Ionicons name="document-text-outline" size={20} color={c.primary} />
                <Text style={s.cardTitle}>{t('sitArticles')}</Text>
              </View>
              {result.articles.length ? (
                result.articles.map((m) => (
                  <PressScale key={m.article.id} style={s.article} onPress={() => navigation.navigate('Article', { id: m.article.id })}>
                    <View style={{ flex: 1 }}>
                      <Text style={s.articleTitle} numberOfLines={2}>{tr(m.article, 'title')}</Text>
                      <Text style={s.articleLaw} numberOfLines={1}>{tr(m.article, 'lawTitle')}</Text>
                      {m.why ? <Text style={s.why}>{m.why}</Text> : null}
                    </View>
                    <Ionicons name="chevron-forward" size={18} color={c.sub} />
                  </PressScale>
                ))
              ) : (
                <Text style={s.body}>{t('sitNoArticles')}</Text>
              )}
            </View>

            <Text style={s.note}>{result.note}</Text>
          </FadeIn>
        ) : null}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    input: {
      minHeight: 130, borderRadius: 16, padding: 14, textAlignVertical: 'top',
      backgroundColor: c.surface, color: c.text, fontSize: 15, lineHeight: 22, borderWidth: 1, borderColor: c.border,
    },
    privacy: { fontSize: 12, color: c.sub, marginTop: 6, marginBottom: 12 },
    example: {
      flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 12, paddingVertical: 10,
      borderRadius: 14, marginBottom: 8, backgroundColor: c.primarySoft,
    },
    exampleText: { flex: 1, color: c.text, fontSize: 14, fontWeight: '600' },
    error: { color: c.danger, marginBottom: 10, fontWeight: '600' },
    button: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 52,
      borderRadius: 16, backgroundColor: c.primary,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    card: { marginTop: 14, padding: 16, borderRadius: 18, backgroundColor: c.card, borderWidth: 1, borderColor: c.border },
    cardHead: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 10 },
    cardTitle: { fontSize: 16, fontWeight: '800', color: c.text },
    body: { fontSize: 15, color: c.text, lineHeight: 22 },
    step: { flexDirection: 'row', gap: 10, marginBottom: 10 },
    stepNum: {
      width: 26, height: 26, borderRadius: 13, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center', marginTop: 1,
    },
    stepNumText: { color: c.onPrimary, fontWeight: '800', fontSize: 13 },
    article: {
      flexDirection: 'row', alignItems: 'center', gap: 8, padding: 12, borderRadius: 14, marginBottom: 8,
      backgroundColor: c.surface,
    },
    articleTitle: { fontSize: 14, fontWeight: '800', color: c.text },
    articleLaw: { fontSize: 12, color: c.sub, marginTop: 2 },
    why: { fontSize: 13, color: c.text, marginTop: 6, lineHeight: 19 },
    note: { fontSize: 12, color: c.sub, marginTop: 14, lineHeight: 17 },
  });