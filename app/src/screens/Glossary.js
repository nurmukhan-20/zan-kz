import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api } from '../api';
import { useApp, useTheme, useTr } from '../store';
import StatusView from '../components/StatusView';

const TEXT = {
  kk: { title: 'Сөздік', hint: 'Терминді іздеу', empty: 'Термин табылмады', intro: 'Заң терминдері қарапайым тілмен. Түсіндірмені ашу үшін терминді басыңыз.' },
  ru: { title: 'Словарь', hint: 'Найти термин', empty: 'Термин не найден', intro: 'Юридические термины простыми словами. Нажмите на термин, чтобы открыть объяснение.' },
  en: { title: 'Glossary', hint: 'Find a term', empty: 'No terms found', intro: 'Legal terms in plain words. Tap a term to open its explanation.' },
};

function pick(tr, obj, names) {
  for (const name of names) {
    const value = tr(obj, name) || obj[name];
    if (value) return String(value);
  }
  return '';
}

export default function GlossaryScreen() {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const { lang } = useApp();
  const L = TEXT[lang] || TEXT.ru;
  const insets = useSafeAreaInsets();

  const [terms, setTerms] = useState([]);
  const [status, setStatus] = useState('loading');
  const [query, setQuery] = useState('');
  const [openId, setOpenId] = useState(null);

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      setTerms(await api.terms());
      setStatus('ok');
    } catch (e) {
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const items = useMemo(() => {
    const list = terms.map((item) => ({
      id: item.id,
      name: pick(tr, item, ['term', 'name', 'title', 'word']),
      text: pick(tr, item, ['definition', 'description', 'explanation', 'text']),
    }));
    list.sort((a, b) => a.name.localeCompare(b.name));
    const q = query.trim().toLowerCase();
    if (!q) return list;
    return list.filter((item) => item.name.toLowerCase().includes(q) || item.text.toLowerCase().includes(q));
  }, [terms, tr, query]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg, paddingTop: insets.top + 12 }}>
      <Text style={s.title}>{L.title}</Text>

      {/* поиск по словарю */}
      <View style={s.inputBox}>
        <Ionicons name="search" size={20} color={c.sub} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder={L.hint}
          placeholderTextColor={c.sub}
          style={s.input}
        />
        {query ? (
          <Pressable onPress={() => setQuery('')} hitSlop={8}>
            <Ionicons name="close-circle" size={20} color={c.sub} />
          </Pressable>
        ) : null}
      </View>

      <StatusView status={status} onRetry={load} />

      {status === 'ok' ? (
        <FlatList
          data={items}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}
          keyboardShouldPersistTaps="handled"
          ListHeaderComponent={query ? null : <Text style={s.intro}>{L.intro}</Text>}
          ListEmptyComponent={<StatusView status="empty" emptyText={L.empty} emptyIcon="reader-outline" />}
          renderItem={({ item }) => {
            const open = openId === item.id;
            return (
              <Pressable style={[s.card, open && s.cardOpen]} onPress={() => setOpenId(open ? null : item.id)}>
                <View style={s.cardHead}>
                  <Text style={s.cardTitle}>{item.name}</Text>
                  <Ionicons name={open ? 'chevron-up' : 'chevron-down'} size={20} color={c.primary} />
                </View>
                <Text style={s.cardText} numberOfLines={open ? undefined : 2}>{item.text}</Text>
              </Pressable>
            );
          }}
        />
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    title: { fontSize: 26, fontWeight: '800', color: c.text, paddingHorizontal: 16, marginBottom: 12 },
    inputBox: {
      flexDirection: 'row', alignItems: 'center', gap: 8, height: 50, borderRadius: 16, marginHorizontal: 16,
      marginBottom: 12, paddingHorizontal: 14, backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
    },
    input: { flex: 1, fontSize: 16, color: c.text },
    intro: { color: c.sub, lineHeight: 20, marginBottom: 12 },
    card: {
      padding: 16, borderRadius: 18, marginBottom: 10,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    cardOpen: { backgroundColor: c.primarySoft, borderColor: c.primarySoft },
    cardHead: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 10 },
    cardTitle: { flex: 1, fontSize: 16, fontWeight: '800', color: c.primary },
    cardText: { fontSize: 14, color: c.text, marginTop: 6, lineHeight: 20 },
  });