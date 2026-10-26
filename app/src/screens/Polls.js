import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Animated, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// Голосование в приложении, как бюллетень:
// вид и дата → название → «о чём это» → вопрос → варианты → «Проголосовать» → результат опроса.
// Один пользователь — один голос.
export default function PollsScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const { user } = useApp();

  const [polls, setPolls] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | ok | error
  const [errorText, setErrorText] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      setPolls(await api.polls(user.token));
      setStatus('ok');
    } catch (e) {
      // нет связи — общий текст; сервер ответил ошибкой — показываем его сообщение
      setErrorText(e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message);
      setStatus('error');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.token]);

  useEffect(() => { load(); }, [load]);

  const onRefresh = async () => {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  };

  // после голоса сервер возвращает обновлённое голосование — заменяем его в списке
  const replace = (updated) => setPolls((list) => list.map((p) => (p.id === updated.id ? updated : p)));

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={t('pollsTitle')} subtitle={t('pollsSubtitle')} onBack={() => navigation.goBack()} />
      <ScrollView
        contentContainerStyle={{ padding: 16, paddingBottom: 40 }}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={c.primary} />}
      >
        {status === 'loading' ? <ActivityIndicator color={c.primary} style={{ marginTop: 30 }} /> : null}
        {status === 'error' ? (
          <View style={s.center}>
            <Ionicons name="cloud-offline-outline" size={30} color={c.danger} />
            <Text style={s.muted}>{errorText || t('errorServer')}</Text>
            <Pressable style={s.retry} onPress={() => { setStatus('loading'); load(); }}>
              <Text style={s.retryText}>{t('retry')}</Text>
            </Pressable>
          </View>
        ) : null}
        {status === 'ok' && !polls.length ? <Text style={[s.muted, { marginTop: 30 }]}>{t('pollsEmpty')}</Text> : null}

        {polls.map((poll, i) => (
          <FadeIn key={poll.id} index={i}>
            <PollCard poll={poll} token={user.token} onUpdate={replace} c={c} s={s} t={t} />
          </FadeIn>
        ))}

        {polls.length ? <Text style={s.note}>{t('pollsNote')}</Text> : null}
      </ScrollView>
    </View>
  );
}

// «2026-08-23» → «23 августа 2026»
function formatDate(value, t) {
  if (!value) return '';
  const [y, m, d] = Array.isArray(value) ? value : String(value).split('-').map(Number);
  const months = t('months');
  return Array.isArray(months) ? t('dateFormat').replace('{d}', d).replace('{m}', months[m - 1]).replace('{y}', y) : String(value);
}

const KIND_ICON = { REFERENDUM: 'document-text-outline', ELECTION: 'people-outline', PROPOSAL: 'bulb-outline' };

function PollCard({ poll, token, onUpdate, c, s, t }) {
  const tr = useTr();
  const [selected, setSelected] = useState(null);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const voted = poll.myOptionId !== null && poll.myOptionId !== undefined;
  const showResult = voted || !poll.active;

  const vote = async () => {
    if (!selected || sending) return;
    setSending(true);
    setError('');
    try {
      onUpdate(await api.vote(token, poll.id, selected));
    } catch (e) {
      setError(/network/i.test(e.message || '') ? t('errorServer') : e.message);
    } finally {
      setSending(false);
    }
  };

  return (
    <View style={s.card}>
      {/* вид и дата */}
      <View style={s.metaRow}>
        <View style={s.kind}>
          <Ionicons name={KIND_ICON[poll.kind] || 'checkbox-outline'} size={14} color={c.primary} />
          <Text style={s.kindText}>{t('pollKind_' + poll.kind)}</Text>
        </View>
        {poll.eventDate ? (
          <View style={s.date}>
            <Ionicons name="calendar-outline" size={14} color={c.sub} />
            <Text style={s.dateText}>{formatDate(poll.eventDate, t)}</Text>
          </View>
        ) : null}
      </View>

      {/* название и о чём */}
      <Text style={s.title}>{tr(poll, 'title')}</Text>
      {tr(poll, 'description') ? <Text style={s.desc}>{tr(poll, 'description')}</Text> : null}

      {/* вопрос как в бюллетене */}
      <View style={s.questionBox}>
        <Text style={s.questionLabel}>{t('pollQuestion')}</Text>
        <Text style={s.question}>{tr(poll, 'question')}</Text>
      </View>

      {!showResult ? (
        <>
          {poll.options.map((o) => {
            const on = selected === o.id;
            return (
              <PressScale key={o.id} scaleTo={0.98} style={[s.option, on && s.optionOn]} onPress={() => setSelected(o.id)}>
                <View style={[s.radio, on && s.radioOn]}>{on ? <View style={s.radioDot} /> : null}</View>
                <Text style={[s.optionText, on && { fontWeight: '800' }]}>{tr(o, 'text')}</Text>
              </PressScale>
            );
          })}
          <Pressable style={[s.voteBtn, (!selected || sending) && { opacity: 0.45 }]} onPress={vote} disabled={!selected || sending}>
            {sending ? <ActivityIndicator color={c.onPrimary} /> : <Ionicons name="checkmark-done" size={20} color={c.onPrimary} />}
            <Text style={s.voteText}>{t('pollVote')}</Text>
          </Pressable>
        </>
      ) : (
        <View style={{ marginTop: 6 }}>
          <Text style={s.resultHead}>{t('pollResult').replace('{n}', poll.total)}</Text>
          {poll.options.map((o) => (
            <Bar
              key={o.id}
              label={tr(o, 'text')}
              pct={poll.total ? Math.round((o.votes * 100) / poll.total) : 0}
              count={o.votes}
              mine={poll.myOptionId === o.id}
              c={c}
              s={s}
            />
          ))}
          {voted ? (
            <View style={s.myVoteRow}>
              <Ionicons name="checkmark-circle" size={16} color={c.primary} />
              <Text style={s.myVote}>{t('pollVoted')}</Text>
            </View>
          ) : null}
        </View>
      )}
      {error ? <Text style={s.error}>{error}</Text> : null}
    </View>
  );
}

// полоска результата, плавно заполняется
function Bar({ label, pct, count, mine, c, s }) {
  const width = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.timing(width, { toValue: pct, duration: 700, useNativeDriver: false }).start();
  }, [pct, width]);
  return (
    <View style={{ marginBottom: 10 }}>
      <View style={s.barLabelRow}>
        <Text style={[s.barLabel, mine && { color: c.primary, fontWeight: '800' }]} numberOfLines={2}>
          {label}{mine ? '  ✓' : ''}
        </Text>
        <Text style={s.barCount}>{pct}% · {count}</Text>
      </View>
      <View style={s.barTrack}>
        <Animated.View
          style={[s.barFill, { backgroundColor: mine ? c.primary : c.sub,
            width: width.interpolate({ inputRange: [0, 100], outputRange: ['0%', '100%'] }) }]}
        />
      </View>
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    center: { alignItems: 'center', gap: 10, marginTop: 30 },
    muted: { color: c.sub, textAlign: 'center', lineHeight: 20 },
    retry: { paddingHorizontal: 18, height: 40, borderRadius: 12, backgroundColor: c.primary, justifyContent: 'center' },
    retryText: { color: c.onPrimary, fontWeight: '700' },
    card: { padding: 16, borderRadius: 20, marginBottom: 16, backgroundColor: c.card, borderWidth: 1, borderColor: c.border },
    metaRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12, gap: 8 },
    kind: {
      flexDirection: 'row', alignItems: 'center', gap: 5, paddingHorizontal: 10, paddingVertical: 5,
      borderRadius: 10, backgroundColor: c.primarySoft,
    },
    kindText: { fontSize: 12, fontWeight: '800', color: c.primary, textTransform: 'uppercase' },
    date: { flexDirection: 'row', alignItems: 'center', gap: 5 },
    dateText: { fontSize: 13, fontWeight: '700', color: c.sub },
    title: { fontSize: 20, fontWeight: '900', color: c.text, lineHeight: 26 },
    desc: { fontSize: 14, color: c.sub, lineHeight: 21, marginTop: 8 },
    questionBox: { marginTop: 14, marginBottom: 12, padding: 12, borderRadius: 14, backgroundColor: c.surface },
    questionLabel: { fontSize: 11, fontWeight: '800', color: c.sub, textTransform: 'uppercase', marginBottom: 4 },
    question: { fontSize: 16, fontWeight: '800', color: c.text, lineHeight: 22 },
    option: {
      flexDirection: 'row', alignItems: 'center', gap: 12, paddingHorizontal: 14, paddingVertical: 13,
      borderRadius: 14, marginBottom: 8, borderWidth: 1.5, borderColor: c.border, backgroundColor: c.bg,
    },
    optionOn: { borderColor: c.primary, backgroundColor: c.primarySoft },
    radio: {
      width: 22, height: 22, borderRadius: 11, borderWidth: 2, borderColor: c.sub,
      alignItems: 'center', justifyContent: 'center',
    },
    radioOn: { borderColor: c.primary },
    radioDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: c.primary },
    optionText: { flex: 1, fontSize: 15, color: c.text, fontWeight: '600' },
    voteBtn: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 50,
      borderRadius: 14, backgroundColor: c.primary, marginTop: 6,
    },
    voteText: { color: c.onPrimary, fontWeight: '800', fontSize: 16 },
    resultHead: { fontSize: 12, fontWeight: '800', color: c.sub, marginBottom: 10, textTransform: 'uppercase' },
    barLabelRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: 5, gap: 8 },
    barLabel: { flex: 1, fontSize: 14, fontWeight: '600', color: c.text },
    barCount: { fontSize: 13, fontWeight: '700', color: c.sub },
    barTrack: { height: 8, borderRadius: 4, backgroundColor: c.surface, overflow: 'hidden' },
    barFill: { height: 8, borderRadius: 4 },
    myVoteRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4 },
    myVote: { fontSize: 13, color: c.primary, fontWeight: '700' },
    error: { color: c.danger, marginTop: 10, fontWeight: '600' },
    note: { fontSize: 12, color: c.sub, lineHeight: 17, marginTop: 4, textAlign: 'center' },
  });