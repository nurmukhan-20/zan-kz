import React, { useCallback, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api } from '../api';
import { useApp, useTheme, useTr } from '../store';
import StatusView from '../components/StatusView';
import { FadeIn, PressScale } from '../components/Motion';

const TEXT = {
  kk: {
    title: 'Таңдаулылар', empty: 'Әзірге таңдаулы бап жоқ. Бапты ашып, жұлдызшаны басыңыз.',
    guest: 'Таңдаулы баптарды сақтау үшін аккаунтқа кіріңіз.', login: 'Кіру', article: 'бап',
  },
  ru: {
    title: 'Избранное', empty: 'Пока нет избранных статей. Откройте статью и нажмите на звёздочку.',
    guest: 'Войдите в аккаунт, чтобы сохранять избранные статьи.', login: 'Войти', article: 'статья',
  },
  en: {
    title: 'Favorites', empty: 'No favorite articles yet. Open an article and tap the star.',
    guest: 'Sign in to save your favorite articles.', login: 'Sign in', article: 'article',
  },
};

export default function FavoritesScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const { lang, user } = useApp();
  const L = TEXT[lang] || TEXT.ru;
  const insets = useSafeAreaInsets();

  const [articles, setArticles] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    if (!user) return;
    setStatus('loading');
    try {
      const data = await api.favorites(user.token);
      const list = data.map((item) => item.article || item);
      setArticles(list);
      setStatus(list.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, [user]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load]),
  );

  const remove = async (articleId) => {
    setArticles((list) => {
      const next = list.filter((a) => a.id !== articleId);
      if (!next.length) setStatus('empty');
      return next;
    });
    try {
      await api.removeFavorite(user.token, articleId);
    } catch (e) {
      load();
    }
  };

  return (
    <View style={{ flex: 1, backgroundColor: c.bg, paddingTop: insets.top + 12 }}>
      <Text style={s.title}>{L.title}</Text>

      {!user ? (
        <View style={s.guest}>
          <Ionicons name="star-outline" size={44} color={c.primary} />
          <Text style={s.guestText}>{L.guest}</Text>
          <Pressable style={s.button} onPress={() => navigation.navigate('Login')}>
            <Text style={s.buttonText}>{L.login}</Text>
          </Pressable>
        </View>
      ) : (
        <>
          <StatusView status={status} onRetry={load} emptyText={L.empty} emptyIcon="star-outline" />
          {status === 'ok' ? (
            <FlatList
              data={articles}
              keyExtractor={(item) => String(item.id)}
              contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}
              renderItem={({ item, index }) => (
                <FadeIn index={index}>
                <PressScale style={s.card} onPress={() => navigation.navigate('Article', { id: item.id })}>
                  <View style={s.badge}>
                    <Text style={s.badgeText}>{item.articleNumber}</Text>
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={s.cardTitle}>{tr(item, 'title') || `${item.articleNumber} ${L.article}`}</Text>
                    {item.law ? <Text style={s.cardLaw} numberOfLines={1}>{tr(item.law, 'title')}</Text> : null}
                  </View>
                  <Pressable onPress={() => remove(item.id)} hitSlop={10}>
                    <Ionicons name="star" size={24} color={c.primary} />
                  </Pressable>
                </PressScale>
                </FadeIn>
              )}
            />
          ) : null}
        </>
      )}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    title: { fontSize: 26, fontWeight: '800', color: c.text, paddingHorizontal: 16, marginBottom: 12 },
    guest: { alignItems: 'center', paddingHorizontal: 32, paddingTop: 60 },
    guestText: { color: c.sub, textAlign: 'center', lineHeight: 21, marginVertical: 14, fontSize: 15 },
    button: {
      height: 48, paddingHorizontal: 32, borderRadius: 16, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
    buttonText: { color: c.onPrimary, fontWeight: '800', fontSize: 15 },
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    badge: {
      width: 44, height: 44, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    badgeText: { color: c.primary, fontWeight: '800', fontSize: 16 },
    cardTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    cardLaw: { fontSize: 12, color: c.sub, marginTop: 3 },
  });