import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useT, useTheme } from '../store';

export default function PlaceholderScreen({ navigation, route }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();
  const insets = useSafeAreaInsets();
  const { titleKey = 'appName', hideBack = false } = route.params || {};

  return (
    <View style={{ flex: 1, backgroundColor: c.bg, paddingTop: insets.top + 12, paddingHorizontal: 16 }}>
      <View style={s.header}>
        {hideBack ? null : (
          <Pressable style={s.back} onPress={() => navigation.goBack()}>
            <Ionicons name="chevron-back" size={24} color={c.text} />
          </Pressable>
        )}
        <Text style={s.title}>{t(titleKey)}</Text>
      </View>
      <View style={s.center}>
        <Ionicons name="construct-outline" size={48} color={c.primary} />
        <Text style={s.text}>{t('comingSoon')}</Text>
      </View>
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    header: { flexDirection: 'row', alignItems: 'center', gap: 10 },
    back: {
      width: 42, height: 42, borderRadius: 21, backgroundColor: c.surface,
      alignItems: 'center', justifyContent: 'center',
    },
    title: { fontSize: 22, fontWeight: '800', color: c.text },
    center: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingBottom: 80 },
    text: { color: c.sub, marginTop: 12, fontSize: 15 },
  });