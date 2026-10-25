import React, { useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// Сравнение двух статей: выбрать статью А и статью Б через поиск, ИИ объясняет общее и разницу
export default function AiCompareScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { user, lang } = useApp();

  const [slots, setSlots] = useState({ a: null, b: null }); // выбранные статьи
  const [active, setActive] = useState(null);               // какой слот сейчас выбираем: 'a' | 'b' | null
  const [query, setQuery] = useState('');
  const [found, setFound] = useState([]);
  const [searching, setSearching] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);               // { text, articles }

  const openSlot = (key) => {
    setActive(active === key ? null : key);
    setQuery('');
    setFound([]);
  };

  const search = async () => {
    if (!query.trim()) return;
    setSearching(true);
    try {
      setFound(await api.search(query.trim()));
    } catch (e) {
      setFound([]);
    } finally {
      setSearching(false);
    }
  };

  const pick = (article) => {
    setSlots((old) => ({ ...old, [active]: article }));
    setActive(null);
    setResult(null);
  };

  const compare = async () => {
    if (!slots.a || !slots.b || loading) return;
    if (slots.a.id === slots.b.id) { setError(t('cmpSame')); return; }
    setError('');
    setLoading(true);
    try {
      setResult(await api.aiCompare(user.token, [slots.a.id, slots.b.id], lang));
    } catch (e) {
      setError(e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message);
    } finally {
      setLoading(false);
    }
  };

  const Slot = ({ k, label }) => {
    const a = slots[k];
    const on = active === k;
    return (
      <PressScale style={[s.slot, on && s.slotOn]} onPress={() => openSlot(k)}>
        <View style={s.slotBadge}>
          <Text style={s.slotBadgeText}>{label}</Text>
        </View>
        <View style={{ flex: 1 }}>
          {a ? (
            <>
              <Text style={s.slotTitle} numberOfLines={2}>{tr(a, 'title')}</Text>
              <Text style={s.slotLaw} numberOfLines={1}>{a.law ? tr(a.law, 'title') : ''}</Text>
            </>
          ) : (
            <Text style={s.slotEmpty}>{t('cmpPick')}</Text>
          )}
        </View>
        <Ionicons name={on ? 'chevron-up' : a ? 'swap-horizontal' : 'add-circle-outline'} size={22} color={c.primary} />
      </PressScale>
    );
  };

  const SearchPanel = () => (
    <FadeIn>
      <View style={s.panel}>
        <View style={s.searchRow}>
          <TextInput
            style={s.searchInput}
            value={query}
            onChangeText={setQuery}
            placeholder={t('searchPlaceholder')}
            placeholderTextColor={c.sub}
            returnKeyType="search"
            onSubmitEditing={search}
            autoFocus
          />
          <Pressable style={s.searchBtn} onPress={search}>
            {searching ? <ActivityIndicator color={c.onPrimary} /> : <Ionicons name="search" size={20} color={c.onPrimary} />}
          </Pressable>
        </View>
        {!found.length && !searching ? <Text style={s.hint}>{t('cmpSearchHint')}</Text> : null}
        {found.slice(0, 8).map((a) => (
          <Pressable key={a.id} style={s.found} onPress={() => pick(a)}>
            <Text style={s.foundTitle} numberOfLines={2}>{tr(a, 'title')}</Text>
            <Text style={s.slotLaw} numberOfLines={1}>{a.law ? tr(a.law, 'title') : ''}</Text>
          </Pressable>
        ))}
      </View>
    </FadeIn>
  );

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <Header title={t('cmpTitle')} subtitle={t('cmpSubtitle')} onBack={() => navigation.goBack()} />

      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
        <Slot k="a" label="A" />
        {active === 'a' ? SearchPanel() : null}

        <View style={s.vs}><Text style={s.vsText}>VS</Text></View>

        <Slot k="b" label="B" />
        {active === 'b' ? SearchPanel() : null}

        {error ? <Text style={s.error}>{error}</Text> : null}

        <Pressable
          style={[s.button, (!slots.a || !slots.b || loading) && { opacity: 0.5 }]}
          onPress={compare}
          disabled={!slots.a || !slots.b || loading}
        >
          {loading ? <ActivityIndicator color={c.onPrimary} /> : <Ionicons name="git-compare-outline" size={20} color={c.onPrimary} />}
          <Text style={s.buttonText}>{loading ? t('cmpLoading') : t('cmpButton')}</Text>
        </Pressable>

        {result ? (
          <FadeIn>
            <View style={s.result}>
              <RichText text={result.text} style={s.resultText} bold={{ fontWeight: '800' }} />
              <View style={{ marginTop: 12 }}>
                {result.articles.map((a) => (
                  <PressScale key={a.id} style={s.article} onPress={() => navigation.navigate('Article', { id: a.id })}>
                    <Ionicons name="document-text-outline" size={18} color={c.primary} />
                    <Text style={s.articleTitle} numberOfLines={2}>{tr(a, 'title')}</Text>
                    <Ionicons name="chevron-forward" size={18} color={c.sub} />
                  </PressScale>
                ))}
              </View>
            </View>
          </FadeIn>
        ) : null}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

// **жирный** текст и списки «* » / «• »
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
    slot: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18, minHeight: 72,
      backgroundColor: c.card, borderWidth: 1.5, borderColor: c.border,
    },
    slotOn: { borderColor: c.primary },
    slotBadge: {
      width: 40, height: 40, borderRadius: 12, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    slotBadgeText: { fontSize: 18, fontWeight: '900', color: c.primary },
    slotTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    slotLaw: { fontSize: 12, color: c.sub, marginTop: 2 },
    slotEmpty: { fontSize: 15, color: c.sub },
    vs: { alignItems: 'center', marginVertical: 8 },
    vsText: { fontSize: 14, fontWeight: '900', color: c.sub, letterSpacing: 2 },
    panel: { marginTop: 8, padding: 12, borderRadius: 16, backgroundColor: c.surface },
    searchRow: { flexDirection: 'row', gap: 8 },
    searchInput: {
      flex: 1, height: 46, borderRadius: 14, paddingHorizontal: 14, backgroundColor: c.card,
      color: c.text, fontSize: 15, borderWidth: 1, borderColor: c.border,
    },
    searchBtn: {
      width: 46, height: 46, borderRadius: 14, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
    hint: { fontSize: 13, color: c.sub, marginTop: 10, lineHeight: 18 },
    found: { padding: 12, borderRadius: 12, backgroundColor: c.card, marginTop: 8 },
    foundTitle: { fontSize: 14, fontWeight: '700', color: c.text },
    error: { color: c.danger, marginTop: 12, fontWeight: '600' },
    button: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 52,
      borderRadius: 16, backgroundColor: c.primary, marginTop: 18,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    result: { marginTop: 18, padding: 16, borderRadius: 18, backgroundColor: c.card, borderWidth: 1, borderColor: c.border },
    resultText: { color: c.text, fontSize: 15, lineHeight: 23 },
    article: {
      flexDirection: 'row', alignItems: 'center', gap: 8, padding: 10, borderRadius: 12, marginBottom: 6,
      backgroundColor: c.surface,
    },
    articleTitle: { flex: 1, fontSize: 14, fontWeight: '700', color: c.text },
  });