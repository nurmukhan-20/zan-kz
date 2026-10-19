import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, Image, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api, mediaList } from '../api';
import { newsImages, newsVideo } from '../newsPhotos';
import { formatDate } from '../i18n';
import { useApp, useTheme, useTr } from '../store';
import StatusView from '../components/StatusView';
import { FadeIn, PressScale, SlideTabs } from '../components/Motion';

const TEXT = {
  kk: {
    title: 'Жаңалықтар', latest: 'Соңғы', popular: 'Танымал',
    popularNews: 'Танымал жаңалықтар', popularArticles: 'Ең көп оқылатын баптар',
    empty: 'Әзірге жаңалық жоқ', noViews: 'Әзірге қаралымдар жоқ', views: 'қаралым', article: 'бап',
  },
  ru: {
    title: 'Новости', latest: 'Последние', popular: 'Популярные',
    popularNews: 'Популярные новости', popularArticles: 'Самые читаемые статьи',
    empty: 'Пока нет новостей', noViews: 'Пока нет просмотров', views: 'просм.', article: 'Статья',
  },
  en: {
    title: 'News', latest: 'Latest', popular: 'Popular',
    popularNews: 'Popular news', popularArticles: 'Most-read articles',
    empty: 'No news yet', noViews: 'No views yet', views: 'views', article: 'Article',
  },
};

// Лента новостей: «Последние» (по дате) и «Популярные» (по просмотрам + самые читаемые статьи)
export default function NewsScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const tr = useTr();
  const insets = useSafeAreaInsets();
  const { lang } = useApp();
  const L = TEXT[lang] || TEXT.ru;

  const [tab, setTab] = useState('latest'); // latest | popular
  const [news, setNews] = useState([]);
  const [articles, setArticles] = useState([]);
  const [status, setStatus] = useState('loading');
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async (quiet) => {
    if (!quiet) setStatus('loading');
    try {
      const list = await api.news(tab);
      setNews(list);
      if (tab === 'popular') setArticles(await api.popularArticles());
      setStatus(list.length ? 'ok' : 'empty');
    } catch (e) {
      setStatus('error');
    }
  }, [tab]);

  useEffect(() => {
    load();
  }, [load]);

  // при возврате на вкладку обновляем: просмотры могли измениться
  useEffect(() => navigation.addListener('focus', () => load(true)), [navigation, load]);

  const refresh = async () => {
    setRefreshing(true);
    await load(true);
    setRefreshing(false);
  };

  const articleTitle = (a) => {
    const number = lang === 'kk' ? `${a.articleNumber}-${L.article}` : `${L.article} ${a.articleNumber}`;
    const title = tr(a, 'title');
    return title.startsWith(number) ? title : `${number}. ${title}`;
  };

  const renderNews = ({ item, index }) => {
    const cover = newsImages(item)[0] || mediaList(item.images)[0];
    const hasVideo = !!(newsVideo(item) || item.videoUrl);
    return (
    <FadeIn index={index}>
    <PressScale style={s.card} onPress={() => navigation.navigate('NewsDetail', { id: item.id })}>
      {tab === 'popular' ? (
        <View style={s.rank}><Text style={s.rankText}>{index + 1}</Text></View>
      ) : null}
      {cover ? (
        <View>
          <Image source={typeof cover === 'number' ? cover : { uri: cover }} style={s.thumb} />
          {hasVideo ? (
            <View style={s.play}><Ionicons name="play" size={14} color="#fff" /></View>
          ) : null}
        </View>
      ) : tab === 'latest' ? (
        <View style={s.icon}><Ionicons name="newspaper-outline" size={22} color={c.primary} /></View>
      ) : null}
      <View style={{ flex: 1 }}>
        <Text style={s.cardTitle} numberOfLines={3}>{tr(item, 'title')}</Text>
        <Text style={s.cardText} numberOfLines={2}>{tr(item, 'text')}</Text>
        <View style={s.meta}>
          <Ionicons name="calendar-outline" size={13} color={c.sub} />
          <Text style={s.metaText}>{formatDate(item.publishedAt)}{item.publishedTime ? `, ${item.publishedTime}` : ''}</Text>
          <Ionicons name="eye-outline" size={14} color={c.sub} style={{ marginLeft: 10 }} />
          <Text style={s.metaText}>{item.views || 0} {L.views}</Text>
        </View>
      </View>
    </PressScale>
    </FadeIn>
    );
  };

  // во вкладке «Популярные» под новостями — самые читаемые статьи
  const footer = tab === 'popular' && status === 'ok' ? (
    <View>
      <Text style={s.section}>{L.popularArticles}</Text>
      {articles.length ? articles.map((a, i) => (
        <FadeIn key={a.id} index={news.length + i}>
        <PressScale style={s.card} onPress={() => navigation.navigate('Article', { id: a.id })}>
          <View style={s.rank}><Text style={s.rankText}>{i + 1}</Text></View>
          <View style={{ flex: 1 }}>
            <Text style={s.cardTitle} numberOfLines={2}>{articleTitle(a)}</Text>
            {a.law ? <Text style={s.metaText} numberOfLines={1}>{tr(a.law, 'title')}</Text> : null}
          </View>
          <Ionicons name="chevron-forward" size={20} color={c.sub} />
        </PressScale>
        </FadeIn>
      )) : <Text style={s.noViews}>{L.noViews}</Text>}
    </View>
  ) : null;

  return (
    <View style={{ flex: 1, backgroundColor: c.bg, paddingTop: insets.top + 12 }}>
      <Text style={s.title}>{L.title}</Text>

      {/* два направления: Последние | Популярные */}
      <SlideTabs
        c={c}
        style={{ marginHorizontal: 16, marginBottom: 12 }}
        value={tab}
        onChange={setTab}
        items={[
          { id: 'latest', title: L.latest, icon: 'time-outline' },
          { id: 'popular', title: L.popular, icon: 'flame-outline' },
        ]}
      />

      <StatusView status={status} onRetry={() => load()} emptyText={L.empty} emptyIcon="newspaper-outline" />

      {status === 'ok' ? (
        <FlatList
          key={tab}
          data={news}
          keyExtractor={(item) => String(item.id)}
          renderItem={renderNews}
          ListHeaderComponent={tab === 'popular' ? <Text style={[s.section, { marginTop: 0 }]}>{L.popularNews}</Text> : null}
          ListFooterComponent={footer}
          contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} tintColor={c.primary} />}
        />
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    title: { fontSize: 26, fontWeight: '800', color: c.text, paddingHorizontal: 16, marginBottom: 12 },
    tabs: {
      flexDirection: 'row', padding: 4, marginHorizontal: 16, marginBottom: 12, borderRadius: 16,
      backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
    },
    tab: {
      flex: 1, height: 42, borderRadius: 12, flexDirection: 'row', gap: 6,
      alignItems: 'center', justifyContent: 'center',
    },
    tabActive: { backgroundColor: c.primary },
    tabText: { color: c.sub, fontWeight: '800', fontSize: 15 },
    tabTextActive: { color: c.onPrimary },
    section: { fontSize: 17, fontWeight: '800', color: c.text, marginTop: 14, marginBottom: 10 },
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    icon: {
      width: 44, height: 44, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center', alignSelf: 'flex-start',
    },
    thumb: { width: 84, height: 84, borderRadius: 14, alignSelf: 'flex-start', backgroundColor: c.surface },
    play: {
      position: 'absolute', left: 30, top: 30, width: 24, height: 24, borderRadius: 12,
      backgroundColor: 'rgba(0,0,0,0.6)', alignItems: 'center', justifyContent: 'center',
    },
    rank: {
      width: 36, height: 36, borderRadius: 18, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
    rankText: { color: c.onPrimary, fontWeight: '800', fontSize: 15 },
    cardTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    cardText: { fontSize: 13, color: c.sub, marginTop: 4, lineHeight: 18 },
    meta: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: 4, marginTop: 8 },
    metaText: { fontSize: 12, color: c.sub, marginTop: 2 },
    noViews: { color: c.sub, textAlign: 'center', paddingVertical: 12 },
  });