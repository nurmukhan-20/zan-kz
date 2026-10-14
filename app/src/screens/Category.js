import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

export default function CategoryScreen({ navigation, route }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { category } = route.params;

  const [laws, setLaws] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      const data = await api.laws(category.id);
      setLaws(data);
      setStatus(data.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, [category.id]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={tr(category, 'name')} subtitle={t('laws')} onBack={() => navigation.goBack()} />

      <StatusView status={status} onRetry={load} emptyText={t('noLaws')} emptyIcon="folder-open-outline" />

      {status === 'ok' ? (
        <FlatList
          data={laws}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={{ padding: 16, paddingTop: 4 }}
          renderItem={({ item }) => (
            <Pressable style={s.card} onPress={() => navigation.navigate('Law', { lawId: item.id })}>
              <View style={s.icon}>
                <Ionicons name={category.icon || 'book-outline'} size={24} color={c.primary} />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={s.title}>{tr(item, 'title')}</Text>
                <Text style={s.meta}>{t('lastUpdated')}: {formatDate(item.lastUpdated)}</Text>
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
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 16, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    icon: {
      width: 48, height: 48, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    title: { fontSize: 16, fontWeight: '800', color: c.text },
    meta: { fontSize: 12, color: c.sub, marginTop: 4 },
  });