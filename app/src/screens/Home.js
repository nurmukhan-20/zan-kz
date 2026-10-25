import React, { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { api } from '../api';
import { useT, useTheme, useTr } from '../store';
import { FadeIn, PressScale } from '../components/Motion';

// Главный экран: поиск, «Мои права», категории
export default function HomeScreen({ navigation }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const tr = useTr();
  const insets = useSafeAreaInsets();

  const [categories, setCategories] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | ok | error

  const load = useCallback(async () => {
    setStatus('loading');
    try {
      setCategories(await api.categories());
      setStatus('ok');
    } catch (e) {
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: c.bg }}
      contentContainerStyle={{ padding: 16, paddingTop: insets.top + 12, paddingBottom: 32 }}
    >
      {/* шапка: логотип, название, колокольчик */}
      <View style={s.header}>
        <View style={s.logo}>
          <Ionicons name="library-outline" size={24} color={c.onPrimary} />
        </View>
        <View style={{ flex: 1, marginLeft: 12 }}>
          <Text style={s.appName}>{t('appName')}</Text>
          <Text style={s.subtitle}>{t('appSubtitle')}</Text>
        </View>
        <Pressable style={s.iconButton} onPress={() => navigation.navigate('Notifications')}>
          <Ionicons name="notifications-outline" size={22} color={c.text} />
        </Pressable>
      </View>

      {/* большая строка поиска */}
      <PressScale scaleTo={0.98} style={s.search} onPress={() => navigation.navigate('Search')}>
        <Ionicons name="search" size={22} color={c.sub} />
        <Text style={s.searchText}>{t('searchPlaceholder')}</Text>
      </PressScale>

      {/* «Мои права» */}
      <FadeIn>
      <PressScale style={s.rights} onPress={() => navigation.navigate('Rights')}>
        <View style={s.rightsIcon}>
          <Ionicons name="shield-checkmark-outline" size={26} color={c.primary} />
        </View>
        <View style={{ flex: 1, marginHorizontal: 12 }}>
          <Text style={s.rightsTitle}>{t('myRights')}</Text>
          <Text style={s.rightsHint}>{t('myRightsHint')}</Text>
        </View>
        <Ionicons name="chevron-forward" size={22} color={c.onPrimary} />
      </PressScale>
      </FadeIn>

      {/* ИИ-помощник */}
      <FadeIn index={1}>
      <PressScale style={s.ai} onPress={() => navigation.navigate('Assistant')}>
        <View style={s.aiIcon}>
          <Ionicons name="sparkles" size={24} color={c.primary} />
        </View>
        <View style={{ flex: 1, marginHorizontal: 12 }}>
          <Text style={s.aiTitle}>{t('aiTitle')}</Text>
          <Text style={s.aiHint}>{t('aiHint')}</Text>
        </View>
        <Ionicons name="chevron-forward" size={22} color={c.sub} />
      </PressScale>
      </FadeIn>

      {/* категории */}
      <Text style={s.section}>{t('categories')}</Text>

      {status === 'loading' ? (
        <View style={s.center}>
          <ActivityIndicator color={c.primary} />
          <Text style={[s.subtitle, { marginTop: 8 }]}>{t('loading')}</Text>
        </View>
      ) : null}

      {status === 'error' ? (
        <View style={s.errorBox}>
          <Ionicons name="cloud-offline-outline" size={28} color={c.danger} />
          <Text style={s.errorText}>{t('errorServer')}</Text>
          <Pressable style={s.retry} onPress={load}>
            <Ionicons name="refresh" size={18} color={c.onPrimary} />
            <Text style={s.retryText}>{t('retry')}</Text>
          </Pressable>
        </View>
      ) : null}

      {status === 'ok' ? (
        <View style={s.grid}>
          {categories.map((category, index) => {
            // Конституция — главный закон, поэтому её карточка на всю ширину (как две клетки)
            const wide = category.nameRu === 'Конституция';
            return (
              <FadeIn key={category.id} index={index + 1} style={{ width: wide ? '100%' : '48%', marginBottom: 12 }}>
              <PressScale
                style={[s.category, wide && s.categoryWide]}
                onPress={() => navigation.navigate('Category', { category })}
              >
                <View style={[s.categoryIcon, wide && s.categoryIconWide]}>
                  <Ionicons name={category.icon || 'book-outline'} size={wide ? 28 : 22} color={c.primary} />
                </View>
                <View style={wide ? { flex: 1 } : null}>
                  <Text style={[s.categoryName, wide && s.categoryNameWide]} numberOfLines={2}>{tr(category, 'name')}</Text>
                  {wide ? <Text style={s.categoryHint}>{t('constitutionHint')}</Text> : null}
                </View>
                {wide ? <Ionicons name="chevron-forward" size={22} color={c.sub} /> : null}
              </PressScale>
              </FadeIn>
            );
          })}
        </View>
      ) : null}

      {/* предупреждение: это не юридическая консультация */}
      <View style={s.disclaimer}>
        <Ionicons name="information-circle-outline" size={18} color={c.sub} />
        <Text style={s.disclaimerText}>{t('disclaimer')}</Text>
      </View>
    </ScrollView>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    header: { flexDirection: 'row', alignItems: 'center', marginBottom: 18 },
    logo: {
      width: 46, height: 46, borderRadius: 14, backgroundColor: c.primary,
      alignItems: 'center', justifyContent: 'center',
    },
    appName: { fontSize: 22, fontWeight: '800', color: c.text },
    subtitle: { fontSize: 13, color: c.sub, marginTop: 2 },
    iconButton: {
      width: 44, height: 44, borderRadius: 22, backgroundColor: c.surface,
      alignItems: 'center', justifyContent: 'center',
    },
    search: {
      flexDirection: 'row', alignItems: 'center', height: 56, borderRadius: 18,
      paddingHorizontal: 16, backgroundColor: c.surface, borderWidth: 1, borderColor: c.border,
      marginBottom: 16,
    },
    searchText: { marginLeft: 10, fontSize: 16, color: c.sub, flex: 1 },
    rights: {
      flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 20,
      backgroundColor: c.primary, marginBottom: 22,
    },
    rightsIcon: {
      width: 48, height: 48, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    rightsTitle: { fontSize: 17, fontWeight: '800', color: c.onPrimary },
    rightsHint: { fontSize: 13, color: c.onPrimary, opacity: 0.85, marginTop: 3 },
    ai: {
      flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 20,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border, marginTop: -10, marginBottom: 22,
    },
    aiIcon: {
      width: 48, height: 48, borderRadius: 14, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center',
    },
    aiTitle: { fontSize: 17, fontWeight: '800', color: c.text },
    aiHint: { fontSize: 13, color: c.sub, marginTop: 3 },
    section: { fontSize: 19, fontWeight: '800', color: c.text, marginBottom: 12 },
    center: { alignItems: 'center', paddingVertical: 24 },
    errorBox: {
      alignItems: 'center', padding: 18, borderRadius: 18, borderWidth: 1,
      borderColor: c.border, backgroundColor: c.card,
    },
    errorText: { color: c.text, textAlign: 'center', marginVertical: 10, lineHeight: 20 },
    retry: {
      flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 18, height: 42,
      borderRadius: 14, backgroundColor: c.primary,
    },
    retryText: { color: c.onPrimary, fontWeight: '700' },
    grid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between' },
    category: {
      width: '100%', minHeight: 104, padding: 14, borderRadius: 18,
      backgroundColor: c.card, borderWidth: 1, borderColor: c.border,
    },
    categoryIcon: {
      width: 40, height: 40, borderRadius: 12, backgroundColor: c.primarySoft,
      alignItems: 'center', justifyContent: 'center', marginBottom: 10,
    },
    categoryName: { fontSize: 14, fontWeight: '700', color: c.text },
    categoryWide: {
      width: '100%', minHeight: 88, flexDirection: 'row', alignItems: 'center', gap: 14,
    },
    categoryIconWide: { width: 52, height: 52, borderRadius: 16, marginBottom: 0 },
    categoryNameWide: { fontSize: 18, fontWeight: '800' },
    categoryHint: { fontSize: 13, color: c.sub, marginTop: 3 },
    disclaimer: { flexDirection: 'row', gap: 8, marginTop: 12, paddingHorizontal: 2 },
    disclaimerText: { flex: 1, fontSize: 12, color: c.sub, lineHeight: 17 },
  });