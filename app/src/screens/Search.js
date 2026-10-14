import React, { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api } from '../api';
import { useT, useTheme, useTr } from '../store';
import StatusView from '../components/StatusView';

function normalize(text) {
  const value = text.trim();
  const match =
    value.match(/^(?:статья|ст\.?|бап|article|art\.?)\s*(\d+)$/i) ||
    value.match(/^(\d+)\s*-?\s*(?:бап|статья)$/i);
  return match ? match[1] : value;
}

export default function SearchScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const insets = useSafeAreaInsets();

  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [status, setStatus] = useState('idle');
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const text = normalize(query);
    if (!text) {
      setResults([]);
      setStatus('idle');
      return undefined;
    }
    let cancelled = false;
    setStatus('loading');
    const timer = setTimeout(async () => {
      try {
        const data = await api.search(text);
        if (cancelled) return;
        const sorted = [...data].sort(
          (a, b) => Number(b.articleNumber === text) - Number(a.articleNumber === text),
        );
        setResults(sorted);
        setStatus(sorted.length ? 'ok' : 'empty');
      } catch (e) {
        if (!cancelled) setStatus('error');
      }
    }, 400);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [query, attempt]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      {/* строка поиска с кнопкой «назад» */}
      <View style={[s.top, { paddingTop: insets.top + 10 }]}>
        <Pressable style={s.round} onPress={() => navigation.goBack()} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={c.text} />
        </Pressable>
        <View style={s.inputBox}>
          <Ionicons name="search" size={20} color={c.sub} />
          <TextInput
            value={query}
            onChangeText={setQuery}
            placeholder={t('searchPlaceholder')}
            placeholderTextColor={c.sub}
            style={s.input}
            autoFocus
            returnKeyType="search"
          />
          {query ? (
            <Pressable onPress={() => setQuery('')} hitSlop={8}>
              <Ionicons name="close-circle" size={20} color={c.sub} />
            </Pressable>
          ) : null}
        </View>
      </View>

      {status === 'idle' ? (
        <View style={s.hint}>
          <Ionicons name="bulb-outline" size={22} color={c.primary} />
          <Text style={s.hintText}>{t('searchHint')}</Text>
        </View>
      ) : null}

      <StatusView
        status={status}
        onRetry={() => setAttempt((n) => n + 1)}
        emptyText={t('nothingFound')}
        emptyIcon="search-outline"
      />

      {status === 'ok' ? (
        <FlatList
          data={results}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={{ padding: 16, paddingTop: 4 }}
          keyboardShouldPersistTaps="handled"
          ListHeaderComponent={<Text style={s.count}>{t('found')}: {results.length}</Text>}
          renderItem={({ item }) => (
            <Pressable style={s.card} onPress={() => navigation.navigate('Article', { id: item.id })}>
              <View style={s.badge}>
                <Text style={s.badgeText}>{item.articleNumber}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={s.cardTitle}>{tr(item, 'title')}</Text>
                <Text style={s.cardLaw} numberOfLines={1}>{tr(item.law, 'title')}</Text>
                <Text style={s.cardText} numberOfLines={2}>{tr(item, 'explanation') || tr(item, 'text')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={20} color={c.sub} />
            </Pressable>
          )}
        />
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    top: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingHorizontal: 16, paddingBottom: 10 },
    round: {
      width: 42, height: 42, borderRadius: 21, backgroundColor: c.surface,
      alignItems: 'center', justifyContent: 'center',
    },
    inputBox: {
      flex: 1, flexDirection: 'row', alignItems: 'center', gap: 8, height: 50, borderRadius: 16,
      paddingHorizontal: 14, backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
    },
    input: { flex: 1, fontSize: 16, color: c.text },
    hint: {
      flexDirection: 'row', gap: 10, margin: 16, padding: 14, borderRadius: 16,
      backgroundColor: c.primarySoft,
    },
    hintText: { flex: 1, color: c.text, lineHeight: 20 },
    count: { color: c.sub, marginBottom: 10, fontWeight: '600' },
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    badge: {
      width: 46, height: 46, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    badgeText: { color: c.primary, fontWeight: '800', fontSize: 16 },
    cardTitle: { fontSize: 16, fontWeight: '800', color: c.text },
    cardLaw: { fontSize: 12, color: c.sub, marginTop: 2 },
    cardText: { fontSize: 13, color: c.text, marginTop: 6, lineHeight: 18 },
  });