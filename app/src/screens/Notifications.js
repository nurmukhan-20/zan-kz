import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useApp, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

const TEXT = {
  kk: { title: 'Хабарламалар', subtitle: 'Заңдардың жаңартулары', empty: 'Әзірге хабарлама жоқ' },
  ru: { title: 'Уведомления', subtitle: 'Обновления законов', empty: 'Пока нет уведомлений' },
  en: { title: 'Notifications', subtitle: 'Law updates', empty: 'No notifications yet' },
};

function pick(tr, obj, names) {
  for (const name of names) {
    const value = tr(obj, name) || obj[name];
    if (value) return String(value);
  }
  return '';
}

export default function NotificationsScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const { lang } = useApp();
  const L = TEXT[lang] || TEXT.ru;

  const [items, setItems] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      const data = await api.notifications();
      const sorted = [...data].sort((a, b) => (b.id || 0) - (a.id || 0));
      setItems(sorted);
      setStatus(sorted.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={L.title} subtitle={L.subtitle} onBack={() => navigation.goBack()} />

      <StatusView status={status} onRetry={load} emptyText={L.empty} emptyIcon="notifications-off-outline" />

      {status === 'ok' ? (
        <FlatList
          data={items}
          keyExtractor={(item, index) => String(item.id ?? index)}
          contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}
          renderItem={({ item }) => {
            const lawId = item.lawId || (item.law && item.law.id);
            const title = pick(tr, item, ['title', 'subject']);
            const message = pick(tr, item, ['message', 'text', 'body', 'description']);
            const date = item.createdAt || item.date;
            return (
              <Pressable
                style={s.card}
                disabled={!lawId}
                onPress={() => navigation.navigate('Law', { lawId })}
              >
                <View style={s.icon}>
                  <Ionicons name="refresh-circle-outline" size={24} color={c.primary} />
                </View>
                <View style={{ flex: 1 }}>
                  {title ? <Text style={s.cardTitle}>{title}</Text> : null}
                  {message ? <Text style={s.cardText}>{message}</Text> : null}
                  {date ? <Text style={s.date}>{formatDate(date)}</Text> : null}
                </View>
                {lawId ? <Ionicons name="chevron-forward" size={20} color={c.sub} /> : null}
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
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    icon: {
      width: 44, height: 44, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    cardTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    cardText: { fontSize: 14, color: c.text, marginTop: 4, lineHeight: 20 },
    date: { fontSize: 12, color: c.sub, marginTop: 6 },
  });