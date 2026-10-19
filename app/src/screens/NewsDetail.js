import React, { useCallback, useEffect, useState } from 'react';
import { Linking, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api, mediaList, mediaUrl } from '../api';
import { Gallery, Video } from '../components/NewsMedia';
import { newsImages, newsVideo } from '../newsPhotos';
import { formatDate } from '../i18n';
import { useApp, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';
import { FadeIn, PressScale } from '../components/Motion';

const TEXT = {
  kk: { title: 'Жаңалық', openArticle: 'Бапты ашу', openLaw: 'Заңды ашу', source: 'Дереккөз', full: 'Толық мәтін', video: 'Бейнені көру', views: 'қаралым' },
  ru: { title: 'Новость', openArticle: 'Открыть статью', openLaw: 'Открыть закон', source: 'Источник', full: 'Полный текст', video: 'Смотреть видео', views: 'просмотров' },
  en: { title: 'News', openArticle: 'Open article', openLaw: 'Open law', source: 'Source', full: 'Full text', video: 'Watch video', views: 'views' },
};

// Одна новость целиком. Открытие считается просмотром (сервер прибавляет +1)
export default function NewsDetailScreen({ navigation, route }) {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const { lang } = useApp();
  const L = TEXT[lang] || TEXT.ru;
  const { id } = route.params;

  const [item, setItem] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      setItem(await api.newsItem(id));
      setStatus('ok');
    } catch (e) {
      setStatus('error');
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={L.title} onBack={() => navigation.goBack()} />
      <StatusView status={status} onRetry={load} />

      {status === 'ok' && item ? (
        <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 32 }}>
          <View style={s.meta}>
            <Ionicons name="calendar-outline" size={15} color={c.sub} />
            <Text style={s.metaText}>{formatDate(item.publishedAt)}{item.publishedTime ? `, ${item.publishedTime}` : ''}</Text>
            <Ionicons name="eye-outline" size={16} color={c.sub} style={{ marginLeft: 12 }} />
            <Text style={s.metaText}>{item.views || 0} {L.views}</Text>
          </View>

          <FadeIn><Gallery images={[...newsImages(item), ...mediaList(item.images)]} /></FadeIn>
          <FadeIn index={1}>

          <Text style={s.title}>{tr(item, 'title')}</Text>
          {item.law ? <Text style={s.law}>{tr(item.law, 'title')}</Text> : null}
          <Text style={s.text}>{tr(item, 'text')}</Text>

          </FadeIn>
          <FadeIn index={2}>
          <Video url={newsVideo(item) || mediaUrl(item.videoUrl)} label={L.video} />

          {item.article ? (
            <PressScale style={s.button} onPress={() => navigation.navigate('Article', { id: item.article.id })}>
              <Ionicons name="document-text-outline" size={20} color={c.onPrimary} />
              <Text style={s.buttonText}>{L.openArticle}</Text>
            </PressScale>
          ) : null}
          {!item.article && item.law ? (
            <PressScale style={s.button} onPress={() => navigation.navigate('Law', { lawId: item.law.id })}>
              <Ionicons name="book-outline" size={20} color={c.onPrimary} />
              <Text style={s.buttonText}>{L.openLaw}</Text>
            </PressScale>
          ) : null}

          {item.sourceUrl ? (
            <Pressable style={s.link} onPress={() => Linking.openURL(item.sourceUrl)}>
              <Ionicons name="open-outline" size={18} color={c.primary} />
              <Text style={s.linkText}>
                {item.sourceName ? `${L.full}: ${item.sourceName}` : `${L.source}: ${item.sourceUrl.replace(/^https?:\/\/(www\.)?/, '').split('/')[0]}`}
              </Text>
            </Pressable>
          ) : null}
          </FadeIn>
        </ScrollView>
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    meta: { flexDirection: 'row', alignItems: 'center', gap: 5, marginBottom: 10 },
    metaText: { fontSize: 13, color: c.sub },
    title: { fontSize: 22, fontWeight: '800', color: c.text, lineHeight: 29 },
    law: { fontSize: 14, color: c.primary, fontWeight: '700', marginTop: 8 },
    text: { fontSize: 16, color: c.text, lineHeight: 25, marginTop: 14 },
    button: {
      flexDirection: 'row', gap: 8, height: 52, borderRadius: 16, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center', marginTop: 22,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    link: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 16, justifyContent: 'center' },
    linkText: { color: c.primary, fontWeight: '700' },
  });