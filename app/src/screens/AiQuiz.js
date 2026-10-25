import React, { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PopIn, PressScale } from '../components/Motion';

// Тест по статьям: ИИ составляет 5 вопросов по случайным статьям выбранной темы.
// Экран: 1) выбор темы  2) вопросы по одному  3) результат
export default function AiQuizScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { user, lang } = useApp();

  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState(null); // null = все темы
  const [stage, setStage] = useState('start');        // start | loading | question | result
  const [error, setError] = useState('');
  const [questions, setQuestions] = useState([]);
  const [index, setIndex] = useState(0);
  const [picked, setPicked] = useState(null);          // какой вариант выбрал пользователь
  const [score, setScore] = useState(0);

  useEffect(() => {
    api.categories().then(setCategories).catch(() => {});
  }, []);

  const start = async () => {
    setError('');
    setStage('loading');
    try {
      const res = await api.aiQuiz(user.token, categoryId, lang);
      setQuestions(res.questions);
      setIndex(0);
      setPicked(null);
      setScore(0);
      setStage('question');
    } catch (e) {
      setError(e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message);
      setStage('start');
    }
  };

  const choose = (i) => {
    if (picked !== null) return;            // ответ уже выбран
    setPicked(i);
    if (i === questions[index].answer) setScore((x) => x + 1);
  };

  const next = () => {
    if (index + 1 < questions.length) {
      setIndex(index + 1);
      setPicked(null);
    } else {
      setStage('result');
    }
  };

  const q = questions[index];

  return (
    <View style={{ flex: 1, backgroundColor: c.bg }}>
      <Header title={t('quizTitle')} subtitle={t('quizSubtitle')} onBack={() => navigation.goBack()} />

      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 40 }}>
        {/* 1. выбор темы */}
        {stage === 'start' ? (
          <FadeIn>
            <Text style={s.lead}>{t('quizIntro')}</Text>
            <Text style={s.label}>{t('quizTopic')}</Text>
            <View style={s.chips}>
              {[{ id: null }, ...categories].map((cat) => {
                const on = cat.id === categoryId;
                return (
                  <Pressable key={String(cat.id)} style={[s.chip, on && s.chipOn]} onPress={() => setCategoryId(cat.id)}>
                    <Text style={[s.chipText, on && { color: c.onPrimary }]}>
                      {cat.id === null ? t('quizAll') : tr(cat, 'name')}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
            {error ? <Text style={s.error}>{error}</Text> : null}
            <Pressable style={s.button} onPress={start}>
              <Ionicons name="play" size={20} color={c.onPrimary} />
              <Text style={s.buttonText}>{t('quizStart')}</Text>
            </Pressable>
          </FadeIn>
        ) : null}

        {/* ожидание */}
        {stage === 'loading' ? (
          <View style={s.center}>
            <ActivityIndicator size="large" color={c.primary} />
            <Text style={s.lead}>{t('quizLoading')}</Text>
          </View>
        ) : null}

        {/* 2. вопрос */}
        {stage === 'question' && q ? (
          <FadeIn key={index}>
            <View style={s.progressRow}>
              <Text style={s.progressText}>{t('quizProgress').replace('{n}', index + 1).replace('{total}', questions.length)}</Text>
              <Text style={s.progressText}>✓ {score}</Text>
            </View>
            <View style={s.bar}>
              <View style={[s.barFill, { width: `${((index + (picked !== null ? 1 : 0)) / questions.length) * 100}%` }]} />
            </View>

            <Text style={s.question}>{q.question}</Text>

            {q.options.map((opt, i) => {
              const done = picked !== null;
              const correct = i === q.answer;
              const wrongPick = done && i === picked && !correct;
              return (
                <PressScale
                  key={i}
                  style={[s.option, done && correct && s.optionRight, wrongPick && s.optionWrong]}
                  onPress={() => choose(i)}
                >
                  <View style={[s.letter, done && correct && { backgroundColor: '#16A34A' }, wrongPick && { backgroundColor: c.danger }]}>
                    <Text style={[s.letterText, ((done && correct) || wrongPick) && { color: '#FFFFFF' }]}>
                      {'ABCD'[i]}
                    </Text>
                  </View>
                  <Text style={s.optionText}>{opt}</Text>
                  {done && correct ? <Ionicons name="checkmark-circle" size={22} color="#16A34A" /> : null}
                  {wrongPick ? <Ionicons name="close-circle" size={22} color={c.danger} /> : null}
                </PressScale>
              );
            })}

            {picked !== null ? (
              <FadeIn>
                <View style={s.explain}>
                  <Text style={[s.verdict, { color: picked === q.answer ? '#16A34A' : c.danger }]}>
                    {picked === q.answer ? t('quizRight') : t('quizWrong')}
                  </Text>
                  <Text style={s.explainText}>{q.explanation}</Text>
                  {q.article ? (
                    <PressScale style={s.article} onPress={() => navigation.navigate('Article', { id: q.article.id })}>
                      <Ionicons name="document-text-outline" size={18} color={c.primary} />
                      <Text style={s.articleTitle} numberOfLines={2}>{tr(q.article, 'title')}</Text>
                      <Ionicons name="chevron-forward" size={18} color={c.sub} />
                    </PressScale>
                  ) : null}
                </View>
                <Pressable style={s.button} onPress={next}>
                  <Text style={s.buttonText}>{index + 1 < questions.length ? t('quizNext') : t('quizFinish')}</Text>
                  <Ionicons name="arrow-forward" size={20} color={c.onPrimary} />
                </Pressable>
              </FadeIn>
            ) : null}
          </FadeIn>
        ) : null}

        {/* 3. результат */}
        {stage === 'result' ? (
          <View style={s.center}>
            <PopIn style={s.trophy}>
              <Ionicons name={score === questions.length ? 'trophy' : 'ribbon-outline'} size={48} color={c.primary} />
            </PopIn>
            <Text style={s.resultScore}>{score} / {questions.length}</Text>
            <Text style={s.lead}>
              {score === questions.length ? t('quizPerfect') : score >= questions.length / 2 ? t('quizGood') : t('quizTryMore')}
            </Text>
            <Pressable style={[s.button, { alignSelf: 'stretch' }]} onPress={start}>
              <Ionicons name="refresh" size={20} color={c.onPrimary} />
              <Text style={s.buttonText}>{t('quizAgain')}</Text>
            </Pressable>
            <Pressable style={s.linkBtn} onPress={() => setStage('start')}>
              <Text style={s.link}>{t('quizChangeTopic')}</Text>
            </Pressable>
          </View>
        ) : null}
      </ScrollView>
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    lead: { fontSize: 15, color: c.sub, lineHeight: 22, marginBottom: 14, textAlign: 'left' },
    label: { fontSize: 13, fontWeight: '800', color: c.sub, marginBottom: 8, textTransform: 'uppercase' },
    chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 18 },
    chip: {
      paddingHorizontal: 14, paddingVertical: 10, borderRadius: 14,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    chipOn: { backgroundColor: c.primary, borderColor: c.primary },
    chipText: { fontSize: 14, fontWeight: '700', color: c.text },
    error: { color: c.danger, marginBottom: 10, fontWeight: '600' },
    button: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 52,
      borderRadius: 16, backgroundColor: c.primary, marginTop: 6,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    center: { alignItems: 'center', paddingTop: 40, gap: 14 },
    progressRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
    progressText: { fontSize: 13, fontWeight: '800', color: c.sub },
    bar: { height: 6, borderRadius: 3, backgroundColor: c.surface, overflow: 'hidden', marginBottom: 18 },
    barFill: { height: 6, borderRadius: 3, backgroundColor: c.primary },
    question: { fontSize: 19, fontWeight: '800', color: c.text, lineHeight: 26, marginBottom: 16 },
    option: {
      flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, borderRadius: 16, marginBottom: 10,
      backgroundColor: c.card, borderWidth: 1.5, borderColor: c.border,
    },
    optionRight: { borderColor: '#16A34A' },
    optionWrong: { borderColor: c.danger },
    letter: {
      width: 32, height: 32, borderRadius: 10, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    letterText: { fontWeight: '800', color: c.primary },
    optionText: { flex: 1, fontSize: 15, color: c.text, lineHeight: 21 },
    explain: { padding: 14, borderRadius: 16, backgroundColor: c.surface, marginTop: 4, marginBottom: 6 },
    verdict: { fontSize: 16, fontWeight: '900', marginBottom: 6 },
    explainText: { fontSize: 14, color: c.text, lineHeight: 21 },
    article: {
      flexDirection: 'row', alignItems: 'center', gap: 8, padding: 10, borderRadius: 12, marginTop: 10,
      backgroundColor: c.card,
    },
    articleTitle: { flex: 1, fontSize: 14, fontWeight: '700', color: c.text },
    trophy: {
      width: 96, height: 96, borderRadius: 48, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    resultScore: { fontSize: 40, fontWeight: '900', color: c.text },
    linkBtn: { padding: 10 },
    link: { color: c.primary, fontWeight: '700', fontSize: 15 },
  });