import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Linking, Pressable, SectionList, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { formatDate } from '../i18n';
import { useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import StatusView from '../components/StatusView';
import { FadeIn, PressScale } from '../components/Motion';

// 1 -> I, 2 -> II, 4 -> IV: разделы всех законов показываем римскими цифрами
function toRoman(n) {
  const map = [[10, 'X'], [9, 'IX'], [5, 'V'], [4, 'IV'], [1, 'I']];
  let result = '';
  for (const [value, letter] of map) {
    while (n >= value) {
      result += letter;
      n -= value;
    }
  }
  return result;
}

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

  const docId = law && law.sourceUrl ? law.sourceUrl.split('/').pop() : '';

  // ключ группы: «2» (только раздел), «2/25» (раздел 2, глава 25), «1/2/1» (раздел 1, глава 2, параграф 1);
  // раздел может содержать подраздел: «3:2/22» = раздел 3, подраздел 2, глава 22 (Гражданский кодекс)
  const splitKey = (key) => {
    const [sectionPart, chapter, paragraph] = String(key || '').split('/');
    const [section, subsection] = String(sectionPart || '').split(':');
    return {
      section: section || '', subsection: subsection || '', chapter: chapter || '', paragraph: paragraph || '',
    };
  };

  // «Раздел II · Общие положения»
  const sectionTitle = (key) => {
    if (!key) return t('articles');
    const label = `${t('section')} ${/^\d+$/.test(key) ? toRoman(Number(key)) : key}`;
    const lawKey = `section_${docId}_${key}`;
    const commonKey = `section_${key}`;
    let name = t(lawKey);
    if (name === lawKey) name = t(commonKey);
    if (name === commonKey) return label;
    return `${label} · ${name}`;
  };

  // «Глава 25 · Административные правонарушения…»
  const chapterTitle = (chapter) => {
    const label = t('chapterNum').replace('{n}', chapter);
    const key = `chapter_${docId}_${chapter}`;
    const name = t(key);
    return name === key ? label : `${label} · ${name}`;
  };

  // «Подраздел 2 · Общие положения о договоре» — номера подразделов начинаются заново в каждом разделе
  const subsectionTitle = (section, subsection) => {
    const label = t('subsectionNum').replace('{n}', subsection);
    const key = `subsection_${docId}_${section}_${subsection}`;
    const name = t(key);
    return name === key ? label : `${label} · ${name}`;
  };

  // «§ 1 · Основные понятия…» — номера параграфов начинаются заново в каждой главе
  const paragraphTitle = (chapter, paragraph) => {
    const label = t('paragraphNum').replace('{n}', paragraph);
    const key = `paragraph_${docId}_${chapter}_${paragraph}`;
    const name = t(key);
    return name === key ? label : `${label} · ${name}`;
  };

  // заголовок группы: раздел, подраздел и глава — только когда сменились; параграф — всегда, если есть
  const renderHeader = (group) => {
    const { section, subsection, chapter, paragraph } = splitKey(group.key);
    const index = sections.indexOf(group);
    const prevGroup = index > 0 ? splitKey(sections[index - 1].key) : null;
    const newSection = !prevGroup || prevGroup.section !== section;
    const newSubsection = newSection || prevGroup.subsection !== subsection;
    const newChapter = newSubsection || prevGroup.chapter !== chapter;
    // статьи без главы после статей с главой — отдельная подпись, чтобы они не «прилипали» к прошлой главе
    const orphan = !chapter && !newSection && prevGroup.chapter;
    // то же для параграфов: статьи главы без параграфа после параграфа
    const orphanParagraph = chapter && !paragraph && !newChapter && prevGroup.paragraph;
    return (
      <View>
        {newSection ? <Text style={s.section}>{sectionTitle(section)}</Text> : null}
        {subsection && newSubsection ? <Text style={s.subsection}>{subsectionTitle(section, subsection)}</Text> : null}
        {chapter && newChapter ? <Text style={s.chapter}>{chapterTitle(chapter)}</Text> : null}
        {paragraph ? <Text style={s.paragraph}>{paragraphTitle(chapter, paragraph)}</Text> : null}
        {orphan ? <Text style={s.chapter}>{t('otherArticles')}</Text> : null}
        {orphanParagraph ? <Text style={s.paragraph}>{t('otherChapterArticles')}</Text> : null}
      </View>
    );
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
          renderSectionHeader={({ section }) => renderHeader(section)}
          renderItem={({ item, index }) => (
            <FadeIn index={index}>
            <PressScale style={s.card} scaleTo={0.97} onPress={() => navigation.navigate('Article', { id: item.id })}>
              <View style={s.badge}>
                <Text style={s.badgeText} numberOfLines={1} adjustsFontSizeToFit>{item.articleNumber}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={s.cardTitle}>{tr(item, 'title')}</Text>
                <Text style={s.cardText} numberOfLines={2}>{tr(item, 'text')}</Text>
              </View>
              <Ionicons name="chevron-forward" size={20} color={c.sub} />
            </PressScale>
            </FadeIn>
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
    subsection: { fontSize: 15, fontWeight: '800', color: c.text, opacity: 0.85, marginTop: 2, marginBottom: 10 },
    chapter: {
      fontSize: 14, fontWeight: '700', color: c.primary, marginTop: 4, marginBottom: 10,
      paddingLeft: 10, borderLeftWidth: 3, borderLeftColor: c.primary,
    },
    paragraph: {
      fontSize: 13, fontWeight: '700', color: c.sub, marginTop: 2, marginBottom: 8, marginLeft: 14,
      paddingLeft: 10, borderLeftWidth: 2, borderLeftColor: c.border,
    },
    card: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 18,
      marginBottom: 10, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    badge: {
      minWidth: 44, height: 44, paddingHorizontal: 6, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    badgeText: { color: c.primary, fontWeight: '800', fontSize: 16 },
    cardTitle: { fontSize: 15, fontWeight: '800', color: c.text },
    cardText: { fontSize: 13, color: c.sub, marginTop: 4, lineHeight: 18 },
  });