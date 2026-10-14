import React, { useCallback, useEffect, useState } from 'react';
import { Alert, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useApp, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

const TEXT = {
  kk: { title: 'Қаралған баптар', empty: 'Тарих бос. Ашқан баптарыңыз осында шығады.', clear: 'Тарихты тазалау', yes: 'Иә', no: 'Жоқ' },
  ru: { title: 'История просмотров', empty: 'История пуста. Здесь появятся статьи, которые вы открывали.', clear: 'Очистить историю', yes: 'Да', no: 'Нет' },
  en: { title: 'History', empty: 'History is empty. Articles you open will appear here.', clear: 'Clear history', yes: 'Yes', no: 'No' },
};

export default function HistoryScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const { lang, user } = useApp();
  const L = TEXT[lang] || TEXT.ru;

  const [items, setItems] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    if (!user) return;
    setStatus('loading');
    try {
      const data = await api.history(user.token);
      const list = data.map((item, index) => ({
        key: String(item.id ?? index),
        article: item.article || item,
        date: item.viewedAt || item.createdAt || item.date,
      }));
      setItems(list);
      setStatus(list.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, [user]);

  useEffect(() => {
    load();
  }, [load]);

  const clear = () => {
    Alert.alert(L.clear, '', [
      { text: L.no, style: 'cancel' },
      {
        text: L.yes,
        style: 'destructive',
        onPress: async () => {
          try {
            await api.clearHistory(user.token);
            setItems([]);
            setStatus('empty');
          } catch (e) {
            setStatus('error');
          }
        },
      },
    ]);
  };

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header
        title={L.title}
        onBack={() => navigation.goBack()}
        right={
          status === 'ok' ? (
            <Pressable style={s.round} onPress={clear} hitSlop={8}>
              <Ionicons name="trash-outline" size={20} color={c.danger} />
            </Pressable>
          ) : null
        }
      />

      <StatusView status={status} onRetry={load} emptyText={L.empty} emptyIcon="time-outline" />

      {status === 'ok' ? (
        <FlatList
          data={items}
          keyExtractor={(item) => item.key}
          contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}
          renderItem={({ item }) => (
            <Pressable style={s.card} onPress={() => navigation.navigate('Article', { id: item.article.id })}>
              <View style={s.icon}>
                <Ionicons name="time-outline" size={22} color={c.primary} />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={s.cardTitle}>{tr(item.article, 'title')}</Text>
                {item.date ? <Text style={s.date}>{formatDate(item.date)}</Text> : null}
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
    round: {
      width: 42, height: 42, borderRadius: 21, backgroundColor: c.surface,
      alignItems: 'center', justifyContent: 'center',
    },
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    icon: {
      width: 44, height: 44, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    cardTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    date: { fontSize: 12, color: c.sub, marginTop: 4 },
  });