import React, { useCallback, useEffect, useState } from 'react';
import { Linking, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';

export default function ArticleScreen({ navigation, route }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { id } = route.params;

  const [article, setArticle] = useState(null);
  const [status, setStatus] = useState('loading');

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      setArticle(await api.article(id));
      setStatus('ok');
    } catch (e) {
      setStatus('error');
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const law = article ? article.law : null;
  const paragraphs = article ? tr(article, 'text').split('\n').filter(Boolean) : [];
  const explanation = article ? tr(article, 'explanation') : '';

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header
        title={article ? tr(article, 'title') : ''}
        subtitle={law ? tr(law, 'title') : ''}
        onBack={() => navigation.goBack()}
      />

      <StatusView status={status} onRetry={load} />

      {status === 'ok' && article ? (
        <ScrollView contentContainerStyle={{ padding: 16, paddingTop: 4, paddingBottom: 32 }}>
          {/* официальный текст */}
          <View style={s.block}>
            <View style={s.blockHead}>
              <Ionicons name="document-text-outline" size={20} color={c.primary} />
              <Text style={s.blockTitle}>{t('officialText')}</Text>
            </View>
            {paragraphs.map((line, index) => (
              <Text key={index} style={s.paragraph}>{line}</Text>
            ))}
          </View>

          {/* объяснение простыми словами */}
          {explanation ? (
            <View style={[s.block, s.plain]}>
              <View style={s.blockHead}>
                <Ionicons name="chatbubble-ellipses-outline" size={20} color={c.primary} />
                <Text style={s.blockTitle}>{t('plainWords')}</Text>
              </View>
              <Text style={s.paragraph}>{explanation}</Text>
              <View style={s.warning}>
                <Ionicons name="alert-circle-outline" size={16} color={c.sub} />
                <Text style={s.warningText}>{t('notAdvice')}</Text>
              </View>
            </View>
          ) : null}

          {/* источник и дата обновления */}
          {law ? (
            <View style={s.meta}>
              <View style={s.metaRow}>
                <Ionicons name="calendar-outline" size={18} color={c.sub} />
                <Text style={s.metaText}>{t('lastUpdated')}: {formatDate(law.lastUpdated)}</Text>
              </View>
              <Pressable style={s.metaRow} onPress={() => Linking.openURL(law.sourceUrl)}>
                <Ionicons name="open-outline" size={18} color={c.primary} />
                <Text style={[s.metaText, s.link]}>{t('openSource')}: adilet.zan.kz</Text>
              </Pressable>
            </View>
          ) : null}
        </ScrollView>
      ) : null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    block: {
      padding: 16, borderRadius: 18, marginBottom: 12,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    plain: { backgroundColor: c.primarySoft, borderColor: c.primarySoft },
    blockHead: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 10 },
    blockTitle: { fontSize: 15, fontWeight: '800', color: c.primary },
    paragraph: { fontSize: 16, lineHeight: 24, color: c.text, marginBottom: 8 },
    warning: { flexDirection: 'row', gap: 6, marginTop: 4 },
    warningText: { flex: 1, fontSize: 12, color: c.sub, lineHeight: 17 },
    meta: { gap: 10, paddingHorizontal: 4, marginTop: 4 },
    metaRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
    metaText: { color: c.sub, flex: 1 },
    link: { color: c.primary, fontWeight: '700' },
  });