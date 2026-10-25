import React, { useState } from 'react';
import {
  ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, Share, StyleSheet, Text, TextInput, View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { api } from '../api';
import { useApp, useT, useTheme, useTr } from '../store';
import Header from '../components/Header';
import { FadeIn, PressScale } from '../components/Motion';

// Составление документа с помощью ИИ: выбрать тип, заполнить поля, получить готовый текст
const TYPES = [
  { id: 'VACATION', icon: 'sunny-outline' },
  { id: 'RESIGNATION', icon: 'exit-outline' },
  { id: 'CLAIM', icon: 'receipt-outline' },
  { id: 'RECEIPT', icon: 'cash-outline' },
];

export default function AiDocumentScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const { user, lang } = useApp();

  const [type, setType] = useState('VACATION');
  const [to, setTo] = useState('');
  const [from, setFrom] = useState('');
  const [details, setDetails] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null); // { title, text, articles }

  const create = async () => {
    if (!details.trim() || loading) {
      if (!details.trim()) setError(t('docNeedDetails'));
      return;
    }
    setError('');
    setLoading(true);
    try {
      setResult(await api.aiDocument(user.token, { type, to, from, details, lang }));
    } catch (e) {
      setError(e.name === 'AbortError' || /network/i.test(e.message || '') ? t('errorServer') : e.message);
    } finally {
      setLoading(false);
    }
  };

  const share = () => {
    if (result) Share.share({ title: result.title, message: result.text });
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.bg }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <Header title={t('docTitle')} subtitle={t('docSubtitle')} onBack={() => navigation.goBack()} />

      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
        {/* 1. тип документа */}
        <Text style={s.label}>{t('docType')}</Text>
        <View style={s.types}>
          {TYPES.map((item) => {
            const on = item.id === type;
            return (
              <View key={item.id} style={s.typeCell}>
                <PressScale style={[s.type, on && s.typeOn]} onPress={() => { setType(item.id); setResult(null); }}>
                  <Ionicons name={item.icon} size={22} color={on ? c.onPrimary : c.primary} />
                  <Text style={[s.typeText, on && { color: c.onPrimary }]}>{t('doc_' + item.id)}</Text>
                </PressScale>
              </View>
            );
          })}
        </View>

        {/* 2. поля */}
        <Text style={s.label}>{t('docTo')}</Text>
        <TextInput style={s.input} value={to} onChangeText={setTo} placeholder={t('docToPh_' + type)} placeholderTextColor={c.sub} />

        <Text style={s.label}>{t('docFrom')}</Text>
        <TextInput style={s.input} value={from} onChangeText={setFrom} placeholder={t('docFromPh')} placeholderTextColor={c.sub} />

        <Text style={s.label}>{t('docDetails')}</Text>
        <TextInput
          style={[s.input, { minHeight: 110, textAlignVertical: 'top' }]}
          value={details}
          onChangeText={setDetails}
          placeholder={t('docDetailsPh_' + type)}
          placeholderTextColor={c.sub}
          multiline
          maxLength={1500}
        />
        <Text style={s.privacy}>{t('docPrivacy')}</Text>

        {error ? <Text style={s.error}>{error}</Text> : null}

        <Pressable style={[s.button, loading && { opacity: 0.6 }]} onPress={create} disabled={loading}>
          {loading ? <ActivityIndicator color={c.onPrimary} /> : <Ionicons name="sparkles" size={20} color={c.onPrimary} />}
          <Text style={s.buttonText}>{loading ? t('docCreating') : t('docCreate')}</Text>
        </Pressable>

        {/* 3. результат */}
        {result ? (
          <FadeIn>
            <View style={s.result}>
              <View style={s.resultHead}>
                <Text style={s.resultTitle}>{result.title}</Text>
                <PressScale style={s.shareBtn} onPress={share}>
                  <Ionicons name="share-social-outline" size={18} color={c.primary} />
                  <Text style={s.shareText}>{t('docShare')}</Text>
                </PressScale>
              </View>
              <Text style={s.docText} selectable>{result.text}</Text>

              {result.articles && result.articles.length ? (
                <View style={{ marginTop: 12 }}>
                  <Text style={s.label}>{t('aiSources')}</Text>
                  {result.articles.map((a) => (
                    <PressScale key={a.id} style={s.article} onPress={() => navigation.navigate('Article', { id: a.id })}>
                      <Ionicons name="document-text-outline" size={18} color={c.primary} />
                      <Text style={s.articleTitle} numberOfLines={2}>{tr(a, 'title')}</Text>
                      <Ionicons name="chevron-forward" size={18} color={c.sub} />
                    </PressScale>
                  ))}
                </View>
              ) : null}

              <Text style={s.note}>{t('docNote')}</Text>
            </View>
          </FadeIn>
        ) : null}
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    label: { fontSize: 13, fontWeight: '800', color: c.sub, marginBottom: 8, marginTop: 6, textTransform: 'uppercase' },
    types: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', marginBottom: 8 },
    // ширину задаёт обёртка: PressScale внутри себя не растягивается на процент
    typeCell: { width: '48.5%', marginBottom: 8 },
    type: {
      width: '100%', minHeight: 56, flexDirection: 'row', alignItems: 'center', gap: 8, padding: 12, borderRadius: 16,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    typeOn: { backgroundColor: c.primary, borderColor: c.primary },
    typeText: { flex: 1, fontSize: 13, fontWeight: '700', color: c.text },
    input: {
      minHeight: 48, borderRadius: 14, paddingHorizontal: 14, paddingVertical: 12, marginBottom: 10,
      backgroundColor: c.surface, color: c.text, fontSize: 15, borderWidth: 1, borderColor: c.border,
    },
    privacy: { fontSize: 12, color: c.sub, marginBottom: 12 },
    error: { color: c.danger, marginBottom: 10, fontWeight: '600' },
    button: {
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, height: 52,
      borderRadius: 16, backgroundColor: c.primary,
    },
    buttonText: { color: c.onPrimary, fontSize: 16, fontWeight: '800' },
    result: {
      marginTop: 18, padding: 16, borderRadius: 18, backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    resultHead: { flexDirection: 'row', alignItems: 'center', marginBottom: 12, gap: 8 },
    resultTitle: { flex: 1, fontSize: 17, fontWeight: '800', color: c.text },
    shareBtn: {
      flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 12, height: 36,
      borderRadius: 12, backgroundColor: c.primarySoft,
    },
    shareText: { color: c.primary, fontWeight: '700', fontSize: 13 },
    docText: { color: c.text, fontSize: 15, lineHeight: 23 },
    article: {
      flexDirection: 'row', alignItems: 'center', gap: 8, padding: 10, borderRadius: 12, marginBottom: 6,
      backgroundColor: c.surface,
    },
    articleTitle: { flex: 1, fontSize: 14, fontWeight: '700', color: c.text },
    note: { fontSize: 12, color: c.sub, marginTop: 12, lineHeight: 17 },
  });