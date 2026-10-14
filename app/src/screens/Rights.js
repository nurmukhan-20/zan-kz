import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

const CONSTITUTION_ID = 1;

export default function RightsScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();

  const [articles, setArticles] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      const data = await api.articles(CONSTITUTION_ID, 'II');
      setArticles(data);
      setStatus(data.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={t('myRights')} subtitle={t('section_II')} onBack={() => navigation.goBack()} />

      <StatusView status={status} onRetry={load} emptyText={t('nothingFound')} />

      {status === 'ok' ? (
        <FlatList
          data={articles}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={{ padding: 16, paddingTop: 4 }}
          ListHeaderComponent={
            <View style={s.intro}>
              <Ionicons name="shield-checkmark-outline" size={22} color={c.primary} />
              <Text style={s.introText}>{t('rightsIntro')}</Text>
            </View>
          }
          renderItem={({ item }) => (
            <Pressable style={s.card} onPress={() => navigation.navigate('Article', { id: item.id })}>
              <View style={s.cardHead}>
                <Text style={s.cardTitle}>{tr(item, 'title')}</Text>
                <Ionicons name="chevron-forward" size={18} color={c.sub} />
              </View>
              <Text style={s.cardText} numberOfLines={3}>{tr(item, 'explanation')}</Text>
            </Pressable>
          )}
        />
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    intro: {
      flexDirection: 'row', gap: 10, padding: 14, borderRadius: 18, marginBottom: 14,
      backgroundColor: c.primarySoft,
    },
    introText: { flex: 1, color: c.text, lineHeight: 20 },
    card: {
      padding: 16, borderRadius: 18, marginBottom: 10,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    cardHead: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
    cardTitle: { fontSize: 16, fontWeight: '800', color: c.primary },
    cardText: { fontSize: 14, color: c.text, marginTop: 6, lineHeight: 20 },
  });