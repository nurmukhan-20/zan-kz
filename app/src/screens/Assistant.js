import React, { useRef, useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// ИИ-помощник: пользователь пишет вопрос, сервер находит статьи и спрашивает Gemini.
// Под ответом — кнопки статей, по которым он отвечал (открывают экран статьи).
export default function AssistantScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const insets = useSafeAreaInsets();
  const { user, lang } = useApp();

  const [messages, setMessages] = useState([]); // { id, from: 'me' | 'bot', text, articles, error }
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef(null);

  const send = async (text) => {
    const question = (text ?? input).trim();
    if (!question || loading) return;
    setInput('');
    setMessages((list) => [...list, { id: Date.now(), from: 'me', text: question }]);
    setLoading(true);
    try {
      const res = await api.ask(user.token, question, lang);
      setMessages((list) => [...list, { id: Date.now() + 1, from: 'bot', text: res.answer, articles: res.articles || [] }]);
    } catch (e) {
      const msg = e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message;
      setMessages((list) => [...list, { id: Date.now() + 1, from: 'bot', text: msg, error: true }]);
    } finally {
      setLoading(false);
    }
  };

  const examples = [t('aiEx1'), t('aiEx2'), t('aiEx3')];

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <Header title={t('aiTitle')} subtitle={t('aiSubtitle')} onBack={() => navigation.goBack()} />

      <ScrollView
        ref={scrollRef}
        style={{ flex: 1 }}
        contentContainerStyle={{ padding: 16, paddingBottom: 24 }}
        keyboardShouldPersistTaps="handled"
        onContentSizeChange={() => scrollRef.current?.scrollToEnd({ animated: true })}
      >
        {/* приветствие и примеры вопросов */}
        <FadeIn>
          <View style={[s.bubble, s.bot]}>
            <View style={s.botHead}>
              <Ionicons name="sparkles" size={16} color={c.primary} />
              <Text style={s.botName}>{t('aiTitle')}</Text>
            </View>
            <Text style={s.botText}>{t('aiWelcome')}</Text>
          </View>
        </FadeIn>

        {messages.length === 0 ? (
          <View style={s.examples}>
            {examples.map((ex, i) => (
              <FadeIn key={ex} index={i + 1}>
                <PressScale style={s.example} onPress={() => send(ex)}>
                  <Ionicons name="chatbubble-ellipses-outline" size={16} color={c.primary} />
                  <Text style={s.exampleText}>{ex}</Text>
                </PressScale>
              </FadeIn>
            ))}
          </View>
        ) : null}

        {messages.map((m) => (
          <FadeIn key={m.id}>
            {m.from === 'me' ? (
              <View style={[s.bubble, s.me]}>
                <Text style={s.meText}>{m.text}</Text>
              </View>
            ) : (
              <View style={[s.bubble, s.bot, m.error && { borderColor: c.danger }]}>
                {m.error ? (
                  <View style={s.botHead}>
                    <Ionicons name="alert-circle-outline" size={16} color={c.danger} />
                  </View>
                ) : null}
                <RichText text={m.text} style={s.botText} bold={{ fontWeight: '800' }} />

                {m.articles && m.articles.length ? (
                  <View style={{ marginTop: 10 }}>
                    <Text style={s.sources}>{t('aiSources')}</Text>
                    {m.articles.map((a) => (
                      <PressScale key={a.id} style={s.article} onPress={() => navigation.navigate('Article', { id: a.id })}>
                        <Ionicons name="document-text-outline" size={18} color={c.primary} />
                        <View style={{ flex: 1, marginHorizontal: 8 }}>
                          <Text style={s.articleTitle} numberOfLines={2}>{tr(a, 'title')}</Text>
                          <Text style={s.articleLaw} numberOfLines={1}>{tr(a, 'lawTitle')}</Text>
                        </View>
                        <Ionicons name="chevron-forward" size={18} color={c.sub} />
                      </PressScale>
                    ))}
                  </View>
                ) : null}
              </View>
            )}
          </FadeIn>
        ))}

        {loading ? (
          <View style={[s.bubble, s.bot, s.thinking]}>
            <ActivityIndicator size="small" color={c.primary} />
            <Text style={[s.botText, { marginLeft: 8, color: c.sub }]}>{t('aiThinking')}</Text>
          </View>
        ) : null}
      </ScrollView>

      {/* строка ввода */}
      <View style={[s.inputBar, { paddingBottom: insets.bottom + 8 }]}>
        <Text style={s.privacy}>{t('aiPrivacy')}</Text>
        <View style={s.inputRow}>
          <TextInput
            style={s.input}
            value={input}
            onChangeText={setInput}
            placeholder={t('aiPlaceholder')}
            placeholderTextColor={c.sub}
            multiline
            maxLength={1000}
          />
          <Pressable
            style={[s.send, (!input.trim() || loading) && { opacity: 0.4 }]}
            onPress={() => send()}
            disabled={!input.trim() || loading}
          >
            <Ionicons name="arrow-up" size={22} color={c.onPrimary} />
          </Pressable>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

// Gemini пишет **жирный** текст и списки «* » — показываем их красиво
function RichText({ text, style, bold }) {
  const lines = String(text || '').split('\n').map((line) => line.replace(/^\s*[*-]\s+/, '• '));
  return (
    <Text style={style}>
      {lines.map((line, i) => (
        <Text key={i}>
          {line.split(/(\*\*[^*]+\*\*)/g).map((part, j) =>
            part.startsWith('**') && part.endsWith('**')
              ? <Text key={j} style={bold}>{part.slice(2, -2)}</Text>
              : part)}
          {i < lines.length - 1 ? '\n' : ''}
        </Text>
      ))}
    </Text>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    bubble: { maxWidth: '88%', borderRadius: 18, padding: 12, marginBottom: 10 },
    me: { alignSelf: 'flex-end', backgroundColor: c.primary, borderBottomRightRadius: 6 },
    meText: { color: c.onPrimary, fontSize: 15, lineHeight: 21 },
    bot: {
      alignSelf: 'flex-start', backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
      borderBottomLeftRadius: 6,
    },
    botHead: { flexDirection: 'row', alignItems: 'center', gap: 6, marginBottom: 6 },
    botName: { fontSize: 13, fontWeight: '800', color: c.primary },
    botText: { color: c.text, fontSize: 15, lineHeight: 22 },
    thinking: { flexDirection: 'row', alignItems: 'center' },
    examples: { marginBottom: 6 },
    example: {
      flexDirection: 'row', alignItems: 'center', gap: 8, alignSelf: 'flex-start',
      paddingHorizontal: 12, paddingVertical: 10, borderRadius: 14, marginBottom: 8,
      backgroundColor: c.primarySoft,
    },
    exampleText: { color: c.text, fontSize: 14, fontWeight: '600', flexShrink: 1 },
    sources: { fontSize: 12, fontWeight: '800', color: c.sub, marginBottom: 6, textTransform: 'uppercase' },
    article: {
      flexDirection: 'row', alignItems: 'center', padding: 10, borderRadius: 12, marginBottom: 6,
      backgroundColor: c.surface,
    },
    articleTitle: { fontSize: 14, fontWeight: '700', color: c.text },
    articleLaw: { fontSize: 12, color: c.sub, marginTop: 2 },
    inputBar: { paddingHorizontal: 12, paddingTop: 6, borderTopWidth: 1, borderTopColor: c.border, backgroundColor: c.bg },
    privacy: { fontSize: 11, color: c.sub, textAlign: 'center', marginBottom: 6 },
    inputRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 8 },
    input: {
      flex: 1, minHeight: 46, maxHeight: 120, borderRadius: 22, paddingHorizontal: 16, paddingTop: 12, paddingBottom: 12,
      backgroundColor: c.surface, color: c.text, fontSize: 15,
    },
    send: {
      width: 46, height: 46, borderRadius: 23, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
  });