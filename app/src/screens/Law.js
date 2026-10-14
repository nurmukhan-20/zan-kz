import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Linking, Pressable, SectionList, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

export default function LawScreen({ navigation, route }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { lawId } = route.params;

  const [law, setLaw] = useState(null);
  const [articles, setArticles] = useState([]);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      const [lawData, articleData] = await Promise.all([api.law(lawId), api.articles(lawId)]);
      setLaw(lawData);
      setArticles(articleData);
      setStatus('ok');
    } catch (e) {
      setStatus('error');
    }
  }, [lawId]);

  useEffect(() => {
    load();
  }, [load]);

  const sections = useMemo(() => {
    const groups = [];
    for (const article of articles) {
      const key = article.section || '';
      const last = groups[groups.length - 1];
      if (last && last.key === key) last.data.push(article);
      else groups.push({ key, data: [article] });
    }
    return groups;
  }, [articles]);

  const sectionTitle = (key) => {
    if (!key) return t('articles');
    const name = t(`section_${key}`);
    return `${t('section')} ${key}${name !== `section_${key}` ? ` · ${name}` : ''}`;
  };

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={law ? tr(law, 'title') : ''} onBack={() => navigation.goBack()} />

      <StatusView status={status} onRetry={load} />

      {status === 'ok' && law ? (
        <SectionList
          sections={sections}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={{ padding: 16, paddingTop: 4 }}
          stickySectionHeadersEnabled={false}
          ListHeaderComponent={
            <View style={s.info}>
              <View style={s.infoRow}>
                <Ionicons name="calendar-outline" size={18} color={c.primary} />
                <Text style={s.infoText}>{t('lastUpdated')}: {formatDate(law.lastUpdated)}</Text>
              </View>
              <Pressable style={s.infoRow} onPress={() => Linking.openURL(law.sourceUrl)}>
                <Ionicons name="open-outline" size={18} color={c.primary} />
                <Text style={[s.infoText, s.link]} numberOfLines={1}>{t('source')}: adilet.zan.kz</Text>
              </Pressable>
            </View>
          }
          renderSectionHeader={({ section }) => <Text style={s.section}>{sectionTitle(section.key)}</Text>}
          renderItem={({ item }) => (
            <Pressable style={s.card} onPress={() => navigation.navigate('Article', { id: item.id })}>
              <View style={s.badge}>
                <Text style={s.badgeText}>{item.articleNumber}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={s.cardTitle}>{tr(item, 'title')}</Text>
                <Text style={s.cardText} numberOfLines={2}>{tr(item, 'text')}</Text>
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
    info: {
      padding: 14, borderRadius: 18, backgroundColor: c.primarySoft, gap: 10, marginBottom: 6,
    },
    infoRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
    infoText: { color: c.text, flex: 1 },
    link: { color: c.primary, fontWeight: '700' },
    section: { fontSize: 16, fontWeight: '800', color: c.text, marginTop: 16, marginBottom: 10 },
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
    cardText: { fontSize: 13, color: c.sub, marginTop: 4, lineHeight: 18 },
  });